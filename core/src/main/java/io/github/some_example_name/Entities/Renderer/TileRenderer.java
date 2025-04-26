package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.Mapa;
import java.util.Random;

public class TileRenderer {
    private final Mapa mapa;
    private final Array<Texture> floorTextures;
    private final Array<Texture> wallTextures;
    private final int tileSize;
    private final int[][] textureIndices;

    public TileRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;

        floorTextures = new Array<>();
        for (int i = 1; i <= 4; i++) {
            floorTextures.add(new Texture(Gdx.files.internal("Tiles/Chão" + i + ".png")));
        }

        wallTextures = new Array<>();
        for (int i = 1; i <= 4; i++) {
            wallTextures.add(new Texture(Gdx.files.internal("Tiles/Parede" + i + ".png")));
        }

    
        textureIndices = new int[mapa.mapWidth][mapa.mapHeight];
        Random rand = new Random();

        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    textureIndices[x][y] = rand.nextInt(wallTextures.size);
                } else {
                    textureIndices[x][y] = rand.nextInt(floorTextures.size);
                }
            }
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, float delta) {
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                float renderY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    Texture wallTex = wallTextures.get(textureIndices[x][y]);
                    batch.draw(wallTex, offsetX + x * tileSize, renderY, tileSize, tileSize);
                } else {
                    Texture floorTex = floorTextures.get(textureIndices[x][y]);
                    batch.draw(floorTex, offsetX + x * tileSize, renderY, tileSize, tileSize);
                }
            }
        }
    }

    public void dispose() {
        for (Texture tex : floorTextures) {
            tex.dispose();
        }
        for (Texture tex : wallTextures) {
            tex.dispose();
        }
    }
}