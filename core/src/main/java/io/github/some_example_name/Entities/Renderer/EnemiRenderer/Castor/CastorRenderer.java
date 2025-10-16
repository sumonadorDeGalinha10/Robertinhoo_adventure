package io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Castor.CastorAnimationState;
import io.github.some_example_name.Entities.Renderer.CorpsesManager.CorpseManager;

public class CastorRenderer implements CorpseManager.CorpseRenderer {
    private CastorAnimationSystem animationSystem;
    private CastorAnimationSystem.Direction currentDirection;
    private int TILE_SIZE = 64;
    private static final float CASTOR_RENDER_WIDTH = 64f;
    private static final float CASTOR_RENDER_HEIGHT = 64f;

    public CastorRenderer() {
        this.animationSystem = new CastorAnimationSystem();
    }

    private CastorAnimationSystem.Direction getDirection(Vector2 velocity, Vector2 targetPosition,
            Vector2 currentPosition, boolean isShooting) {

        if (isShooting) {
            Vector2 directionToTarget = targetPosition.cpy().sub(currentPosition).nor();

            float absX = Math.abs(directionToTarget.x);
            float absY = Math.abs(directionToTarget.y);

            if (absX > absY) {
                return directionToTarget.x > 0 ? CastorAnimationSystem.Direction.RIGHT
                        : CastorAnimationSystem.Direction.LEFT;
            } else {
                return directionToTarget.y > 0 ? CastorAnimationSystem.Direction.UP
                        : CastorAnimationSystem.Direction.DOWN;
            }
        }

        float absVelX = Math.abs(velocity.x);
        float absVelY = Math.abs(velocity.y);

        if (absVelX > absVelY) {
            return velocity.x > 0 ? CastorAnimationSystem.Direction.RIGHT : CastorAnimationSystem.Direction.LEFT;
        } else {
            return velocity.y > 0 ? CastorAnimationSystem.Direction.UP : CastorAnimationSystem.Direction.DOWN;
        }
    }

    public void render(SpriteBatch batch, Castor castor, float offsetX, float offsetY, float delta) {
        CastorAnimationState animState = castor.getAnimationState();

        if (castor.isDead()) {
            renderDeathAnimation(batch, castor, offsetX, offsetY, delta, animState); // 🔥 PASSA ANIMSTATE
            return;
        }

        Vector2 velocity = castor.getLinearVelocity();
        Vector2 currentPosition = castor.getPosition();
        Vector2 targetPosition = castor.target.getPosition();

        boolean isMoving = velocity.len() > 0.1f;
        boolean isShooting = castor.isShooting();
        boolean isTakingDamage = castor.isTakingDamage();
        boolean isDashing = castor.isDashing();

        currentDirection = getDirection(velocity, targetPosition, currentPosition, isShooting);

        CastorAnimationSystem.CastorState state = getCastorState(isMoving, isShooting, isTakingDamage, isDashing);

        // 🔥 PASSAR: animState para o animationSystem
        CastorAnimationSystem.AnimationFrame animationFrame = animationSystem.getCurrentFrame(
                state, currentDirection, velocity, targetPosition, currentPosition, animState);

        float drawWidth = TILE_SIZE;
        float drawHeight = TILE_SIZE;

        float x = offsetX + (currentPosition.x - 0.5f) * TILE_SIZE;
        float y = offsetY + (currentPosition.y - 0.5f) * TILE_SIZE;

        // 🔥 CORREÇÃO: Usar animState.damageAnimationTime em vez de
        // animationSystem.getDamageTime()
        if (isTakingDamage) {
            if ((int) (animState.damageAnimationTime * 10) % 2 == 0) {
                batch.setColor(1, 0.3f, 0.3f, 1);
            } else {
                batch.setColor(1, 1, 1, 1);
            }
        }

        if (animationFrame.flipX) {
            batch.draw(animationFrame.frame, x + drawWidth, y, -drawWidth, drawHeight);
        } else {
            batch.draw(animationFrame.frame, x, y, drawWidth, drawHeight);
        }

        batch.setColor(1, 1, 1, 1);

        // 🔥 CORREÇÃO: Usar animState.shootAnimationTime em vez de
        // animationSystem.isShootAtFirePoint()
        if (isShooting && animState.shootAnimationTime >= 0.5f && !castor.hasShot()) {
            castor.setHasShot(true);
            castor.fireProjectile();
        }
    }

