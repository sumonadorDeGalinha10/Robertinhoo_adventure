package io.github.some_example_name.Entities.Renderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import io.github.some_example_name.MapConfig.Mapa;

import java.util.Random;

public class TileRenderer {
    private final Mapa mapa;
    private final Texture floorTexture;
    private final TextureRegion floorRegion;
    private final Array<Texture> wallTextures; // agora 5 textures
    private final int tileSize;
    private final int[][] textureIndices; // índice para paredes (0..4)

    public TileRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;

        // floor como antes
        floorTexture = new Texture(Gdx.files.internal("Tiles/tiles_fase_1/textura_chão.png"));
        floorTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        floorTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        floorRegion = new TextureRegion(floorTexture);

        // carregar 5 variantes de parede
        wallTextures = new Array<>();
        wallTextures.add(new Texture(Gdx.files.internal("Tiles/tiles_fase_1/Parede_center.png")));   // 0
        wallTextures.add(new Texture(Gdx.files.internal("Tiles/tiles_fase_1/Parede_no_left.png")));  // 1
        wallTextures.add(new Texture(Gdx.files.internal("Tiles/tiles_fase_1/Parede_no_right.png"))); // 2
        wallTextures.add(new Texture(Gdx.files.internal("Tiles/tiles_fase_1/Parede_no_up.png")));    // 3
        wallTextures.add(new Texture(Gdx.files.internal("Tiles/tiles_fase_1/Parede_no_down.png")));  // 4

        textureIndices = new int[mapa.mapWidth][mapa.mapHeight];

        // calcular uma vez (chame recomputeTextureIndices() novamente se o mapa mudar)
        recomputeTextureIndices();
    }

    /** Recalcula o índice do tile de parede para cada posição do mapa. */
    public void recomputeTextureIndices() {
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    textureIndices[x][y] = determineWallIndex(x, y);
                } else {
                    textureIndices[x][y] = -1; // não é parede
                }
            }
        }
    }


  private int determineWallIndex(int x, int y) {
        boolean hasLeft  = isWallAt(x - 1, y);
        boolean hasRight = isWallAt(x + 1, y);
        
        // Ajuste para o sistema de coordenadas invertido na renderização
        boolean hasUp    = isWallAt(x, y - 1);  // Visualmente "acima" é y-1 no array
        boolean hasDown  = isWallAt(x, y + 1);  // Visualmente "abaixo" é y+1 no array

        // Se todas as direções estiverem ocupadas -> tile central
        if (hasLeft && hasRight && hasUp && hasDown) {
            return 0; // center
        }

        // Prioridade ajustada para o sistema visual
        if (!hasUp)    return 3; // no_up (visual)
        if (!hasDown)  return 4; // no_down (visual)
        if (!hasLeft)  return 1; // no_left
        if (!hasRight) return 2; // no_right

        // fallback
        return 0;
    }

    /** Considera borda do mapa: OUT_OF_BOUNDS -> false (não é parede). */
    private boolean isWallAt(int x, int y) {
        if (x < 0 || y < 0 || x >= mapa.mapWidth || y >= mapa.mapHeight) return false;
        return mapa.tiles[x][y] == Mapa.PAREDE;
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, float delta) {
        // desenha floor repetido (como antes)
        int mapPixelWidth = mapa.mapWidth * tileSize;
        int mapPixelHeight = mapa.mapHeight * tileSize;
        floorRegion.setRegion(0, 0, mapPixelWidth, mapPixelHeight);
        batch.draw(floorRegion, offsetX, offsetY, mapPixelWidth, mapPixelHeight);

        // desenha paredes com base em textureIndices
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    int idx = textureIndices[x][y];
                    if (idx < 0 || idx >= wallTextures.size) idx = 0; // safety
                    Texture wallTex = wallTextures.get(idx);
                    float renderY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                    batch.draw(wallTex, offsetX + x * tileSize, renderY, tileSize, tileSize);
                }
            }
        }
    }

    public void dispose() {
        floorTexture.dispose();
        for (Texture tex : wallTextures) tex.dispose();
    }
}
