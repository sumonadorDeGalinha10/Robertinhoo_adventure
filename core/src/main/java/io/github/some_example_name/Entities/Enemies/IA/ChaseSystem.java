package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ChaseSystem {
    private final PathfindingSystem pathfindingSystem;
    private final Random random;
    private static final float TARGET_REACHED_DISTANCE = 1.5f;

    private static final boolean DEBUG_LOGS = false;

    private List<Vector2> chasePath = new ArrayList<>();
    private int chasePathIndex = 0;
    private Deque<Vector2> targetTrail = new ArrayDeque<>();

    private float repathTimer = 0;
    private static final float REPATH_INTERVAL = 0.3f; // Reduzido para reagir mais rápido
    private static final float CHASE_SPEED = 4f;
    private static final float STEERING_FORCE = 12f;
    private static final float PATH_NODE_TOLERANCE = 1.2f;
    private boolean reachedLastKnown = false;

    private Vector2 lastKnownTargetPosition = null;

    public ChaseSystem(PathfindingSystem pathfindingSystem, Mapa mapa) {
        this.pathfindingSystem = pathfindingSystem;
        this.random = new Random();
    }

    public void update(float deltaTime, Body body, Vector2 currentPosition,
            Vector2 targetPosition, boolean hasDirectSight) {
        repathTimer += deltaTime;

        if (DEBUG_LOGS) {
            Gdx.app.log("ChaseSystem", "Posição atual: " + currentPosition);
            Gdx.app.log("ChaseSystem", "Alvo: " + targetPosition);
            Gdx.app.log("ChaseSystem", "Visão direta: " + hasDirectSight);
        }

        // Atualiza última posição conhecida se tiver visão
        if (hasDirectSight && targetPosition != null) {
            lastKnownTargetPosition = new Vector2(targetPosition);
            reachedLastKnown = false; // Reset pois tem visão do alvo
        }

        Vector2 effectiveTargetPosition = getEffectiveTargetPosition(currentPosition, targetPosition, hasDirectSight);

        if (effectiveTargetPosition == null) {
            // Fallback para movimento aleatório suave
            chasePath.clear();
            applyWanderBehavior(body, deltaTime);
            return;
        }

        float distanceToTarget = currentPosition.dst(effectiveTargetPosition);

        // VERIFICAÇÃO MELHORADA: Só marca como "alcançado" se não tiver visão direta
        if (distanceToTarget < TARGET_REACHED_DISTANCE && !hasDirectSight) {
            chasePath.clear();
            reachedLastKnown = true;
            if (DEBUG_LOGS)
                Gdx.app.log("ChaseSystem", "✅ Alcançou última posição conhecida sem visão do jogador");
            return;
        }

        // Recalcula caminho se necessário
        if (shouldRecalculatePath(currentPosition, effectiveTargetPosition)) {
            calculateNewChasePath(currentPosition, effectiveTargetPosition);
            repathTimer = 0;
        }

        // Segue o caminho ou move diretamente
        if (chasePath != null && !chasePath.isEmpty()) {
            followChasePath(body, currentPosition, CHASE_SPEED, deltaTime);
        } else {
            moveDirectlyToTarget(body, currentPosition, effectiveTargetPosition, CHASE_SPEED);
        }
    }

    // Novo método para comportamento de "vaguear" quando não tem alvo
    private void applyWanderBehavior(Body body, float deltaTime) {
        Vector2 currentVelocity = body.getLinearVelocity();

        // Aplica um pequeno movimento aleatório suave
        if (currentVelocity.len() < 1.0f) {
            Vector2 wanderForce = new Vector2(
                    (random.nextFloat() - 0.5f) * 2f,
                    (random.nextFloat() - 0.5f) * 2f).nor().scl(CHASE_SPEED * 0.3f);

            body.applyForceToCenter(wanderForce, true);
        }

        // Limita velocidade máxima
        if (body.getLinearVelocity().len() > CHASE_SPEED * 0.3f) {
            body.setLinearVelocity(body.getLinearVelocity().nor().scl(CHASE_SPEED * 0.3f));
        }
    }

    private Vector2 getEffectiveTargetPosition(Vector2 currentPosition, Vector2 currentTargetPosition,
            boolean hasDirectSight) {
        if (hasDirectSight && currentTargetPosition != null) {
            return currentTargetPosition;
        }

        // Usa a última posição conhecida pelo próprio ChaseSystem
        if (lastKnownTargetPosition != null) {
            return lastKnownTargetPosition;
        }

        // Usa o último breadcrumb se existir
        if (!targetTrail.isEmpty()) {
            return targetTrail.getLast();
        }

        // Sem nada útil — retornar null para acionar fallback
        return null;
    }

    private boolean shouldRecalculatePath(Vector2 currentPosition, Vector2 targetPosition) {
        if (targetPosition == null)
            return true; // força fallback path quando target indefinido

        if (chasePath == null || chasePath.isEmpty()) {
            return true;
        }

        if (repathTimer >= REPATH_INTERVAL) {
            return true;
        }

        if (lastKnownTargetPosition != null &&
                targetPosition.dst(lastKnownTargetPosition) > 1.5f) {
            return true;
        }

        // Se estivermos "presa" no nó
        if (chasePathIndex > 0 && chasePathIndex < chasePath.size()) {
            Vector2 currentTarget = chasePath.get(chasePathIndex);
            float distanceToNode = currentPosition.dst(currentTarget);

            if (distanceToNode < 0.3f && repathTimer > REPATH_INTERVAL * 0.5f) {
                return true;
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

        if (DEBUG_LOGS) {
            Gdx.app.log("ChaseSystem", "Seguindo nó " + chasePathIndex + "/" + (chasePath.size() - 1));
            Gdx.app.log("ChaseSystem", "Nó atual: " + targetNode);
            Gdx.app.log("ChaseSystem", "Distância até nó: " + distanceToNode);
        }

        if (distanceToNode < PATH_NODE_TOLERANCE) {
            chasePathIndex++;
            if (chasePathIndex >= chasePath.size()) {
                return;
            }
            targetNode = chasePath.get(chasePathIndex);
        }

        Vector2 direction = targetNode.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(speed);
        applySteeringForce(body, desiredVelocity, deltaTime);
    }

    private void moveDirectlyToTarget(Body body, Vector2 currentPosition,
            Vector2 targetPosition, float speed) {
        if (targetPosition == null) {
            body.setLinearVelocity(0, 0);
            return;
        }

        float distance = currentPosition.dst(targetPosition);

        if (distance > 0.5f) {
            Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
            Vector2 desiredVelocity = direction.scl(speed);
            applySteeringForce(body, desiredVelocity, 0.1f);
        } else {
            body.setLinearVelocity(0, 0);
        }
    }

    private void applySteeringForce(Body body, Vector2 desiredVelocity, float deltaTime) {
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 velocityError = desiredVelocity.cpy().sub(currentVelocity);
        Vector2 steeringForce = velocityError.scl(STEERING_FORCE * body.getMass());
        body.applyForceToCenter(steeringForce, true);

        if (body.getLinearVelocity().len() > CHASE_SPEED) {
            body.setLinearVelocity(body.getLinearVelocity().nor().scl(CHASE_SPEED));
        }
    }

    private void calculateNewChasePath(Vector2 start, Vector2 end) {
        if (start == null || end == null) {
            chasePath = new ArrayList<>();
            chasePathIndex = 0;
            if (DEBUG_LOGS)
                Gdx.app.log("ChaseSystem", "calculateNewChasePath: start ou end nulo");
            return;
        }

        chasePath = pathfindingSystem.findPath(start, end);
        chasePathIndex = 0;

        if (chasePath != null && !chasePath.isEmpty()) {
            Gdx.app.log("ChaseSystem", "Novo caminho com " + chasePath.size() + " pontos");
        } else {
            Gdx.app.log("ChaseSystem", "Falha ao calcular caminho");
            chasePath = new ArrayList<>();
        }
    }

    public void reset() {
        chasePath.clear();
        chasePathIndex = 0;
        repathTimer = 0;
        lastKnownTargetPosition = null;
    }

    public boolean hasReachedLastKnown() {
        return reachedLastKnown;
    }

    public void clearReachedLastKnown() {
        reachedLastKnown = false;
    }

    public List<Vector2> getChasePath() {
        return chasePath;
    }

    public Vector2 getLastKnownPosition() {
        return lastKnownTargetPosition;
    }

    public void debugRender(ShapeRenderer shapeRenderer, Vector2 cameraOffset, float tileSize) {
        ShapeRenderer.ShapeType originalType = shapeRenderer.getCurrentType();

        if (chasePath != null && !chasePath.isEmpty()) {
            shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);

            for (int i = 0; i < chasePath.size() - 1; i++) {
                Vector2 start = chasePath.get(i);
                Vector2 end = chasePath.get(i + 1);

                float startX = cameraOffset.x + start.x * tileSize;
                float startY = cameraOffset.y + start.y * tileSize;
                float endX = cameraOffset.x + end.x * tileSize;
                float endY = cameraOffset.y + end.y * tileSize;

                shapeRenderer.line(startX, startY, endX, endY);
            }

            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < chasePath.size(); i++) {
                Vector2 point = chasePath.get(i);
                float pointX = cameraOffset.x + point.x * tileSize;
                float pointY = cameraOffset.y + point.y * tileSize;

                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.circle(pointX, pointY, 2f);

                if (i == chasePathIndex) {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.circle(pointX, pointY, 3f);
                }
            }
        }

        if (lastKnownTargetPosition != null) {
            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.ORANGE);
            float posX = cameraOffset.x + lastKnownTargetPosition.x * tileSize;
            float posY = cameraOffset.y + lastKnownTargetPosition.y * tileSize;
            shapeRenderer.circle(posX, posY, 4f);
        }

        shapeRenderer.set(originalType);
    }
}