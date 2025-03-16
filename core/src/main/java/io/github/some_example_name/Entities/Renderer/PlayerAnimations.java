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
        };

        idleDown = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Down-Sheet.png", 0.2f, false, 6);
        idleUp = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Up-Sheet.png", 0.2f, false, 6);
        idleLeft = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Left-Sheet.png", 0.2f, false, 6);
        idleRigth = AnimationLoader.loadAnimation("rober/idle/1_Template_Idle_Left-Sheet.png", 0.2f, true, 6);
        runLeft = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Left-Sheet.png", 0.1f, true, 6);
        runRight = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Left-Sheet.png", 0.1f, false, 6);
        runUp = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Up-Sheet.png", 0.1f, false, 6);
        runDown = AnimationLoader.loadAnimation("rober/run/2_Template_Run_Down-Sheet.png", 0.1f, false, 6);
        dash_down = AnimationLoader.loadAnimation("rober/dash/dash_down.png", 0.07f, false, 9);
        dash_top = AnimationLoader.loadAnimation("rober/dash/dash_top.png", 0.07f, false, 10);
        dash_sides = AnimationLoader.loadAnimation("rober/dash/dash_sides.png", 0.07f, false, 8);
    }

    @Override
    public void dispose() {
        for(Texture texture : loadedTextures) {
            texture.dispose();
        }
    }
}