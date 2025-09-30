// MapBorderManager.java
package io.github.some_example_name.Otimizations;

import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.Gdx;

public class MapBorderManager {
    
    public static void createOptimizedMapBorders(Mapa mapa) {
        // Criar paredes apenas para as bordas externas do mapa que são necessárias
        int mapWidth = mapa.mapWidth;
        int mapHeight = mapa.mapHeight;
        
        // Verificar quais bordas realmente precisam de colisão
        createNecessaryBorders(mapa, mapWidth, mapHeight);
        
        Gdx.app.log("MapBorderManager", "Bordas otimizadas do mapa criadas");
    }
    
    private static void createNecessaryBorders(Mapa mapa, int mapWidth, int mapHeight) {
        // Borda superior - apenas se houver salas/corredores próximos
        if (hasPlayableAreaNearBorder(mapa, 0, mapHeight - 1, mapWidth, 1)) {
            createBorderWall(mapa, 0, mapHeight - 1, mapWidth, 1);
        }
        
        // Borda inferior - apenas se houver salas/corredores próximos
        if (hasPlayableAreaNearBorder(mapa, 0, 0, mapWidth, 1)) {
            createBorderWall(mapa, 0, 0, mapWidth, 1);
        }
        
        // Borda esquerda - apenas se houver salas/corredores próximos
        if (hasPlayableAreaNearBorder(mapa, 0, 0, 1, mapHeight)) {
            createBorderWall(mapa, 0, 0, 1, mapHeight);
        }
        
        // Borda direita - apenas se houver salas/corredores próximos
        if (hasPlayableAreaNearBorder(mapa, mapWidth - 1, 0, 1, mapHeight)) {
            createBorderWall(mapa, mapWidth - 1, 0, 1, mapHeight);
        }
    }
    
    private static boolean hasPlayableAreaNearBorder(Mapa mapa, int x, int y, int width, int height) {
        // Verificar se há área jogável perto desta borda
        for (int checkX = Math.max(0, x - 3); checkX < Math.min(mapa.mapWidth, x + width + 3); checkX++) {
            for (int checkY = Math.max(0, y - 3); checkY < Math.min(mapa.mapHeight, y + height + 3); checkY++) {
                if (mapa.tiles[checkX][checkY] == Mapa.TILE) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static void createBorderWall(Mapa mapa, int x, int y, int width, int height) {
        Rectangle border = new Rectangle(x, y, width, height);
        mapa.createWallBody(border);
    }
}