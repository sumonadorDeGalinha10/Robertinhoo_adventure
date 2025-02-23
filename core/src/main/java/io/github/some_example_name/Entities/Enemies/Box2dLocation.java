package io.github.some_example_name.Entities.Enemies;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

public class Box2dLocation implements Location<Vector2> {
    public final Vector2 position = new Vector2();
    public float orientation;

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    // Remove the @Override annotation, as it does not override a method in Location<Vector2>
    public Location<Vector2> setPosition(Vector2 position) {
        this.position.set(position);
        return this; 
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(vector.y, vector.x);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.set(
            (float) Math.cos(angle), 
            (float) Math.sin(angle)
        );
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }
}
