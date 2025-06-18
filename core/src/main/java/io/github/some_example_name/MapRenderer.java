package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import box2dLight.RayHandler;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Enemies.Ratinho;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ProjectileRenderer;
import io.github.some_example_name.Entities.Renderer.RenderInventory;
import io.github.some_example_name.Entities.Renderer.TileRenderer;
import io.github.some_example_name.Entities.Renderer.AmmoRenderer.AmmoRenderer;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.RatRenderer;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Camera.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MapRenderer {
    private RayHandler rayHandler;
    private PointLight playerLight;
    private Mapa mapa;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private ProjectileRenderer projectileRenderer;
    private PlayerRenderer playerRenderer;
    private RatRenderer ratRenderer;
    private Camera cameraController;
    private AmmoRenderer ammoRenderer;
    public RenderInventory renderInventory;
    private PointLight debugLight;
    public static final int TILE_SIZE = 16;
    public float offsetX;
    public float offsetY;

    private TileRenderer tileRenderer;

    public MapRenderer(Mapa mapa) {
        this.mapa = mapa;

        if (mapa.getRayHandler() == null) {
            mapa.initializeLights();
        }

        if (mapa.getRayHandler() == null) {
            Gdx.app.error("MapRenderer", "RayHandler não inicializado!");
            mapa.initializeLights();
        }

        playerLight = new PointLight(mapa.getRayHandler(), 100, Color.BLUE, 15, 0, 0);
        playerLight.setSoft(true);
        playerLight.setSoftnessLength(8f);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        cameraController = new Camera();
        this.tileRenderer = new TileRenderer(mapa, TILE_SIZE);
        this.projectileRenderer = new ProjectileRenderer(mapa, TILE_SIZE);
        this.playerRenderer = new PlayerRenderer(mapa.robertinhoo.getWeaponSystem());
        this.ammoRenderer = new AmmoRenderer(TILE_SIZE);
        ratRenderer = new RatRenderer();
        this.renderInventory = new RenderInventory(
                mapa.robertinhoo.getInventory(),
                64,
                new Vector2(100, 100));

        mapa.robertinhoo.setCamera(cameraController.getCamera());
    }

    public void render(float delta, Robertinhoo player) {
        // Limpa a tela
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calcula offsets e atualiza câmera
        calculateOffsets();
        cameraController.centerOnPlayer(player, offsetX, offsetY);

        // Atualiza física do mundo
        mapa.world.step(delta, 6, 2);

        // Configura luz do jogador
        playerLight.setPosition(
                offsetX + player.pos.x * TILE_SIZE + TILE_SIZE / 2f,
                offsetY + player.pos.y * TILE_SIZE + TILE_SIZE / 2f);

        // --- RENDERIZAÇÃO DE SPRITES ---
        spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
        spriteBatch.begin();
        {
            // Renderiza camadas de tiles
            tileRenderer.render(spriteBatch, offsetX, offsetY, delta);

            // Renderiza projéteis
            projectileRenderer.render(spriteBatch, delta, offsetX, offsetY);

            // Renderiza jogador
            float playerX = offsetX + (player.bounds.x * TILE_SIZE) - (playerRenderer.getRenderScale() - 1) * 8;
            float playerY = offsetY + (player.bounds.y * TILE_SIZE) - (playerRenderer.getRenderScale() - 1) * 8;

            playerRenderer.render(spriteBatch, player, delta, offsetX, offsetY);

            // Renderiza arma do jogador
            player.getWeaponSystem().renderWeapon(spriteBatch, delta, player, playerX, playerY);
            // player.setPlayerRenderer(playerRenderer);

            // Renderiza inimigos
            for (Enemy enemy : mapa.getEnemies()) {
                enemy.update(delta);
                if (enemy instanceof Ratinho) {
                    Ratinho rat = (Ratinho) enemy;
                    boolean flip = rat.getDirectionX() < 0 && rat.getState() == Ratinho.State.RUNNING_HORIZONTAL;
                    TextureRegion frame = ratRenderer.getFrame(rat, flip);

                    spriteBatch.draw(
                            frame,
                            offsetX + (rat.getPosition().x - 0.5f) * TILE_SIZE,
                            offsetY + (rat.getPosition().y - 0.5f) * TILE_SIZE,
                            12, 12);
                }
            }

            // Renderiza armas no chão
            for (Weapon weapon : mapa.getWeapons()) {
                weapon.update(delta);
                TextureRegion frame = weapon.getCurrentFrame(delta);
                float floatY = weapon.getPosition().y * TILE_SIZE + weapon.getFloatOffset();

                spriteBatch.draw(
                        frame,
                        offsetX + weapon.getPosition().x * TILE_SIZE,
                        offsetY + floatY,
                        10, 6);
            }

            // Renderiza munições
            ammoRenderer.render(spriteBatch, mapa.getAmmo(), offsetX, offsetY);
        }
        spriteBatch.end();

        // --- RENDERIZAÇÃO DE FORMAS (MIRA E DEBUG) ---
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);

        // Renderiza mira apenas se jogador estiver com arma equipada
        if (player.getInventory().getEquippedWeapon() != null) {
            player.getWeaponSystem().renderMiraArma(shapeRenderer);
            // Opcional: descomente para debug do ponto de disparo
            // player.getWeaponSystem().renderMuzzleDebug(shapeRenderer);

        }

        // shapeRenderer.begin(ShapeRenderer.ShapeType.Line); // Inicia apenas uma vez
        // {
        //     for (Enemy enemy : mapa.getEnemies()) {
        //         if (enemy instanceof Ratinho) {
        //             Ratinho rat = (Ratinho) enemy;
        //             rat.debugDraw(shapeRenderer);
        //         }
        //     }
        // }
        // shapeRenderer.end();

        // --- RENDERIZAÇÃO DA INTERFACE ---
        if (player.getInventoryController().GetIsOpen()) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            renderInventory.render(
                    null,
                    0, 0,
                    false,
                    player.getInventoryController().getSelectedItem(),
                    player.getInventoryController().getOriginalGridX(),
                    player.getInventoryController().getOriginalGridY(),
                    player.getInventoryController().getCursorGridX(),
                    player.getInventoryController().getCursorGridY());
        }

        if (player.getInventoryController().isInPlacementMode()) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            renderInventory.render(
                    player.getInventoryController().getCurrentPlacementItem(),
                    player.getInventoryController().getPlacementGridX(),
                    player.getInventoryController().getPlacementGridY(),
                    player.getInventoryController().isValidPlacement(),
                    null,
                    -1, -1,
                    player.getInventoryController().getPlacementGridX(),
                    player.getInventoryController().getPlacementGridY());
        }

        // --- RENDERIZAÇÃO DE LUZES ---
        mapa.getRayHandler().setCombinedMatrix(cameraController.getCamera());
        mapa.getRayHandler().updateAndRender();
    }

    public void calculateOffsets() {

        float viewportWidth = cameraController.getCamera().viewportWidth;
        float viewportHeight = cameraController.getCamera().viewportHeight;

        offsetX = (viewportWidth - (mapa.mapWidth * TILE_SIZE)) / 2f;
        offsetY = (viewportHeight - (mapa.mapHeight * TILE_SIZE)) / 2f;
    }

    public void resize(int width, int height) {
        cameraController.resize(width, height);
        calculateOffsets();
    }

    private void debugRender() {
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof Ratinho) {
                Ratinho rat = (Ratinho) enemy;
                rat.debugDraw(shapeRenderer);
            }
        }

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        tileRenderer.dispose();
        playerRenderer.dispose();

    }
}