package io.github.some_example_name;

import io.github.some_example_name.Otimizations.WallOtimizations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Contact.GameContactListener;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import com.badlogic.gdx.graphics.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.physics.box2d.World;

import box2dLight.RayHandler;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.Entities.Itens.CraftinItens.Polvora;

import java.util.List;

public class Mapa {

    private List<Enemy> enemies;
    private List<Weapon> weapons;
    private List<Ammo> ammo;
    private List<Polvora> polvoras;
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Destructible> destructibles = new ArrayList<>();
    private List<Item> craftItems = new ArrayList<>();
    private List<Runnable> pendingActions = new ArrayList<>();
   public PathfindingSystem pathfindingSystem;

    public World world;
    public WallOtimizations agruparParedes;

    public static final short CATEGORY_PROJECTILE = 0x0002;
    public static final short MASK_PROJECTILE = ~CATEGORY_PROJECTILE;

    static int TILE = 0x000000; // #000000 (tiles normais)
    static int START = 0xFF0000; // #FF0000 (ponto de início)
    public static int PAREDE = 0x00FFF4; // #00FFF4 (paredes)
    public static int ENEMY = 0X913d77; // #913d77 (inimigos)
    public static int REVOLVER = 0X22ff00; // #22ff00
    public static int AMMO09MM = 0Xffffff; // #FFFFFF
    public static int BARRIL = 0Xff8f00; // #ff8f00

    ArrayList<Vector2> wallPositions = new ArrayList<>();

    public int mapWidth;
    public int mapHeight;

    public int[][] tiles;
    public Vector2 startPosition;

    public Robertinhoo robertinhoo;
    public Ratinho ratinho;
    private RayHandler rayHandler;
    private boolean lightsInitialized = false;

    public void setRayHandler(RayHandler rayHandler) {
        this.rayHandler = rayHandler;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public Mapa() {
        world = new World(new Vector2(0, 0), true);
        enemies = new ArrayList<>();
        weapons = new ArrayList<>();
        ammo = new ArrayList<>();
        polvoras = new ArrayList<>();
        agruparParedes = new WallOtimizations(this);
        this.pathfindingSystem = new PathfindingSystem(this);

        initializeLights();
        try {
            loadImageMap("assets/maps/TesteMap.png");
        } catch (Exception e) {
            Gdx.app.error("Mapa", "Erro crítico: " + e.getMessage());
        }


        world.setContactListener(new GameContactListener(robertinhoo));
    }

    public void initializeLights() {
        if (rayHandler == null) {
            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(0.8f);
            rayHandler.setShadows(true);
            rayHandler.setBlurNum(3);
            lightsInitialized = true;
            Gdx.app.log("Mapa", "RayHandler inicializado com sucesso!");
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    private void loadImageMap(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            mapWidth = image.getWidth();
            mapHeight = image.getHeight();
            tiles = new int[mapWidth][mapHeight];

            Vector2 tempVector = new Vector2();
            boolean spawnEncontrado = false;
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int color = image.getRGB(x, y) & 0xFFFFFF;
                    System.out.println(color);
                    if (color == START) {
                        if (spawnEncontrado) {
                            Gdx.app.error("Mapa", "Mapa tem múltiplos pontos de início (START)!");
                        }
                        startPosition = new Vector2(x, y);
                        robertinhoo = new Robertinhoo(this, x, y, null, null);
                        tiles[x][y] = TILE;
                        spawnEncontrado = true;
                    }
                }
            }

            if (!spawnEncontrado) {
                throw new RuntimeException("Mapa não tem ponto de início (START)!");
            }
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int color = image.getRGB(x, y) & 0xFFFFFF;

                    if (color == PAREDE) {
                        tiles[x][y] = PAREDE;
                        tempVector.set(x, y);
                        wallPositions.add(tempVector.cpy());
                    } else if (color == ENEMY) {
                        Ratinho ratinho = new Ratinho(this, x, y, robertinhoo);
                        enemies.add(ratinho);
                        tiles[x][y] = TILE;
                    } else if (color == REVOLVER) {
                        Pistol pistol = new Pistol(this, x, y, robertinhoo.getInventory());
                        weapons.add(pistol);

                        tiles[x][y] = PAREDE;
                    }

                    else if (color == AMMO09MM) {
                        Ammo9mm ammo9mm = new Ammo9mm(this, x, y);
                        this.ammo.add(ammo9mm);
                        tiles[x][y] = TILE;
                        // PolvoraBruta polvoraBruta = new PolvoraBruta(this, x, y);
                        // polvoras.add(polvoraBruta);
                        // tiles[x][y] = TILE;
                    }

                    else if (color == BARRIL) {
                        Barrel barrel = new Barrel(this, x + 0.5f, y + 0.5f, null, null);
                        destructibles.add(barrel);
                        tiles[x][y] = TILE;
                    }
                }
            }
            agruparEPCriarParedes();
        } catch (IOException e) {
            Gdx.app.error("Mapa", "Erro ao carregar imagem: " + e.getMessage());
        }
    }

    private void agruparEPCriarParedes() {
        List<Rectangle> retangulos = agruparParedes.optimizeWalls(wallPositions);
        for (Rectangle ret : retangulos) {
            createWallBody(ret);
        }
    }

    private void createWallBody(Rectangle ret) {
        float escala = 1.0f;

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyType.StaticBody;
        float posY = (mapHeight - ret.y - ret.height / 2) * escala;
        bodyDef.position.set(
                (ret.x + ret.width / 2) * escala,
                posY);

        Body body = world.createBody(bodyDef);
        body.setUserData("WALL");
    
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                (ret.width / 2) * escala,
                (ret.height / 2) * escala);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Constants.BIT_WALL;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

     public boolean isTileBlocked(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= mapWidth || tileY >= mapHeight) {
            return true;
        }
        return tiles[tileX][tileY] == PAREDE || isPhysicalWallAt(tileX, tileY);
    }
