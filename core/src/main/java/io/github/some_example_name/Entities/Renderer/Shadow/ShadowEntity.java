package io.github.some_example_name.Entities.Renderer.Shadow;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;

public interface ShadowEntity {
    ShadowComponent getShadowComponent();
    Vector2 getPosition();
}