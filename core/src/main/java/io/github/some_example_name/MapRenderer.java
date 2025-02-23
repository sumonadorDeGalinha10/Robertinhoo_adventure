package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import io.github.some_example_name.Camera.Camera;

public class MapRenderer {
    private RayHandler rayHandler;
    private PointLight playerLight;
    private Mapa mapa;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private ProjectileRenderer projectileRenderer;
    
    
    // Usaremos o CameraController para gerenciar a câmera
    private Camera cameraController;
    
    private Animation<TextureRegion> playerAnimation;
    private Animation<TextureRegion> playerAnimationleft;
    private Animation<TextureRegion> playerAnimationRigth;
   
    private float animationTime = 0f;
    
    public static final int TILE_SIZE = 16;
    public float offsetX;
    public float offsetY;
    
    private TileRenderer tileRenderer;
    private Texture spriteSheet;
    private Texture leftSheet;
    private Texture rigthSheet;

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
        
        // Inicializa o CameraController
        cameraController = new Camera();
        
        // Carrega as texturas
        Texture floorTexture = new Texture("Tiles/tile_0028.png");
        Texture wallTexture = new Texture("Tiles/tile_0015.png");
        this.tileRenderer = new TileRenderer(mapa, floorTexture, wallTexture, TILE_SIZE);
        this.projectileRenderer = new ProjectileRenderer(mapa, TILE_SIZE);
        
        spriteSheet = new Texture("rober/NEWRobert-Sheet.png");
        leftSheet  = new Texture("rober/Sprite-0001-Recovered-Sheet.png");
        rigthSheet  = new Texture("rober/Sprite-0001-Recovered-Sheet.png");


        int frameWidth = spriteSheet.getWidth() / 6;
        int frameHeight = spriteSheet.getHeight();
        TextureRegion[] animationFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            animationFrames[i] = new TextureRegion(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }
        int frameWidthRigth = rigthSheet.getWidth() / 11;
        int frameHeightRight = rigthSheet.getHeight();
        int frameWidthLeft = leftSheet.getWidth() / 11;
        int frameHeighLeft = leftSheet.getHeight();
        
        TextureRegion[] animationRigth = new TextureRegion[11];
        for (int i = 0; i < 11; i++) {
            animationRigth[i] = new TextureRegion(
                rigthSheet,
                i * frameWidthRigth,
                0,
                frameWidthRigth,
                frameHeightRight
            );
        }
        
        TextureRegion[] animationleft = new TextureRegion[11];
        for (int i = 0; i < 11; i++) {
            animationleft[i] = new TextureRegion(
                leftSheet,
                i * frameWidthLeft,
                0,
                frameWidthLeft,
                frameHeighLeft
            );
            animationleft[i].flip(true, false);
        }
        
        playerAnimation = new Animation<>(0.3f, animationFrames);
        playerAnimation.setPlayMode(Animation.PlayMode.LOOP);
        playerAnimationleft = new Animation<>(0.1f, animationleft);
        playerAnimationleft.setPlayMode(Animation.PlayMode.LOOP);
        playerAnimationRigth = new Animation<>(0.1f, animationRigth);
        playerAnimationRigth.setPlayMode(Animation.PlayMode.LOOP);
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
        spriteBatch.end();


        for (Enemy enemy : mapa.getEnemies()) {
            enemy.update(delta);
        }
        spriteBatch.begin();
        for (Enemy enemy : mapa.getEnemies()) {
            TextureRegion frame = enemy.getCurrentFrame(delta);
            spriteBatch.draw(
                frame,
                offsetX + enemy.getPosition().x * TILE_SIZE,
                offsetY + enemy.getPosition().y * TILE_SIZE,
                8,
                8
            );
        }

     

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





        renderPlayer(player, delta);

   
        rayHandler.setCombinedMatrix(cameraController.getCamera());
        rayHandler.updateAndRender();

  
       // renderHitboxes(player);

        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);

        player.getWeaponSystem().renderDebug(shapeRenderer);

        spriteBatch.begin();
        player.getWeaponSystem().renderWeapon(spriteBatch,delta);
        spriteBatch.end();
    
   
        //player.getWeaponSystem().logAimInfo();

    
    }
    
    public void renderPlayer(Robertinhoo player, float delta) {
        animationTime += delta;
        TextureRegion currentFrame;
      
        if (player.dir == player.LEFT) {
            currentFrame = playerAnimationleft.getKeyFrame(animationTime);
        } else if (player.dir == player.RIGHT) {
            currentFrame = playerAnimationRigth.getKeyFrame(animationTime);
        } else {
            currentFrame = playerAnimation.getKeyFrame(animationTime);
        }

        spriteBatch.begin();
        spriteBatch.draw(
            currentFrame,
            offsetX + player.bounds.x * TILE_SIZE,
            offsetY + player.bounds.y * TILE_SIZE,
            player.bounds.width * TILE_SIZE,
            player.bounds.height * TILE_SIZE
        );
        spriteBatch.end();
    }

    public void renderHitboxes(Robertinhoo player) {
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        // Desenha retângulos para os inimigos
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);
        for (Enemy enemy : mapa.getEnemies()) {
            float x = offsetX + enemy.getPosition().x * TILE_SIZE;
            float y = offsetY + enemy.getPosition().y * TILE_SIZE;
            shapeRenderer.rect(x, y, 8, 8);
        }
        shapeRenderer.end();

        // Desenha linhas e vetores (por exemplo, para inimigos do tipo Ratinho)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (Enemy enemy : mapa.getEnemies()) {
            if (enemy instanceof Ratinho) {
                Ratinho ratinho = (Ratinho) enemy;
                Vector2 pos = ratinho.getPosition();
                Vector2 targetPos = ratinho.target.getPosition();
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.line(
                    offsetX + pos.x * TILE_SIZE,
                    offsetY + pos.y * TILE_SIZE,
                    offsetX + targetPos.x * TILE_SIZE,
                    offsetY + targetPos.y * TILE_SIZE
                );
                shapeRenderer.setColor(Color.GREEN);
                Vector2 velocity = ratinho.getLinearVelocity().nor().scl(20);
                shapeRenderer.line(
                    offsetX + pos.x * TILE_SIZE,
                    offsetY + pos.y * TILE_SIZE,
                    offsetX + pos.x * TILE_SIZE + velocity.x,
                    offsetY + pos.y * TILE_SIZE + velocity.y
                );
            }
        }
        shapeRenderer.end();

    
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        com.badlogic.gdx.utils.Array<Body> bodies = new com.badlogic.gdx.utils.Array<>();
        mapa.world.getBodies(bodies);
        for (Body body : bodies) {
            Vector2 pos = body.getPosition();
            float x = offsetX + pos.x * TILE_SIZE;
            float y = offsetY + pos.y * TILE_SIZE;
            if (body.getUserData() instanceof String) {
                String userData = (String) body.getUserData();
                if (userData.startsWith("PLAYER")) {
                    shapeRenderer.setColor(Color.GREEN);
                } else if (userData.startsWith("RATINHO")) {
                    shapeRenderer.setColor(Color.RED);
                } else if (userData.startsWith("PAREDE")) {
                    shapeRenderer.setColor(Color.BLUE);
                } else {
                    shapeRenderer.setColor(Color.PURPLE);
                }
            } else {
                shapeRenderer.setColor(Color.WHITE);
            }
            shapeRenderer.circle(x, y, 5);
        }
        shapeRenderer.end();
    }


public void calculateOffsets() {
    // Usar dimensões da viewport (em pixels)
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
        spriteSheet.dispose();
        leftSheet.dispose();
        rigthSheet.dispose();
    }
}
