package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.PlayerWeaponSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.MapRenderer;

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
         if (player.state == Robertinhoo.MELEE_ATTACK) {
        return getMeleeAnimation(player);
    }

        Weapon equippedWeapon = player.getInventory().getEquippedWeapon();
        boolean isReloading = equippedWeapon != null &&
                equippedWeapon.getCurrentState() == Weapon.WeaponState.RELOADING;
        if (player.state == Robertinhoo.DASH) {
            return getDashAnimation(player);
        }

        if (isReloading) {
            if (player.dir != Robertinhoo.IDLE) {
                return animations.weapon.runDown;
            } else {
                return animations.weapon.idleDown;
            }
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
            return getAimedAnimation(player, false);
        }
        return getDirectionalAnimation(player.lastDir, false);
    }

    private Animation<TextureRegion> getDashAnimation(Robertinhoo player) {
        switch (player.dashDirection) {
            case Robertinhoo.UP:
            case Robertinhoo.NORTH_EAST:
            case Robertinhoo.NORTH_WEST:
                return animations.special.rollUp;
                
            case Robertinhoo.DOWN:
            case Robertinhoo.SOUTH_EAST:
            case Robertinhoo.SOUTH_WEST:
                return animations.special.rollDown;
                
            case Robertinhoo.LEFT:
            case Robertinhoo.RIGHT:
            default:
                return animations.special.rollSide;
        }
    }

    private Animation<TextureRegion> getDirectionalAnimation(int direction, boolean isMoving) {
        boolean isOneHand = Robertinhoo.IsUsingOneHandWeapon;

        if (isMoving) {
            switch (direction) {
                case Robertinhoo.RIGHT:
                    return isOneHand ? animations.weapon.runRight : animations.basic.walkRight;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.weapon.runLeft : animations.basic.walkLeft;
                case Robertinhoo.UP:
                    return isOneHand ? animations.weapon.runUp : animations.basic.walkUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.weapon.runDown : animations.basic.walkDown;
                case Robertinhoo.NORTH_WEST:
                    return isOneHand ? animations.weapon.runNW : animations.basic.walkNortWast;
                case Robertinhoo.NORTH_EAST:
                    return isOneHand ? animations.weapon.runNE : animations.basic.walkNortEast;
                case Robertinhoo.SOUTH_EAST:
                    return isOneHand ? animations.weapon.runSE : animations.basic.walkSE;
                case Robertinhoo.SOUTH_WEST:
                    return isOneHand ? animations.weapon.runSW : animations.basic.walkSW;

                default:
                    return animations.basic.idleDown;
            }
        } else {
             switch (direction) {
            case Robertinhoo.UP:
                return isOneHand ? animations.weapon.idleUp : animations.basic.idleUp;
            case Robertinhoo.DOWN:
                return isOneHand ? animations.weapon.idleDown : animations.basic.idleDown;
            case Robertinhoo.LEFT:
                return isOneHand ? animations.weapon.idleLeft : animations.basic.idleLeft;
            case Robertinhoo.RIGHT:
                return isOneHand ? animations.weapon.idleRight : animations.basic.idleRight;
            case Robertinhoo.NORTH_WEST:
                return isOneHand ? animations.weapon.idleNW : animations.basic.idleNorthWest;
            case Robertinhoo.NORTH_EAST:
                return isOneHand ? animations.weapon.idleNE : animations.basic.idleNorthEast;
            case Robertinhoo.SOUTH_WEST:
                return isOneHand ? animations.weapon.idleSW : animations.basic.idleSouthWest;
            case Robertinhoo.SOUTH_EAST:
                return isOneHand ? animations.weapon.idleSE : animations.basic.idleSouthEast;
            default:
                return animations.basic.idleDown;

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
        if (angle >= 22.5 && angle < 67.5)
            return Robertinhoo.NORTH_EAST;
        if (angle >= 67.5 && angle < 112.5)
            return Robertinhoo.UP;
        if (angle >= 112.5 && angle < 157.5)
            return Robertinhoo.NORTH_WEST;
        if (angle >= 157.5 && angle < 202.5)
            return Robertinhoo.LEFT;
        if (angle >= 202.5 && angle < 247.5)
            return Robertinhoo.SOUTH_WEST;
        if (angle >= 247.5 && angle < 292.5)
            return Robertinhoo.DOWN;
        if (angle >= 292.5 && angle < 337.5)
            return Robertinhoo.SOUTH_EAST;
        return Robertinhoo.RIGHT;
    }

    private boolean areOpposite(int dir1, int dir2) {
        return (dir1 == Robertinhoo.UP && dir2 == Robertinhoo.DOWN) ||
                (dir1 == Robertinhoo.DOWN && dir2 == Robertinhoo.UP) ||
                (dir1 == Robertinhoo.LEFT && dir2 == Robertinhoo.RIGHT) ||
                (dir1 == Robertinhoo.RIGHT && dir2 == Robertinhoo.LEFT);

    }

    private Animation<TextureRegion> getMeleeAnimation(Robertinhoo player) {
    switch (player.meleeDirection) {
        case Robertinhoo.RIGHT:
            return animations.special.meleeAttackRight;
        case Robertinhoo.LEFT:
            return animations.special.meleeAttackLeft;
        case Robertinhoo.UP:
            return animations.special.meleeAttackUp;
        case Robertinhoo.DOWN:
        default:
            return animations.special.meleeAttackDown;
    }
}

  private boolean shouldReverseAnimation(Robertinhoo player) {
  
    if (player.state == Robertinhoo.DASH || player.state == Robertinhoo.MELEE_ATTACK) {
        return false;
    }
    
    if (player.dir == Robertinhoo.IDLE || !weaponSystem.isAiming()
            || player.getInventory().getEquippedWeapon() == null) {
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

        float originalWidth = player.bounds.width * MapRenderer.TILE_SIZE;
        float originalHeight = player.bounds.height * MapRenderer.TILE_SIZE;

        float scale = 1.4f;
        if (currentAnimation == animations.basic.walkDown ||
                currentAnimation == animations.basic.idleDown ||
                currentAnimation == animations.basic.walkUp ||
                currentAnimation == animations.basic.idleUp) {
            scale = 1.2f;
        }

        float scaledWidth = originalWidth * scale;
        float scaledHeight = originalHeight * scale;

        float x = offsetX + (player.bounds.x * MapRenderer.TILE_SIZE) - (scaledWidth - originalWidth) / 2;
        float y = offsetY + (player.bounds.y * MapRenderer.TILE_SIZE) - (scaledHeight - originalHeight) / 2;

        boolean shouldFlip = false;
        if (currentAnimation == animations.basic.walkLeft ||
                (currentAnimation == animations.special.rollSide && player.dashDirection == Robertinhoo.LEFT)) {
            shouldFlip = true;
        } else if (currentAnimation == animations.weapon.idleDown) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle > 90 && aimAngle < 270);
        } else if (currentAnimation == animations.weapon.idleUp) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        } else if (currentAnimation == animations.weapon.runDown) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        } else if (currentAnimation == animations.weapon.runUp) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        }

        this.currentFlipState = shouldFlip;

        if (player.isTakingDamage) {
        float alpha = (float) (Math.sin(animationTime * 30) + 1) / 2;
        batch.setColor(1, 1, 1, alpha);
        }

        batch.draw(frame,
                shouldFlip ? x + scaledWidth : x,
                y,
                shouldFlip ? -scaledWidth : scaledWidth,
                scaledHeight);
    }

    private boolean currentFlipState;

    public boolean getCurrentFlipState() {
        return currentFlipState;
    }

  public float getRenderScale() {
        if (currentAnimation == null) return 1.4f;
        
        if (currentAnimation == animations.basic.walkDown ||
            currentAnimation == animations.basic.idleDown ||
            currentAnimation == animations.basic.walkUp ||
            currentAnimation == animations.basic.idleUp) {
            return 1.2f;
        }
        return 1.4f;
    }

    public void dispose() {
        animations.dispose();
    }
}