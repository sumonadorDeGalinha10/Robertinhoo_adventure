package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import com.badlogic.gdx.Gdx;

public class WeaponAnimations implements Disposable {

    public enum WeaponState {
        IDLE, SHOOTING, RELOADING
    }

    public enum WeaponDirection {
        E, NE, N, NW, W, SW, S, SE
    }

    private final Animation<TextureRegion>[][] animations;
    private final Texture[] loadedTextures;

    public WeaponAnimations(String weaponType) {
        animations = new Animation[WeaponDirection.values().length][Weapon.WeaponState.values().length];
        loadedTextures = loadWeaponTextures(weaponType);

        for (WeaponDirection dir : WeaponDirection.values()) {
            for (Weapon.WeaponState state : Weapon.WeaponState.values()) {
                Animation<TextureRegion> anim = animations[dir.ordinal()][state.ordinal()];
                Gdx.app.log("ANIM_DEBUG", "Direção: " + dir + " | Estado: " + state +
                        " | Carregado: " + (anim != null));
            }
        }
    }

    private Texture[] loadWeaponTextures(String weaponType) {
        java.util.List<Texture> texturesList = new java.util.ArrayList<>();

        if (weaponType.equals("Pistol")) {
            loadDirection(texturesList, "ITENS/Pistol/pistol-E-Sheet.png", WeaponDirection.E, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol_NE-Sheet.png", WeaponDirection.NE, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol-N-Sheet.png", WeaponDirection.N, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol_NW-Sheet.png", WeaponDirection.NW, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol-L-Sheet.png", WeaponDirection.W, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol_SW-Sheet.png", WeaponDirection.SW, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol_S_Sheet.png", WeaponDirection.S, WeaponState.IDLE, 9);
            loadDirection(texturesList, "ITENS/Pistol/pistol_SE-Sheet.png", WeaponDirection.SE, WeaponState.IDLE, 1);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_S-Sheet.png", WeaponDirection.S,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_E-Sheet.png", WeaponDirection.E,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_L-Sheet.png", WeaponDirection.W,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_N-Sheet.png", WeaponDirection.N,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_NW-Sheet.png", WeaponDirection.NW,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_NE-Sheet.png", WeaponDirection.NE,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_SE-Sheet.png", WeaponDirection.SE,WeaponState.SHOOTING, 5);
            loadDirection(texturesList, "ITENS/Pistol/pistol_shoot_SW-Sheet.png", WeaponDirection.SW,WeaponState.SHOOTING, 5);
            for (WeaponDirection dir : WeaponDirection.values()) {
            loadDirection(texturesList, "ITENS/Pistol/pistolReload-Sheet.png", 
                         dir, WeaponState.RELOADING, 10);
        }
        }

        return texturesList.toArray(new Texture[0]);
    }

    private void loadDirection(java.util.List<Texture> texturesList, String path,
            WeaponDirection direction, WeaponState state, int frameCount) {
        try {
            Texture sheet = new Texture(Gdx.files.internal(path));
            float frameDuration = (state == WeaponState.SHOOTING) ? 0.05f : 0.2f;

            TextureRegion[] frames = new TextureRegion[frameCount];
            int frameWidth = sheet.getWidth() / frameCount;

            for (int i = 0; i < frameCount; i++) {
                frames[i] = new TextureRegion(sheet, i * frameWidth, 0, frameWidth, sheet.getHeight());
            }

            animations[direction.ordinal()][state.ordinal()] = new Animation<>(frameDuration, frames);

            texturesList.add(sheet);

            Gdx.app.log("LOAD_DIRECTION", "Carregado: " + path +
                    " | Estado: " + state +
                    " | Frames: " + frameCount);
        } catch (Exception e) {
            Gdx.app.error("WeaponAnimations", "Erro ao carregar " + path, e);
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    public Animation<TextureRegion> getAnimation(WeaponDirection direction, Weapon.WeaponState state) {
        Animation<TextureRegion> anim = animations[direction.ordinal()][state.ordinal()];

        if (anim == null) {
            Gdx.app.error("WeaponAnimations", "Animation missing for: " + direction + " - " + state);
            return animations[WeaponDirection.E.ordinal()][Weapon.WeaponState.IDLE.ordinal()];
        }

        return anim;
    }
}