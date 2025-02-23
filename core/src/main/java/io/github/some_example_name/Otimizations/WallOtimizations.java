package io.github.some_example_name.Otimizations;
import java.util.List; // Correto
import java.util.ArrayList; // Já está correto
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

import io.github.some_example_name.Mapa;



public class WallOtimizations{
 private Mapa mapa;


 public WallOtimizations(Mapa mapa) {
    this.mapa = mapa;
}
public List<Rectangle> optimizeWalls(ArrayList<Vector2> wallPositions) {
    boolean[][] visited = new boolean[mapa.mapWidth][mapa.mapHeight];
    List<Rectangle> retangulos = new ArrayList<>();
    boolean[][] isWall = new boolean[mapa.mapWidth][mapa.mapHeight];

    // Preenche a matriz isWall
    for (Vector2 pos : wallPositions) {
        int x = (int) pos.x;
        int y = (int) pos.y;
        if (x >= 0 && x < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
            isWall[x][y] = true;
        } else {
            Gdx.app.error("WallOtimizations", "Posição de parede inválida: " + x + ", " + y);
        }
    }

    // Detecta retângulos
    for (int y = 0; y < mapa.mapHeight; y++) {
        for (int x = 0; x < mapa.mapWidth; x++) {
            if (isWall[x][y] && !visited[x][y]) {
                int maxWidth = findMaxWidth(isWall, x, y);
                int maxHeight = findMaxHeight(isWall, x, y, maxWidth);
                Rectangle ret = new Rectangle(x, y, maxWidth, maxHeight);
                retangulos.add(ret);
                markVisited(visited, x, y, maxWidth, maxHeight);
                Gdx.app.log("WallOtimizations", "Retângulo criado: " + ret); // Log de debug
            }
        }
    }
    return retangulos;
}

// Encontra a largura máxima de uma linha contígua de paredes
private int findMaxWidth(boolean[][] isWall, int startX, int y) {
    int width = 0;
    while (startX + width < mapa.mapWidth && isWall[startX + width][y]) {
        width++;
    }
    return width;
}

// Encontra a altura máxima para a largura definida
private int findMaxHeight(boolean[][] isWall, int startX, int startY, int width) {
    int height = 0;
    boolean valid = true;

    while (valid && startY + height < mapa.mapHeight) {
        for (int x = startX; x < startX + width; x++) {
            if (!isWall[x][startY + height]) {
                valid = false;
                break;
            }
        }
        if (valid) height++;
    }

    return height;
}

// Marca a região como visitada
private void markVisited(boolean[][] visited, int startX, int startY, int width, int height) {
    for (int y = startY; y < startY + height; y++) {
        for (int x = startX; x < startX + width; x++) {
            visited[x][y] = true;
        }
    }
}

}