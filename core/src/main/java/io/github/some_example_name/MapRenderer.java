package io.github.some_example_name;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import box2dLight.RayHandler;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;


import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho.DeathType;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ProjectileRenderer;
import io.github.some_example_name.Entities.Renderer.TileRenderer;
import io.github.some_example_name.Entities.Renderer.AmmoRenderer.AmmoRenderer;
import io.github.some_example_name.Entities.Renderer.CorpsesManager.CorpseManager;
import io.github.some_example_name.Entities.Renderer.CraftItensRenderer.CraftItensRenderer;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Rat.RatRenderer;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.DestructibleRenderer;
import io.github.some_example_name.Entities.Renderer.RenderInventory.RenderInventory;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowRenderer;
// import io.github.some_example_name.Entities.Renderer.MeleeAttackRenderer;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Camera.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Stage;

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
    private ShadowRenderer shadowRenderer;
    private CraftItensRenderer craftItensRenderer;
    private CorpseManager corpseManager;

    private DestructibleRenderer destructibleRenderer;
    // private MeleeAttackRenderer meleeAttackRenderer;
    private PointLight debugLight;
    public static final int TILE_SIZE = 16;
    public float offsetX;
    public float offsetY;

    private Stage uiStage;
    private Skin uiSkin;

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
                new Vector2(175, 100), mapa.robertinhoo.getInventoryController());
        mapa.robertinhoo.getInventoryController().setContextMenu(renderInventory.getContextMenu());;

        mapa.robertinhoo.setCamera(cameraController.getCamera());
        this.destructibleRenderer = new DestructibleRenderer(TILE_SIZE);
        this.shadowRenderer = new ShadowRenderer(shapeRenderer);
        this.craftItensRenderer = new CraftItensRenderer(TILE_SIZE);
        this.corpseManager = new CorpseManager();
        // this.meleeAttackRenderer = new MeleeAttackRenderer(mapa.robertinhoo);
    }

    public void render(float delta, Robertinhoo player) {
        // Limpa a tela
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calcula offsets e atualiza câmera
        calculateOffsets();
        cameraController.centerOnPlayer(player, offsetX, offsetY);

        // Configura matrizes de projeção
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);

        // Atualiza física do mundo
        mapa.world.step(delta, 6, 2);

        // Configura luz do jogador
        playerLight.setPosition(
                offsetX + player.pos.x * TILE_SIZE + TILE_SIZE / 2f,
                offsetY + player.pos.y * TILE_SIZE + TILE_SIZE / 2f);

        // --- COLETA DE ENTIDADES PARA SOMBRA ---
        List<ShadowEntity> shadowEntities = new ArrayList<>();
        shadowEntities.add(player); // Jogador

        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof ShadowEntity) {
                shadowEntities.add((ShadowEntity) enemy);
            }
        }

        for (Destructible d : mapa.getDestructibles()) {
            if (d instanceof ShadowEntity) {
                shadowEntities.add((ShadowEntity) d);
            }
        }


        // 1. RENDERIZAÇÃO DO CHÃO (TILES)
        spriteBatch.begin();
        tileRenderer.render(spriteBatch, offsetX, offsetY, delta);
        spriteBatch.end();

        // 2. RENDERIZAÇÃO DAS SOMBRAS
        shadowRenderer.renderShadows(shadowEntities, offsetX, offsetY, TILE_SIZE);

        // 3. RENDERIZAÇÃO DOS OBJETOS E ENTIDADES
        spriteBatch.begin();
        {
            // Renderiza itens do cenário (barris, etc.)
            destructibleRenderer.render(spriteBatch, mapa.getDestructibles(), offsetX, offsetY);

            // Renderiza projéteis
            projectileRenderer.render(spriteBatch, delta, offsetX, offsetY);

            // Renderiza jogador
            float playerX = offsetX + (player.bounds.x * TILE_SIZE) - (playerRenderer.getRenderScale() - 1) * 8;
            float playerY = offsetY + (player.bounds.y * TILE_SIZE) - (playerRenderer.getRenderScale() - 1) * 8;
            corpseManager.render(spriteBatch, offsetX, offsetY);

            playerRenderer.render(spriteBatch, player, delta, offsetX, offsetY);

            // Renderiza arma do jogador
            player.getWeaponSystem().renderWeapon(spriteBatch, delta, player, playerX, playerY);

            // Renderiza inimigos
            for (Enemy enemy : mapa.getEnemies()) {
                if (enemy instanceof Ratinho) {
                    Ratinho rat = (Ratinho) enemy;

                    if (rat.isDead()) {
                        float duration = (rat.getDeathType() == DeathType.MELEE) ? ratRenderer.getMeleeDeathDuration()
                                : ratRenderer.getProjectileDeathDuration();

                        if (rat.isDeathAnimationFinished(duration)) {
                            if (!rat.isMarkedForDestruction()) {

                                TextureRegion corpseTexture = ratRenderer.getCorpseFrame(rat);
                                // boolean flip = ratRenderer.flipou(rat);
                                corpseManager.addCorpse(rat, ratRenderer, corpseTexture);

                                rat.markForDestruction();
                            }
                        } else {
                            // Renderiza animação de morte em andamento
                            ratRenderer.render(spriteBatch, delta, rat, offsetX, offsetY);
                        }
                    } else {
                        // Renderiza rato vivo
                        ratRenderer.render(spriteBatch, delta, rat, offsetX, offsetY);
                    }
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

            ammoRenderer.render(spriteBatch, mapa.getAmmo(), offsetX, offsetY);
            craftItensRenderer.render(spriteBatch, mapa.getCraftItems(), offsetX, offsetY);
        }
        spriteBatch.end();

        // --- DEBUG RENDER ---
        // debugRender(offsetX, offsetY);
        // debugMeleeHitbox(offsetX, offsetY);
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // INÍCIO DO
        // SHAPERENDERER
        // {
        // // Debug do mapa (paredes e caminhos)
        // //mapa.renderDebug(shapeRenderer);

        // // Debug adicional (hitboxes, etc)
        // // debugRender(offsetX, offsetY);
        // // debugMeleeHitbox(offsetX, offsetY);
        // //renderInventory.debugRenderPosition();
        // //renderInventory.debugRenderInteractionArea();
        // }
        // shapeRenderer.end();

        // --- RENDERIZAÇÃO DE FORMAS (MIRA E DEBUG) ---
        // Reconfigura a matriz (importante após renderização de sombras)
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);

        // Renderiza mira apenas se jogador estiver com arma equipada
        if (player.getInventory().getEquippedWeapon() != null) {
            player.getWeaponSystem().renderMiraArma(shapeRenderer);
        }
                // --- RENDERIZAÇÃO DE LUZES ---
        mapa.getRayHandler().setCombinedMatrix(cameraController.getCamera());
        mapa.getRayHandler().updateAndRender();

        // --- RENDERIZAÇÃO DA INTERFACE ---
        if (player.getInventoryController().GetIsOpen()) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
            renderInventory.render(
                    null,
                    0, 0,
                    false,
                    player.getInventoryController().getSelectedItem(),
                    player.getInventoryController().getOriginalGridX(),
                    player.getInventoryController().getOriginalGridY(),
                    player.getInventoryController().getCursorGridX(),
                    player.getInventoryController().getCursorGridY(),
                    player.getInventoryController().getAvailableRecipes(),
                    player.getInventoryController().getSelectedRecipe()

            );
            // renderInventory.debugRenderInventoryBounds();
        }

        if (player.getInventoryController().isInPlacementMode()) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
            renderInventory.render(
                    player.getInventoryController().getCurrentPlacementItem(),
                    player.getInventoryController().getPlacementGridX(),
                    player.getInventoryController().getPlacementGridY(),
                    player.getInventoryController().isValidPlacement(),
                    null,
                    -1, -1,
                    player.getInventoryController().getPlacementGridX(),
                    player.getInventoryController().getPlacementGridY(),
                    player.getInventoryController().getAvailableRecipes(),
                    player.getInventoryController().getSelectedRecipe());
            // renderInventory.debugRenderInventoryBounds();
        } 
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

    private void debugRender(float offsetX, float offsetY) {
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Enemy e : mapa.getEnemies()) {
            if (e instanceof Ratinho) {
                ((Ratinho) e).debugDraw(shapeRenderer, offsetX, offsetY);
            }
        }

        shapeRenderer.end();
    }

    private void debugMeleeHitbox(float offsetX, float offsetY) {
        Body hitbox = mapa.robertinhoo.getMeleeAttackSystem().getMeleeHitboxBody();
        if (hitbox == null)
            return;

        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN); // cor que destaque o ataque

        for (Fixture f : hitbox.getFixtureList()) {
            if (f.getShape() instanceof PolygonShape) {
                PolygonShape poly = (PolygonShape) f.getShape();
                Vector2[] verts = new Vector2[poly.getVertexCount()];
                for (int i = 0; i < poly.getVertexCount(); i++) {
                    Vector2 local = new Vector2();
                    poly.getVertex(i, local);

                    // converte de metros → pixels
                    float worldX = local.x * TILE_SIZE;
                    float worldY = local.y * TILE_SIZE;

                    // aplica posição do body e offset
                    Vector2 bodyPos = hitbox.getPosition();
                    float px = offsetX + bodyPos.x * TILE_SIZE + worldX;
                    float py = offsetY + bodyPos.y * TILE_SIZE + worldY;

                    verts[i] = new Vector2(px, py);
                }
                // desenha arestas
                for (int i = 0; i < verts.length; i++) {
                    Vector2 a = verts[i];
                    Vector2 b = verts[(i + 1) % verts.length];
                    shapeRenderer.line(a, b);
                }
            }
        }

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        tileRenderer.dispose();
        playerRenderer.dispose();
        uiStage.dispose();
        uiSkin.dispose();

    }
}