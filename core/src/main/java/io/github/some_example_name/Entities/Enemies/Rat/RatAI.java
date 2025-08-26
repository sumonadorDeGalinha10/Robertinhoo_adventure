package io.github.some_example_name.Entities.Enemies.Rat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class RatAI {
    private final Robertinhoo target;
    private final PathfindingSystem pathfindingSystem;
    private final Mapa mapa;
    private List<Vector2> currentPath;
    private int currentPathIndex;
    private float repathTimer = 0;
    private static final float REPATH_INTERVAL = 0.5f;
    private final float detectionRange;
    private final float attackRange;
    private final Rectangle homeRoom;

    public RatAI(Ratinho owner, Robertinhoo target, PathfindingSystem pathfindingSystem, Mapa mapa, Rectangle homeRoom) {
        this.target = target;
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.homeRoom = homeRoom;
        this.detectionRange = 8f;
        this.attackRange = 1.5f;
    }

    public void update(float deltaTime, Body body, Vector2 ratPosition) {
        repathTimer += deltaTime;
        
        float distanceToPlayer = ratPosition.dst(target.getPosition());
        
        if (distanceToPlayer > detectionRange) {
    
            currentPath = null;
            body.setLinearVelocity(0, 0);
            return;
        }
        
       
        if (!isPlayerInSameRoom(target.getPosition())) {
            currentPath = null;
            body.setLinearVelocity(0, 0);
            return;
        }

        if (repathTimer >= REPATH_INTERVAL) {
            repathTimer = 0;
            calculateNewPath(body, ratPosition);
        }
        
        if (distanceToPlayer <= attackRange) {
          
            body.setLinearVelocity(0, 0);
            return;
        }
        
        followPath(body, deltaTime, ratPosition);
    }

    private void calculateNewPath(Body body, Vector2 ratPosition) {
        Vector2 start = ratPosition;
        Vector2 end = target.getPosition();
        currentPath = pathfindingSystem.findPath(start, end);
        currentPathIndex = 0;
    }

    private void followPath(Body body, float deltaTime, Vector2 currentPosition) {
        if (currentPath == null || currentPath.isEmpty() || currentPathIndex >= currentPath.size()) {
            return;
        }
        
        Vector2 targetPosition = currentPath.get(currentPathIndex);
        
        if (currentPosition.dst(targetPosition) < 0.5f) {
            currentPathIndex++;
            
            if (currentPathIndex >= currentPath.size()) {
                return;
            }
            
            targetPosition = currentPath.get(currentPathIndex);
        }

        Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(3f);
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 steering = desiredVelocity.sub(currentVelocity);

        body.applyForceToCenter(steering.scl(body.getMass()), true);
    }

    private boolean isPlayerInSameRoom(Vector2 playerPosition) {
        if (homeRoom == null) return false;
        
        // Converte a posição do jogador para coordenadas de tile
        Vector2 playerTile = mapa.worldToTile(playerPosition);
        
        // Verifica se o jogador está dentro da sala deste rato
        return homeRoom.contains(playerTile);
    }
    public List<Vector2> getCurrentPath() {
        return currentPath;
    }
}