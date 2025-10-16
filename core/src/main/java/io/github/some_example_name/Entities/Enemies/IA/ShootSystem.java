package io.github.some_example_name.Entities.Enemies.IA;

import io.github.some_example_name.Entities.Enemies.Castor.Castor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.MapConfig.Mapa;
import java.util.List;
import java.util.Random;

public class ShootSystem {
    private final Castor castor;
    private final PathfindingSystem pathfindingSystem;
    private final Mapa mapa;
    private final Random random;

    private static final float IDEAL_SHOOTING_DISTANCE = 6f;
    private static final float MIN_SHOOTING_DISTANCE = 3f;
    private static final float MOVEMENT_ERROR = 0.4f;
    private static final float SHOOTING_RANGE = 7f;

    public ShootSystem(Castor castor, PathfindingSystem pathfindingSystem, Mapa mapa) {
        this.castor = castor;
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.random = new Random();
    }

    public boolean update(float deltaTime, Body body, Vector2 currentPosition, Vector2 targetPosition) {
        float distanceToTarget = currentPosition.dst(targetPosition);
        boolean hasLOS = hasLineOfSight(currentPosition, targetPosition);

        // VERIFICAÇÃO CRÍTICA: Não permitir atirar sem linha de visão
        if (!hasLOS) {
            return handleRepositioning(body, currentPosition, targetPosition);
        }

        // Comportamento de combate corpo a corpo
        if (distanceToTarget < MIN_SHOOTING_DISTANCE) {
            handleCloseCombat(body, currentPosition, targetPosition);
            return false;
        }

        if (distanceToTarget > SHOOTING_RANGE) {
            return true;
        }

        handleRangedCombat(body, currentPosition, targetPosition, distanceToTarget);

        // VERIFICAÇÃO EXTRA: Só permitir atirar se tiver linha de visão
        if (castor.canShoot() && hasLOS && !castor.isShooting()) {
            castor.startShooting();
        }

        return false;
    }

    private boolean handleRepositioning(Body body, Vector2 currentPosition, Vector2 targetPosition) {
        Vector2 repositionTarget = findRepositionTarget(currentPosition, targetPosition);

        if (repositionTarget != null) {
            List<Vector2> repositionPath = pathfindingSystem.findPath(currentPosition, repositionTarget);

            if (repositionPath != null && !repositionPath.isEmpty()) {
                Vector2 nextPoint = repositionPath.get(0);
                Vector2 direction = nextPoint.cpy().sub(currentPosition).nor();
                Vector2 desiredVelocity = direction.scl(3f);
                applySteeringForce(body, desiredVelocity);

            } else {
                Vector2 direction = repositionTarget.cpy().sub(currentPosition).nor();
                Vector2 desiredVelocity = direction.scl(3f);
                applySteeringForce(body, desiredVelocity);
            }
            return false;
        } else {
            return true; // Indica que deve mudar para estado CHASE
        }
    }

    private void handleCloseCombat(Body body, Vector2 currentPosition, Vector2 targetPosition) {
        Vector2 retreatDirection = findSafeRetreatDirection(currentPosition, targetPosition);

        if (retreatDirection.equals(Vector2.Zero)) {
            if (castor.canShoot()) {
                castor.shootAtPlayer();
            }

            Vector2 circleDirection = targetPosition.cpy().sub(currentPosition).nor().rotateDeg(90f);
            Vector2 desiredVelocity = circleDirection.scl(2.5f);
            applySteeringForce(body, desiredVelocity);
        } else {
            Vector2 desiredVelocity = retreatDirection.scl(3.5f);
            applySteeringForce(body, desiredVelocity);
            if (castor.canShoot()) {
                castor.shootAtPlayer();
            }
        }
    }

    private void handleRangedCombat(Body body, Vector2 currentPosition, Vector2 targetPosition,
            float distanceToTarget) {
        float adjustedIdealDistance = IDEAL_SHOOTING_DISTANCE *
                (1 + (random.nextFloat() * 2 - 1) * MOVEMENT_ERROR);

        Vector2 desiredVelocity;
        if (distanceToTarget < adjustedIdealDistance - 1f) {
            Vector2 retreatDirection = findSafeRetreatDirection(currentPosition, targetPosition);
            if (retreatDirection.equals(Vector2.Zero)) {
                retreatDirection = currentPosition.cpy().sub(targetPosition).nor();
            }
            retreatDirection.rotateRad((random.nextFloat() * 2 - 1) * 0.2f);
            desiredVelocity = retreatDirection.scl(2.5f);
        } else if (distanceToTarget > adjustedIdealDistance + 1f) {
            Vector2 approachDirection = targetPosition.cpy().sub(currentPosition).nor();
            approachDirection.rotateRad((random.nextFloat() * 2 - 1) * 0.2f);
            desiredVelocity = approachDirection.scl(2.5f);
        } else {
            if (random.nextFloat() > 0.5f) {
                Vector2 lateralDirection = targetPosition.cpy().sub(currentPosition).nor();
                lateralDirection.rotate90((random.nextBoolean()) ? 1 : -1);
                desiredVelocity = lateralDirection.scl(1.5f);
            } else {
                desiredVelocity = Vector2.Zero;
            }
        }

        applySteeringForce(body, desiredVelocity);
    }

