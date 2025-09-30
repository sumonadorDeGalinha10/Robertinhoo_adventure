// BarrelSpawner.java
package io.github.some_example_name.MapConfig.Spawner;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.MapConfig.Mapa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;

public class BarrelSpawner {
    
    // Classe interna para armazenar posição com informação da parede
    private static class WallPosition {
        Vector2 tilePos;
        int wallDirection; // 0=norte, 1=sul, 2=leste, 3=oeste
        
        WallPosition(Vector2 tilePos, int wallDirection) {
            this.tilePos = tilePos;
            this.wallDirection = wallDirection;
        }
    }
    
    public static void spawnBarrels(Mapa mapa, int numberOfBarrels) {
        if (mapa.getRooms().isEmpty()) {
            return;
        }
        
        Random rand = new Random();
        List<WallPosition> validWallPositions = new ArrayList<>();
        
        // Coletar posições válidas junto às paredes das salas
        for (Rectangle room : mapa.getRooms()) {
            List<WallPosition> roomWallPositions = getWallPositionsForRoom(mapa, room);
            validWallPositions.addAll(roomWallPositions);
        }
        
        // Remover posições que podem bloquear corredores
        validWallPositions = filterPositionsNearCorridors(mapa, validWallPositions);
        
        // Embaralhar as posições
        Collections.shuffle(validWallPositions, rand);
        
        // Adicionar barris
        int barrelsToAdd = Math.min(numberOfBarrels, validWallPositions.size());
        for (int i = 0; i < barrelsToAdd; i++) {
            WallPosition wallPos = validWallPositions.get(i);
            Vector2 worldPos = getAdjustedWorldPosition(mapa, wallPos.tilePos, wallPos.wallDirection);
            mapa.getDestructibles().add(new Barrel(mapa, worldPos.x, worldPos.y, null, null));
        }
        
        // Log para debug
        Gdx.app.log("BarrelSpawner", "Barris adicionados nas paredes: " + barrelsToAdd);
    }
    
    private static List<WallPosition> getWallPositionsForRoom(Mapa mapa, Rectangle room) {
        List<WallPosition> wallPositions = new ArrayList<>();
        
        int roomX = (int) room.x;
        int roomY = (int) room.y;
        int roomWidth = (int) room.width;
        int roomHeight = (int) room.height;
        
        // Parede superior (norte) - direção 0
        for (int x = roomX + 2; x < roomX + roomWidth - 2; x++) {
            int y = roomY + roomHeight - 2;
            if (isValidWallPosition(mapa, x, y, room)) {
                wallPositions.add(new WallPosition(new Vector2(x, y), 0));
            }
        }
        
        // Parede inferior (sul) - direção 1
        for (int x = roomX + 2; x < roomX + roomWidth - 2; x++) {
            int y = roomY + 1;
            if (isValidWallPosition(mapa, x, y, room)) {
                wallPositions.add(new WallPosition(new Vector2(x, y), 1));
            }
        }
        
        // Parede esquerda (oeste) - direção 2
        for (int y = roomY + 2; y < roomY + roomHeight - 2; y++) {
            int x = roomX + 1;
            if (isValidWallPosition(mapa, x, y, room)) {
                wallPositions.add(new WallPosition(new Vector2(x, y), 2));
            }
        }
        
        // Parede direita (leste) - direção 3
        for (int y = roomY + 2; y < roomY + roomHeight - 2; y++) {
            int x = roomX + roomWidth - 2;
            if (isValidWallPosition(mapa, x, y, room)) {
                wallPositions.add(new WallPosition(new Vector2(x, y), 3));
            }
        }
        
        return wallPositions;
    }
    
    private static Vector2 getAdjustedWorldPosition(Mapa mapa, Vector2 tilePos, int wallDirection) {
        // Posição padrão no centro do tile
        Vector2 worldPos = mapa.tileToWorld((int) tilePos.x, (int) tilePos.y);
        
        // Ajuste para encostar na parede (deslocamento de 0.3f em direção à parede)
        // CORREÇÃO: Invertemos norte e sul
        switch (wallDirection) {
            case 0: // Norte - mover para BAIXO (em direção à parede norte)
                worldPos.y -= 0.3f;
                break;
            case 1: // Sul - mover para CIMA (em direção à parede sul)
                worldPos.y += 0.3f;
                break;
            case 2: // Oeste - mover para esquerda
                worldPos.x -= 0.3f;
                break;
            case 3: // Leste - mover para direita
                worldPos.x += 0.3f;
                break;
        }
        
        return worldPos;
    }
    
