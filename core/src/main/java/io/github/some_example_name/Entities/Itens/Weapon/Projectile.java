package io.github.some_example_name.Entities.Itens.Weapon;


import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Mapa;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import java.util.List;

public class Projectile {
    private Body body;
    private float damage;
    private float lifespan = 3f; // Tempo de vida em segundos
    private float timeAlive = 0f;
    private float stateTime = 0f;
    private float angle;
    private static final float WIDTH = 0.4f; // 0.2 * 2 (box2d usa half-width)
    private static final float HEIGHT = 0.4f;
    private boolean toBeDestroyed = false;


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

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(WIDTH/2, HEIGHT/2); 

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        body.createFixture(fixtureDef);
        shape.dispose();

        body.setLinearVelocity(velocity);
        body.setUserData(this);
    }

    public void update(float delta) {
        timeAlive += delta;
        if (timeAlive >= lifespan) {
            destroy();
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
            body.getWorld().destroyBody(body); // Destrói imediatamente
            body = null;
        }
        markForDestruction();
    }
    public Body getBody() {
        return body;
    }

    public float getDamage() { return damage; }


    public Vector2 getPosition() {
        return body.getPosition(); // Retorna a posição central do corpo
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
}