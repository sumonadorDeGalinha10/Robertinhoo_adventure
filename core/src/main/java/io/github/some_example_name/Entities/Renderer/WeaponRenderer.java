package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import com.badlogic.gdx.graphics.g2d.Animation;

public class WeaponRenderer {
    private float animationTime = 0f;
    private WeaponAnimations animations;
    private Weapon.WeaponState currentState;
    private WeaponAnimations.WeaponDirection currentDirection;
    private Weapon.WeaponState previousState = Weapon.WeaponState.IDLE;
    private boolean animationCompleted = true;
    private boolean shotTriggered = false;
    private boolean reloadTriggered = false;

    public void loadWeaponAnimations(Weapon weapon) {
        String weaponType = weapon.getClass().getSimpleName();
        Gdx.app.log("WEAPON_RENDERER", "Carregando animações para: " + weaponType);
        this.animations = new WeaponAnimations(weaponType);
        this.currentState = Weapon.WeaponState.IDLE;
        this.previousState = Weapon.WeaponState.IDLE;
    }

    public void update(float delta, Vector2 aimDirection, Weapon.WeaponState state,
            boolean shotJustFired, boolean reloadJustTriggered) {

        // Gerenciar trigger de recarga
        if (reloadJustTriggered) {
            reloadTriggered = true;
            animationTime = 0f;
            animationCompleted = false;
            currentState = Weapon.WeaponState.RELOADING; // Forçar estado
        }

        // Gerenciar trigger de tiro (apenas se não estiver recarregando)
        if (shotJustFired && !reloadTriggered) {
            shotTriggered = true;
            animationTime = 0f;
            animationCompleted = false;
        }

        // Atualizar estado se não houver trigger ativo
        if (!reloadTriggered && !shotTriggered) {
            currentState = state;
        }

        currentDirection = getDirectionFromAngle(aimDirection.angleDeg());

        // Atualizar tempo de animação se necessário
        if (!animationCompleted) {
            animationTime += delta;

            Animation<TextureRegion> anim = animations.getAnimation(currentDirection, currentState);
            if (anim != null && anim.isAnimationFinished(animationTime)) {
                animationCompleted = true;

                // Resetar triggers quando a animação terminar
                shotTriggered = false;
                reloadTriggered = false;
            }
        }
    }

    private WeaponAnimations.WeaponDirection getDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360;
        WeaponAnimations.WeaponDirection dir;

        if (angle >= 337.5 || angle < 22.5) {
            dir = WeaponAnimations.WeaponDirection.E;
        } else if (angle >= 22.5 && angle < 67.5) {
            dir = WeaponAnimations.WeaponDirection.NE;
        } else if (angle >= 67.5 && angle < 112.5) {
            dir = WeaponAnimations.WeaponDirection.N;
        } else if (angle >= 112.5 && angle < 157.5) {
            dir = WeaponAnimations.WeaponDirection.NW;
        } else if (angle >= 157.5 && angle < 202.5) {
            dir = WeaponAnimations.WeaponDirection.W;
        } else if (angle >= 202.5 && angle < 247.5) {
            dir = WeaponAnimations.WeaponDirection.SW;
        } else if (angle >= 247.5 && angle < 292.5) {
            dir = WeaponAnimations.WeaponDirection.S;
        } else { // 292.5 a 337.5
            dir = WeaponAnimations.WeaponDirection.SE;
        }
        return dir;
    }
   public void render(SpriteBatch batch, Vector2 position, float offsetX, float offsetY)  {
        if (animations == null)
            return;

        TextureRegion frame = null;
        Animation<TextureRegion> anim = null;

        WeaponAnimations.WeaponDirection renderDirection = currentDirection;
        if (reloadTriggered || currentState == Weapon.WeaponState.RELOADING) {
            renderDirection = WeaponAnimations.WeaponDirection.S;
        }
        if (reloadTriggered || currentState == Weapon.WeaponState.RELOADING) {
            anim = animations.getAnimation(renderDirection, Weapon.WeaponState.RELOADING);
            if (anim != null) {
                frame = anim.getKeyFrame(animationTime, false);
            }
        } else if (shotTriggered || currentState == Weapon.WeaponState.SHOOTING) {
            anim = animations.getAnimation(renderDirection, Weapon.WeaponState.SHOOTING);
            if (anim != null) {
                frame = anim.getKeyFrame(animationTime, false);
            }
        } else {
            anim = animations.getAnimation(renderDirection, Weapon.WeaponState.IDLE);
            if (anim != null) {
                frame = anim.getKeyFrame(0);
            }
        }

        if (frame == null) {
            Gdx.app.error("WeaponRenderer", "No frame available for: " + currentState);
            return;
        }

        float scale = 1.4f;
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        float adjustedX = position.x - width * 0.57f;
        float adjustedY = position.y - height * 0.52f;
        
        batch.draw(frame,
                adjustedX,
                adjustedY,
                width, height);
    }
    

    public void dispose() {
        if (animations != null) {
            animations.dispose();
        }
    }
}