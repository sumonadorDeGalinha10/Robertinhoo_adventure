package io.github.some_example_name.Entities.Renderer.ItensRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Color;

public class DestructibleRenderer {
    private final List<Destructible> destructibles;
    private final int tileSize;

    public DestructibleRenderer(int tileSize) {
        this.destructibles = new ArrayList<>();
        this.tileSize = tileSize;
    }

    public void addDestructible(Destructible destructible) {
        destructibles.add(destructible);
    }

    public void update(float delta) {
        for (Destructible d : destructibles) {
            d.update(delta);
        }
    }

public void render(SpriteBatch batch, List<Destructible> destructibles, float offsetX, float offsetY) {
    float newSize = tileSize * 0.5f;
    float padding = (tileSize - newSize) / 2f;

    for (Destructible d : destructibles) {
        if (d instanceof Barrel) {
            Barrel barrel = (Barrel) d;
            // pula barris que já terminaram a animação
            if (barrel.isAnimationFinished()) continue;

            // se estiver no flash, pinta de branco intenso
            if (barrel.isFlashActive()) {
                batch.setColor(Color.WHITE);
            } else {
                // aqui você pode resetar pra cor normal do seu jogo (ex: cinza, vermelho, etc)
                batch.setColor(Color.WHITE);
            }
        }

        TextureRegion texture = d.getTexture();
        Vector2 pos = d.getPosition();

        if (texture != null) {
            float x = offsetX + (pos.x - 0.5f) * tileSize + padding;
            float y = offsetY + (pos.y - 0.5f) * tileSize + padding;
            batch.draw(texture, x, y, newSize, newSize);
        }

        // depois do draw, sempre resetar pra cor padrão
        batch.setColor(Color.WHITE);
    }
}


    public void dispose() {
        for (Destructible d : destructibles) {
            if (d instanceof Barrel) {
                ((Barrel) d).dispose();
            }
        }
    }
}