package io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import com.badlogic.gdx.Gdx;

public class CastorRenderer {
    private CastorAnimationSystem animationSystem;
    private CastorAnimationSystem.Direction currentDirection;
    private int TILE_SIZE = 64;

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
        Vector2 velocity = castor.getLinearVelocity();
        Vector2 currentPosition = castor.getPosition();
        Vector2 targetPosition = castor.target.getPosition();

        boolean isMoving = velocity.len() > 0.1f;
        boolean isShooting = castor.isShooting();
        boolean isTakingDamage = castor.isTakingDamage();
        boolean isDashing = castor.isDashing();

        // Atualizar sistema de animação
        animationSystem.update(delta);

        // Atualizar tempos específicos
        if (isShooting) {
            animationSystem.updateShootTime(delta);
        } else {
            animationSystem.resetShootTime();
        }

        if (isTakingDamage) {
            animationSystem.updateDamageTime(delta);
        } else {
            animationSystem.resetDamageTime();
        }

        if (isDashing) {
            animationSystem.updateDashTime(delta);

            // 🔥 MELHOR LOG: Mostrar fase atual do dash
            float dashTime = animationSystem.getDashTime();
            if (dashTime < 0.6f) {
                // Fase de preparação
                if ((int) (dashTime * 10) % 10 == 0) { // Log a cada 0.1s
                    Gdx.app.log("CastorRenderer", "⏳ Dash PREPARATION - Time: " + dashTime);
                }
            } else {
                // Fase de execução
                if ((int) (dashTime * 10) % 5 == 0) { // Log mais frequente
                    Gdx.app.log("CastorRenderer", "🚀 Dash EXECUTION - Time: " + dashTime);
                }
            }
        } else {
            animationSystem.resetDashTime();
        }

        currentDirection = getDirection(velocity, targetPosition, currentPosition, isShooting);

        // Obter estado e frame atual
        CastorAnimationSystem.CastorState state = getCastorState(isMoving, isShooting, isTakingDamage, isDashing);
        CastorAnimationSystem.AnimationFrame animationFrame = animationSystem.getCurrentFrame(state, currentDirection,
                velocity, targetPosition, currentPosition);

        float drawWidth = TILE_SIZE;
        float drawHeight = TILE_SIZE;

        float x = offsetX + (currentPosition.x - 0.5f) * TILE_SIZE;
        float y = offsetY + (currentPosition.y - 0.5f) * TILE_SIZE;

        // Efeito visual durante o dano
        if (isTakingDamage) {
            if ((int) (animationSystem.getDamageTime() * 10) % 2 == 0) {
                batch.setColor(1, 0.3f, 0.3f, 1);
            } else {
                batch.setColor(1, 1, 1, 1);
            }
        }

        // 🔥 FLIP CORRIGIDO: Aplicado baseado no AnimationSystem
        if (animationFrame.flipX) {
            batch.draw(animationFrame.frame, x + drawWidth, y, -drawWidth, drawHeight);
            if (isDashing && animationFrame.dashPhase == CastorAnimationSystem.DashPhase.DASH) {
                Gdx.app.log("CastorRenderer", "🔄 Dash com FLIP para DIREITA");
            }
        } else {
            batch.draw(animationFrame.frame, x, y, drawWidth, drawHeight);
            if (isDashing && animationFrame.dashPhase == CastorAnimationSystem.DashPhase.DASH) {
                Gdx.app.log("CastorRenderer", "⬅️ Dash sem flip para ESQUERDA");
            }
        }

        // Resetar cor
        batch.setColor(1, 1, 1, 1);

        // Disparar projeto no momento certo
        if (isShooting && animationSystem.isShootAtFirePoint() && !castor.hasShot()) {
            castor.setHasShot(true);
            castor.fireProjectile();
        }
    }

    /**
     * 🔥 SIMPLIFICADO: Determina o estado atual do castor
     */
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

    // 🔥 NOVO: Getters para acesso aos tempos (se necessário)
    public float getDamageAnimationTime() {
        return animationSystem.getDamageTime();
    }

    public float getDashAnimationTime() {
        return animationSystem.getDashTime();
    }
}