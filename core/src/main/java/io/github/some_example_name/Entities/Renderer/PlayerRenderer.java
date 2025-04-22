package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


import io.github.some_example_name.Entities.Player.PlayerWeaponSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapRenderer;

public class PlayerRenderer {
    private float animationTime = 0f;
    private final PlayerAnimations animations;
    private Animation<TextureRegion> currentAnimation;
    private final PlayerWeaponSystem weaponSystem;

    public PlayerRenderer(PlayerWeaponSystem weaponSystem) {
        this.weaponSystem = weaponSystem;
        animations = new PlayerAnimations();
        currentAnimation = null;
    }
    private Animation<TextureRegion> selectAnimation(Robertinhoo player) {
        if (player.state == Robertinhoo.DASH) {
            return getDashAnimation(player);
        }
        if (weaponSystem.isAiming() && player.getInventory().getEquippedWeapon() != null) {
            return getAimedAnimation(player, (player.dir != Robertinhoo.IDLE));
        }
        if (player.dir != Robertinhoo.IDLE) {
            return getMovementAnimation(player);
        }
        return getIdleAnimation(player);
    }
    
    private Animation<TextureRegion> getIdleAnimation(Robertinhoo player) {
        if (weaponSystem.isAiming() && player.getInventory().getEquippedWeapon() != null) {
            return getAimedAnimation(player,false);
        }
        return getDirectionalAnimation(player.lastDir, false);
    }

    private Animation<TextureRegion> getDashAnimation(Robertinhoo player) {
        switch (player.dashDirection) {
            case Robertinhoo.UP:
                return animations.dash_top;
            case Robertinhoo.DOWN:
                return animations.dash_down;
            case Robertinhoo.LEFT:
            case Robertinhoo.RIGHT:
                return animations.dash_sides;
            default:
                return animations.idleDown;
        }
    }

    private Animation<TextureRegion> getDirectionalAnimation(int direction, boolean isMoving) {
        boolean isOneHand = Robertinhoo.IsUsingOneHandWeapon;

        if (isMoving) {
            switch (direction) {
                case Robertinhoo.RIGHT:
                    return isOneHand ? animations.runRightWeaponOneHand : animations.runRight;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.runLeftWeaponOneHand : animations.runLeft;
                case Robertinhoo.UP:
                     return isOneHand ? animations.runUpWeaponOneHand : animations.runUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.runDownWeaponOneHand : animations.runDown;
                default:
                    return animations.idleDown;
            }} else {
            switch (direction) {
                case Robertinhoo.UP:
                    return isOneHand ? animations.idleUpWeaponOneHand : animations.idleUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.idleDownWeaponOneHand : animations.idleDown;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.idleLeftWeaponOneHand : animations.idleLeft;
                case Robertinhoo.RIGHT:
                    return isOneHand ? animations.idleRightWeaponOneHand : animations.idleRigth;
                default:
                    return animations.idleDown;
            }
        }
    }
    private Animation<TextureRegion> getMovementAnimation(Robertinhoo player) {
        return getDirectionalAnimation(player.dir, true);
    }
    
    private Animation<TextureRegion> getAimedAnimation(Robertinhoo player, boolean isMoving) {
        float aimAngle = player.applyAimRotation();
        int direction = getDirectionFromAngle(aimAngle);
        return getDirectionalAnimation(direction, isMoving);
        
    }
    private int getDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360;
        if (angle >= 45 && angle < 135)
            return Robertinhoo.UP;
        if (angle >= 135 && angle < 225)
            return Robertinhoo.LEFT;
        if (angle >= 225 && angle < 315)
            return Robertinhoo.DOWN;
        return Robertinhoo.RIGHT;
    }


    private boolean areOpposite(int dir1, int dir2) {
        return (dir1 == Robertinhoo.UP && dir2 == Robertinhoo.DOWN) ||
               (dir1 == Robertinhoo.DOWN && dir2 == Robertinhoo.UP) ||
               (dir1 == Robertinhoo.LEFT && dir2 == Robertinhoo.RIGHT) ||
               (dir1 == Robertinhoo.RIGHT && dir2 == Robertinhoo.LEFT);
               

    }

    private boolean shouldReverseAnimation(Robertinhoo player) {
        if (player.dir == Robertinhoo.IDLE || !weaponSystem.isAiming() || player.getInventory().getEquippedWeapon() == null) {
            return false;
        }
        int movementDir = player.dir;
        int aimingDir = getDirectionFromAngle(player.applyAimRotation());
        return areOpposite(movementDir, aimingDir);
    }


    public void render(SpriteBatch batch, Robertinhoo player, float delta, float offsetX, float offsetY) {
        animationTime += delta;
        Animation<TextureRegion> selectedAnimation = selectAnimation(player);
        if (selectedAnimation != currentAnimation) {
            animationTime = 0f;
            currentAnimation = selectedAnimation;
        }
        float effectiveTime = animationTime;
        if (shouldReverseAnimation(player)) {
            float totalDuration = currentAnimation.getAnimationDuration();
            effectiveTime = totalDuration - (animationTime % totalDuration);
        }

        TextureRegion frame = currentAnimation.getKeyFrame(effectiveTime, true);
        float x = offsetX + player.bounds.x * MapRenderer.TILE_SIZE;
        float y = offsetY + player.bounds.y * MapRenderer.TILE_SIZE;
        float width = player.bounds.width * MapRenderer.TILE_SIZE;
        float height = player.bounds.height * MapRenderer.TILE_SIZE;
        boolean shouldFlip = false;
        if (currentAnimation == animations.runLeft ||
                (currentAnimation == animations.dash_sides && player.dashDirection == Robertinhoo.LEFT)) {
            shouldFlip = true;
        } else if (currentAnimation == animations.idleDownWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle > 90 && aimAngle < 270);
        } else if (currentAnimation == animations.idleUpWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        }
        else if (currentAnimation == animations.runDownWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        }
        else if (currentAnimation == animations.runUpWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        }
        batch.draw(frame,
                shouldFlip ? x + width : x,
                y,
                shouldFlip ? -width : width,
                height);
    }

    public void dispose() {
        animations.dispose();
    }
}