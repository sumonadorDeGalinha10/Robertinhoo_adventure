package io.github.some_example_name.Entities.Enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public abstract class Enemy {

    protected Vector2 position;
    protected float health;
    protected float speed;
    protected boolean isAlive = true;
    protected boolean toBeDestroyed = false;

    // Propriedades de morte movidas para cá
    public enum DeathType {
        NONE, MELEE, PROJECTILE
    }

    protected DeathType deathType = DeathType.NONE;
    protected float deathAnimationTime = 0;
    protected boolean isDead = false;
    protected boolean shouldDeactivate = false;

    public Enemy(float x, float y, float health, float speed) {
        this.position = new Vector2(x, y);
        this.health = health;
        this.speed = speed;
        this.isAlive = true;
    }

    public abstract void update(float deltaTime);

    public abstract TextureRegion getCurrentFrame(float deltaTime);

    public void takeDamage(float damage) {
        health -= damage;

        if (health <= 0) {
            isAlive = false;
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public void destroy() {
        toBeDestroyed = true;
    }

    public boolean isToBeDestroyed() {
        return toBeDestroyed;
    }

    public float getHealth() {
        return health;
    }

    public boolean isDead() {
        return !isAlive;
    }

    public abstract Body getBody();

    public abstract float getAttackDamage();

    // Novos métodos gerais para morte
    public void die(DeathType type) {
        if (isDead)
            return;

        isDead = true;
        isAlive = false;
        deathType = type;
        deathAnimationTime = 0;

        disableCollisions();
    }

    protected void disableCollisions() {
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setSensor(true);
        }
        getBody().setLinearVelocity(0, 0);
        getBody().setAngularVelocity(0);
    }

    public DeathType getDeathType() {
        return deathType;
    }

    public float getDeathAnimationTime() {
        return deathAnimationTime;
    }

    public boolean isDying() {
        return isDead && !isDeathAnimationFinished();
    }

    // Método abstrato para cada inimigo implementar sua própria lógica de animação
    public abstract boolean isDeathAnimationFinished();

    public void safeDeactivate() {
        if (shouldDeactivate) {
            getBody().setActive(false);
            shouldDeactivate = false;
        }
    }
}