package io.github.some_example_name.Entities.Enemies.Castor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Enemies.IA.PatrolSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CastorIA {
    private final Robertinhoo target;
    private final PathfindingSystem pathfindingSystem;
    private final Mapa mapa;
    private final Castor castor;
    private final PatrolSystem patrolSystem;
    private List<Vector2> chasePath = new ArrayList<>();
    private int chasePathIndex = 0;

    private enum State {
        PATROL, CHASE, SHOOTING
    }

    private State currentState = State.PATROL;

    private static final float DETECTION_RANGE = 12f;
    private static final float SHOOTING_RANGE = 7f;
    private static final float LOSE_SIGHT_RANGE = 15f;
    private static final float IDEAL_SHOOTING_DISTANCE = 6f;
    private static final float MIN_SHOOTING_DISTANCE = 4f;
    private static final float CHASE_SPEED = 4f;
    private static final float STEERING_FORCE = 5f;

    private float reactionTime = 0f;
    private static final float REACTION_DELAY = 0.7f;
    private static final float REACTION_VARIABILITY = 0.3f;
    private static final float MOVEMENT_ERROR = 0.4f;

    private float repathTimer = 0;
    private static final float REPATH_INTERVAL = 0.5f;

    private float stateCooldown = 0f;
    private static final float STATE_COOLDOWN_TIME = 3f;

    private Random random = new Random();

    public CastorIA(Castor castor, Robertinhoo target, PathfindingSystem pathfindingSystem, Mapa mapa) {
        this.castor = castor;
        this.target = target;
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.currentState = State.PATROL;
        this.patrolSystem = new PatrolSystem(mapa, castor.getBody(), pathfindingSystem);
    }

    public void update(float deltaTime, Body body) {

        debugMovementInfo();
        Vector2 currentPosition = body.getPosition();
        Vector2 targetPosition = target.getPosition();
        float distanceToTarget = currentPosition.dst(targetPosition);
        if (stateCooldown > 0) {
            stateCooldown -= deltaTime;
        }

        reactionTime -= deltaTime;
        if (reactionTime > 0) {
            return;
        }
        reactionTime = REACTION_DELAY + (random.nextFloat() * 2 - 1) * REACTION_VARIABILITY;

        if (target == null || target.getPosition() == null) {
            Gdx.app.log("CastorIA-ERROR", "Target ou posição do target é nula!");
            return;
        }

        Gdx.app.log("CastorIA", "Estado atual: " + currentState +
                ", Distância: " + distanceToTarget +
                ", Posição: " + currentPosition);

        if (stateCooldown <= 0) {
            checkStateTransitions(distanceToTarget);
        }
        switch (currentState) {
            case PATROL:
                Gdx.app.log("CastorIA", "Executando PATRULHA");
                updatePatrolWithForces(deltaTime, body, currentPosition);
                break;
            case CHASE:
                Gdx.app.log("CastorIA", "Executando PERSEGUIR");
                updateChaseWithForces(deltaTime, body, currentPosition, targetPosition);
                break;
            case SHOOTING:
                Gdx.app.log("CastorIA", "Executando ATIRAR");
                updateShootingWithForces(deltaTime, body, currentPosition, targetPosition);
                break;
        }
    }

    private void updateChaseWithForces(float deltaTime, Body body, Vector2 currentPosition, Vector2 targetPosition) {
        repathTimer += deltaTime;

        if (repathTimer >= REPATH_INTERVAL || chasePath.isEmpty()) {
            repathTimer = 0;
            calculateNewChasePath(currentPosition, targetPosition);
        }

        if (chasePath == null || chasePath.isEmpty()) {
            Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
            Vector2 desiredVelocity = direction.scl(CHASE_SPEED);
            applySteeringForce(body, desiredVelocity);
        } else {
            followChasePathWithForces(body, currentPosition, CHASE_SPEED, deltaTime);
        }
    }

    private void updateShootingWithForces(float deltaTime, Body body, Vector2 currentPosition, Vector2 targetPosition) {
        float distanceToTarget = currentPosition.dst(targetPosition);
        boolean hasLOS = hasLineOfSight(currentPosition, targetPosition);

        if (distanceToTarget < MIN_SHOOTING_DISTANCE || distanceToTarget > SHOOTING_RANGE || !hasLOS) {
            currentState = State.CHASE;
            stateCooldown = STATE_COOLDOWN_TIME;
            Gdx.app.log("CastorIA", "Fora do alcance ou sem linha de visão, voltando a perseguir");
            return;
        }

        float adjustedIdealDistance = IDEAL_SHOOTING_DISTANCE *
                (1 + (random.nextFloat() * 2 - 1) * MOVEMENT_ERROR);

        Vector2 desiredVelocity;
        if (distanceToTarget < adjustedIdealDistance - 1f) {
            Vector2 retreatDirection = currentPosition.cpy().sub(targetPosition).nor();
            retreatDirection.rotateRad((random.nextFloat() * 2 - 1) * 0.5f);
            desiredVelocity = retreatDirection.scl(2.5f);
        } else if (distanceToTarget > adjustedIdealDistance + 1f) {
            Vector2 approachDirection = targetPosition.cpy().sub(currentPosition).nor();
            approachDirection.rotateRad((random.nextFloat() * 2 - 1) * 0.5f);
            desiredVelocity = approachDirection.scl(2.5f);
        } else {
            if (random.nextFloat() > 0.3f) {
                desiredVelocity = Vector2.Zero;
            } else {
                float angle = random.nextFloat() * (float) Math.PI * 2;
                desiredVelocity = new Vector2((float) Math.cos(angle), (float) Math.sin(angle)).scl(1f);
            }
        }

        applySteeringForce(body, desiredVelocity);

        if (castor.canShoot() && hasLOS) {
            castor.shootAtPlayer();
        }
    }

    private void followChasePathWithForces(Body body, Vector2 currentPosition, float speed, float deltaTime) {
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

    private void checkStateTransitions(float distanceToTarget) {
        State previousState = currentState;
        Vector2 currentPosition = castor.getBody().getPosition();
        Vector2 targetPosition = target.getPosition();

        boolean hasLOS = hasLineOfSight(currentPosition, targetPosition);

        switch (currentState) {
            case PATROL:
                if (distanceToTarget <= DETECTION_RANGE && hasLOS) {
                    currentState = State.CHASE;
                    chasePath.clear();
                    chasePathIndex = 0;
                    stateCooldown = STATE_COOLDOWN_TIME;
                    Gdx.app.log("CastorIA", "Modo: PERSEGUIR (jogador visível)");
                }
                break;

            case CHASE:
                if (distanceToTarget > LOSE_SIGHT_RANGE || !hasLOS) {
                    currentState = State.PATROL;
                    patrolSystem.reset();
                    stateCooldown = STATE_COOLDOWN_TIME;
                    Gdx.app.log("CastorIA", "Modo: PATRULHA (perdeu visão)");
                } else if (distanceToTarget <= SHOOTING_RANGE && hasLOS) {
                    currentState = State.SHOOTING;
                    stateCooldown = STATE_COOLDOWN_TIME;
                    Gdx.app.log("CastorIA", "Modo: ATIRANDO (jogador visível)");
                }
                break;

            case SHOOTING:
                if (distanceToTarget > SHOOTING_RANGE * 1.1f || !hasLOS) {
                    currentState = State.CHASE;
                    stateCooldown = STATE_COOLDOWN_TIME;
                    Gdx.app.log("CastorIA", "Modo: PERSEGUIR (saiu do alcance ou perdeu visão)");
                } else if (distanceToTarget > LOSE_SIGHT_RANGE) {
                    currentState = State.PATROL;
                    patrolSystem.reset();
                    stateCooldown = STATE_COOLDOWN_TIME;
                    Gdx.app.log("CastorIA", "Modo: PATRULHA (perdeu visão)");
                }
                break;
        }

        if (previousState != currentState) {
            Gdx.app.log("CastorIA", "Transição de estado: " + previousState + " -> " + currentState);
        }
    }

    private void updatePatrolWithForces(float deltaTime, Body body, Vector2 currentPosition) {
        patrolSystem.update(deltaTime, currentPosition);
    }

    private void calculateNewChasePath(Vector2 start, Vector2 end) {
        chasePath = pathfindingSystem.findPath(start, end);
        chasePathIndex = 0;

        if (chasePath != null && !chasePath.isEmpty()) {
            Gdx.app.log("CastorIA-CHASE", "Novo caminho de perseguição calculado com " + chasePath.size() + " pontos");
        } else {
            Gdx.app.log("CastorIA-CHASE", "Falha ao calcular caminho de perseguição");
        }
    }

    public void debugMovementInfo() {
        Vector2 velocity = castor.getBody().getLinearVelocity();
        Vector2 position = castor.getBody().getPosition();

        Gdx.app.log("CastorDebug",
                "Velocidade: " + String.format("%.2f", velocity.len()) +
                        ", Estado: " + currentState +
                        ", Posição: " + String.format("(%.2f, %.2f)", position.x, position.y) +
                        ", Massa: " + castor.getBody().getMass() +
                        ", Damping: " + castor.getBody().getLinearDamping());
    }

    public State getCurrentState() {
        return currentState;
    }

    public void debugRender(ShapeRenderer shapeRenderer) {
        if (patrolSystem != null) {
            patrolSystem.debugRender(shapeRenderer);
        }
    }

    private boolean hasDetailedLineOfSight(Vector2 start, Vector2 end) {
        float distance = start.dst(end);
        int samples = (int) (distance * 2);

        for (int i = 1; i <= samples; i++) {
            float t = (float) i / samples;
            Vector2 point = start.cpy().lerp(end, t);
            Vector2 tilePoint = mapa.worldToTile(point);

            int tileX = (int) tilePoint.x;
            int tileY = (int) tilePoint.y;

            if (mapa.isTileBlocked(tileX, tileY)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasLineOfSight(Vector2 start, Vector2 end) {
        return hasDetailedLineOfSight(start, end);
    }

    public void debugRenderVision(ShapeRenderer shapeRenderer) {
        if (patrolSystem != null) {
            patrolSystem.debugRender(shapeRenderer);
        }

        Vector2 currentPos = castor.getBody().getPosition();
        Vector2 targetPos = target.getPosition();

        if (hasLineOfSight(currentPos, targetPos)) {
            shapeRenderer.setColor(Color.GREEN);
        } else {
            shapeRenderer.setColor(Color.RED);
        }

        shapeRenderer.line(currentPos.x, currentPos.y, targetPos.x, targetPos.y);
    }
}