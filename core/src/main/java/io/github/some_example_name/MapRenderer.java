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
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Enemies.Ratinho;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ProjectileRenderer;
import io.github.some_example_name.Entities.Renderer.TileRenderer;
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
    
    // Usaremos o CameraController para gerenciar a câmera
    private Camera cameraController;
    
    public static final int TILE_SIZE = 16;
    public float offsetX;
    public float offsetY;
    
    private TileRenderer tileRenderer;

    public MapRenderer(Mapa mapa) {
        this.mapa = mapa;

        // Configuração das luzes
        rayHandler = new RayHandler(mapa.world);
        rayHandler.setAmbientLight(0.7f);
        rayHandler.setShadows(true);
        playerLight = new PointLight(rayHandler, 100, Color.BLUE, 30, 0, 0);
        playerLight.setSoft(true);
        playerLight.setSoftnessLength(2f);
        
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        
        cameraController = new Camera();
        
        Texture floorTexture = new Texture("Tiles/tile_0028.png");
        Texture wallTexture = new Texture("Tiles/tile_0015.png");
        this.tileRenderer = new TileRenderer(mapa, floorTexture, wallTexture, TILE_SIZE);
        this.projectileRenderer = new ProjectileRenderer(mapa, TILE_SIZE);
        this.playerRenderer = new PlayerRenderer();
        
        mapa.robertinhoo.setCamera(cameraController.getCamera());
    }

    public void render(float delta, Robertinhoo player) {
        // Atualiza a posição da câmera centralizando no player
        calculateOffsets(); // Atualiza os offsets
        cameraController.centerOnPlayer(player, offsetX, offsetY);
        spriteBatch.setProjectionMatrix(cameraController.getCamera().combined);
    
        spriteBatch.begin();
        mapa.world.step(delta, 6, 2);
        playerLight.setPosition(
            offsetX + player.pos.x * TILE_SIZE + TILE_SIZE / 2f,
            offsetY + player.pos.y * TILE_SIZE + TILE_SIZE / 2f
        );
    
        tileRenderer.render(spriteBatch, offsetX, offsetY, delta);
        projectileRenderer.render(spriteBatch, delta, offsetX, offsetY);
        playerRenderer.render(spriteBatch, player, delta, offsetX, offsetY);
        for (Enemy enemy : mapa.getEnemies()) {
            enemy.update(delta);
            TextureRegion frame = enemy.getCurrentFrame(delta);
            spriteBatch.draw(
                frame,
                offsetX + enemy.getPosition().x * TILE_SIZE,
                offsetY + enemy.getPosition().y * TILE_SIZE,
                8,
                8
            );
        }
        
      
   
        
        // Render weapons
        for(Weapon weapon : mapa.getWeapons()) {
            TextureRegion frame = weapon.getCurrentFrame(delta);
            spriteBatch.draw(
                frame,
                offsetX + weapon.getPosition().x * TILE_SIZE,
                offsetY + weapon.getPosition().y * TILE_SIZE,
                8, 8
            );
        }
        spriteBatch.end();

        rayHandler.setCombinedMatrix(cameraController.getCamera());
        rayHandler.updateAndRender();

        if (player.getInventory().getEquippedWeapon() != null) {
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            player.getWeaponSystem().renderMiraArma(shapeRenderer);
        }
        spriteBatch.begin();
        player.getWeaponSystem().renderWeapon(spriteBatch, delta);
        spriteBatch.end();
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
    
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        tileRenderer.dispose();
        playerRenderer.dispose();
    }
}