    private Vector2 findSafeRetreatDirection(Vector2 currentPosition, Vector2 threatPosition) {
        Vector2 baseDirection = currentPosition.cpy().sub(threatPosition).nor();
        float[] angles = { -45f, -30f, -15f, 0f, 15f, 30f, 45f, 90f, -90f };

        for (float angle : angles) {
            Vector2 testDirection = baseDirection.cpy().rotateDeg(angle);
            Vector2 testPosition = currentPosition.cpy().add(testDirection.scl(2f));

            if (isValidPosition(testPosition) && !isPathBlocked(currentPosition, testPosition)) {
                return testDirection;
            }
        }

        Vector2 lateralDirection = baseDirection.cpy().rotateDeg(90f);
        Vector2 testPosition = currentPosition.cpy().add(lateralDirection.scl(2f));

        if (isValidPosition(testPosition) && !isPathBlocked(currentPosition, testPosition)) {
            return lateralDirection;
        }

        return Vector2.Zero;
    }

    private boolean isPathBlocked(Vector2 start, Vector2 end) {
        Vector2 direction = end.cpy().sub(start).nor();
        float distance = start.dst(end);

        for (float i = 0.5f; i < distance; i += 0.5f) {
            Vector2 checkPoint = start.cpy().add(direction.scl(i));
            Vector2 tilePos = mapa.worldToTile(checkPoint);
            int tileX = (int) tilePos.x;
            int tileY = (int) tilePos.y;

            if (tileX < 0 || tileX >= mapa.mapWidth ||
                    tileY < 0 || tileY >= mapa.mapHeight ||
                    mapa.isTileBlocked(tileX, tileY)) {
                return true;
            }
        }

        return false;
    }

    private Vector2 findRepositionTarget(Vector2 currentPosition, Vector2 targetPosition) {
        for (int i = 0; i < 8; i++) {
            float angle = i * (float) Math.PI / 4;
            float distance = 3f;

            Vector2 testPosition = new Vector2(
                    currentPosition.x + (float) Math.cos(angle) * distance,
                    currentPosition.y + (float) Math.sin(angle) * distance);

            if (isValidPosition(testPosition) && hasLineOfSight(testPosition, targetPosition)) {
                return testPosition;
            }
        }

        Vector2 directionToTarget = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 approachPosition = currentPosition.cpy().add(directionToTarget.scl(2f));

        if (isValidPosition(approachPosition)) {
            return approachPosition;
        }

        return null;
    }

    private boolean isValidPosition(Vector2 position) {
        Vector2 tilePos = mapa.worldToTile(position);
        int tileX = (int) tilePos.x;
        int tileY = (int) tilePos.y;

        return tileX >= 0 && tileX < mapa.mapWidth &&
                tileY >= 0 && tileY < mapa.mapHeight &&
                !mapa.isTileBlocked(tileX, tileY);
    }

    private void applySteeringForce(Body body, Vector2 desiredVelocity) {
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 steering = desiredVelocity.cpy().sub(currentVelocity).scl(body.getMass() * 8f);
        body.applyForceToCenter(steering, true);
    }

    // Adicione este método para verificação de linha de visão mais precisa
    private boolean hasLineOfSight(Vector2 start, Vector2 end) {
        float distance = start.dst(end);
        int samples = (int) (distance * 3); // Aumentado de 2 para 3 amostras

        for (int i = 1; i <= samples; i++) {
            float t = (float) i / samples;
            Vector2 point = start.cpy().lerp(end, t);
            Vector2 tilePoint = mapa.worldToTile(point);
            int tileX = (int) tilePoint.x;
            int tileY = (int) tilePoint.y;

            // Verificar também os tiles adjacentes para evitar "furos" na parede
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (mapa.isTileBlocked(tileX + dx, tileY + dy)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}