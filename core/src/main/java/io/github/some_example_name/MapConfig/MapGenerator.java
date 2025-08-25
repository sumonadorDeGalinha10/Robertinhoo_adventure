// MapGenerator.java
package io.github.some_example_name.MapConfig;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenerator {
    public static final int MAX_SALA_WIDTH = 15;
    public static final int MIN_SALA_WIDTH = 10;
    public static final int MAX_SALA_HEIGHT = 15;
    public static final int MIN_SALA_HEIGHT = 10;
    public static final int TUNEL_WIDTH = 1;
    

    private int mapWidth;
    private int mapHeight;
    private int[][] tiles;
    private Vector2 startPosition;
    private ArrayList<Vector2> wallPositions = new ArrayList<>();
    private List<Rectangle> rooms = new ArrayList<>();


public MapGenerator(int width, int height) {
    Gdx.app.log("MapGenerator", "Iniciando MapGenerator...");
    this.mapWidth = width;
    this.mapHeight = height;
    this.tiles = new int[mapWidth][mapHeight];
    generateRandomMap();
    Gdx.app.log("MapGenerator", "Mapa gerado com sucesso.");
}

  private void generateRandomMap() {
    Gdx.app.log("MapGenerator", "Iniciando geração aleatória do mapa...");
    // Inicializa tudo como parede
    for (int x = 0; x < mapWidth; x++) {
        for (int y = 0; y < mapHeight; y++) {
            tiles[x][y] = Mapa.PAREDE;
        }
    }
    Gdx.app.log("MapGenerator", "Mapa inicializado com paredes.");

    Random rand = new Random();

    // Gerar primeira sala
    Gdx.app.log("MapGenerator", "Gerando primeira sala...");
    Rectangle sala1 = generateRandomRoom(rand);
    drawRoom(sala1);
    Gdx.app.log("MapGenerator", "Primeira sala desenhada: " + sala1);
    rooms.add(sala1);

    // Gerar segunda sala (garantir distância mínima)
    Gdx.app.log("MapGenerator", "Gerando segunda sala...");
    Rectangle sala2;
    int attempts = 0;
    do {
        sala2 = generateRandomRoom(rand);
        attempts++;
        if (attempts > 30) {
            Gdx.app.error("MapGenerator", "Falha ao gerar segunda sala após 100 tentativas.");
            break;
        }
    } while (Math.abs(sala1.x - sala2.x) < sala1.width + sala2.width ||
            Math.abs(sala1.y - sala2.y) < sala1.height + sala2.height);

    Gdx.app.log("MapGenerator", "Segunda sala gerada após " + attempts + " tentativas: " + sala2);
    drawRoom(sala2);
    rooms.add(sala2);
    Gdx.app.log("MapGenerator", "Segunda sala desenhada.");

    // Conectar salas com túneis
    Gdx.app.log("MapGenerator", "Conectando salas...");
    connectRooms(sala1, sala2, rand);
    Gdx.app.log("MapGenerator", "Salas conectadas.");

    // Definir ponto de início na primeira sala
      int startX, startY;
        int startAttempts = 0;
        do {
            startX = (int) sala1.x + 1 + rand.nextInt((int) sala1.width - 2);
            startY = (int) sala1.y + 1 + rand.nextInt((int) sala1.height - 2);
            startAttempts++;
            // ... [fallback se necessário]
        } while (tiles[startX][startY] != Mapa.TILE);
        
        startPosition = new Vector2(startX, startY);

    Gdx.app.log("MapGenerator", "Posição inicial gerada após " + startAttempts + " tentativas: (" + startX + ", " + startY + ")");
    startPosition = new Vector2(startX, startY);
    
    // Coletar posições das paredes para física
    collectWallPositions();
    Gdx.app.log("MapGenerator", "Posições das paredes coletadas. Total: " + wallPositions.size());
}

    private Rectangle generateRandomRoom(Random rand) {
        int width = rand.nextInt(MAX_SALA_WIDTH - MIN_SALA_WIDTH) + MIN_SALA_WIDTH;
        int height = rand.nextInt(MAX_SALA_HEIGHT - MIN_SALA_HEIGHT) + MIN_SALA_HEIGHT;
        int x = rand.nextInt(mapWidth - width - 2) + 1;
        int y = rand.nextInt(mapHeight - height - 2) + 1;
        return new Rectangle(x, y, width, height);
    }

    private void drawRoom(Rectangle room) {
        for (int x = (int) room.x; x < room.x + room.width; x++) {
            for (int y = (int) room.y; y < room.y + room.height; y++) {
                // Deixa espaço para paredes externas
                if (x == room.x || x == room.x + room.width - 1 ||
                        y == room.y || y == room.y + room.height - 1) {
                    tiles[x][y] = Mapa.PAREDE;
                } else {
                    tiles[x][y] = Mapa.TILE;
                }
            }
        }
    }

    private void connectRooms(Rectangle sala1, Rectangle sala2, Random rand) {
        // Centro das salas
        int centroX1 = (int) (sala1.x + sala1.width / 2);
        int centroY1 = (int) (sala1.y + sala1.height / 2);
        int centroX2 = (int) (sala2.x + sala2.width / 2);
        int centroY2 = (int) (sala2.y + sala2.height / 2);

        // Escolher direção do túnel
        if (rand.nextBoolean()) {
            createHorizontalTunnel(centroX1, centroX2, centroY1);
            createVerticalTunnel(centroY1, centroY2, centroX2);
        } else {
            createVerticalTunnel(centroY1, centroY2, centroX1);
            createHorizontalTunnel(centroX1, centroX2, centroY2);
        }
    }

    private void createHorizontalTunnel(int xStart, int xEnd, int y) {
        int start = Math.min(xStart, xEnd);
        int end = Math.max(xStart, xEnd);

        for (int x = start; x <= end; x++) {
            for (int dy = -TUNEL_WIDTH / 2; dy <= TUNEL_WIDTH / 2; dy++) {
                if (x >= 0 && x < mapWidth && y + dy >= 0 && y + dy < mapHeight) {
                    tiles[x][y + dy] = Mapa.TILE;
                }
            }
        }
    }

    private void createVerticalTunnel(int yStart, int yEnd, int x) {
        int start = Math.min(yStart, yEnd);
        int end = Math.max(yStart, yEnd);

        for (int y = start; y <= end; y++) {
            for (int dx = -TUNEL_WIDTH / 2; dx <= TUNEL_WIDTH / 2; dx++) {
                if (x + dx >= 0 && x + dx < mapWidth && y >= 0 && y < mapHeight) {
                    tiles[x + dx][y] = Mapa.TILE;
                }
            }
        }
    }

    private void collectWallPositions() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (tiles[x][y] == Mapa.PAREDE) {
                    wallPositions.add(new Vector2(x, y));
                }
            }
        }
    }

    public int[][] getTiles() {
        return tiles;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }
        public Vector2 getWorldStartPosition(int mapHeight) {
        return new Vector2(
            startPosition.x + 0.5f,
            mapHeight - 1 - startPosition.y + 0.5f
        );
    }

    public ArrayList<Vector2> getWallPositions() {
        return wallPositions;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getStartX() {
        return (int) startPosition.x;
    }

    public int getStartY() {
        return (int) startPosition.y;
    }

    public List<Rectangle> getRooms() {
        return rooms;
    }
}