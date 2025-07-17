// PlayerAnimations.java
package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Texture;

public class PlayerAnimations implements Disposable {
    public final Animation<TextureRegion> idleDown;
    public final Animation<TextureRegion> idleUp;
    public final Animation<TextureRegion> idleLeft;
    public final Animation<TextureRegion> idleRigth;
    public final Animation<TextureRegion> walkLeft;
    public final Animation<TextureRegion> walkRight;
    public final Animation<TextureRegion> walkUp;
    public final Animation<TextureRegion> walkDown;
    public final Animation<TextureRegion> walkSE;
    public final Animation<TextureRegion> walkSW;
    public final Animation<TextureRegion> walkNortEast;
    public final Animation<TextureRegion> walkNortWast;
    // public final Animation<TextureRegion> dash_down;
    // public final Animation<TextureRegion> dash_top;
    // public final Animation<TextureRegion> dash_sides;
    public final Animation<TextureRegion> idleNorthWest;
    public final Animation<TextureRegion> idleNorthEast;
    public final Animation<TextureRegion> idleSouthWest;
    public final Animation<TextureRegion> idleSouthEast;

    public final Animation<TextureRegion> rollDown;
    public final Animation<TextureRegion> rollUp;
    public final Animation<TextureRegion> rollSide;



    public final Animation<TextureRegion> idleDownWeaponOneHand;
    public final Animation<TextureRegion> idleUpWeaponOneHand;
    public final Animation<TextureRegion> idleLeftWeaponOneHand;
    public final Animation<TextureRegion> idleRightWeaponOneHand;
    public final Animation<TextureRegion> idleSEWeapon;
    public final Animation<TextureRegion> idleSWWeapon;
    public final Animation<TextureRegion> idleNEWeapon;
    public final Animation<TextureRegion> idleNWWeapon;

    public final Animation<TextureRegion> runLeftWeaponOneHand;
    public final Animation<TextureRegion> runRightWeaponOneHand;
    public final Animation<TextureRegion> runUpWeaponOneHand;
    public final Animation<TextureRegion> runDownWeaponOneHand;
    public final Animation<TextureRegion> runNEWeaponOneHand;
    public final Animation<TextureRegion> runNWWeaponOneHand;
    public final Animation<TextureRegion> runSEWeaponOneHand;
    public final Animation<TextureRegion> runSWWeaponOneHand;
  

    private final Texture[] loadedTextures;
    

