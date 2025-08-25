package io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public class CastorRenderer {
    private Animation<TextureRegion> idleAnimation;
    private float stateTime;
    private static final float FRAME_DURATION = 0.1f;

    public CastorRenderer() {
        Texture castorSheet = new Texture(Gdx.files.internal("enemies/castor/idle-Sheet.png"));
        TextureRegion[][] tmp = TextureRegion.split(castorSheet, 
            castorSheet.getWidth() / 8, 
            castorSheet.getHeight());
        
        TextureRegion[] idleFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            idleFrames[i] = tmp[0][i];
        }
        
        idleAnimation = new Animation<>(FRAME_DURATION, idleFrames);
        stateTime = 0f;
    }

    public void render(SpriteBatch batch, Castor castor, float offsetX, float offsetY, float delta) {
        stateTime += delta;
        TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        
        Vector2 position = castor.getPosition();
        float x = offsetX + position.x * 16 - 8; // Ajuste para centralizar
        float y = offsetY + position.y * 16 - 8; // Ajuste para centralizar
        
        batch.draw(currentFrame, x, y, 16, 16);
    }

    public void dispose() {
        idleAnimation.getKeyFrames()[0].getTexture().dispose();
    }
}