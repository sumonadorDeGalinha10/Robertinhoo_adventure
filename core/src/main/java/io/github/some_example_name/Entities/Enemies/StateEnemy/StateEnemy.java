package io.github.some_example_name.Entities.Enemies.StateEnemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class StateEnemy {
    public enum StateIcon {
        PATROL,
        CHASE,
        SHOOTING,
        NONE
    }

    private StateIcon currentState;
    private Vector2 entityPosition;
    private float iconHeight;
    private float stateTime;

    private final Animation<TextureRegion> patrolIcon;
    private final Animation<TextureRegion> chaseIcon;
    private final Animation<TextureRegion> shootingIcon;

    public StateEnemy() {
        this.patrolIcon = createAnimation("enemies/Icon/patrolIcon-Sheet.png", 11, 0.1f);
        this.chaseIcon = createAnimation("enemies/Icon/persueIcon-Sheet.png", 5, 0.15f);
        this.shootingIcon = createAnimation("enemies/Icon/ataqueIcon-Sheet.png", 2, 0.2f);

        this.currentState = StateIcon.NONE;
        this.entityPosition = new Vector2();
        this.iconHeight = 1.5f;
        this.stateTime = 0f;
    }

    private Animation<TextureRegion> createAnimation(String texturePath, int frameCount, float frameDuration) {
        Texture texture = new Texture(texturePath);
        int frameWidth = texture.getWidth() / frameCount;
        int frameHeight = texture.getHeight();

        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight));
        }

        return new Animation<>(frameDuration, frames);
    }

    public void setState(StateIcon state) {
        this.currentState = state;
    }

    public void updatePosition(Vector2 entityPosition) {
        this.entityPosition.set(entityPosition);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    public void render(SpriteBatch batch) {
        if (currentState == StateIcon.NONE)
            return;

        Animation<TextureRegion> currentAnimation = null;

        switch (currentState) {
            case PATROL:
                currentAnimation = patrolIcon;
                break;
            case CHASE:
                currentAnimation = chaseIcon;
                break;
            case SHOOTING:
                currentAnimation = shootingIcon;
                break;
            default:
                return;
        }

        if (currentAnimation != null) {
            TextureRegion currentFrame = currentAnimation.getKeyFrame(stateTime, true);
            float width = currentFrame.getRegionWidth() / 16f;
            float height = currentFrame.getRegionHeight() / 16f;
            float x = entityPosition.x - width / 2;
            float y = entityPosition.y + iconHeight;
          

            batch.draw(currentFrame, x, y + 5f, width, height);
          
        }
    }


    public void dispose() {
    if (patrolIcon != null && patrolIcon.getKeyFrames().length > 0) {
        patrolIcon.getKeyFrames()[0].getTexture().dispose();
    }
    if (chaseIcon != null && chaseIcon.getKeyFrames().length > 0) {
        chaseIcon.getKeyFrames()[0].getTexture().dispose();
    }
    if (shootingIcon != null && shootingIcon.getKeyFrames().length > 0) {
        shootingIcon.getKeyFrames()[0].getTexture().dispose();
    }
}
}