package io.github.some_example_name.Entities.Enemies.StateEnemy;



import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class StateEnemy {
    public enum StateIcon {
        PATROL,
        CHASE,
        SHOOTING,
        NONE
    }

    private TextureRegion patrolIcon;
    private TextureRegion chaseIcon;
    private TextureRegion shootingIcon;
    
    private StateIcon currentState;
    private Vector2 entityPosition;
    private float iconHeight;

    public StateEnemy(TextureRegion patrolIcon, TextureRegion chaseIcon, TextureRegion shootingIcon) {
        this.patrolIcon = patrolIcon;
        this.chaseIcon = chaseIcon;
        this.shootingIcon = shootingIcon;
        this.currentState = StateIcon.NONE;
        this.entityPosition = new Vector2();
        this.iconHeight = 1.5f;
    }

    public void setState(StateIcon state) {
        this.currentState = state;
    }

    public void updatePosition(Vector2 entityPosition) {
        this.entityPosition.set(entityPosition);
    }

    public void render(SpriteBatch batch) {
        if (currentState == StateIcon.NONE) return;
        
        TextureRegion currentTexture = null;
        
        switch (currentState) {
            case PATROL:
                currentTexture = patrolIcon;
                break;
            case CHASE:
                currentTexture = chaseIcon;
                break;
            case SHOOTING:
                currentTexture = shootingIcon;
                break;
            default:
                return;
        }

        if (currentTexture != null) {
            float width = currentTexture.getRegionWidth() / 16f;
            float height = currentTexture.getRegionHeight() / 16f;
            float x = entityPosition.x - width / 2;
            float y = entityPosition.y + iconHeight;

            batch.draw(currentTexture, x, y, width, height);
        }
    }
}