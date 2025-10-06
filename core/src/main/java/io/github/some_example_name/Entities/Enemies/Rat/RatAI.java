package io.github.some_example_name.Entities.Enemies.Rat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;

import java.util.List;

public class RatAI {

    private static final boolean DEBUG = false;
    
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
    private final String ratId;

    public RatAI(Ratinho owner, Robertinhoo target, PathfindingSystem pathfindingSystem, Mapa mapa, Rectangle homeRoom) {
        this.target = target;
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.homeRoom = homeRoom;
        this.detectionRange = 8f;
        this.attackRange = 1.5f;
        this.ratId = "Rato_" + System.identityHashCode(this);
        
        if (DEBUG) {
            Gdx.app.log(ratId, "🐀 RatAI criado");
            Gdx.app.log(ratId, "HomeRoom: " + homeRoom);
        }
    }

    public void update(float deltaTime, Body body, Vector2 ratPosition) {
        repathTimer += deltaTime;
        
        float distanceToPlayer = ratPosition.dst(target.getPosition());
        
        if (DEBUG) {
            Gdx.app.log(ratId, "=== UPDATE RAT AI ===");
            Gdx.app.log(ratId, "Posição rato: " + ratPosition);
            Gdx.app.log(ratId, "Posição player: " + target.getPosition());
            Gdx.app.log(ratId, "Distância para player: " + distanceToPlayer);
            Gdx.app.log(ratId, "Detection range: " + detectionRange);
        }
        
        if (distanceToPlayer > detectionRange) {
            if (DEBUG) Gdx.app.log(ratId, "❌ Player fora do alcance de detecção");
            currentPath = null;
            body.setLinearVelocity(0, 0);
            return;
        }
        
        boolean sameRoom = isPlayerInSameRoom(target.getPosition());
        if (DEBUG) Gdx.app.log(ratId, "Player na mesma sala: " + sameRoom);
        
        if (!sameRoom) {
            if (DEBUG) Gdx.app.log(ratId, "❌ Player não está na mesma sala");
            currentPath = null;
            body.setLinearVelocity(0, 0);
            return;
        }

        if (DEBUG) {
            Gdx.app.log(ratId, "✅ Player detectado e na mesma sala");
            Gdx.app.log(ratId, "Repath timer: " + repathTimer + "/" + REPATH_INTERVAL);
        }

        if (repathTimer >= REPATH_INTERVAL) {
            if (DEBUG) Gdx.app.log(ratId, "🔄 Calculando novo caminho...");
            repathTimer = 0;
            calculateNewPath(body, ratPosition);
        }
        
        if (DEBUG) Gdx.app.log(ratId, "Attack range: " + attackRange);
        if (distanceToPlayer <= attackRange) {
            if (DEBUG) Gdx.app.log(ratId, "⚔️ Player no alcance de ataque - PARANDO");
            body.setLinearVelocity(0, 0);
            return;
        }
        
        if (DEBUG) Gdx.app.log(ratId, "🎯 Seguindo caminho...");
        followPath(body, deltaTime, ratPosition);
    }

    private void calculateNewPath(Body body, Vector2 ratPosition) {
        Vector2 start = ratPosition;
        Vector2 end = target.getPosition();
        
        if (DEBUG) Gdx.app.log(ratId, "📍 Calculando caminho de " + start + " para " + end);
        
        currentPath = pathfindingSystem.findPath(start, end);
        
        if (DEBUG) {
            if (currentPath == null) {
                Gdx.app.log(ratId, "❌ PATHFINDING: currentPath é NULL");
            } else if (currentPath.isEmpty()) {
                Gdx.app.log(ratId, "❌ PATHFINDING: currentPath está VAZIO");
            } else {
                Gdx.app.log(ratId, "✅ PATHFINDING: Caminho encontrado com " + currentPath.size() + " pontos");
                for (int i = 0; i < currentPath.size(); i++) {
                    Gdx.app.log(ratId, "  Ponto " + i + ": " + currentPath.get(i));
                }
            }
        }
        
        currentPathIndex = 0;
    }

