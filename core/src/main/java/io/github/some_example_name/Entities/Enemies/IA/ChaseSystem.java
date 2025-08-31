package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.MapConfig.Mapa;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;

public class ChaseSystem {
    private final PathfindingSystem pathfindingSystem;
    private final Mapa mapa;
    private final Random random;
    
    private List<Vector2> chasePath = new ArrayList<>();
    private int chasePathIndex = 0;
    
    private float repathTimer = 0;
    private static final float REPATH_INTERVAL = 0.3f; // Reduzido para reagir mais rápido
    private static final float CHASE_SPEED = 4f; // Velocidade reduzida para melhor controle
    private static final float STEERING_FORCE = 12f; // Aumentado para respostas mais rápidas
    private static final float PATH_NODE_TOLERANCE = 1.2f; // Aumentado para avançar nós mais cedo
    
    // Sistema de memória e persistência
    private Vector2 lastKnownTargetPosition = null;
    private float lastKnownPositionTimer = 0;
    private static final float LAST_KNOWN_POSITION_DURATION = 5f; // 5 segundos de memória
    private static final float INVESTIGATION_RADIUS = 3f; // Raio para investigar área
    
    // Sistema de busca quando perde o alvo
    private boolean isInvestigating = false;
    private Vector2 investigationPoint = null;
    private float investigationTimer = 0;
    private static final float INVESTIGATION_TIME = 3f;

    public ChaseSystem(PathfindingSystem pathfindingSystem, Mapa mapa) {
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.random = new Random();
    }

    public void update(float deltaTime, Body body, Vector2 currentPosition, 
                      Vector2 targetPosition, boolean hasDirectSight) {
        repathTimer += deltaTime;
        lastKnownPositionTimer += deltaTime;

        // Atualizar última posição conhecida se tiver visão direta
        if (hasDirectSight) {
            lastKnownTargetPosition = new Vector2(targetPosition);
            lastKnownPositionTimer = 0;
            isInvestigating = false; // Parar investigação se avistar o alvo
        }

        // Determinar posição alvo efetiva
        Vector2 effectiveTargetPosition = getEffectiveTargetPosition(targetPosition, hasDirectSight);

        // Recalcular caminho se necessário
        if (shouldRecalculatePath(currentPosition, effectiveTargetPosition)) {
            calculateNewChasePath(currentPosition, effectiveTargetPosition);
            repathTimer = 0;
        }

        // Seguir o caminho ou investigar
        if (isInvestigating) {
            updateInvestigation(deltaTime, body, currentPosition);
        } else if (chasePath != null && !chasePath.isEmpty()) {
            followChasePath(body, currentPosition, CHASE_SPEED, deltaTime);
        } else {
            // Movimento direto como fallback
            moveDirectlyToTarget(body, currentPosition, effectiveTargetPosition, CHASE_SPEED);
        }

        // Verificar se precisa iniciar investigação
        if (!hasDirectSight && lastKnownPositionTimer > LAST_KNOWN_POSITION_DURATION && !isInvestigating) {
            startInvestigation(lastKnownTargetPosition != null ? 
                lastKnownTargetPosition : currentPosition);
        }
    }

    private Vector2 getEffectiveTargetPosition(Vector2 currentTargetPosition, boolean hasDirectSight) {
        if (hasDirectSight) {
            return currentTargetPosition;
        } else if (lastKnownTargetPosition != null && lastKnownPositionTimer < LAST_KNOWN_POSITION_DURATION) {
            return lastKnownTargetPosition;
        } else {
            return currentTargetPosition; // Fallback
        }
    }

    private boolean shouldRecalculatePath(Vector2 currentPosition, Vector2 targetPosition) {
        // Recalcular se:
        // 1. É tempo de recalcular
        // 2. O caminho está vazio
        // 3. O alvo se moveu significativamente
        // 4. Está preso em um obstáculo
        
        if (repathTimer >= REPATH_INTERVAL || chasePath.isEmpty()) {
            return true;
        }
        
        if (lastKnownTargetPosition != null && 
            targetPosition.dst(lastKnownTargetPosition) > 2.0f) {
            return true;
        }
        
        // Verificar se está preso (pouco movimento)
        if (chasePathIndex > 0 && chasePathIndex < chasePath.size()) {
            Vector2 currentTarget = chasePath.get(chasePathIndex);
            if (currentPosition.dst(currentTarget) < 0.5f) {
                // Avançar para o próximo ponto do caminho
                chasePathIndex++;
                if (chasePathIndex >= chasePath.size()) {
                    return true; // Precisa de novo caminho
                }
            }
        }
        
        return false;
    }

