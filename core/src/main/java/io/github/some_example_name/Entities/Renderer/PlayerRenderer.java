package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapRenderer;




public class PlayerRenderer {
    private float animationTime = 0f;
    private final PlayerAnimations animations;
    private Animation<TextureRegion> currentAnimation;

    public PlayerRenderer() {
        animations = new PlayerAnimations();
        currentAnimation = null; 
    }

    private Animation<TextureRegion> selectAnimation(Robertinhoo player) {
        if (player.state == Robertinhoo.DASH) {
            return getDashAnimation(player);
        }
        return getMovementAnimation(player);
    }

    private Animation<TextureRegion> getDashAnimation(Robertinhoo player) {
        switch (player.dashDirection) {
            case Robertinhoo.UP:
                return animations.dash_top;
            case Robertinhoo.DOWN:
                return animations.dash_down;
            case Robertinhoo.LEFT:
            case Robertinhoo.RIGHT:
                return animations.dash_sides;
            default:
                return animations.idleDown;
        }
    }

    private Animation<TextureRegion> getMovementAnimation(Robertinhoo player) {
        switch (player.dir) {
            case Robertinhoo.RIGHT:
                return animations.runLeft;
            case Robertinhoo.LEFT:
                return animations.runRight;
            case Robertinhoo.UP:
                return animations.runUp;
            case Robertinhoo.DOWN:
                return animations.runDown;
            default:
                return getIdleAnimation(player);
        }
    }

    private Animation<TextureRegion> getIdleAnimation(Robertinhoo player) {
        switch (player.lastDir) {
            case Robertinhoo.UP:
                return animations.idleUp;
            case Robertinhoo.DOWN:
                return animations.idleDown;

            case Robertinhoo.LEFT:
                return animations.idleLeft;

            case Robertinhoo.RIGHT:
                return animations.idleRigth;
            default:
                return animations.idleDown;
        }
    }

    public void render(SpriteBatch batch, Robertinhoo player, float delta, float offsetX, float offsetY) {
        animationTime += delta;

        Animation<TextureRegion> selectedAnimation = selectAnimation(player);


        if (selectedAnimation != currentAnimation) {
            animationTime = 0f;
            currentAnimation = selectedAnimation;
        }


        boolean isDashAnimation = currentAnimation == animations.dash_down 
                                || currentAnimation == animations.dash_top 
                                || currentAnimation == animations.dash_sides;


        TextureRegion frame = currentAnimation.getKeyFrame(animationTime, !isDashAnimation);

        float x = offsetX + player.bounds.x * MapRenderer.TILE_SIZE;
        float y = offsetY + player.bounds.y * MapRenderer.TILE_SIZE;
        float width = player.bounds.width * MapRenderer.TILE_SIZE;
        float height = player.bounds.height * MapRenderer.TILE_SIZE;

        if (player.state == Robertinhoo.DASH && player.dashDirection == Robertinhoo.LEFT) {
            batch.draw(frame, x + width, y, -width, height);
        } else {
            batch.draw(frame, x, y, width, height);
        }
    }

    public void dispose() {
        animations.dispose();
    }
}