    private void followPath(Body body, float deltaTime, Vector2 currentPosition) {
        // Validações mais rigorosas
        if (currentPath == null || currentPath.isEmpty()) {
            if (DEBUG) Gdx.app.log(ratId, "❌ FOLLOW PATH: Caminho inválido - null ou vazio");
            body.setLinearVelocity(0, 0);
            return;
        }
        
        if (currentPathIndex < 0 || currentPathIndex >= currentPath.size()) {
            if (DEBUG) Gdx.app.log(ratId, "❌ FOLLOW PATH: Índice inválido - " + currentPathIndex + " em path de tamanho " + currentPath.size());
            body.setLinearVelocity(0, 0);
            currentPathIndex = 0; // Reset para evitar problemas futuros
            return;
        }
        
        Vector2 targetPosition = currentPath.get(currentPathIndex);
        
        // Validação adicional da posição alvo
        if (targetPosition == null) {
            if (DEBUG) Gdx.app.log(ratId, "❌ FOLLOW PATH: Posição alvo é null");
            body.setLinearVelocity(0, 0);
            return;
        }
        
        float distanceToTarget = currentPosition.dst(targetPosition);
        
        if (DEBUG) {
            Gdx.app.log(ratId, "🎯 Seguindo ponto " + currentPathIndex + "/" + (currentPath.size()-1));
            Gdx.app.log(ratId, "  Posição atual: " + currentPosition);
            Gdx.app.log(ratId, "  Alvo: " + targetPosition);
            Gdx.app.log(ratId, "  Distância para alvo: " + distanceToTarget);
        }
        
        // Resto do método permanece igual...
        if (distanceToTarget < 0.5f) {
            if (DEBUG) Gdx.app.log(ratId, "✅ Ponto " + currentPathIndex + " alcançado, indo para próximo");
            currentPathIndex++;
            
            if (currentPathIndex >= currentPath.size()) {
                if (DEBUG) Gdx.app.log(ratId, "🏁 Fim do caminho alcançado");
                currentPath = null; // Limpa o caminho quando terminar
                return;
            }
            
            // Valida o próximo ponto também
            if (currentPath.get(currentPathIndex) == null) {
                if (DEBUG) Gdx.app.log(ratId, "❌ Próximo ponto do caminho é null, cancelando");
                currentPath = null;
                return;
            }
            
            targetPosition = currentPath.get(currentPathIndex);
            if (DEBUG) Gdx.app.log(ratId, "🎯 Novo alvo: ponto " + currentPathIndex + " - " + targetPosition);
        }
    
        Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(3f);
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 steering = desiredVelocity.sub(currentVelocity);
        
        body.applyForceToCenter(steering.scl(body.getMass()), true);
    }
    private boolean isPlayerInSameRoom(Vector2 playerPosition) {
        if (homeRoom == null) {
            if (DEBUG) Gdx.app.log(ratId, "❌ HOME ROOM: homeRoom é NULL");
            return false;
        }
        
        // Converte a posição do jogador para coordenadas de tile
        Vector2 playerTile = mapa.worldToTile(playerPosition);
        
        if (DEBUG) {
            Gdx.app.log(ratId, "🏠 Verificando sala - Player tile: " + playerTile);
            Gdx.app.log(ratId, "🏠 Home room: " + homeRoom);
        }
        
        boolean contains = homeRoom.contains(playerTile);
        if (DEBUG) Gdx.app.log(ratId, "🏠 Player na home room: " + contains);
        
        return contains;
    }
    
    public List<Vector2> getCurrentPath() {
        return currentPath;
    }
    
    // Método para debug adicional
    public void debugState() {
        if (DEBUG) {
            Gdx.app.log(ratId, "=== DEBUG STATE ===");
            Gdx.app.log(ratId, "Home Room: " + homeRoom);
            Gdx.app.log(ratId, "Current Path: " + (currentPath != null ? currentPath.size() + " pontos" : "NULL"));
            Gdx.app.log(ratId, "Current Path Index: " + currentPathIndex);
            Gdx.app.log(ratId, "Repath Timer: " + repathTimer);
        }
    }
}