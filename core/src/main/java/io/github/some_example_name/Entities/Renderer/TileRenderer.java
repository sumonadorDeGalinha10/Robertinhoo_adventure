package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.some_example_name.Mapa;

public class TileRenderer {

    private final Mapa mapa;
    private final Texture floorTexture;
    private final Texture wallTexture;
    private final int tileSize;


    public TileRenderer(Mapa mapa, Texture floorTexture, Texture wallTexture, int tileSize) {
        this.mapa = mapa;
        this.floorTexture = floorTexture;
        this.wallTexture = wallTexture;
        this.tileSize = tileSize;
    }


        public void render(SpriteBatch batch, float offsetX, float offsetY, float delta) {
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                float renderY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    batch.draw(wallTexture,
                             offsetX + x * tileSize,
                             renderY,
                             tileSize,
                             tileSize);
                } else {
                    batch.draw(floorTexture,
                             offsetX + x * tileSize,
                             renderY,
                             tileSize,
                             tileSize);
                }
            }
        }
    }

    public void dispose() {
        floorTexture.dispose();
        wallTexture.dispose();
    }
    
}
