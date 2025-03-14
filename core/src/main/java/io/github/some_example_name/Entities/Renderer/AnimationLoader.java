package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationLoader {
    
    public static Animation<TextureRegion> loadAnimation(String path, float frameDuration, boolean flipX, int frameCount) {
        Texture texture = new Texture(path);
        int frameWidth = texture.getWidth() / frameCount;
        int frameHeight = texture.getHeight();
        
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight);
            if(flipX) frames[i].flip(true, false);
        }
        
        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    public static Texture loadTexture(String path) {
        return new Texture(path);
    }
}