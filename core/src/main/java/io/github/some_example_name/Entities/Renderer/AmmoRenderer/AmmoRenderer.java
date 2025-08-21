package io.github.some_example_name.Entities.Renderer.AmmoRenderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.MapConfig.MapRenderer;

import com.badlogic.gdx.graphics.Texture;

import java.util.List;

public class AmmoRenderer {
    private final TextureRegion ammoTexture;
    private final int tileSize;
    private final float scaleFactor;

    public AmmoRenderer(int tileSize) {
        this(tileSize, 0.4f);
    }

    public AmmoRenderer(int tileSize, float scaleFactor) {
        this.tileSize = tileSize;
        this.scaleFactor = scaleFactor;
        this.ammoTexture = new TextureRegion(new Texture("ITENS/Ammo/ammo0.9mm.png"));
    }
    public void render(SpriteBatch batch, List<Ammo> ammoList, float offsetX, float offsetY) {
        float scaledSize = tileSize * scaleFactor;
        float offset = (tileSize - scaledSize) / 2f;
      
        for(Ammo ammo : ammoList) {
            float x = offsetX + (ammo.getPosition().x * tileSize) + offset;
            float y = offsetY + (ammo.getPosition().y * tileSize) + offset;
            
            batch.draw(
                ammoTexture,
                x,
                y,
                scaledSize,
                scaledSize
            );
        }
    }

    public void dispose() {
        ammoTexture.getTexture().dispose();
    }
}