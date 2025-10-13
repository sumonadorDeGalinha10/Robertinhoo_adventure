package io.github.some_example_name.Entities.Renderer.CorpsesManager;

import java.util.List;
import java.util.ArrayList;

import io.github.some_example_name.Entities.Enemies.Enemy;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CorpseManager {
    private static class Corpse {
        final Vector2 position;
        final Vector2 renderOffset;
        final TextureRegion texture;
        final float width;
        final float height;
        final boolean flipX; // Adicionado para suportar flip

        Corpse(Vector2 position, Vector2 renderOffset, TextureRegion texture, float width, float height,
                boolean flipX) {
            this.position = position;
            this.renderOffset = renderOffset;
            this.texture = texture;
            this.width = width;
            this.height = height;
            this.flipX = flipX;
        }
    }

    private final List<Corpse> corpses = new ArrayList<>();

    // Interface para renderers que suportam corpse
    public interface CorpseRenderer {
        Vector2 calculateRenderOffset(Enemy enemy);

        float getRenderWidth();

        float getRenderHeight();

        TextureRegion getCorpseFrame(Enemy enemy);

        boolean shouldFlipCorpse(Enemy enemy); // Novo método para flip do cadáver
    }

    // Método genérico para qualquer inimigo
    public void addCorpse(Enemy enemy, CorpseRenderer renderer) {
        Vector2 pos = enemy.getPosition().cpy();
        Vector2 renderOffset = renderer.calculateRenderOffset(enemy);
        TextureRegion corpseTexture = renderer.getCorpseFrame(enemy);
        float width = renderer.getRenderWidth();
        float height = renderer.getRenderHeight();
        boolean flip = renderer.shouldFlipCorpse(enemy);

        corpses.add(new Corpse(pos, renderOffset, corpseTexture, width, height, flip));
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        for (Corpse corpse : corpses) {
            float renderX = offsetX + (corpse.position.x + corpse.renderOffset.x) * 64;
            float renderY = offsetY + (corpse.position.y + corpse.renderOffset.y) * 64;

            if (corpse.flipX) {
                batch.draw(
                        corpse.texture,
                        renderX + corpse.width, // Desloca para a direita
                        renderY,
                        -corpse.width, // Largura negativa para flip
                        corpse.height);
            } else {
                batch.draw(
                        corpse.texture,
                        renderX,
                        renderY,
                        corpse.width,
                        corpse.height);
            }
        }
    }

    // Método para limpar cadáveres se necessário
    public void clearCorpses() {
        corpses.clear();
    }
}