package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Enemies.Box2dLocation;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Contact.PlayerItemHandler;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;
import io.github.some_example_name.Entities.Renderer.RenderInventory.RenderInventory;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.MapConfig.MapRenderer;
import io.github.some_example_name.MapConfig.Mapa;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.utils.Timer;

public class Robertinhoo implements ShadowEntity {

    public Body body;
    public static final int RUN = 1;
    public static final int DASH = 2;
    public static final int SPAWN = 3;
    public static final int DYING = 4;
    public static final int DEAD = 5;
    public static final int LEFT = 8;
    public static final int RIGHT = 7;
    public static final int UP = 9;
    public static final int TOP = 1;
    public static final int DOWN = -1;
    public static final int IDLE = 6;
    public static final int TILE_SIZE = 1;

    public static final int NORTH_WEST = 10;
    public static final int NORTH_EAST = 11;
    public static final int SOUTH_WEST = 12;
    public static final int SOUTH_EAST = 13;
    public static final int MELEE_ATTACK = 14;

    private float meleeAttackTime = 0;
    private float meleeAttackDuration = 0.4f;
    public int meleeDirection = DOWN;

    public boolean isTakingDamage = false;
    private float damageTimer = 0f;

    public final MeleeAttackSystem meleeSystem;

    public int state = SPAWN;
    public int dir = IDLE;
    public int lastDir = DOWN;
    public int dashDirection = DOWN;
    private boolean isInvulnerable = false;
    public static boolean IsUsingOneHandWeapon = false;

    public final Mapa map;
    public final Rectangle bounds = new Rectangle();
    public final Vector2 pos = new Vector2();

    private float life = 100;
    private int maxLife = 100;

    public PlayerWeaponSystem weaponSystem;
    private OrthographicCamera camera;
    public Weapon weaponToPickup;
    public Ammo ammoToPickup;
    public Item itemToPickup;

    private Weapon currentWeapon;
    private Inventory inventory;
    private ShapeRenderer shapeRenderer;
    public InventoryController inventoryController;
    private PlayerController playerController;
    private final StaminaSystem staminaSystem;
    private ShadowComponent shadowComponent;
    public Vector2 dashVelocity;
    private PlayerItemHandler itemHandler;

    public Robertinhoo(Mapa map, float x, float y, MapRenderer mapRenderer, PlayerRenderer playerRenderer) {
        this.map = map;
        pos.set(x, y);
        bounds.set(pos.x, pos.y, TILE_SIZE, TILE_SIZE);
        state = SPAWN;
        this.weaponSystem = new PlayerWeaponSystem(this, mapRenderer);
        this.inventory = new Inventory(this);
        shapeRenderer = new ShapeRenderer();
        this.inventoryController = new InventoryController(this, inventory, map);
        this.playerController = new PlayerController(this);
        this.meleeSystem = new MeleeAttackSystem(this);
        this.staminaSystem = new StaminaSystem(100f, 10f, 23f, 0.95f);
        this.shadowComponent = new ShadowComponent(
                25, // Largura
                20, // Altura
                -0.24f,
                0.7f,
                new Color(0.05f, 0.05f, 0.05f, 1) // Cinza
        );

        createBody(x, y);
         this.itemHandler = new PlayerItemHandler(this);

    }

    public void equipWeapon(Weapon weapon) {
        this.currentWeapon = (Weapon) weapon;
        if (weapon.getTipoMao() == Weapon.TipoMao.UMA_MAO) {
            IsUsingOneHandWeapon = true;
        }

    }

    public void unequipWeapon() {
        this.currentWeapon = null;
    }

    public Weapon getCurrentWeapon() {
        if (weaponToPickup instanceof Pistol) {

        }
        return inventory.getEquippedWeapon();
    }

    private void createBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = false;

        body = map.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;
        body.setUserData("PLAYER");
        System.out.println("[DEBUG] Criando corpo do Robertinho em (" + x + ", " + y + ")");
        fixtureDef.filter.categoryBits = Constants.BIT_PLAYER;
       fixtureDef.filter.maskBits = Constants.BIT_OBJECT | Constants.BIT_PLAYER_ATTACK | 
                          Constants.BIT_ENEMY | Constants.BIT_PROJECTILE | 
                          Constants.BIT_ITEM | Constants.BIT_WALL;