    // 🔥 ATUALIZAR: Método renderDeathAnimation para receber animState
    private void renderDeathAnimation(SpriteBatch batch, Castor castor, float offsetX, float offsetY,
            float delta, CastorAnimationState animState) {
        Vector2 currentPosition = castor.getPosition();
        CastorAnimationSystem.Direction deathDirection = currentDirection != null ? currentDirection
                : CastorAnimationSystem.Direction.DOWN;

        // 🔥 PASSAR: animState para o animationSystem
        TextureRegion deathFrame = animationSystem.getDeathFrame(castor.getDeathType(), deathDirection, animState);

        boolean flipX = flipou(castor);

        float drawWidth = TILE_SIZE;
        float drawHeight = TILE_SIZE;

        float x = offsetX + (currentPosition.x - 0.5f) * TILE_SIZE;
        float y = offsetY + (currentPosition.y - 0.5f) * TILE_SIZE;

        // 🔥 CORREÇÃO: Usar animState.deathAnimationTime
        if ((int) (animState.deathAnimationTime * 10) % 3 == 0) {
            batch.setColor(0.8f, 0.8f, 0.8f, 1f);
        }

        if (flipX) {
            batch.draw(deathFrame, x + drawWidth, y, -drawWidth, drawHeight);
        } else {
            batch.draw(deathFrame, x, y, drawWidth, drawHeight);
        }

        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public Vector2 calculateRenderOffset(Enemy enemy) {
        if (enemy instanceof Castor) {
            return calculateRenderOffsetForCastor((Castor) enemy);
        }
        return new Vector2();
    }

    public Vector2 calculateRenderOffsetForCastor(Castor castor) {
        Vector2 offset = new Vector2();
        switch (castor.getState()) {
            case DASHING:
                offset.x = -0.1f;
                offset.y = -0.05f;
                break;
            case SHOOTING:
                offset.x = 0.05f;
                break;
            case TAKING_DAMAGE:
                offset.y = -0.03f;
                break;
            case MELEE_DEATH:
            case PROJECTILE_DEATH:
                offset.y -= 0.02f;
                break;
        }

        offset.x -= (CASTOR_RENDER_WIDTH / TILE_SIZE) / 2f;
        offset.y -= (CASTOR_RENDER_HEIGHT / TILE_SIZE) / 2f;

        return offset;
    }

    public float getMeleeDeathDuration() {
        return animationSystem.getMeleeDeathDuration();
    }

    public float getProjectileDeathDuration() {
        return animationSystem.getProjectileDeathDuration();
    }

    private CastorAnimationSystem.CastorState getCastorState(boolean isMoving, boolean isShooting,
            boolean isTakingDamage, boolean isDashing) {
        if (isDashing) {
            return CastorAnimationSystem.CastorState.DASHING;
        } else if (isTakingDamage) {
            return CastorAnimationSystem.CastorState.TAKING_DAMAGE;
        } else if (isShooting) {
            return CastorAnimationSystem.CastorState.SHOOTING;
        } else if (isMoving) {
            return CastorAnimationSystem.CastorState.MOVING;
        } else {
            return CastorAnimationSystem.CastorState.IDLE;
        }
    }

    public void dispose() {
        animationSystem.dispose();
    }

    @Override
    public float getRenderWidth() {
        return getCastorRenderWidth();
    }

    @Override
    public float getRenderHeight() {
        return getCastorRenderHeight();
    }

    @Override
    public TextureRegion getCorpseFrame(Enemy enemy) {
        if (enemy instanceof Castor) {
            return getCorpseFrame((Castor) enemy);
        }
        return null;
    }

    public boolean flipou(Castor castor) {
        return castor.getDirectionX() > 0;
    }

    @Override
    public boolean shouldFlipCorpse(Enemy enemy) {
        if (enemy instanceof Castor) {
            return flipou((Castor) enemy);
        }
        return false;
    }

    public float getCastorRenderWidth() {
        return CASTOR_RENDER_WIDTH;
    }

    public float getCastorRenderHeight() {
        return CASTOR_RENDER_HEIGHT;
    }

    public TextureRegion getCorpseFrame(Castor castor) {
        return animationSystem.getLastDeathFrame(castor.getDeathType());
    }
}