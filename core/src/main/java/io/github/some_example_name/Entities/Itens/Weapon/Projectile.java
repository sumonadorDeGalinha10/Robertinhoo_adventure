package io.github.some_example_name.Entities.Itens.Weapon;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Mapa;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import box2dLight.PointLight;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

public class Projectile {
    private Body body;
    private float damage;
    private float lifespan = 3f;
    private float timeAlive = 0f;
    private float stateTime = 0f;
    private float angle;
    private static final float WIDTH = 0.4f;
    private static final float HEIGHT = 0.3f;
    private Mapa mapa;

    private static final float HITBOX_RADIUS = 0.1f; // Rai
    private boolean toBeDestroyed = false;

    private boolean isDestroying = false;
    private float destructionTime = 0f;
    private Vector2 lastPosition;
    public float destructionAngle;

    private PointLight light;

    public PointLight getLight() {
        return light;
    }

    public void setLight(PointLight light) {
        this.light = light;
    }

    private List<Vector2> trailPositions = new ArrayList<>();
    private static final int TRAIL_LENGTH = 8;

    public void markForDestruction() {
        toBeDestroyed = true;
    }

    public boolean isMarkedForDestruction() {
        return toBeDestroyed;
    }

    public Projectile(Mapa mapa, Vector2 position, Vector2 velocity, float damage) {
        this.damage = damage;
        this.mapa = mapa;
        createBody(mapa, position, velocity);
        mapa.addProjectile(this);
        
        
    }

    public void updateStateTime(float delta) {
        stateTime += delta;
    }

    public float getStateTime() {
        return stateTime;
    }

    private void createBody(Mapa mapa, Vector2 position, Vector2 velocity) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        body = mapa.world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(HITBOX_RADIUS);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;

        fixtureDef.filter.categoryBits = Mapa.CATEGORY_PROJECTILE;
        fixtureDef.filter.maskBits = Mapa.MASK_PROJECTILE;

        body.createFixture(fixtureDef);
        shape.dispose();
        body.setLinearVelocity(velocity);
        body.setUserData(this);
    
}
    

    public void update(float delta) {
        if (light != null && body != null) {
            light.setPosition(body.getPosition());
            Gdx.app.log("PROJECTILE", "Posição Box2D da luz: " + light.getPosition());
        }
        if (isDestroying) {
            destructionTime += delta;
            if (destructionTime >= 0.5f) {
                markForDestruction();
            }
        } else {
            timeAlive += delta;
            if (timeAlive >= lifespan) {
                startDestruction();
            }
            if (body != null) {
                lastPosition = body.getPosition().cpy();
            }
        }

        trailPositions.add(new Vector2(getPosition()));
        if (trailPositions.size() > TRAIL_LENGTH) {
            trailPositions.remove(0);
        }
    }

    public List<Vector2> getTrailPositions() {
        return trailPositions;
    }

    public void destroy() {
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
        if (light != null) {
            light.remove();
            light = null;
        }
        markForDestruction();
    }

    public Body getBody() {
        return body;
    }

    public float getDamage() {
        return damage;
    }

    public Vector2 getPosition() {
        if (body != null) {
            return body.getPosition();
        } else {
            return lastPosition;
        }
    }

    public float getWidth() {
        return WIDTH;
    }

    public float getHeight() {
        return HEIGHT;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public boolean isDestroying() {
        return isDestroying;
    }

    public float getDestructionTime() {
        return destructionTime;
    }

    public void startDestruction() {
        if (!isDestroying) {
            isDestroying = true;
            destructionTime = 0f;
            if (body != null) {
                body.setLinearVelocity(0, 0);
                body.setAngularVelocity(0);
                lastPosition = body.getPosition().cpy();
                destructionAngle = angle;
            }
            if (light != null) {
                light.remove();
                light = null;
            }
        }
    }
}