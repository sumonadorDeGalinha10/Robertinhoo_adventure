
package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PatrolSystem {
    private final Mapa mapa;
    private final Body body;
    private final PathfindingSystem pathfindingSystem;
    private final Random random = new Random();

    private List<Vector2> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;
    private Vector2 currentTargetRoomCenter;

    // 🔥 AJUSTE: Velocidades aumentadas
    private static final float BASE_SPEED = 6f;
    private static final float CORRIDOR_SPEED_BOOST = 1.8f; // 80% mais rápido em corredores
    private static final float WAYPOINT_REACHED_DISTANCE = 0.8f;
    private static final float MIN_ROOM_DISTANCE = 20.0f;

    private boolean shouldGenerateNewPath = true;
    private float pathGenerationCooldown = 0f;
    private static final float PATH_GENERATION_DELAY = 1.0f;

    // 🔥 AJUSTE: Parâmetros para velocidade em corredores
    private static final float CORRIDOR_NAVIGATION_THRESHOLD = 1.2f;
    private static final float SPEED_BOOST_FACTOR = 1.8f; // Boost de 80% em corredores
    private static final float PREDICTIVE_STEERING_FACTOR = 2.0f;
    private static final float CORRIDOR_STEERING_MULTIPLIER = 6f; // Steering normal em corredores

    // Sistema anti-encravamento
    private float stuckTimer = 0f;
    private static final float STUCK_THRESHOLD = 2.0f;
    private Vector2 lastPosition = new Vector2();
    private static final float MIN_MOVEMENT_DISTANCE = 0.3f;

    private int failedWaypointAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public PatrolSystem(Mapa mapa, Body body, PathfindingSystem pathfindingSystem) {
        this.mapa = mapa;
        this.body = body;
        this.pathfindingSystem = pathfindingSystem;
        this.lastPosition.set(body.getPosition());
    }

    public void update(float deltaTime, Vector2 currentPosition) {
        updateStuckDetection(currentPosition, deltaTime);

        if (pathGenerationCooldown > 0) {
            pathGenerationCooldown -= deltaTime;
        }

        if ((shouldGenerateNewPath && pathGenerationCooldown <= 0) || isStuck()) {
            generateLongPath();
            shouldGenerateNewPath = false;
            pathGenerationCooldown = PATH_GENERATION_DELAY;
            resetStuckDetection();
        }

        if (currentPath.isEmpty() || currentPathIndex >= currentPath.size()) {
            shouldGenerateNewPath = true;
            return;
        }

        followPath(currentPosition, deltaTime);

        Vector2 finalTarget = currentPath.get(currentPath.size() - 1);
        if (currentPosition.dst(finalTarget) < WAYPOINT_REACHED_DISTANCE * 2) {
            shouldGenerateNewPath = true;
            Gdx.app.log("PatrolSystem", "🎯 Chegou ao destino, gerando nova rota...");
            resetStuckDetection();
        }
    }

    private void updateStuckDetection(Vector2 currentPosition, float deltaTime) {
        float movement = currentPosition.dst(lastPosition);

        if (movement < MIN_MOVEMENT_DISTANCE) {
            stuckTimer += deltaTime;
        } else {
            stuckTimer = 0f;
            failedWaypointAttempts = 0;
        }

        lastPosition.set(currentPosition);
    }

    private boolean isStuck() {
        return stuckTimer >= STUCK_THRESHOLD;
    }

    private void resetStuckDetection() {
        stuckTimer = 0f;
        failedWaypointAttempts = 0;
    }

    private void generateLongPath() {
        Vector2 currentPos = body.getPosition();

        Rectangle targetRoom = findDistantRoom(currentPos);
        if (targetRoom == null) {
            generateFallbackLongPath(currentPos);
            return;
        }

        currentTargetRoomCenter = new Vector2(
                targetRoom.x + targetRoom.width / 2,
                targetRoom.y + targetRoom.height / 2);
        Vector2 worldTarget = mapa.tileToWorld(
                (int) currentTargetRoomCenter.x,
                (int) currentTargetRoomCenter.y);

        currentPath = findRobustPath(currentPos, worldTarget);
        currentPathIndex = 0;

        if (currentPath == null || currentPath.isEmpty()) {
            generateFallbackLongPath(currentPos);
        }
    }

    private List<Vector2> findRobustPath(Vector2 start, Vector2 target) {
        List<Vector2> path = pathfindingSystem.findPath(start, target);

        if (path == null || path.isEmpty()) {

            for (int i = 0; i < 8; i++) {
                float angle = (float) (i * Math.PI / 4);
                float distance = 3f;
                Vector2 alternativeTarget = target.cpy().add(
                        new Vector2((float) Math.cos(angle) * distance,
                                (float) Math.sin(angle) * distance));

                path = pathfindingSystem.findPath(start, alternativeTarget);
                if (path != null && !path.isEmpty()) {
                    return path;
                }
            }
        }

        return path;
    }

    private Rectangle findDistantRoom(Vector2 currentPosition) {
        List<Rectangle> rooms = mapa.getRooms();
        if (rooms.size() < 2)
            return null;

        Vector2 currentTilePos = mapa.worldToTile(currentPosition);

        Rectangle mostDistantRoom = null;
        float maxDistance = 0;

        for (Rectangle room : rooms) {
            Vector2 roomCenter = new Vector2(room.x + room.width / 2, room.y + room.height / 2);
            float distance = currentTilePos.dst(roomCenter);

            float roomSizeBonus = room.width * room.height * 0.01f;
            float weightedDistance = distance + roomSizeBonus;

            if (weightedDistance > maxDistance) {
                maxDistance = weightedDistance;
                mostDistantRoom = room;
            }
        }

        return mostDistantRoom;
    }

    private void generateFallbackLongPath(Vector2 currentPos) {
        Vector2[] primaryDirections = {
                new Vector2(1, 0), new Vector2(-1, 0), new Vector2(0, 1), new Vector2(0, -1),
                new Vector2(1, 1), new Vector2(-1, 1), new Vector2(1, -1), new Vector2(-1, -1)
        };

        for (Vector2 dir : primaryDirections) {
            Vector2 target = currentPos.cpy().add(dir.scl(15f));
            Vector2 tileTarget = mapa.worldToTile(target);
            tileTarget.x = Math.max(1, Math.min(mapa.mapWidth - 2, tileTarget.x));
            tileTarget.y = Math.max(1, Math.min(mapa.mapHeight - 2, tileTarget.y));

            target = mapa.tileToWorld((int) tileTarget.x, (int) tileTarget.y);
            currentPath = pathfindingSystem.findPath(currentPos, target);

            if (currentPath != null && !currentPath.isEmpty()) {
                currentPathIndex = 0;
                return;
            }
        }

        Vector2 target = new Vector2(
                currentPos.x + (random.nextFloat() * 8 - 4f),
                currentPos.y + (random.nextFloat() * 8 - 4f));
        currentPath = pathfindingSystem.findPath(currentPos, target);
        currentPathIndex = 0;
    }

    private void followPath(Vector2 currentPosition, float deltaTime) {
        if (currentPath.isEmpty() || currentPathIndex >= currentPath.size())
            return;

        Vector2 targetPosition = currentPath.get(currentPathIndex);
        float distance = currentPosition.dst(targetPosition);

        // 🔥 MUDANÇA PRINCIPAL: AGORA ACELERA EM CORREDORES
        boolean inNarrowCorridor = isInNarrowCorridor(currentPosition);
        boolean approachingCorner = isApproachingCorner(currentPosition, targetPosition);

        // 🔥 VELOCIDADE AUMENTADA EM CORREDORES
        float adjustedSpeed = inNarrowCorridor ? BASE_SPEED * SPEED_BOOST_FACTOR : BASE_SPEED;

        // Apenas reduz velocidade em cantos muito fechados, não em corredores
        if (approachingCorner && !inNarrowCorridor) {
            adjustedSpeed *= 0.8f; // Pequena redução apenas em cantos fora de corredores
        }

        if (distance < WAYPOINT_REACHED_DISTANCE) {
            currentPathIndex++;
            failedWaypointAttempts = 0;
            if (currentPathIndex >= currentPath.size()) {
                shouldGenerateNewPath = true;
                return;
            }
            targetPosition = currentPath.get(currentPathIndex);
        } else if (distance > WAYPOINT_REACHED_DISTANCE * 3f) {
            failedWaypointAttempts++;
            if (failedWaypointAttempts >= MAX_FAILED_ATTEMPTS) {
                currentPathIndex++;
                failedWaypointAttempts = 0;
                if (currentPathIndex >= currentPath.size()) {
                    shouldGenerateNewPath = true;
                    return;
                }
            }
        }

        Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(adjustedSpeed);

        // Steering preditivo - mais agressivo em corredores
        if (currentPathIndex + 1 < currentPath.size()) {
            Vector2 nextTarget = currentPath.get(currentPathIndex + 1);
            Vector2 nextDirection = nextTarget.cpy().sub(targetPosition).nor();

            float predictiveFactor = inNarrowCorridor ? PREDICTIVE_STEERING_FACTOR * 1.5f : // Mais preditivo em
                                                                                            // corredores
                    PREDICTIVE_STEERING_FACTOR;

            float angle = direction.angleDeg(nextDirection);
            if (Math.abs(angle) < 90f) {
                desiredVelocity.add(nextDirection.scl(predictiveFactor * deltaTime));
                desiredVelocity.nor().scl(adjustedSpeed);
            }
        }

        // 🔥 Steering mais suave em corredores (já que está mais rápido)
        float steeringForce = inNarrowCorridor ? CORRIDOR_STEERING_MULTIPLIER : 8f;
        if (approachingCorner) {
            steeringForce *= 1.3f; // Leve aumento nas curvas
        }

        applySteeringForce(desiredVelocity, steeringForce);
    }

    private boolean isInNarrowCorridor(Vector2 position) {
        Vector2 tilePos = mapa.worldToTile(position);
        int tileX = (int) tilePos.x;
        int tileY = (int) tilePos.y;

        int freeTiles = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;

                int checkX = tileX + dx;
                int checkY = tileY + dy;

                if (checkX >= 0 && checkX < mapa.mapWidth &&
                        checkY >= 0 && checkY < mapa.mapHeight &&
                        !mapa.isTileBlocked(checkX, checkY)) {
                    freeTiles++;
                }
            }
        }

        return freeTiles >= 2 && freeTiles <= 5;
    }

    private boolean isApproachingCorner(Vector2 currentPosition, Vector2 nextWaypoint) {
        if (currentPathIndex + 1 >= currentPath.size())
            return false;

        Vector2 currentDirection = nextWaypoint.cpy().sub(currentPosition).nor();
        Vector2 nextWaypoint2 = currentPath.get(currentPathIndex + 1);
        Vector2 nextDirection = nextWaypoint2.cpy().sub(nextWaypoint).nor();

        float angle = Math.abs(currentDirection.angleDeg(nextDirection));
        return angle > 60f; // Só considera canto se mudança > 60 graus
    }

    private void applySteeringForce(Vector2 desiredVelocity, float forceMultiplier) {
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 steering = desiredVelocity.sub(currentVelocity).scl(body.getMass() * forceMultiplier);

        float maxForce = body.getMass() * 25f; // Aumentado para permitir manobras mais rápidas
        if (steering.len() > maxForce) {
            steering.nor().scl(maxForce);
        }

        body.applyForceToCenter(steering, true);
    }

    public void reset() {
        shouldGenerateNewPath = true;
        pathGenerationCooldown = 0;
        resetStuckDetection();
    }

    public void forceNewPath() {
        shouldGenerateNewPath = true;
        pathGenerationCooldown = 0;
    }

    public void debugRender(ShapeRenderer shapeRenderer) {
        if (currentPath != null && !currentPath.isEmpty()) {
            // Cor baseada no tipo de área
            boolean inCorridor = isInNarrowCorridor(body.getPosition());
            shapeRenderer.setColor(inCorridor ? Color.CYAN : Color.GREEN);

            for (int i = 1; i < currentPath.size(); i++) {
                Vector2 prev = currentPath.get(i - 1);
                Vector2 current = currentPath.get(i);
                shapeRenderer.line(prev.x, prev.y, current.x, current.y);
            }

            shapeRenderer.setColor(Color.YELLOW);
            for (Vector2 point : currentPath) {
                shapeRenderer.circle(point.x, point.y, 0.1f);
            }

            if (currentPathIndex < currentPath.size()) {
                shapeRenderer.setColor(Color.ORANGE);
                Vector2 currentTarget = currentPath.get(currentPathIndex);
                shapeRenderer.circle(currentTarget.x, currentTarget.y, 0.2f);
            }

            shapeRenderer.setColor(Color.RED);
            Vector2 finalTarget = currentPath.get(currentPath.size() - 1);
            shapeRenderer.circle(finalTarget.x, finalTarget.y, 0.3f);

            if (isStuck()) {
                Vector2 pos = body.getPosition();
                shapeRenderer.setColor(Color.MAGENTA);
                shapeRenderer.circle(pos.x, pos.y, 0.5f);
                shapeRenderer.circle(pos.x, pos.y, 0.7f);
            }
        }
    }
}