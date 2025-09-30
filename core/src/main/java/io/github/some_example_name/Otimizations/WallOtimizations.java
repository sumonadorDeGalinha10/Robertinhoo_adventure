package io.github.some_example_name.Otimizations;

import java.util.List;
import java.util.ArrayList;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;

public class WallOtimizations {
    private Mapa mapa;

    public WallOtimizations(Mapa mapa) {
        this.mapa = mapa;
    }

    public List<Rectangle> optimizeWalls(ArrayList<Vector2> wallPositions) {
        boolean[][] visited = new boolean[mapa.mapWidth][mapa.mapHeight];
        List<Rectangle> retangulos = new ArrayList<>();
        boolean[][] isWall = new boolean[mapa.mapWidth][mapa.mapHeight];

        // Marcar TODAS as paredes do mapa (salas + corredores)
        for (Vector2 pos : wallPositions) {
            int x = (int) pos.x;
            int y = (int) pos.y;
            if (x >= 0 && x < mapa.mapWidth && y >= 0 && y < mapa.mapHeight) {
                isWall[x][y] = true;
            }
        }

        // Agrupar paredes em retângulos otimizados
        for (int y = 0; y < mapa.mapHeight; y++) {
            for (int x = 0; x < mapa.mapWidth; x++) {
                if (isWall[x][y] && !visited[x][y]) {
                    int maxWidth = findMaxWidth(isWall, x, y);
                    int maxHeight = findMaxHeight(isWall, x, y, maxWidth);
                    
                    // Só criar retângulo se for relevante (evitar retângulos muito pequenos em áreas desnecessárias)
                    if (shouldCreateWallBody(x, y, maxWidth, maxHeight)) {
                        Rectangle ret = new Rectangle(x, y, maxWidth, maxHeight);
                        retangulos.add(ret);
                    }
                    
                    markVisited(visited, x, y, maxWidth, maxHeight);
                }
            }
        }
        
        Gdx.app.log("WallOtimizations", "Total de retângulos otimizados: " + retangulos.size());
        return retangulos;
    }

    private boolean shouldCreateWallBody(int x, int y, int width, int height) {
        // Criar corpo físico para paredes que são relevantes para o gameplay
        // - Paredes de salas
        // - Paredes de corredores que são necessárias para contenção
        // - Evitar paredes isoladas ou muito pequenas que não afetam jogabilidade
        
        // Verificar se está em área importante (perto de salas ou corredores principais)
        return isNearPlayableArea(x, y, width, height);
    }
    
    private boolean isNearPlayableArea(int x, int y, int width, int height) {
        // Verificar se esta parede está próxima a áreas jogáveis (salas ou corredores)
        for (int checkX = x - 2; checkX < x + width + 2; checkX++) {
            for (int checkY = y - 2; checkY < y + height + 2; checkY++) {
                if (checkX >= 0 && checkX < mapa.mapWidth && checkY >= 0 && checkY < mapa.mapHeight) {
                    if (mapa.tiles[checkX][checkY] == Mapa.TILE) {
                        return true; // Está perto de área jogável
                    }
                }
            }
        }
        return false;
    }

    private int findMaxWidth(boolean[][] isWall, int startX, int y) {
        int width = 0;
        while (startX + width < mapa.mapWidth && isWall[startX + width][y]) {
            width++;
        }
        return width;
    }

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

    private void markVisited(boolean[][] visited, int startX, int startY, int width, int height) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                visited[x][y] = true;
            }
        }
    }
}