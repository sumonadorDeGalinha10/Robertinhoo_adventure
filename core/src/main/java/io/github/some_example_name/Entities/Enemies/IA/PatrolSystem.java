// PatrolSystem.java
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

    private static final float SPEED = 6f;
    private static final float WAYPOINT_REACHED_DISTANCE = 0.8f;
    private static final float MIN_ROOM_DISTANCE = 20.0f;

    private boolean shouldGenerateNewPath = true;
    private float pathGenerationCooldown = 0f;
    private static final float PATH_GENERATION_DELAY = 1.0f;

    public PatrolSystem(Mapa mapa, Body body, PathfindingSystem pathfindingSystem) {
        this.mapa = mapa;
        this.body = body;
        this.pathfindingSystem = pathfindingSystem;
    }

    public void update(float deltaTime, Vector2 currentPosition) {

        if (pathGenerationCooldown > 0) {
            pathGenerationCooldown -= deltaTime;
        }

        if (shouldGenerateNewPath && pathGenerationCooldown <= 0) {
            generateLongPath();
            shouldGenerateNewPath = false;
            pathGenerationCooldown = PATH_GENERATION_DELAY;
        }

        if (currentPath.isEmpty() || currentPathIndex >= currentPath.size()) {
            shouldGenerateNewPath = true;
            return;
        }

        followPath(currentPosition, deltaTime);

        Vector2 finalTarget = currentPath.get(currentPath.size() - 1);
        if (currentPosition.dst(finalTarget) < WAYPOINT_REACHED_DISTANCE * 2) {
            shouldGenerateNewPath = true;
            Gdx.app.log("PatrolSystem", "Chegou ao destino, gerando nova rota...");
        }
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
        currentPath = pathfindingSystem.findPath(currentPos, worldTarget);
        currentPathIndex = 0;

        if (currentPath == null || currentPath.isEmpty()) {
            generateFallbackLongPath(currentPos);
        } else {
            Gdx.app.log("PatrolSystem", "Nova rota longa gerada com " + currentPath.size() + " pontos");
            Gdx.app.log("PatrolSystem", "Destino: " + worldTarget);
        }
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

            if (distance > maxDistance) {
                maxDistance = distance;
                mostDistantRoom = room;
            }
        }

        return mostDistantRoom;
    }

    private void generateFallbackLongPath(Vector2 currentPos) {

        float angle = random.nextFloat() * (float) Math.PI * 2;
        float distance = 20 + random.nextFloat() * 25;

        Vector2 target = new Vector2(
                currentPos.x + (float) Math.cos(angle) * distance,
                currentPos.y + (float) Math.sin(angle) * distance);

        Vector2 tileTarget = mapa.worldToTile(target);
        tileTarget.x = Math.max(0, Math.min(mapa.mapWidth - 1, tileTarget.x));
        tileTarget.y = Math.max(0, Math.min(mapa.mapHeight - 1, tileTarget.y));

        target = mapa.tileToWorld((int) tileTarget.x, (int) tileTarget.y);

        currentPath = pathfindingSystem.findPath(currentPos, target);
        currentPathIndex = 0;

        if (currentPath == null || currentPath.isEmpty()) {
            target = new Vector2(
                    currentPos.x + (random.nextFloat() * 15 - 7.5f),
                    currentPos.y + (random.nextFloat() * 15 - 7.5f));
            currentPath = pathfindingSystem.findPath(currentPos, target);
            currentPathIndex = 0;
        }

        Gdx.app.log("PatrolSystem", "Rota de fallback gerada com " +
                (currentPath != null ? currentPath.size() : 0) + " pontos");
    }

    private static final float CORRIDOR_NAVIGATION_THRESHOLD = 1.5f;
    private static final float SLOW_DOWN_FACTOR = 0.6f;
    private static final float PREDICTIVE_STEERING_FACTOR = 1.5f;

    private void followPath(Vector2 currentPosition, float deltaTime) {
        if (currentPath.isEmpty() || currentPathIndex >= currentPath.size())
            return;

        Vector2 targetPosition = currentPath.get(currentPathIndex);
        float distance = currentPosition.dst(targetPosition);

        boolean inNarrowCorridor = isInNarrowCorridor(currentPosition);

        float adjustedSpeed = inNarrowCorridor ? SPEED * SLOW_DOWN_FACTOR : SPEED;

        Vector2 futurePosition = predictFuturePosition(currentPosition, body.getLinearVelocity(), 0.3f);
        boolean willBeInCorridor = isInNarrowCorridor(futurePosition);

        if (willBeInCorridor) {
            adjustedSpeed *= SLOW_DOWN_FACTOR;
        }

        if (distance < WAYPOINT_REACHED_DISTANCE) {
            currentPathIndex++;
            if (currentPathIndex >= currentPath.size()) {
                shouldGenerateNewPath = true;
                return;
            }
            targetPosition = currentPath.get(currentPathIndex);
        }

        Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(adjustedSpeed);

        if (currentPathIndex + 1 < currentPath.size()) {
            Vector2 nextTarget = currentPath.get(currentPathIndex + 1);
            Vector2 nextDirection = nextTarget.cpy().sub(targetPosition).nor();
            desiredVelocity.add(nextDirection.scl(PREDICTIVE_STEERING_FACTOR * deltaTime));
            desiredVelocity.nor().scl(adjustedSpeed);
        }

        applySteeringForce(desiredVelocity, inNarrowCorridor ? 54f : 6f);
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

        return freeTiles <= 3;
    }

    private Vector2 predictFuturePosition(Vector2 currentPosition, Vector2 velocity, float time) {
        return currentPosition.cpy().add(velocity.cpy().scl(time));
    }

    private void applySteeringForce(Vector2 desiredVelocity, float forceMultiplier) {
        Vector2 currentVelocity = body.getLinearVelocity();
        Vector2 steering = desiredVelocity.sub(currentVelocity).scl(body.getMass() * forceMultiplier);
        body.applyForceToCenter(steering, true);
    }

    public void reset() {
        shouldGenerateNewPath = true;
        pathGenerationCooldown = 0;
    }

    public void debugRender(ShapeRenderer shapeRenderer) {
        if (currentPath != null && !currentPath.isEmpty()) {
            shapeRenderer.setColor(Color.GREEN);
            for (int i = 0; i < currentPath.size(); i++) {
                Vector2 point = currentPath.get(i);
                shapeRenderer.circle(point.x, point.y, 0.1f);

                if (i > 0) {
                    Vector2 prev = currentPath.get(i - 1);
                    shapeRenderer.line(prev.x, prev.y, point.x, point.y);
                }
            }

            if (currentPathIndex < currentPath.size()) {
                shapeRenderer.setColor(Color.YELLOW);
                Vector2 currentTarget = currentPath.get(currentPathIndex);
                shapeRenderer.circle(currentTarget.x, currentTarget.y, 0.3f);

                shapeRenderer.setColor(Color.CYAN);
                Vector2 currentPos = body.getPosition();
                shapeRenderer.line(currentPos.x, currentPos.y, currentTarget.x, currentTarget.y);
            }

            shapeRenderer.setColor(Color.RED);
            Vector2 finalTarget = currentPath.get(currentPath.size() - 1);
            shapeRenderer.circle(finalTarget.x, finalTarget.y, 0.5f);
        }

        if (currentTargetRoomCenter != null) {
            Vector2 worldTarget = mapa.tileToWorld(
                    (int) currentTargetRoomCenter.x,
                    (int) currentTargetRoomCenter.y);

            shapeRenderer.setColor(Color.MAGENTA);
            shapeRenderer.circle(worldTarget.x, worldTarget.y, 0.5f);
            shapeRenderer.circle(worldTarget.x, worldTarget.y, 1.0f);
        }
    }
}