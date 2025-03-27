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
     
        
        // Prioridade 1: Dash
        if (player.state == Robertinhoo.DASH) {
            System.out.println("Selecionando animação de DASH");
            return getDashAnimation(player);
        }
        
    //    Weapon currentWeapon = player.getInventory().getEquippedWeapon();
    //     if (weaponSystem.isAiming() && currentWeapon != null) {
    //         System.out.println("Selecionando animação de MIRA");
    //         Animation<TextureRegion> aimedAnim = getAimedAnimation(player);
    //         System.out.println("Animação da mira selecionada: " + aimedAnim);
    //         return aimedAnim;
    //     }
     
        if (player.dir != Robertinhoo.IDLE) {
            return getMovementAnimation(player);
        }
        
        
        return getIdleAnimation(player);
    }

    private Animation<TextureRegion> getDashAnimation(Robertinhoo player) {
        switch (player.dashDirection) {
            case Robertinhoo.UP: return animations.dash_top;
            case Robertinhoo.DOWN: return animations.dash_down;
            case Robertinhoo.LEFT:
            case Robertinhoo.RIGHT: return animations.dash_sides;
            default: return animations.idleDown;
        }
    }

    private Animation<TextureRegion> getMovementAnimation(Robertinhoo player) {
        switch (player.dir) {
            case Robertinhoo.RIGHT: return animations.runRight;
            case Robertinhoo.LEFT: return animations.runLeft;
            case Robertinhoo.UP: return animations.runUp;
            case Robertinhoo.DOWN: return animations.runDown;
            default: return getIdleAnimation(player);
        }
    }

    private Animation<TextureRegion> getAimedAnimation(Robertinhoo player) {
   
        float aimAngle =player.applyAimRotation();
        int direction = getDirectionFromAngle(aimAngle);
        
        switch (direction) {
            case Robertinhoo.UP: return animations.idleUp;
            case Robertinhoo.DOWN: return animations.idleDown;
            case Robertinhoo.LEFT: return animations.idleLeft;
            case Robertinhoo.RIGHT: return animations.idleRigth;
            default: return animations.idleDown;
        }
    }

    private int getDirectionFromAngle(float angle) {
        angle = (angle + 360) % 360; // Normaliza o ângulo
        if (angle >= 45 && angle < 135) return Robertinhoo.UP;
        if (angle >= 135 && angle < 225) return Robertinhoo.LEFT;
        if (angle >= 225 && angle < 315) return Robertinhoo.DOWN;
        return Robertinhoo.RIGHT;
    }

    private Animation<TextureRegion> getIdleAnimation(Robertinhoo player) {
        Weapon currentWeapon = player.getInventory().getEquippedWeapon();
        if (weaponSystem.isAiming() && currentWeapon != null) {
            return getAimedAnimation(player);
        }
        
     
        switch (player.lastDir) {
            case Robertinhoo.UP: return animations.idleUp;
            case Robertinhoo.DOWN: return animations.idleDown;
            case Robertinhoo.LEFT: return animations.idleLeft;
            case Robertinhoo.RIGHT: return animations.idleRigth;
            default: return animations.idleDown;
        }
    }

    public void render(SpriteBatch batch, Robertinhoo player, float delta, float offsetX, float offsetY) {
        animationTime += delta;

        Animation<TextureRegion> selectedAnimation = selectAnimation(player);
        if (selectedAnimation != currentAnimation) {
            animationTime = 0f;
            currentAnimation = selectedAnimation;
        }

        TextureRegion frame = currentAnimation.getKeyFrame(animationTime, true);
        float x = offsetX + player.bounds.x * MapRenderer.TILE_SIZE;
        float y = offsetY + player.bounds.y * MapRenderer.TILE_SIZE;
        float width = player.bounds.width * MapRenderer.TILE_SIZE;
        float height = player.bounds.height * MapRenderer.TILE_SIZE;

        

    
      
        boolean shouldFlip = false;
        if (currentAnimation == animations.runLeft || 
            (currentAnimation == animations.dash_sides && player.dashDirection == Robertinhoo.LEFT)) {
            shouldFlip = true;
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