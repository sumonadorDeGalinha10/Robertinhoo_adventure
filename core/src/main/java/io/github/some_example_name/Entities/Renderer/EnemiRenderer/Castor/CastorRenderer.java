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
    private Animation<TextureRegion> shootLeftAnimation;
    private Animation<TextureRegion> shootDownAnimation;
    private Animation<TextureRegion> shootUpAnimation;
    private boolean isShooting = false;
    private float shootAnimationTime = 0f;

    private enum Direction {
        LEFT, RIGHT, DOWN, UP
    }

    public CastorRenderer() {
        Texture idleSheet = new Texture(Gdx.files.internal("enemies/castor/idle-Sheet.png"));
        TextureRegion[][] idleFramesGrid = TextureRegion.split(idleSheet, 
            idleSheet.getWidth() / 8, 
            idleSheet.getHeight());
        idleAnimation = new Animation<>(FRAME_DURATION, idleFramesGrid[0]);
        
        Texture runSheet = new Texture(Gdx.files.internal("enemies/castor/RUN-Sheet.png"));
        int runFrameWidth = runSheet.getWidth() / 5;
        int runFrameHeight = runSheet.getHeight() / 3;
        TextureRegion[][] runFrames = TextureRegion.split(runSheet, runFrameWidth, runFrameHeight);
        
        runLeftAnimation = new Animation<>(FRAME_DURATION, runFrames[0]);
        runDownAnimation = new Animation<>(FRAME_DURATION, runFrames[1]);
        runUpAnimation = new Animation<>(FRAME_DURATION, runFrames[2]);
        
        stateTime = 0f;

        Texture shootSheet = new Texture(Gdx.files.internal("enemies/castor/all_shoot-Sheet.png"));
        int shootFrameWidth = shootSheet.getWidth() / 6;
        int shootFrameHeight = shootSheet.getHeight() / 3;
        TextureRegion[][] shootFrames = TextureRegion.split(shootSheet, shootFrameWidth, shootFrameHeight);
        
        shootLeftAnimation = new Animation<>(FRAME_DURATION, shootFrames[0]);
        shootDownAnimation = new Animation<>(FRAME_DURATION, shootFrames[1]);
        shootUpAnimation = new Animation<>(FRAME_DURATION, shootFrames[2]);
    }
    
    private Direction getDirection(Vector2 velocity, Vector2 targetPosition, Vector2 currentPosition, boolean isShooting) {
        // Durante o tiro, usa a direção do alvo em vez da velocidade
        if (isShooting) {
            Vector2 directionToTarget = targetPosition.cpy().sub(currentPosition).nor();
            
            // Determina a direção principal baseada no vetor para o alvo
            float absX = Math.abs(directionToTarget.x);
            float absY = Math.abs(directionToTarget.y);
            
            if (absX > absY) {
                return directionToTarget.x > 0 ? Direction.RIGHT : Direction.LEFT;
            } else {
                return directionToTarget.y > 0 ? Direction.UP : Direction.DOWN;
            }
        }
        
        // Comportamento normal para movimento
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
        Vector2 currentPosition = castor.getPosition();
        Vector2 targetPosition = castor.target.getPosition();
        
        isMoving = velocity.len() > 0.1f;
        isShooting = castor.isShooting();

        if (isShooting) {
            shootAnimationTime += delta;
        } else {
            shootAnimationTime = 0f;
        }
        
        // Sempre atualiza a direção, especialmente durante o tiro
        currentDirection = getDirection(velocity, targetPosition, currentPosition, isShooting);
        
        stateTime += delta;
        TextureRegion currentFrame;
        boolean flipX = false;
        
        if (isShooting) {
            switch (currentDirection) {
                case LEFT:
                    currentFrame = shootLeftAnimation.getKeyFrame(shootAnimationTime, false);
                    break;
                case RIGHT:
                    currentFrame = shootLeftAnimation.getKeyFrame(shootAnimationTime, false);
                    flipX = true;
                    break;
                case DOWN:
                    currentFrame = shootDownAnimation.getKeyFrame(shootAnimationTime, false);
                    break;
                case UP:
                    currentFrame = shootUpAnimation.getKeyFrame(shootAnimationTime, false);
                    break;
                default:
                    currentFrame = idleAnimation.getKeyFrame(stateTime, true);
            }

            if (shootAnimationTime >= 0.5f && !castor.hasShot()) {
            castor.setHasShot(true);
            castor.fireProjectile();
        }
    }
        else if (isMoving) {
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
        
        float x = offsetX + currentPosition.x * 16 - 8;
        float y = offsetY + currentPosition.y * 16 - 8;
        
        if (flipX) {
            batch.draw(currentFrame, x + 16, y, -16, 16);
        } else {
            batch.draw(currentFrame, x, y, 16, 16);
        }
    }

    public void dispose() {
        idleAnimation.getKeyFrames()[0].getTexture().dispose();
        runLeftAnimation.getKeyFrames()[0].getTexture().dispose();
        shootLeftAnimation.getKeyFrames()[0].getTexture().dispose();
    }
}