package io.github.some_example_name.MapConfig;

import io.github.some_example_name.Otimizations.WallOtimizations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
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
import java.util.Random;

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
    private List<Rectangle> rooms = new ArrayList<>();

    public PathfindingSystem pathfindingSystem;

    public World world;
    public WallOtimizations agruparParedes;
    public MapGenerator mapGenerator;

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

        // 1. Criar gerador de mapa
        this.mapGenerator = new MapGenerator(50, 50);

        initializeLights();

        // 2. Copiar dados do mapa
        this.mapWidth = mapGenerator.getMapWidth();
        this.mapHeight = mapGenerator.getMapHeight();
        this.tiles = mapGenerator.getTiles();
        this.wallPositions = mapGenerator.getWallPositions();

        // 3. Criar Robertinhoo usando a posição do gerador
        Vector2 worldStartPos = mapGenerator.getWorldStartPosition(mapHeight);
        robertinhoo = new Robertinhoo(
                this,
                worldStartPos.x,
                worldStartPos.y,
                null,
                null);

        // 4. Armazenar a posição inicial em coordenadas de tile
        this.startPosition = mapGenerator.getStartPosition();

        // 5. Criar paredes físicas
        agruparEPCriarParedes();

        // 6. Adicionar entidades (que dependem de robertinhoo)
        addRandomEntities();

        // 7. Configurar listener de colisões
        world.setContactListener(new GameContactListener(robertinhoo));
        generateProceduralMap(mapWidth, mapHeight, mapGenerator);
    }

    private void generateProceduralMap(int width, int height, MapGenerator mapGenerator) {

        this.mapWidth = mapGenerator.getMapWidth();
        this.mapHeight = mapGenerator.getMapHeight();
        this.tiles = mapGenerator.getTiles();
        this.startPosition = mapGenerator.getStartPosition();
        this.wallPositions = mapGenerator.getWallPositions();
        this.rooms = mapGenerator.getRooms();
        addRandomEntities();
        agruparEPCriarParedes();
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

    private void addRandomEntities() {
        Random rand = new Random();
        List<Vector2> validTilePositions = new ArrayList<>();

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (tiles[x][y] == TILE) {
                    if (x != (int) startPosition.x || y != (int) startPosition.y) {
                        validTilePositions.add(new Vector2(x, y));
                    }
                }
            }
        }

        java.util.Collections.shuffle(validTilePositions, rand);

        // 3. Adicionar itens (usando coordenadas de mundo)
        for (int i = 0; i < 3 && i < validTilePositions.size(); i++) {
            Vector2 tilePos = validTilePositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);

            if (rand.nextBoolean()) {
                weapons.add(new Pistol(this, worldPos.x, worldPos.y, robertinhoo.getInventory()));
            } else {
                ammo.add(new Ammo9mm(this, worldPos.x, worldPos.y));
            }
        }

        int ratsAdded = 0;
        for (int i = 0; i < validTilePositions.size() && ratsAdded < 2; i++) {
            Vector2 tilePos = validTilePositions.get(i);

            boolean inRoom = false;
            Rectangle ratRoom = null;

            for (Rectangle room : rooms) {

                if (tilePos.x >= room.x + 1 && tilePos.x < room.x + room.width - 1 &&
                        tilePos.y >= room.y + 1 && tilePos.y < room.y + room.height - 1) {
                    inRoom = true;
                    ratRoom = room;
                    break;
                }
            }

            if (!inRoom) {
                continue;
            }

            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            enemies.add(new Ratinho(this, worldPos.x, worldPos.y, robertinhoo, ratRoom));
            ratsAdded++;

            Gdx.app.log("Mapa", "Rato adicionado na sala: " + ratRoom + " em posição: " + tilePos);
        }
        int castoresAdded = 0;
        for (int i = 8; i < validTilePositions.size() && castoresAdded < 1; i++) {
            Vector2 tilePos = validTilePositions.get(i);

            boolean inRoom = false;
            Rectangle castorRoom = null;

            for (Rectangle room : rooms) {
                if (tilePos.x >= room.x + 1 && tilePos.x < room.x + room.width - 1 &&
                        tilePos.y >= room.y + 1 && tilePos.y < room.y + room.height - 1) {
                    inRoom = true;
                    castorRoom = room;
                    break;
                }
            }

            if (!inRoom) {
                continue;
            }

            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            enemies.add(new Castor(this, worldPos.x, worldPos.y, robertinhoo));
            castoresAdded++;

            Gdx.app.log("Mapa", "Castor adicionado na sala: " + castorRoom + " em posição: " + tilePos);
        }

        for (int i = 8; i < 11 && i < validTilePositions.size(); i++) {
            Vector2 tilePos = validTilePositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            destructibles.add(new Barrel(this, worldPos.x, worldPos.y, null, null));
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Weapon> getWeapons() {
        return weapons;
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
        // Item item = craftIt.next();
        // if (item.isMarkedForRemoval()) {
        // if (item instanceof Polvora) {
        // ((Polvora) item).destroyBody();
        // }
        // craftIt.remove();
        // }
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

    public List<Rectangle> getRooms() {
        return rooms;
    }

    public Rectangle findRoomContaining(Vector2 position) {
        Vector2 tilePos = worldToTile(position);

        for (Rectangle room : rooms) {
            if (room.contains(tilePos)) {
                return room;
            }
        }

        return null;
    }

    public MapGenerator getMapGenerator() {
        return mapGenerator;
    }   
}
