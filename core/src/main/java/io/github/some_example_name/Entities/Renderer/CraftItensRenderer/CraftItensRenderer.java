package io.github.some_example_name.Entities.Renderer.CraftItensRenderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Itens.CraftinItens.Polvora;
import io.github.some_example_name.Entities.Inventory.Item;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;

public class CraftItensRenderer {
    private final int tileSize;
    
    public CraftItensRenderer(int tileSize) {
        this.tileSize = tileSize;
    }
    
    public void render(SpriteBatch batch, List<Item> craftItems, float offsetX, float offsetY) {
        for (Item item : craftItems) {
            if (item instanceof Polvora) {
                renderPolvora(batch, (Polvora) item, offsetX, offsetY);
            }
        }
    }
    
    private void renderPolvora(SpriteBatch batch, Polvora polvora, float offsetX, float offsetY) {
        Vector2 position = polvora.getPosition();
        TextureRegion texture = polvora.getIcon();
        
        float renderX = offsetX + position.x * tileSize;
        float renderY = offsetY + position.y * tileSize;
        
        float floatOffset = (float) Math.sin(System.currentTimeMillis() * 0.002) * 0.5f;
        
        batch.draw(
            texture,
            renderX,
            renderY + floatOffset,
            tileSize / 2f *0.5f,
            tileSize / 2f *0.5f
        );
    }
    
    // Adicione métodos para outros tipos de itens de crafting
    // private void renderOutroItem(...) {...}
}