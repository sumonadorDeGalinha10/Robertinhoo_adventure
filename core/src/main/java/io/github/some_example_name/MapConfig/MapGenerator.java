// MapGenerator.java
package io.github.some_example_name.MapConfig;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

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

    int numSalas = 8; // <-- define quantas salas você quer
    rooms.clear();

    for (int i = 0; i < numSalas; i++) {
        Rectangle novaSala;
        int attempts = 0;
        boolean valido;

        do {
            novaSala = generateRandomRoom(rand);
            attempts++;
            valido = true;

            // verificar sobreposição com outras salas
            for (Rectangle r : rooms) {
                if (novaSala.overlaps(r)) {
                    valido = false;
                    break;
                }
            }

            if (attempts > 50) {
                Gdx.app.error("MapGenerator", "Não conseguiu posicionar sala " + i);
                valido = false;
                break;
            }
        } while (!valido);

        if (valido) {
            drawRoom(novaSala);
            rooms.add(novaSala);
            Gdx.app.log("MapGenerator", "Sala " + i + " criada: " + novaSala);
        }
    }

    // conectar salas em sequência
    for (int i = 0; i < rooms.size() - 1; i++) {
        connectRooms(rooms.get(i), rooms.get(i + 1), rand);
    }

    // Definir ponto de início (primeira sala)
    Rectangle salaInicial = rooms.get(0);
    int startX, startY;
    int startAttempts = 0;
    do {
        startX = (int) salaInicial.x + 1 + rand.nextInt((int) salaInicial.width - 2);
        startY = (int) salaInicial.y + 1 + rand.nextInt((int) salaInicial.height - 2);
        startAttempts++;
    } while (tiles[startX][startY] != Mapa.TILE);

    startPosition = new Vector2(startX, startY);
    Gdx.app.log("MapGenerator", "Posição inicial: (" + startX + ", " + startY + ")");

    // Coletar paredes
    collectWallPositions();
    Gdx.app.log("MapGenerator", "Paredes coletadas. Total: " + wallPositions.size());
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

public List<Vector2> generateFixedPatrolRoute(PathfindingSystem pathfindingSystem) {
    List<Vector2> route = new ArrayList<>();
    
    if (rooms.isEmpty()) {
        return route;
    }
    
    // Ordenar salas para criar uma rota lógica (usando centro das salas)
    List<Vector2> roomCenters = new ArrayList<>();
    for (Rectangle room : rooms) {
        Vector2 center = new Vector2(room.x + room.width / 2, room.y + room.height / 2);
        roomCenters.add(center);
    }
    
    // Ordenar salas pela proximidade (algoritmo do vizinho mais próximo)
    List<Vector2> sortedCenters = new ArrayList<>();
    Vector2 current = roomCenters.remove(0);
    sortedCenters.add(current);
    
    while (!roomCenters.isEmpty()) {
        Vector2 nearest = null;
        float nearestDist = Float.MAX_VALUE;
        
        for (Vector2 center : roomCenters) {
            float dist = current.dst2(center);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = center;
            }
        }
        
        if (nearest != null) {
            roomCenters.remove(nearest);
            sortedCenters.add(nearest);
            current = nearest;
        }
    }
    
    // Converter para coordenadas mundiais e conectar com pathfinding
    List<Vector2> worldCenters = new ArrayList<>();
    for (Vector2 center : sortedCenters) {
        worldCenters.add(tileToWorld((int)center.x, (int)center.y));
    }
    
    // Conectar todos os pontos em um loop contínuo
    for (int i = 0; i < worldCenters.size(); i++) {
        Vector2 start = worldCenters.get(i);
        Vector2 end = worldCenters.get((i + 1) % worldCenters.size());
        
        List<Vector2> pathSegment = pathfindingSystem.findPath(start, end);
        if (pathSegment != null && !pathSegment.isEmpty()) {
            // Suavizar transições entre segmentos
            if (!route.isEmpty()) {
                Vector2 lastPoint = route.get(route.size() - 1);
                if (lastPoint.dst2(pathSegment.get(0)) > 0.1f) {
                    route.addAll(pathfindingSystem.findPath(lastPoint, pathSegment.get(0)));
                }
            }
            route.addAll(pathSegment);
        }
    }
    
    // Fechar o loop conectando último ponto ao primeiro
    if (!route.isEmpty()) {
        Vector2 lastPoint = route.get(route.size() - 1);
        Vector2 firstPoint = route.get(0);
        if (lastPoint.dst2(firstPoint) > 0.1f) {
            route.addAll(pathfindingSystem.findPath(lastPoint, firstPoint));
        }
    }
    
    return route;
}
public Vector2 tileToWorld(int tileX, int tileY) {
    return new Vector2(tileX + 0.5f, tileY + 0.5f);
}
    public List<Vector2> getFixedPatrolRoute(PathfindingSystem pathfindingSystem) {
        return generateFixedPatrolRoute(pathfindingSystem );
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