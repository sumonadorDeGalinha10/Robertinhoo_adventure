package io.github.some_example_name.Entities.Renderer.Shadow;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
public class ShadowRenderer {
    private ShapeRenderer shapeRenderer;
    
    public ShadowRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }
    
    public void renderShadows(Iterable<ShadowEntity> entities, float offsetX, float offsetY, float tileSize) {
        // Habilita o blending para suportar transparência
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (ShadowEntity entity : entities) {
            ShadowComponent shadow = entity.getShadowComponent();
            if (shadow != null) {
                shadow.render(shapeRenderer, entity.getPosition(), offsetX, offsetY, tileSize);
            }
        }
        
        shapeRenderer.end();
        
        // Desabilita o blending se necessário (opcional)
        // Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}