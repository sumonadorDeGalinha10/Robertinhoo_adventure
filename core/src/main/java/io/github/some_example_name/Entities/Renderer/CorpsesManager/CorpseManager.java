package io.github.some_example_name.Entities.Renderer.CorpsesManager;
import java.util.List;
import java.util.ArrayList;

import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Rat.RatRenderer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CorpseManager {
    private static class Corpse {
        final Vector2 position;
        final Vector2 renderOffset; // Novo campo
        final TextureRegion texture;
        final float width;
        final float height;

        Corpse(Vector2 position, Vector2 renderOffset, TextureRegion texture, float width, float height) {
            this.position = position;
            this.renderOffset = renderOffset;
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }

    private final List<Corpse> corpses = new ArrayList<>();

    public void addCorpse(Ratinho rat, RatRenderer ratRenderer, 
                          TextureRegion corpseTexture) {
        Vector2 pos = rat.getPosition().cpy();
        Vector2 renderOffset = ratRenderer.calculateRenderOffset(rat);
        float width = ratRenderer.getRatRenderWidth();
        float height = ratRenderer.getRatRenderHeight();
        
        corpses.add(new Corpse(pos, renderOffset, corpseTexture,  width, height));
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        for (Corpse corpse : corpses) {
            float renderX = offsetX + (corpse.position.x + corpse.renderOffset.x) * 16;
            float renderY = offsetY + (corpse.position.y + corpse.renderOffset.y) * 16;
            
       
                batch.draw(
                    corpse.texture,
                    renderX,
                    renderY,
                    corpse.width,
                    corpse.height
                );
            
        }
    }
}