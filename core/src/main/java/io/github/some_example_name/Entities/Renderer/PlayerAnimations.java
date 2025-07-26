// PlayerAnimations.java
package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Texture;
// PlayerAnimations.java



import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.Arrays;

public class PlayerAnimations implements Disposable {
    // Categorias de animações
    public static class BasicAnimations {
        public final Animation<TextureRegion> idleDown;
        public final Animation<TextureRegion> idleUp;
        public final Animation<TextureRegion> idleLeft;
        public final Animation<TextureRegion> idleRight;
        public final Animation<TextureRegion> walkLeft;
        public final Animation<TextureRegion> walkRight;
        public final Animation<TextureRegion> walkUp;
        public final Animation<TextureRegion> walkDown;
        public final Animation<TextureRegion> walkSE;
        public final Animation<TextureRegion> walkSW;
        public final Animation<TextureRegion> walkNortEast;
        public final Animation<TextureRegion> walkNortWast;
        public final Animation<TextureRegion> idleNorthWest;
        public final Animation<TextureRegion> idleNorthEast;
        public final Animation<TextureRegion> idleSouthWest;
        public final Animation<TextureRegion> idleSouthEast;
        
        // Construtor
        public BasicAnimations() {
            idleDown = AnimationLoader.loadAnimation("rober/idle/idle_S-Sheet.png", 0.2f, false, 12);
            idleUp = AnimationLoader.loadAnimation("rober/idle/idle_N-Sheet.png", 0.2f, false, 12);
            idleLeft = AnimationLoader.loadAnimation("rober/idle/idle_E-Sheet.png", 0.2f, true, 12);
            idleRight = AnimationLoader.loadAnimation("rober/idle/idle_E-Sheet.png", 0.2f, false, 12);
            walkLeft = AnimationLoader.loadAnimation("rober/walk/walk_E-Sheet.png", 0.1f, false, 8);
            walkRight = AnimationLoader.loadAnimation("rober/walk/walk_E-Sheet.png", 0.1f, false, 8);
            walkUp = AnimationLoader.loadAnimation("rober/walk/walk_N-Sheet.png", 0.1f, false, 8);
            walkNortEast = AnimationLoader.loadAnimation("rober/walk/walk_NE-Sheet.png", 0.1f, false, 8);
            walkNortWast = AnimationLoader.loadAnimation("rober/walk/walk_NE-Sheet.png", 0.1f, true, 8);
            walkDown = AnimationLoader.loadAnimation("rober/walk/walk_S-Sheet.png", 0.1f, false, 8);
            walkSE = AnimationLoader.loadAnimation("rober/walk/walk_SE-Sheet.png", 0.1f, false, 8);
            walkSW = AnimationLoader.loadAnimation("rober/walk/walk_SE-Sheet.png", 0.1f, true, 8);
            idleNorthWest = AnimationLoader.loadAnimation("rober/idle/idle_NE-Sheet.png", 0.2f, true, 12);
            idleNorthEast = AnimationLoader.loadAnimation("rober/idle/idle_NE-Sheet.png", 0.2f, false, 12);
            idleSouthWest = AnimationLoader.loadAnimation("rober/idle/idle_SE-Sheet.png", 0.2f, true, 12);
            idleSouthEast = AnimationLoader.loadAnimation("rober/idle/idle_SE-Sheet.png", 0.2f, false, 12);
        }
    }

    public static class WeaponAnimations {
        public final Animation<TextureRegion> idleDown;
        public final Animation<TextureRegion> idleUp;
        public final Animation<TextureRegion> idleLeft;
        public final Animation<TextureRegion> idleRight;
        public final Animation<TextureRegion> idleSE;
        public final Animation<TextureRegion> idleSW;
        public final Animation<TextureRegion> idleNE;
        public final Animation<TextureRegion> idleNW;
        public final Animation<TextureRegion> runLeft;
        public final Animation<TextureRegion> runRight;
        public final Animation<TextureRegion> runUp;
        public final Animation<TextureRegion> runDown;
        public final Animation<TextureRegion> runNE;
        public final Animation<TextureRegion> runNW;
        public final Animation<TextureRegion> runSE;
        public final Animation<TextureRegion> runSW;

        // Construtor
        public WeaponAnimations() {
            idleDown = AnimationLoader.loadAnimation("rober/idle_with_weapon/idle_S_with_weapon_Sheet.png", 0.2f, false, 12);
            idleLeft = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_E-Sheet.png", 0.2f, true, 12);
            idleRight = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_E-Sheet.png", 0.2f, false, 12);
            idleUp = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_N-Sheet.png", 0.2f, true, 12);
            idleSE = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_SE-Sheet.png", 0.2f, false, 12);
            idleSW = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_SE-Sheet.png", 0.2f, true, 12);
            idleNE = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_NE-Sheet.png", 0.2f, false, 12);
            idleNW = AnimationLoader.loadAnimation("rober/idle_with_weapon/Idle_NE-Sheet.png", 0.2f, true, 12);
            runLeft = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_E-Sheet.png", 0.1f, true, 8);
            runRight = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_E-Sheet.png", 0.1f, false, 8);
            runDown = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_S-With_weapon.png", 0.1f, false, 8);
            runUp = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_N-Sheet.png", 0.1f, true, 8);
            runSE = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_SE-Sheet.png", 0.1f, false, 8);
            runSW = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_SE-Sheet.png", 0.1f, true, 8);
            runNE = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_NE-Sheet.png", 0.1f, false, 8);
            runNW = AnimationLoader.loadAnimation("rober/run_with_weapon/walk_NE-Sheet.png", 0.1f, true, 8);
        }
    }

