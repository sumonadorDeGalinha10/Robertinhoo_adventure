package io.github.some_example_name.MapConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import box2dLight.RayHandler;
import io.github.some_example_name.Entities.Enemies.Castor.Castor;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Contact.GameContactListener;
import io.github.some_example_name.Entities.Itens.CraftinItens.Polvora;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.Destructible;
import io.github.some_example_name.Otimizations.MapBorderManager;
import io.github.some_example_name.Otimizations.WallOtimizations;
import io.github.some_example_name.MapConfig.Spawner.BarrelSpawner;

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
    private BarrelSpawner barrelSpawner;

    public static int TILE = 0x000000; // #000000 (tiles normais)
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
        List<Vector2> validRoomPositions = new ArrayList<>();
        for (Rectangle room : rooms) {
            for (int x = (int) room.x + 1; x < room.x + room.width - 1; x++) {
                for (int y = (int) room.y + 1; y < room.y + room.height - 1; y++) {
                    if (tiles[x][y] == TILE) {
                        if (x != (int) startPosition.x || y != (int) startPosition.y) {
                            validRoomPositions.add(new Vector2(x, y));
                        }
                    }
                }
            }
        }

        java.util.Collections.shuffle(validRoomPositions, rand);

        for (int i = 0; i < 3 && i < validRoomPositions.size(); i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);

            if (rand.nextBoolean()) {
                weapons.add(new Pistol(this, worldPos.x, worldPos.y, robertinhoo.getInventory()));
            } else {
                ammo.add(new Ammo9mm(this, worldPos.x, worldPos.y));
            }
        }

        int ratsAdded = 0;
        for (int i = 0; i < validRoomPositions.size() && ratsAdded < 14; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            Rectangle ratRoom = findRoomContainingTile(tilePos);
            enemies.add(new Ratinho(this, worldPos.x, worldPos.y, robertinhoo, ratRoom));
            ratsAdded++;
        }

        int castoresAdded = 0;
        for (int i = 8; i < validRoomPositions.size() && castoresAdded < 4; i++) {
            Vector2 tilePos = validRoomPositions.get(i);
            Vector2 worldPos = tileToWorld((int) tilePos.x, (int) tilePos.y);
            enemies.add(new Castor(this, worldPos.x, worldPos.y, robertinhoo));
            castoresAdded++;
        }

        BarrelSpawner.spawnBarrels(this, 14);

    }

    public Rectangle findRoomContainingTile(Vector2 tilePos) {
        // Gdx.app.log("Mapa", "Procurando sala para tile: " + tilePos);

        for (Rectangle room : rooms) {
            // Gdx.app.log("Mapa", "Verificando sala: " + room);

            // Verificar se o tile está dentro da sala (excluindo as paredes)
            if (tilePos.x >= room.x + 1 && tilePos.x < room.x + room.width - 1 &&
                    tilePos.y >= room.y + 1 && tilePos.y < room.y + room.height - 1) {
                Gdx.app.log("Mapa", "✅ Sala encontrada: " + room);
                return room;
            }
        }

        // Gdx.app.error("Mapa", "❌ Nenhuma sala encontrada para tile: " + tilePos);
        return null;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    private void agruparEPCriarParedes() {
        // 1. Criar bordas do mapa otimizadas (apenas onde necessário)
        MapBorderManager.createOptimizedMapBorders(this);

        // 2. Otimizar e criar paredes internas (salas + corredores)
        List<Rectangle> retangulos = agruparParedes.optimizeWalls(wallPositions);
        for (Rectangle ret : retangulos) {
            createWallBody(ret);
        }

        Gdx.app.log("Mapa", "Sistema de colisão otimizado criado:");
        Gdx.app.log("Mapa", "- Bordas: criadas apenas onde necessário");
        Gdx.app.log("Mapa", "- Paredes internas: " + retangulos.size() + " retângulos otimizados");
    }

    public void createWallBody(Rectangle ret) {
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
                    Gdx.app.log("Mapa", "Ratinho removido do jogo.");
                }
            } else if (enemy instanceof Castor) {
                Castor castor = (Castor) enemy;
                if (castor.isMarkedForDestruction()) {
                    world.destroyBody(castor.getBody());
                    iterator.remove();
                    Gdx.app.log("Mapa", "Castor removido do jogo.");
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
