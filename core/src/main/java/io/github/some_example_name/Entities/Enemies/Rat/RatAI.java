package io.github.some_example_name.Entities.Enemies.Rat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;

import java.util.List;

public class RatAI {
    private final Robertinhoo target;
    private final PathfindingSystem pathfindingSystem;
    private List<Vector2> currentPath;
    private int currentPathIndex;
    private float repathTimer = 0;
    private static final float REPATH_INTERVAL = 0.5f;

    public RatAI(Ratinho owner, Robertinhoo target, PathfindingSystem pathfindingSystem) {
        this.target = target;
        this.pathfindingSystem = pathfindingSystem;
    }

    public void update(float deltaTime, Body body) {
        repathTimer += deltaTime;
        if (repathTimer >= REPATH_INTERVAL) {
            repathTimer = 0;
            calculateNewPath(body);
        }
        followPath(body, deltaTime);
    }

    private void calculateNewPath(Body body) {
        Vector2 start = body.getPosition();
        Vector2 end = target.getPosition();
        currentPath = pathfindingSystem.findPath(start, end);
        currentPathIndex = 0;
    }

    private void followPath(Body body, float deltaTime) {
        if (currentPath == null || currentPath.isEmpty() || currentPathIndex >= currentPath.size()) {
            return;
        }
        Vector2 targetPosition = currentPath.get(currentPathIndex);
        Vector2 currentPosition = body.getPosition();

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

    public List<Vector2> getCurrentPath() {
        return currentPath;
    }
}
