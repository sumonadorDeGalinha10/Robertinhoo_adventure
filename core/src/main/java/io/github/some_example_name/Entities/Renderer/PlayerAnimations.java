// PlayerAnimations.java
package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.Texture;

public class PlayerAnimations implements Disposable {
    public final Animation<TextureRegion> idleDown;
    public final Animation<TextureRegion> idleUp;
    public final Animation<TextureRegion> idleLeft;
    public final Animation<TextureRegion> idleRigth;
    public final Animation<TextureRegion> runLeft;
    public final Animation<TextureRegion> runRight;
    public final Animation<TextureRegion> runUp;
    public final Animation<TextureRegion> runDown;
    public final Animation<TextureRegion> dash_down;
    public final Animation<TextureRegion> dash_top;
    public final Animation<TextureRegion> dash_sides;



    public final Animation<TextureRegion> idleDownWeaponOneHand;
    public final Animation<TextureRegion> idleUpWeaponOneHand;
    public final Animation<TextureRegion> idleLeftWeaponOneHand;
    public final Animation<TextureRegion> idleRightWeaponOneHand;
    public final Animation<TextureRegion> runLeftWeaponOneHand;
    public final Animation<TextureRegion> runRightWeaponOneHand;
    // public final Animation<TextureRegion> runUpWeapon;
    public final Animation<TextureRegion> runDownWeaponOneHand;

    private final Texture[] loadedTextures;
    

    public PlayerAnimations() {
        loadedTextures = new Texture[]{
            AnimationLoader.loadTexture("rober/idle/1_Template_Idle_Down-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle/1_Template_Idle_Up-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle/1_Template_Idle_Left-Sheet.png"),
            AnimationLoader.loadTexture("rober/run/2_Template_Run_Left-Sheet.png"),
            AnimationLoader.loadTexture("rober/run/2_Template_Run_Up-Sheet.png"),
            AnimationLoader.loadTexture("rober/run/2_Template_Run_Down-Sheet.png"),
            AnimationLoader.loadTexture("rober/dash/dash_top.png"),
            AnimationLoader.loadTexture("rober/dash/dash_down.png"),
            AnimationLoader.loadTexture("rober/dash/dash_sides.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/Idle_down_With_weapon-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/idle_left_with_weapon.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/1_Template_Idle_Up_with_weapon-Sheet.png"),
            AnimationLoader.loadTexture("rober/run_with_weapon/2_Template_Run_Left_withe_oneHand_WEAPON.png"),
            AnimationLoader.loadTexture("rober/run_with_weapon/runDown_With_One_HandWEAPON-Sheet.png")
            
            
        };

        idleDown = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Down-Sheet.png", 0.2f, false, 6);
        idleUp = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Up-Sheet.png", 0.2f, false, 6);
        idleLeft = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Left-Sheet.png", 0.2f, false, 6);
        idleRigth = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Left-Sheet.png", 0.2f, true, 6);
        runLeft = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Left-Sheet.png", 0.1f, true, 6);
        runRight = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Left-Sheet.png", 0.1f, true, 6);
        runUp = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Up-Sheet.png", 0.1f, false, 6);
        runDown = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Down-Sheet.png", 0.1f, false, 6);
        dash_down = AnimationLoader.loadAnimation("rober/dash/dash_down.png", 0.04f, false, 9);
        dash_top = AnimationLoader.loadAnimation("rober/dash/dash_top.png", 0.05f, false, 10);
        dash_sides = AnimationLoader.loadAnimation("rober/dash/dash_sides.png", 0.06f, false, 8);
        idleDownWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_down_With_weapon-Sheet.png", 0.2f, false, 6);
        idleLeftWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/idle_left_with_weapon.png", 0.2f, false, 6);
        idleRightWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/idle_left_with_weapon.png", 0.2f, true, 6);
        idleUpWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/1_Template_Idle_Up_with_weapon-Sheet.png", 0.2f, true, 6);
        runLeftWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/2_Template_Run_Left_withe_oneHand_WEAPON.png", 0.1f, false, 6);
        runRightWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/2_Template_Run_Left_withe_oneHand_WEAPON.png", 0.1f, true, 6);
        runDownWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/runDown_With_One_HandWEAPON-Sheet.png", 0.1f, false, 6);
        


    }

    @Override
    public void dispose() {
        for(Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}