private boolean isPhysicalWallAt(int tileX, int tileY) {
    Vector2 worldPos = tileToWorld(tileX, tileY);
    
    com.badlogic.gdx.utils.Array<Body> bodies = new com.badlogic.gdx.utils.Array<>();
    world.getBodies(bodies);
    
    for (Body body : bodies) {
        if ("WALL".equals(body.getUserData())) {
            Vector2 bodyPos = body.getPosition();
            if (MathUtils.isEqual(bodyPos.x, worldPos.x, 0.5f) && 
                MathUtils.isEqual(bodyPos.y, worldPos.y, 0.5f)) {
                return true;
            }
        }
    }
    return false;
}

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<Ammo> getAmmo() {
        return ammo;
    }

    public void update(float deltaTime) {
        java.util.Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(deltaTime);
            if (p.isMarkedForDestruction()) {
                p.destroy();
                it.remove();
            }
        }

        java.util.Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (enemy instanceof Ratinho) {
                Ratinho rat = (Ratinho) enemy;
                if (rat.isMarkedForDestruction()) {
                    world.destroyBody(rat.getBody());
                    iterator.remove();
                }
            }
        }
        

    for (Destructible d : destructibles) {
        d.update(deltaTime);
    }
    
    java.util.Iterator<Destructible> destructibleIterator = destructibles.iterator();
    while (destructibleIterator.hasNext()) {
        Destructible d = destructibleIterator.next();
        
        if (d instanceof Barrel) {
            Barrel barrel = (Barrel) d;
            
            if (barrel.isBodyMarkedForDestruction()) {
                barrel.destroyBody();
                barrel.setBodyMarkedForDestruction(false);
            }
            
            // Remove após a animação terminar
            if (barrel.isAnimationFinished() && barrel.isDestroyed()) {
                destructibleIterator.remove();
            }
        }
    }
         processPendingActions();

        // java.util.Iterator<Item> craftIt = craftItems.iterator();
        // while (craftIt.hasNext()) {
        //     Item item = craftIt.next();
        //     if (item.isMarkedForRemoval()) {
        //         if (item instanceof Polvora) {
        //             ((Polvora) item).destroyBody();
        //         }
        //         craftIt.remove();
        //     }
        // }
    }
    public void addPendingAction(Runnable action) {
        pendingActions.add(action);
    }
    public void processPendingActions() {
        for (Runnable action : pendingActions) {
            action.run();
        }
        pendingActions.clear();
    }

    public List<Destructible> getDestructibles() {
        return destructibles;
    }

    boolean match(int src, int dst) {
        return src == dst;
    }

    public void addCraftItem(Item item) {
        craftItems.add(item);
    }

    public List<Item> getCraftItems() {
        return craftItems;
    }
    

    public void dispose() {
        if (rayHandler != null) {
            rayHandler.dispose();
        }
    }

        public PathfindingSystem getPathfindingSystem() {
        return pathfindingSystem;
    }

    public Vector2 worldToTile(Vector2 worldPos) {
        return new Vector2(
            (int) Math.floor(worldPos.x),
            mapHeight - 1 - (int) Math.floor(worldPos.y) // Inverte Y
        );
    }

    public Vector2 tileToWorld(int tileX, int tileY) {
        return new Vector2(
            tileX + 0.5f,
            mapHeight - 1 - tileY + 0.5f // Inverte Y
        );
    }

    public void renderDebug(ShapeRenderer renderer) {
    // Desenha tiles bloqueados
    renderer.setColor(Color.RED);
    for (int x = 0; x < mapWidth; x++) {
        for (int y = 0; y < mapHeight; y++) {
            if (isTileBlocked(x, y)) {
                Vector2 worldPos = tileToWorld(x, y);
                renderer.rect(worldPos.x - 0.4f, worldPos.y - 0.4f, 0.8f, 0.8f);
            }
        }
    }
    
    // Desenha caminhos ativos
    renderer.setColor(Color.GREEN);
    for (Enemy enemy : enemies) {
        if (enemy instanceof Ratinho) {
            Ratinho rat = (Ratinho) enemy;
            List<Vector2> path = rat.getCurrentPath();
            if (path != null && !path.isEmpty()) {
                Vector2 prev = rat.getBody().getPosition();
                for (Vector2 point : path) {
                    renderer.line(prev.x, prev.y, point.x, point.y);
                    prev = point;
                }
            }
        }
    }
}


public void checkPlayerItemContacts() {
    for (Item item : craftItems) {
        if (item instanceof PolvoraBruta) {
            PolvoraBruta polvora = (PolvoraBruta) item;
            
            // Verificar proximidade mesmo sem contato físico
            float distance = robertinhoo.getPosition().dst(polvora.getPosition());
            
            // Se estiver dentro do raio de coleta e ainda não registrado
            if (distance < 1.5f && !robertinhoo.getItemHandler().isPlayerTouching(polvora)) {
                robertinhoo.getItemHandler().forceItemContact(polvora);
            }
        }
    }
}
}