    private void followChasePath(Body body, Vector2 currentPosition, float speed, float deltaTime) {
        if (chasePathIndex >= chasePath.size()) {
            return;
        }

        Vector2 targetNode = chasePath.get(chasePathIndex);
        float distanceToNode = currentPosition.dst(targetNode);

        // Avançar para o próximo nó se estiver próximo o suficiente
        if (distanceToNode < PATH_NODE_TOLERANCE) {
            chasePathIndex++;
            if (chasePathIndex >= chasePath.size()) {
                return;
            }
            targetNode = chasePath.get(chasePathIndex);
        }

        // Calcular direção e aplicar força
        Vector2 direction = targetNode.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(speed);
        applySteeringForce(body, desiredVelocity, deltaTime);
    }

    private void moveDirectlyToTarget(Body body, Vector2 currentPosition, 
                                    Vector2 targetPosition, float speed) {
        float distance = currentPosition.dst(targetPosition);
        
        if (distance > 0.5f) {
            Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
            Vector2 desiredVelocity = direction.scl(speed);
            applySteeringForce(body, desiredVelocity, 0.1f); // deltaTime fixo para movimento direto
        } else {
            // Parar quando chegar muito perto
            body.setLinearVelocity(0, 0);
        }
    }

    private void applySteeringForce(Body body, Vector2 desiredVelocity, float deltaTime) {
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 velocityError = desiredVelocity.cpy().sub(currentVelocity);
        Vector2 steeringForce = velocityError.scl(STEERING_FORCE * body.getMass());
        
        body.applyForceToCenter(steeringForce, true);
        
        // Limitar velocidade máxima
        if (body.getLinearVelocity().len() > CHASE_SPEED) {
            body.setLinearVelocity(body.getLinearVelocity().nor().scl(CHASE_SPEED));
        }
    }

    private void calculateNewChasePath(Vector2 start, Vector2 end) {
        chasePath = pathfindingSystem.findPath(start, end);
        chasePathIndex = 0;

        if (chasePath != null && !chasePath.isEmpty()) {
            Gdx.app.log("ChaseSystem", "Novo caminho com " + chasePath.size() + " pontos");
        } else {
            Gdx.app.log("ChaseSystem", "Falha ao calcular caminho");
            chasePath = new ArrayList<>();
        }
    }

    private void startInvestigation(Vector2 point) {
        isInvestigating = true;
        investigationPoint = new Vector2(point);
        investigationTimer = 0;
        Gdx.app.log("ChaseSystem", "Iniciando investigação em: " + point);
    }

    private void updateInvestigation(float deltaTime, Body body, Vector2 currentPosition) {
        investigationTimer += deltaTime;
        
        if (investigationTimer > INVESTIGATION_TIME) {
            // Terminar investigação
            isInvestigating = false;
            Gdx.app.log("ChaseSystem", "Investigação concluída");
            return;
        }
        
        // Mover-se para o ponto de investigação
        if (investigationPoint != null) {
            if (currentPosition.dst(investigationPoint) > 0.5f) {
                Vector2 direction = investigationPoint.cpy().sub(currentPosition).nor();
                Vector2 desiredVelocity = direction.scl(CHASE_SPEED * 0.6f); // Velocidade reduzida
                applySteeringForce(body, desiredVelocity, deltaTime);
            } else {
                // Procurar em pontos aleatórios próximos
                float angle = random.nextFloat() * 6.2832f; // 0-2π
                float distance = random.nextFloat() * INVESTIGATION_RADIUS;
                investigationPoint.set(
                    currentPosition.x + (float)Math.cos(angle) * distance,
                    currentPosition.y + (float)Math.sin(angle) * distance
                );
            }
        }
    }

    public void reset() {
        chasePath.clear();
        chasePathIndex = 0;
        repathTimer = 0;
        lastKnownTargetPosition = null;
        lastKnownPositionTimer = LAST_KNOWN_POSITION_DURATION;
        isInvestigating = false;
    }

    public List<Vector2> getChasePath() {
        return chasePath;
    }
    
    public boolean isInvestigating() {
        return isInvestigating;
    }
    
    public Vector2 getLastKnownPosition() {
        return lastKnownTargetPosition;
    }
}