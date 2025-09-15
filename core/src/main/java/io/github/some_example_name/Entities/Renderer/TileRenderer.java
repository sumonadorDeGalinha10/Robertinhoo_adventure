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
    private final Array<Texture> wallTextures;
    private final int tileSize;
    private final int[][] textureIndices; // só usado para paredes

    public TileRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;

        // CARREGA UMA TEXTURA de chão tileable (ex: "Tiles/floor_tile_128.png")
        floorTexture = new Texture(Gdx.files.internal("Tiles/tiles_fase_1/textura_chão .png"));
        floorTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        floorTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        floorRegion = new TextureRegion(floorTexture);

        // paredes (mantém seu mecanismo atual)
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
                    textureIndices[x][y] = 0; // floor não precisa variar aqui
                }
            }
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY, float delta) {
        // ---------- DESENHA O PISO REPETIDO (uma só chamada) ----------
        // Calcula dimensão do mapa em pixels
        int mapPixelWidth = mapa.mapWidth * tileSize;
        int mapPixelHeight = mapa.mapHeight * tileSize;

        // Ajusta a região para repetir exatamente mapPixelWidth x mapPixelHeight
        // ATENÇÃO: setRegion usa pixels da textura repetida — se a região é maior que a textura,
        // a GPU repetirá a imagem.
        floorRegion.setRegion(0, 0, mapPixelWidth, mapPixelHeight);

        // Desenha o floorRegion cobrindo a área inteira do mapa
        // Observe: use os mesmos offsetX/offsetY que você usava como origem.
        batch.draw(floorRegion, offsetX, offsetY, mapPixelWidth, mapPixelHeight);

        // ---------- DESENHA AS PAREDES POR TILE (como antes) ----------
        for (int x = 0; x < mapa.mapWidth; x++) {
            for (int y = 0; y < mapa.mapHeight; y++) {
                float renderY = offsetY + (mapa.mapHeight - 1 - y) * tileSize;
                if (mapa.tiles[x][y] == Mapa.PAREDE) {
                    Texture wallTex = wallTextures.get(textureIndices[x][y]);
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