        body.createFixture(fixtureDef);
        body.setAngularDamping(2f);
        shape.dispose();
    }

    public void update(float deltaTime) {
        inventoryController.update(deltaTime);
        playerController.update(deltaTime);
        meleeSystem.getParrySystem().update(deltaTime);
        if (isTakingDamage) {
            damageTimer -= deltaTime;
            if (damageTimer <= 0) {
                isTakingDamage = false;
            }
        }

        if (state == MELEE_ATTACK) {
            meleeAttackTime += deltaTime;
            if (meleeAttackTime >= meleeAttackDuration) {
                state = IDLE;
                meleeAttackTime = 0;
            }
        }

        if (inventoryController.isInPlacementMode()) {
            return;
        }

        if (weaponSystem != null) {
            weaponSystem.update(deltaTime);
            applyAimRotation();
        }

        Weapon currentWeapon = getCurrentWeapon();
        if (currentWeapon != null) {
            currentWeapon.update(deltaTime);
            currentWeapon.getCurrentState();
        }

        pos.set(body.getPosition().x - 0.5f, body.getPosition().y - 0.5f);
        bounds.setPosition(pos);

        render(shapeRenderer);
    }

    public void startMeleeAttack() {
        if (state != DASH && state != MELEE_ATTACK) {
            state = MELEE_ATTACK;
            meleeAttackTime = 0;

            if (weaponSystem.isAiming() && getInventory().getEquippedWeapon() != null) {
                float aimAngle = applyAimRotation();
                WeaponAnimations.WeaponDirection weaponDir = DirectionUtils.getDirectionFromAngle(aimAngle);
                meleeDirection = DirectionUtils.convertWeaponDirectionToRobertinhooDirection(weaponDir);
            } else {
                meleeDirection = lastDir;
            }

            meleeSystem.startAttack(meleeDirection);
        }
    }

    public void takeDamage(float damage) {
        if (!isInvulnerable) {
            life = life - damage;
            isTakingDamage = true;
            damageTimer = 0.5f;
            setInvulnerable(true);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    setInvulnerable(false);
                }
            }, 1f);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public InventoryController getInventoryController() {
        return inventoryController;
    }

    public PlayerWeaponSystem getWeaponSystem() {
        return weaponSystem;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void updateCameraViewport(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    public float applyAimRotation() {
        float angle = weaponSystem.getAimAngle();
        if (inventory.getEquippedWeapon() != null) {
            body.setTransform(body.getPosition(), (float) Math.toRadians(angle));
        }
        return angle;
    }

    public void setMapRenderer(MapRenderer mapRenderer) {
        this.weaponSystem = new PlayerWeaponSystem(this, mapRenderer);
    }

    public float getMeleeAttackDuration() {
        return meleeAttackDuration;
    }

    // public void setPlayerRenderer(PlayerRenderer playerRenderer) {
    // this.playerRenderer = playerRenderer;
    // }

    public void setWeaponToPickup(Weapon weapon) {

        this.weaponToPickup = weapon;
    }

    public void clearWeaponToPickup() {
        this.weaponToPickup = null;
    }

    public void setAmmoToPickup(Ammo ammo) {
        this.ammoToPickup = ammo;
    }

    public void clearAmmoToPickup() {
        this.ammoToPickup = null;
    }

    public void setItemToPickup(Item item) {
        this.itemToPickup = item;
    }

    public void clearItemToPickup() {
        this.itemToPickup = null;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.isInvulnerable = invulnerable;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void render(ShapeRenderer shapeRenderer) {

    }

    public Mapa getMap() {
        return map;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public int getMaxLife() {
        return maxLife;
    }

    public float getLife() {
        return life;
    }

    public MeleeAttackSystem getMeleeAttackSystem() {
        return this.meleeSystem;
    }

    public StaminaSystem getStaminaSystem() {
        return staminaSystem;
    }

    @Override
    public ShadowComponent getShadowComponent() {
        return shadowComponent;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public PlayerItemHandler getItemHandler() {
        return itemHandler;
    }

}
