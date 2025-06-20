package io.github.some_example_name.Interface;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;


public class RobertinhoFaceHUD {
    private final Texture metalFrame;
    private final Texture faceSheet;
    private float size;
    private float x, y;
    private float screenWidth, screenHeight;

    private final Animation<TextureRegion> faceAnimation;
    private float stateTime;

    private float faceWidth, faceHeight;

    public RobertinhoFaceHUD(float size, float screenWidth, float screenHeight) {
        this.size = size;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        calculatePosition();
        
        metalFrame = new Texture("rober/interface/molde.png");
        faceSheet = new Texture("rober/interface/robertinhoo_idle-Sheet.png");

        int FRAME_COLS = 8;
        int FRAME_ROWS = 1;

        TextureRegion[][] tmp = TextureRegion.split(
                faceSheet,
                faceSheet.getWidth() / FRAME_COLS,
                faceSheet.getHeight() / FRAME_ROWS
        );

        TextureRegion[] idleFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                idleFrames[index++] = tmp[i][j];
            }
        }

        faceAnimation = new Animation<>(0.4f, idleFrames);
        stateTime = 0f;
        
        this.faceWidth = size;
        this.faceHeight = size;
    }

    private void calculatePosition() {
        float margin = -20f;
        this.x = screenWidth - size - margin;
        this.y = margin ;
    }

    public void updateScreenSize(float width, float height) {
        this.screenWidth = width;
        this.screenHeight = height;
        calculatePosition();
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(metalFrame, x, y, size, size);
        
        TextureRegion currentFrame = faceAnimation.getKeyFrame(stateTime, true);
        float faceX = x + (size - faceWidth) / 2;
        float faceY = y + (size - faceHeight) / 2;
        
        batch.draw(currentFrame, faceX, faceY, faceWidth, faceHeight);
    }

    public void setSize(float size) {
        this.size = size;
        this.faceWidth = size;
        this.faceHeight = size;
        calculatePosition();
    }

    public float getSize() {
        return size;
    }

    public void dispose() {
        metalFrame.dispose();
        faceSheet.dispose();
    }
}