    public PlayerAnimations() {
        loadedTextures = new Texture[]{
            AnimationLoader.loadTexture("rober/idle/idle_S-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle/idle_N-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle/idle_E-Sheet.png"),
            AnimationLoader.loadTexture("rober/run/2_Template_Run_Left-Sheet.png"),
            AnimationLoader.loadTexture("rober/run/2_Template_Run_Up-Sheet.png"),
            AnimationLoader.loadTexture("rober/run/2_Template_Run_Down-Sheet.png"),
            // AnimationLoader.loadTexture("rober/dash/dash_top.png"),
            // AnimationLoader.loadTexture("rober/dash/dash_down.png"),
            // AnimationLoader.loadTexture("rober/dash/dash_sides.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/Idle_down_With_weapon-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/idle_left_with_weapon.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/Idle_E-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/Idle_N-Sheet.png"),
            AnimationLoader.loadTexture("rober/run_with_weapon/2_Template_Run_Up_With_One_HandWEAPON-Sheet.png"),
            AnimationLoader.loadTexture("rober/idle_with_weapon/1_Template_Idle_Up_with_weapon-Sheet.png"),
            AnimationLoader.loadTexture("rober/run_with_weapon/2_Template_Run_Left_withe_oneHand_WEAPON.png"),
            AnimationLoader.loadTexture("rober/run_with_weapon/runDown_With_One_HandWEAPON-Sheet.png"),
            AnimationLoader.loadTexture("rober/run_with_weapon/2_Template_Run_Up_With_One_HandWEAPON-Sheet.png"),
            AnimationLoader.loadTexture("rober/walk/walk_SE-Sheet.png"),
            AnimationLoader.loadTexture("rober/roll/roll-Sheet.png")
        };

        idleDown = AnimationLoader.loadAnimation("rober/idle/idle_S-Sheet.png", 0.2f, false, 12);
        idleUp = AnimationLoader.loadAnimation("rober/idle/idle_N-Sheet.png", 0.2f, false, 12);
        idleLeft = AnimationLoader.loadAnimation("rober/idle/idle_E-Sheet.png", 0.2f, true, 12);
        idleRigth = AnimationLoader.loadAnimation("rober/idle/idle_E-Sheet.png", 0.2f, false, 12);
        walkLeft = AnimationLoader.loadAnimation("rober/walk/walk_E-Sheet.png", 0.1f, false, 8);
        walkRight = AnimationLoader.loadAnimation("rober/walk/walk_E-Sheet.png", 0.1f, false, 8);
        walkUp = AnimationLoader.loadAnimation("rober/walk/walk_N-Sheet.png", 0.1f, false, 8);
        walkNortEast = AnimationLoader.loadAnimation("rober/walk/walk_NE-Sheet.png", 0.1f, false, 8);
        walkNortWast = AnimationLoader.loadAnimation("rober/walk/walk_NE-Sheet.png", 0.1f, true, 8);
        walkDown = AnimationLoader.loadAnimation("rober/walk/walk_S-Sheet.png", 0.1f, false, 8);
        walkSE= AnimationLoader.loadAnimation("rober/walk/walk_SE-Sheet.png", 0.1f, false, 8);  
        walkSW= AnimationLoader.loadAnimation("rober/walk/walk_SE-Sheet.png", 0.1f, true, 8);  
        // dash_down = AnimationLoader.loadAnimation("rober/dash/dash_down.png", 0.04f, false, 9);
        // dash_top = AnimationLoader.loadAnimation("rober/dash/dash_top.png", 0.05f, false, 10);
        // dash_sides = AnimationLoader.loadAnimation("rober/dash/dash_sides.png", 0.06f, false, 8);
        idleDownWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/idle_S_with_weapon_Sheet.png", 0.2f, false, 12);
        idleLeftWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_E-Sheet.png", 0.2f, true, 12);
        idleRightWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_E-Sheet.png", 0.2f, false, 12);
        idleUpWeaponOneHand = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_N-Sheet.png", 0.2f, true, 12);
        idleSEWeapon = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_SE-Sheet.png", 0.2f, false, 12);
        idleSWWeapon = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_SE-Sheet.png", 0.2f, true, 12);
        idleNEWeapon = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_NE-Sheet.png", 0.2f, false, 12);
        idleNWWeapon = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_NE-Sheet.png", 0.2f, true, 12);
        runLeftWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_E-Sheet.png", 0.1f, true, 8);
        runRightWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_E-Sheet.png", 0.1f, false, 8);
        runDownWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_S-With_weapon.png", 0.1f, false, 8);
        runUpWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_N-Sheet.png", 0.1f, true, 8);
        runSEWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_SE-Sheet.png", 0.1f, false, 8);
        runSWWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_SE-Sheet.png", 0.1f, true, 8);
        runNEWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_NE-Sheet.png", 0.1f, false, 8);
        runNWWeaponOneHand = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_NE-Sheet.png", 0.1f, true, 8);
        idleNorthWest = AnimationLoader.loadAnimation("rober/idle/idle_NE-Sheet.png", 0.2f, true, 12);
        idleNorthEast = AnimationLoader.loadAnimation("rober/idle/idle_NE-Sheet.png", 0.2f, false, 12);
        idleSouthWest = AnimationLoader.loadAnimation("rober/idle/idle_SE-Sheet.png", 0.2f, true, 12);
        idleSouthEast = AnimationLoader.loadAnimation("rober/idle/idle_SE-Sheet.png", 0.2f, false, 12);

        Texture rollTexture = loadedTextures[loadedTextures.length - 1];

        int frameWidth = rollTexture.getWidth() / 21;
        int frameHeight = rollTexture.getHeight();

        TextureRegion[][] rollFrames = TextureRegion.split(
            rollTexture,
            frameWidth,
            frameHeight
        );

        TextureRegion[] rollDownFrames = Arrays.copyOfRange(rollFrames[0], 0, 7);
        TextureRegion[] rollUpFrames = Arrays.copyOfRange(rollFrames[0], 14, 21);
        TextureRegion[] rollSideFrames = Arrays.copyOfRange(rollFrames[0], 7, 14);

        float rollFrameDuration = 0.1f;
        rollDown = new Animation<>(rollFrameDuration, rollDownFrames);
        rollUp = new Animation<>(rollFrameDuration, rollUpFrames);
        rollSide = new Animation<>(rollFrameDuration, rollSideFrames);
    }

    @Override
    public void dispose() {
        for(Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}