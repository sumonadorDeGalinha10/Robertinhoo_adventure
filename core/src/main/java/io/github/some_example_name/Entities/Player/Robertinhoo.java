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
import io.github.some_example_name.MapRenderer;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Enemies.Box2dLocation;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Entities.Renderer.RenderInventory;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.Item;

public class Robertinhoo implements Steerable<Vector2> {

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

    public int state = SPAWN;
    public int dir = IDLE;
    public int lastDir = DOWN;
    public int dashDirection = DOWN;
    private boolean isInvulnerable = false;
    public static boolean IsUsingOneHandWeapon = false;
    private Item itemToPickup;

    public final Mapa map;
    public final Rectangle bounds = new Rectangle();
    public final Vector2 pos = new Vector2();

    private int life =100;
    private int maxLife = 100;


    public PlayerWeaponSystem weaponSystem;
    private OrthographicCamera camera;
    public Weapon weaponToPickup;
     public  Ammo ammoToPickup;

    private float dashTime = 0;
    private Weapon currentWeapon;
    private Inventory inventory;
    private ShapeRenderer shapeRenderer;

    public InventoryController inventoryController;
    private PlayerController playerController;



    public Robertinhoo(Mapa map, int x, int y, MapRenderer mapRenderer, PlayerRenderer playerRenderer) {
        this.map = map;
        pos.set(x, y);
        bounds.set(pos.x, pos.y, TILE_SIZE, TILE_SIZE);
        state = SPAWN;
        this.weaponSystem = new PlayerWeaponSystem(this, mapRenderer);
        this.inventory = new Inventory(this);
        shapeRenderer = new ShapeRenderer();
        this.inventoryController = new InventoryController(this, inventory, map);
        this.playerController = new PlayerController(this);

        createBody(x, y);

    }

    public void equipWeapon(Weapon weapon) {
        this.currentWeapon =(Weapon) weapon;
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

    private void createBody(int x, int y) {
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

        body.createFixture(fixtureDef);
        body.setAngularDamping(2f);
        shape.dispose();
    }

    public void update(float deltaTime) {
        inventoryController.update(deltaTime);
        playerController.update(deltaTime);

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
        linearVelocity.set(body.getLinearVelocity());
        pos.set(body.getPosition().x - 0.5f, body.getPosition().y - 0.5f);
        bounds.setPosition(pos);
        linearVelocity.set(body.getLinearVelocity());
        angularVelocity = body.getAngularVelocity();
        render(shapeRenderer);
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

    // public void setPlayerRenderer(PlayerRenderer playerRenderer) {
    //     this.playerRenderer = playerRenderer;
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
    private Vector2 linearVelocity = new Vector2();
    private float angularVelocity = 0f;
    private float maxLinearSpeed = 10f;
    private float maxAngularSpeed = 10f;
    private float maxLinearAcceleration = 10f;
    private float maxAngularAcceleration = 10f;
    private boolean tagged = false;

    @Override
    public Vector2 getLinearVelocity() {

        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return angularVelocity;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public float getBoundingRadius() {
        return 0.5f;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(body.getPosition(), orientation);

    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(vector.y, vector.x);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.set((float) Math.cos(angle), (float) Math.sin(angle));
        return outVector;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.1f; // Example value
    }

    @Override
    public void setZeroLinearSpeedThreshold(float threshold) {
        // Implement logic for setting the zero linear speed threshold
    }

    @Override
    public float getOrientation() {
        return body.getAngle(); // Assuming you're using Box2D for physics
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

    public int getLife() {
        return life;
    }

}
