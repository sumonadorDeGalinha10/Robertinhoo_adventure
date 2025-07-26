package io.github.some_example_name.Entities.Renderer.ItensRenderer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Destructible {
    Vector2 getPosition();
    TextureRegion getTexture();
    boolean isDestroyed();
    void update(float delta);
}