package io.github.some_example_name.Entities.Renderer.ItensRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

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
    for (Destructible d : destructibles) {
        if (!d.isDestroyed()) {
            Vector2 position = d.getPosition();
            Gdx.app.log("Renderer", "Renderizando objeto em " + position);
            batch.draw(
                d.getTexture(),
                offsetX + (position.x - 0.5f) * tileSize,
                offsetY + (position.y - 0.5f) * tileSize,
                tileSize,
                tileSize
            );
        }
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