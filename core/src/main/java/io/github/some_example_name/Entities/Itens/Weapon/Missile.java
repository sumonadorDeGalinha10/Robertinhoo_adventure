package io.github.some_example_name.Entities.Itens.Weapon;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.MapConfig.Mapa;

public class Missile extends Projectile {
    private static final float MISSILE_SPEED = 8f;
    private static final float MISSILE_DAMAGE = 25f;
    private static final float MISSILE_LIFESPAN = 4f;
    public Missile(Mapa mapa, Vector2 position, Vector2 direction) {
        super(mapa, position, direction.scl(MISSILE_SPEED), MISSILE_DAMAGE);
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
    
    @Override
    public float getWidth() {
        return 0.6f; 
    }
    
    @Override
    public float getHeight() {
        return 0.6f;
    }
}