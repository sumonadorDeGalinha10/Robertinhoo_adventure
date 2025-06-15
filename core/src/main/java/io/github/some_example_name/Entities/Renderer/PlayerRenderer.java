package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
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

        Weapon equippedWeapon = player.getInventory().getEquippedWeapon();
        boolean isReloading = equippedWeapon != null &&
                equippedWeapon.getCurrentState() == Weapon.WeaponState.RELOADING;
        if (player.state == Robertinhoo.DASH) {
            return getDashAnimation(player);
        }

        if (isReloading) {
            if (player.dir != Robertinhoo.IDLE) {
                // Animação de movimento enquanto recarrega
                return animations.runDownWeaponOneHand; // Usar animação de movimento para baixo
            } else {
                // Animação idle enquanto recarrega
                return animations.idleDownWeaponOneHand; // Usar animação idle para baixo
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
                    return isOneHand ? animations.runRightWeaponOneHand : animations.walkRight;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.runLeftWeaponOneHand : animations.walkLeft;
                case Robertinhoo.UP:
                    return isOneHand ? animations.runUpWeaponOneHand : animations.walkUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.runDownWeaponOneHand : animations.walkDown;
                case Robertinhoo.NORTH_WEST:
                    return isOneHand ? animations.runNWWeaponOneHand : animations.walkNortWast;
                case Robertinhoo.NORTH_EAST:
                    return isOneHand ? animations.runNEWeaponOneHand : animations.walkNortEast;
                case Robertinhoo.SOUTH_EAST:
                    return isOneHand ? animations.runSEWeaponOneHand : animations.walkSE;
                case Robertinhoo.SOUTH_WEST:
                    return isOneHand ? animations.runSWWeaponOneHand : animations.walkSW;

                default:
                    return animations.idleDown;
            }
        } else {
            switch (direction) {
                case Robertinhoo.UP:
                    return isOneHand ? animations.idleUpWeaponOneHand : animations.idleUp;
                case Robertinhoo.DOWN:
                    return isOneHand ? animations.idleDownWeaponOneHand : animations.idleDown;
                case Robertinhoo.LEFT:
                    return isOneHand ? animations.idleLeftWeaponOneHand : animations.idleLeft;
                case Robertinhoo.RIGHT:
                    return isOneHand ? animations.idleRightWeaponOneHand : animations.idleRigth;
                case Robertinhoo.NORTH_WEST:
                    return isOneHand ? animations.idleNWWeapon : animations.idleNorthWest;
                case Robertinhoo.NORTH_EAST:
                    return isOneHand ? animations.idleNEWeapon : animations.idleNorthEast;
                case Robertinhoo.SOUTH_WEST:
                    return isOneHand ? animations.idleSWWeapon : animations.idleSouthWest;
                case Robertinhoo.SOUTH_EAST:
                    return isOneHand ? animations.idleSEWeapon : animations.idleSouthEast;
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

    private boolean shouldReverseAnimation(Robertinhoo player) {
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
        if (currentAnimation == animations.walkDown ||
                currentAnimation == animations.idleDown ||
                currentAnimation == animations.walkUp ||
                currentAnimation == animations.idleUp) {
            scale = 1.2f;
        }

        float scaledWidth = originalWidth * scale;
        float scaledHeight = originalHeight * scale;

        float x = offsetX + (player.bounds.x * MapRenderer.TILE_SIZE) - (scaledWidth - originalWidth) / 2;
        float y = offsetY + (player.bounds.y * MapRenderer.TILE_SIZE) - (scaledHeight - originalHeight) / 2;

        boolean shouldFlip = false;
        if (currentAnimation == animations.walkLeft ||
                (currentAnimation == animations.dash_sides && player.dashDirection == Robertinhoo.LEFT)) {
            shouldFlip = true;
        } else if (currentAnimation == animations.idleDownWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle > 90 && aimAngle < 270);
        } else if (currentAnimation == animations.idleUpWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        } else if (currentAnimation == animations.runDownWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        } else if (currentAnimation == animations.runUpWeaponOneHand) {
            float aimAngle = player.applyAimRotation();
            shouldFlip = (aimAngle < 270 && aimAngle > 90);
        }

        this.currentFlipState = shouldFlip;

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
        
        if (currentAnimation == animations.walkDown ||
            currentAnimation == animations.idleDown ||
            currentAnimation == animations.walkUp ||
            currentAnimation == animations.idleUp) {
            return 1.2f;
        }
        return 1.4f;
    }

    public void dispose() {
        animations.dispose();
    }
}