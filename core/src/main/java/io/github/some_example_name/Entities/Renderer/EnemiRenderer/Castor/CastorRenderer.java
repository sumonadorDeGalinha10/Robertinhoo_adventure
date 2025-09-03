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
    private Animation<TextureRegion> runLeftAnimation;
    private Animation<TextureRegion> runDownAnimation;
    private Animation<TextureRegion> runUpAnimation;
    private float stateTime;
    private static final float FRAME_DURATION = 0.1f;
    private Direction currentDirection = Direction.DOWN;
    private boolean isMoving = false;

    private enum Direction {
        LEFT, RIGHT, DOWN, UP
    }

    public CastorRenderer() {
        // Carrega a spritesheet do idle
        Texture idleSheet = new Texture(Gdx.files.internal("enemies/castor/idle-Sheet.png"));
        TextureRegion[][] idleFramesGrid = TextureRegion.split(idleSheet, 
            idleSheet.getWidth() / 8, 
            idleSheet.getHeight());
        idleAnimation = new Animation<>(FRAME_DURATION, idleFramesGrid[0]);
        
        // Carrega a spritesheet do run
        Texture runSheet = new Texture(Gdx.files.internal("enemies/castor/RUN-Sheet.png"));
        int runFrameWidth = runSheet.getWidth() / 5;
        int runFrameHeight = runSheet.getHeight() / 3;
        TextureRegion[][] runFrames = TextureRegion.split(runSheet, runFrameWidth, runFrameHeight);
        
        runLeftAnimation = new Animation<>(FRAME_DURATION, runFrames[0]);
        runDownAnimation = new Animation<>(FRAME_DURATION, runFrames[1]);
        runUpAnimation = new Animation<>(FRAME_DURATION, runFrames[2]);
        
        stateTime = 0f;
    }

    private Direction getDirection(Vector2 velocity) {
        float absVelX = Math.abs(velocity.x);
        float absVelY = Math.abs(velocity.y);
        
        if (absVelX > absVelY) {
            return velocity.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return velocity.y > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    public void render(SpriteBatch batch, Castor castor, float offsetX, float offsetY, float delta) {
        Vector2 velocity = castor.getLinearVelocity();
        isMoving = velocity.len() > 0.1f;
        
        if (isMoving) {
            currentDirection = getDirection(velocity);
        }
        
        stateTime += delta;
        TextureRegion currentFrame;
        boolean flipX = false;
        
        if (isMoving) {
            switch (currentDirection) {
                case LEFT:
                    currentFrame = runLeftAnimation.getKeyFrame(stateTime, true);
                    break;
                case RIGHT:
                    currentFrame = runLeftAnimation.getKeyFrame(stateTime, true);
                    flipX = true;
                    break;
                case DOWN:
                    currentFrame = runDownAnimation.getKeyFrame(stateTime, true);
                    break;
                case UP:
                    currentFrame = runUpAnimation.getKeyFrame(stateTime, true);
                    break;
                default:
                    currentFrame = idleAnimation.getKeyFrame(stateTime, true);
            }
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime, true);
        }
        
        Vector2 position = castor.getPosition();
        float x = offsetX + position.x * 16 - 8;
        float y = offsetY + position.y * 16 - 8;
        
        if (flipX) {
            batch.draw(currentFrame, x + 16, y, -16, 16);
        } else {
            batch.draw(currentFrame, x, y, 16, 16);
        }
    }

    public void dispose() {
        idleAnimation.getKeyFrames()[0].getTexture().dispose();
        runLeftAnimation.getKeyFrames()[0].getTexture().dispose();
    }
}