    private static boolean isValidWallPosition(Mapa mapa, int x, int y, Rectangle room) {
        // Verificar se está dentro dos limites do mapa
        if (x < 0 || x >= mapa.mapWidth || y < 0 || y >= mapa.mapHeight) {
            return false;
        }
        
        // Verificar se é um tile chão
        if (mapa.tiles[x][y] != Mapa.TILE) {
            return false;
        }
        
        // Verificar se não é a posição inicial do jogador
        if (x == (int) mapa.startPosition.x && y == (int) mapa.startPosition.y) {
            return false;
        }
        
        // Verificar se está realmente junto à parede da sala
        // Deve ter uma parede adjacente dentro da sala
        boolean hasAdjacentWall = false;
        
        // Verificar direção específica baseada na posição na sala
        if (y == room.y + 1) { // Parede sul
            hasAdjacentWall = (mapa.tiles[x][(int)room.y] == Mapa.PAREDE);
        } else if (y == room.y + room.height - 2) { // Parede norte
            hasAdjacentWall = (mapa.tiles[x][(int)room.y + (int)room.height - 1] == Mapa.PAREDE);
        } else if (x == room.x + 1) { // Parede oeste
            hasAdjacentWall = (mapa.tiles[(int)room.x][y] == Mapa.PAREDE);
        } else if (x == room.x + room.width - 2) { // Parede leste
            hasAdjacentWall = (mapa.tiles[(int)room.x + (int)room.width - 1][y] == Mapa.PAREDE);
        }
        
        return hasAdjacentWall;
    }
    
    private static List<WallPosition> filterPositionsNearCorridors(Mapa mapa, List<WallPosition> positions) {
        List<WallPosition> filtered = new ArrayList<>();
        
        for (WallPosition wallPos : positions) {
            if (!isNearCorridor(mapa, (int) wallPos.tilePos.x, (int) wallPos.tilePos.y)) {
                filtered.add(wallPos);
            }
        }
        
        return filtered;
    }
    
    private static boolean isNearCorridor(Mapa mapa, int x, int y) {
        // Verificar se há corredores próximos (tiles que não pertencem a nenhuma sala)
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        for (int[] dir : directions) {
            int checkX = x + dir[0];
            int checkY = y + dir[1];
            
            if (checkX >= 0 && checkX < mapa.mapWidth && checkY >= 0 && checkY < mapa.mapHeight) {
                // Se o tile vizinho é chão mas não está em nenhuma sala, provavelmente é corredor
                if (mapa.tiles[checkX][checkY] == Mapa.TILE && !isInAnyRoom(mapa, checkX, checkY)) {
                    return true;
                }
                
                // Verificar também se é uma entrada de corredor (dois tiles seguidos de corredor)
                if (mapa.tiles[checkX][checkY] == Mapa.TILE) {
                    int checkX2 = checkX + dir[0];
                    int checkY2 = checkY + dir[1];
                    if (checkX2 >= 0 && checkX2 < mapa.mapWidth && checkY2 >= 0 && checkY2 < mapa.mapHeight) {
                        if (mapa.tiles[checkX2][checkY2] == Mapa.TILE && !isInAnyRoom(mapa, checkX2, checkY2)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private static boolean isInAnyRoom(Mapa mapa, int x, int y) {
        for (Rectangle room : mapa.getRooms()) {
            if (x >= room.x && x < room.x + room.width &&
                y >= room.y && y < room.y + room.height) {
                return true;
            }
        }
        return false;
    }
    
    // Método para spawnar barris em uma sala específica
    public static void spawnBarrelsInRoom(Mapa mapa, Rectangle specificRoom, int barrelsInThisRoom) {
        Random rand = new Random();
        List<WallPosition> validWallPositions = getWallPositionsForRoom(mapa, specificRoom);
        
        // Remover posições próximas a corredores
        validWallPositions = filterPositionsNearCorridors(mapa, validWallPositions);
        
        Collections.shuffle(validWallPositions, rand);
        
        int barrelsToAdd = Math.min(barrelsInThisRoom, validWallPositions.size());
        for (int i = 0; i < barrelsToAdd; i++) {
            WallPosition wallPos = validWallPositions.get(i);
            Vector2 worldPos = getAdjustedWorldPosition(mapa, wallPos.tilePos, wallPos.wallDirection);
            mapa.getDestructibles().add(new Barrel(mapa, worldPos.x, worldPos.y, null, null));
        }
        
        Gdx.app.log("BarrelSpawner", "Barris adicionados na sala específica: " + barrelsToAdd);
    }
}