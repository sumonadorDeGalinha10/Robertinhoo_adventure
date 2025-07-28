package io.github.some_example_name.Entities.Renderer.Shadow;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

public class ShadowComponent {
    private float width;
    private float height;
    private float yOffset;
    private float opacity;
    private Color color;
    
    public ShadowComponent(float width, float height) {
        this(width, height, -0.2f, 0.1f, new Color(0, 0, 0, 0.5f));
    }
    
    public ShadowComponent(float width, float height, float yOffset, float opacity, Color color) {
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
        this.opacity = opacity;
        this.color = color;
    }
    
    public void render(ShapeRenderer shapeRenderer, Vector2 position, float offsetX, float offsetY, float tileSize) {
        // Define cor com opacidade ajustada
        Color renderColor = new Color(color);
        renderColor.a *= opacity; // Aplica opacidade adicional
        
        shapeRenderer.setColor(renderColor);
        
        float shadowX = offsetX + position.x * tileSize;
        float shadowY = offsetY + (position.y + yOffset) * tileSize;
        
        shapeRenderer.ellipse(
            shadowX - width/2,
            shadowY - height/2,
            width,
            height
        );
    }
    
    // Getters e Setters para personalização dinâmica
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
    public void setOpacity(float opacity) { this.opacity = opacity; }
    public void setColor(Color color) { this.color = color; }
    public void setYOffset(float yOffset) { this.yOffset = yOffset; }
}