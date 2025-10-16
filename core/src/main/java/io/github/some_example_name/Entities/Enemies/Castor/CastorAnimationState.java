package io.github.some_example_name.Entities.Enemies.Castor;

public class CastorAnimationState {
    public float stateTime = 0f;
    public float shootAnimationTime = 0f;
    public float damageAnimationTime = 0f;
    public float dashAnimationTime = 0f;
    public float deathAnimationTime = 0f;

    public void update(float deltaTime, Castor castor) {
        stateTime += deltaTime;

        if (castor.isShooting()) {
            shootAnimationTime += deltaTime;
        } else {
            shootAnimationTime = 0f;
        }

        if (castor.isTakingDamage()) {
            damageAnimationTime += deltaTime;
        } else {
            damageAnimationTime = 0f;
        }

        if (castor.isDashing()) {
            dashAnimationTime += deltaTime;
        } else {
            dashAnimationTime = 0f;
        }

        if (castor.isDead()) {
            deathAnimationTime += deltaTime;
        } else {
            deathAnimationTime = 0f;
        }
    }
}