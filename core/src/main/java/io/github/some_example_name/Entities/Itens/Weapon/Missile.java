package io.github.some_example_name.Entities.Itens.Weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.MapConfig.Mapa;

public class Missile extends Projectile {
    private static final float MISSILE_SPEED = 5f;
    private static final float MISSILE_DAMAGE = 25f;
    private static final float MISSILE_LIFESPAN = 4f;
    private Castor owner;
    private boolean isReflected = false;

    public Missile(Mapa mapa, Vector2 position, Vector2 direction, Castor owner) {
        super(mapa, position, direction.scl(MISSILE_SPEED), MISSILE_DAMAGE);
        this.owner = owner;
        this.lifespan = MISSILE_LIFESPAN;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (body != null && !body.getLinearVelocity().isZero(0.1f)) {
            Vector2 velocity = body.getLinearVelocity();
            float angle = velocity.angleDeg() - 90f;
            setAngle(angle);
        }
    }

    public void reflect(Vector2 newDirection) {
        if (body != null) {
            // A direção já está calculada e normalizada, apenas aplicamos
            float newSpeed = MISSILE_SPEED * 1.5f;
            body.setLinearVelocity(newDirection.scl(newSpeed));
            isReflected = true;

            Gdx.app.log("Missile", "Míssil refletido! Nova direção: " + newDirection + ", Velocidade: " + newSpeed);
        }
    }

    public Castor getOwner() {
        return owner;
    }
    public Vector2 getPosition() {
    if (body != null) {
        return body.getPosition();
    }
    return new Vector2(0, 0);
}

    public boolean isReflected() {
        return isReflected;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 0.6f;
    }
}