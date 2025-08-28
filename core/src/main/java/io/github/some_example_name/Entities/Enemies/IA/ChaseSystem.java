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
    private static final float REPATH_INTERVAL = 0.5f;
    private static final float CHASE_SPEED = 10f;
    private static final float STEERING_FORCE = 5f;
    private static final float LOSE_SIGHT_RANGE = 15f;

    public ChaseSystem(PathfindingSystem pathfindingSystem, Mapa mapa) {
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.random = new Random();
    }

    public void update(float deltaTime, Body body, Vector2 currentPosition, 
                      Vector2 targetPosition, boolean hasDirectSight) {
        repathTimer += deltaTime;

        float effectiveChaseSpeed = hasDirectSight ? CHASE_SPEED : CHASE_SPEED * 0.7f;

        if (repathTimer >= REPATH_INTERVAL || chasePath.isEmpty()) {
            repathTimer = 0;
            calculateNewChasePath(currentPosition, targetPosition);
        }

        if (chasePath == null || chasePath.isEmpty()) {
            Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
            Vector2 desiredVelocity = direction.scl(effectiveChaseSpeed);
            applySteeringForce(body, desiredVelocity);

            if (!hasDirectSight && currentPosition.dst(targetPosition) < 1.0f) {
                Gdx.app.log("ChaseSystem", "Procurando jogador na última posição conhecida");
                Vector2 searchDirection = new Vector2(
                        (random.nextFloat() * 2 - 1),
                        (random.nextFloat() * 2 - 1)).nor();
                Vector2 searchVelocity = searchDirection.scl(effectiveChaseSpeed * 0.5f);
                applySteeringForce(body, searchVelocity);
            }
        } else {
            followChasePathWithForces(body, currentPosition, effectiveChaseSpeed, deltaTime);
        }
    }

    private void followChasePathWithForces(Body body, Vector2 currentPosition, 
                                         float speed, float deltaTime) {
        if (chasePath == null || chasePath.isEmpty() || chasePathIndex >= chasePath.size()) {
            return;
        }

        Vector2 targetPosition = chasePath.get(chasePathIndex);
        float distance = currentPosition.dst(targetPosition);

        if (distance < 0.8f) {
            chasePathIndex++;
            if (chasePathIndex >= chasePath.size()) {
                return;
            }
            targetPosition = chasePath.get(chasePathIndex);
        }

        Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(speed);
        applySteeringForce(body, desiredVelocity);
    }

    private void applySteeringForce(Body body, Vector2 desiredVelocity) {
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 steering = desiredVelocity.sub(currentVelocity).scl(body.getMass() * 8f);
        body.applyForceToCenter(steering, true);
    }

    private void calculateNewChasePath(Vector2 start, Vector2 end) {
        chasePath = pathfindingSystem.findPath(start, end);
        chasePathIndex = 0;

        if (chasePath != null && !chasePath.isEmpty()) {
            Gdx.app.log("ChaseSystem", "Novo caminho de perseguição calculado com " + chasePath.size() + " pontos");
        } else {
            Gdx.app.log("ChaseSystem", "Falha ao calcular caminho de perseguição");
        }
    }

    public void reset() {
        chasePath.clear();
        chasePathIndex = 0;
        repathTimer = 0;
    }

    public List<Vector2> getChasePath() {
        return chasePath;
    }
}