    public static class SpecialAnimations {
        public final Animation<TextureRegion> rollDown;
        public final Animation<TextureRegion> rollUp;
        public final Animation<TextureRegion> rollSide;
        public final Animation<TextureRegion> meleeAttackRight;
        public final Animation<TextureRegion> meleeAttackLeft;
        public final Animation<TextureRegion> meleeAttackUp;
        public final Animation<TextureRegion> meleeAttackDown;

        // Construtor
        public SpecialAnimations() {
            // Roll animations
            Texture rollTexture = AnimationLoader.loadTexture("rober/roll/roll-Sheet.png");
            int frameWidth = rollTexture.getWidth() / 21;
            int frameHeight = rollTexture.getHeight();
            TextureRegion[][] rollFrames = TextureRegion.split(rollTexture, frameWidth, frameHeight);
            TextureRegion[] rollDownFrames = Arrays.copyOfRange(rollFrames[0], 0, 7);
            TextureRegion[] rollUpFrames = Arrays.copyOfRange(rollFrames[0], 14, 21);
            TextureRegion[] rollSideFrames = Arrays.copyOfRange(rollFrames[0], 7, 14);
            float rollFrameDuration = 0.1f;
            rollDown = new Animation<>(rollFrameDuration, rollDownFrames);
            rollUp = new Animation<>(rollFrameDuration, rollUpFrames);
            rollSide = new Animation<>(rollFrameDuration, rollSideFrames);

            // Melee attack animations
            Texture meleeTexture = AnimationLoader.loadTexture("rober/corpo_a_corpo/ataque_sheet.png");
            int frameWidthMelee = meleeTexture.getWidth() / 7;
            int frameHeightMelee = meleeTexture.getHeight() / 4;
            TextureRegion[][] meleeFrames = TextureRegion.split(meleeTexture, frameWidthMelee, frameHeightMelee);
            meleeAttackRight = new Animation<>(0.088f, meleeFrames[0]);
            meleeAttackLeft = new Animation<>(0.088f, meleeFrames[1]);
            meleeAttackDown = new Animation<>(0.088f, meleeFrames[2]);
            meleeAttackUp = new Animation<>(0.088f, meleeFrames[3]);
        }
    }

    // Instâncias das categorias
    public final BasicAnimations basic;
    public final WeaponAnimations weapon;
    public final SpecialAnimations special;

    private final Array<Texture> loadedTextures = new Array<>();

    public PlayerAnimations() {
        // Carregar todas as texturas primeiro
        loadAllTextures();
        
        // Inicializar categorias
        basic = new BasicAnimations();
        weapon = new WeaponAnimations();
        special = new SpecialAnimations();
    }

    private void loadAllTextures() {
        // Lista de caminhos de textura
        String[] texturePaths = {
            "rober/idle/idle_S-Sheet.png",
            "rober/idle/idle_N-Sheet.png",
            "rober/idle/idle_E-Sheet.png",
            "rober/run/2_Template_Run_Left-Sheet.png",
            "rober/run/2_Template_Run_Up-Sheet.png",
            "rober/run/2_Template_Run_Down-Sheet.png",
            "rober/idle_with_weapon/Idle_down_With_weapon-Sheet.png",
            "rober/idle_with_weapon/idle_left_with_weapon.png",
            "rober/idle_with_weapon/Idle_E-Sheet.png",
            "rober/idle_with_weapon/Idle_N-Sheet.png",
            "rober/run_with_weapon/2_Template_Run_Up_With_One_HandWEAPON-Sheet.png",
            "rober/idle_with_weapon/1_Template_Idle_Up_with_weapon-Sheet.png",
            "rober/run_with_weapon/2_Template_Run_Left_withe_oneHand_WEAPON.png",
            "rober/run_with_weapon/runDown_With_One_HandWEAPON-Sheet.png",
            "rober/run_with_weapon/2_Template_Run_Up_With_One_HandWEAPON-Sheet.png",
            "rober/walk/walk_SE-Sheet.png",
            "rober/roll/roll-Sheet.png",
            "rober/corpo_a_corpo/ataque_sheet.png"
        };

        // Carregar e armazenar texturas
        for (String path : texturePaths) {
            Texture texture = AnimationLoader.loadTexture(path);
            loadedTextures.add(texture);
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}