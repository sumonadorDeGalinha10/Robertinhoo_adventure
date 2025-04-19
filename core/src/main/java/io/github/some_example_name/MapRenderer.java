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
import io.github.some_example_name.Entities.Renderer.RenderInventory;
import io.github.some_example_name.Entities.Renderer.TileRenderer;
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
    public RenderInventory renderInventory;
    
    public static final int TILE_SIZE = 16;
    public float offsetX;
    public float offsetY;
    
    private TileRenderer tileRenderer;

    public MapRenderer(Mapa mapa) {
        this.mapa = mapa;
        rayHandler = new RayHandler(mapa.world);
        rayHandler = new RayHandler(mapa.world);
        rayHandler.setAmbientLight(0.9f);
        rayHandler.setShadows(true);
        playerLight = new PointLight(rayHandler, 100, Color.BLUE, 15, 0, 0);
        playerLight.setSoft(true);
        playerLight.setSoftnessLength(8f);
        
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        
        cameraController = new Camera();
        
        
        Texture floorTexture = new Texture("Tiles/tile_0028.png");
        Texture wallTexture = new Texture("Tiles/tile_0015.png");
        this.tileRenderer = new TileRenderer(mapa, floorTexture, wallTexture, TILE_SIZE);
        this.projectileRenderer = new ProjectileRenderer(mapa, TILE_SIZE); 
        this.playerRenderer = new PlayerRenderer(mapa.robertinhoo.getWeaponSystem());
        ratRenderer = new RatRenderer();
        this.renderInventory = new RenderInventory(
            mapa.robertinhoo.getInventory(), 
            32, 
            new Vector2(100, 100)
        );
        
        
        mapa.robertinhoo.setCamera(cameraController.getCamera());
    }

    public void render(float delta, Robertinhoo player) {
      
        calculateOffsets(); 
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
        

        
 
          
            player.getWeaponSystem().renderWeapon(spriteBatch, delta);
            playerRenderer.render(spriteBatch, player, delta, offsetX, offsetY);
        

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
                    12,
                    12
                );
            }
        }

        
        

        for(Weapon weapon : mapa.getWeapons()) {
            TextureRegion frame = weapon.getCurrentFrame(delta);
            spriteBatch.draw(
                frame,
                offsetX + weapon.getPosition().x * TILE_SIZE,
                offsetY + weapon.getPosition().y * TILE_SIZE,
                10, 6
            );
        }
        spriteBatch.end();
        
        if (player.placementMode) {
            // Configurar projeção adequada
            shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
            
            renderInventory.render(
                player.getCurrentPlacementWeapon(),
                player.getPlacementGridX(),
                player.getPlacementGridY(),
                player.isValidPlacement()
            );
        }
    
        rayHandler.setCombinedMatrix(cameraController.getCamera());
        rayHandler.updateAndRender();


        rayHandler.setCombinedMatrix(cameraController.getCamera());
        rayHandler.updateAndRender();

        // if (player.getInventory().getEquippedWeapon() != null) {
        //     shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        //     player.getWeaponSystem().renderMiraArma(shapeRenderer);
        // }

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
        
        for(Enemy enemy : mapa.getEnemies()) {
            if(enemy instanceof Ratinho) {
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