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
    public static final float ACCELERATION = 1.5f;
    public static final float DASH_DURATION = 0.4f;
    public static final float DASH_COOLDOWN = 1f;
    public static final float DASH_SPEED = 8f;
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
    private PlayerWeaponSystem weaponSystem;
    private OrthographicCamera camera;
    public Weapon weaponToPickup;
     public  Ammo ammoToPickup;

    private float dashTime = 0;
    private float dashCooldownTime = 0;
    private Weapon currentWeapon;
    private Inventory inventory;
    private ShapeRenderer shapeRenderer;

    private InventoryController inventoryController;

    public Robertinhoo(Mapa map, int x, int y, MapRenderer mapRenderer) {
        this.map = map;
        pos.set(x, y);
        bounds.set(pos.x, pos.y, TILE_SIZE, TILE_SIZE);
        state = SPAWN;
        this.weaponSystem = new PlayerWeaponSystem(this, mapRenderer);
        this.inventory = new Inventory(this);
        shapeRenderer = new ShapeRenderer();
        this.inventoryController = new InventoryController(this, inventory, map);

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

        if (dashCooldownTime > 0)
            dashCooldownTime -= deltaTime;

        if (dashTime > 0) {
            dashTime -= deltaTime;

            if (dashTime <= 0) {
                state = IDLE;
                isInvulnerable = false;
                body.setLinearVelocity(0, 0);
            }
        } else {
            processKeys();
        }
        linearVelocity.set(body.getLinearVelocity());
        pos.set(body.getPosition().x - 0.5f, body.getPosition().y - 0.5f);
        bounds.setPosition(pos);
        linearVelocity.set(body.getLinearVelocity());
        angularVelocity = body.getAngularVelocity();
        render(shapeRenderer);
    }

    private void processKeys() {
    Vector2 moveDir = new Vector2();
    boolean wPressed = Gdx.input.isKeyPressed(Keys.W);
    boolean sPressed = Gdx.input.isKeyPressed(Keys.S);
    boolean aPressed = Gdx.input.isKeyPressed(Keys.A);
    boolean dPressed = Gdx.input.isKeyPressed(Keys.D);
    boolean isMoving = false;

    // Verifica combinações de teclas para diagonais primeiro
    if (wPressed && dPressed) { // Noroeste
        dir = NORTH_EAST;
        moveDir.set(1, 1);
        isMoving = true;
          lastDir = NORTH_EAST;
    } else if (wPressed && aPressed) { // Nordeste
        dir = NORTH_WEST;
        moveDir.set(-1, 1);
        isMoving = true;
         lastDir = NORTH_WEST;
    } else if (sPressed && dPressed) { // Sudoeste
        dir = SOUTH_EAST;
        moveDir.set(1, -1);
        isMoving = true;
        lastDir=SOUTH_EAST;
    } else if (sPressed && aPressed) { // Sudeste
        dir = SOUTH_WEST;
        moveDir.set(-1, -1);
        isMoving = true;
        lastDir=SOUTH_WEST;
    } else if (wPressed) { // Cima
        dir = UP;
        moveDir.set(0, 1);
        isMoving = true;
    } else if (sPressed) { // Baixo
        dir = DOWN;
        moveDir.set(0, -1);
        isMoving = true;
    } else if (dPressed) { // Direita
        dir = RIGHT;
        moveDir.set(1, 0);
        isMoving = true;
    } else if (aPressed) { // Esquerda
        dir = LEFT;
        moveDir.set(-1, 0);
        isMoving = true;
    }
    if (isMoving) {
        lastDir = dir;
    } else {
        dir = IDLE;
    }
if (Gdx.input.isKeyPressed(Keys.SPACE) && dashCooldownTime <= 0 && state != DASH) {
    if (!moveDir.isZero()) {
        moveDir.nor();


        Vector2 dashVector = new Vector2(moveDir.x, moveDir.y);
        
 
        state = DASH;
        dashTime = DASH_DURATION;
        dashCooldownTime = DASH_COOLDOWN;
        isInvulnerable = true;
        body.setLinearVelocity(dashVector.scl(DASH_SPEED));
    }
}else if (!moveDir.isZero()) {
            moveDir.nor();
            state = RUN;
            body.setLinearVelocity(moveDir.scl(ACCELERATION));
        } else {
            state = IDLE;
            body.setLinearVelocity(0, 0);
            if (!isMoving) {
                dir = IDLE;
            }
        }
        

        if (Gdx.input.isKeyJustPressed(Keys.T)) {
            if (weaponToPickup != null) {
                inventoryController.enterPlacementMode(weaponToPickup);
                clearWeaponToPickup();
            } else if (ammoToPickup != null) {
                inventoryController.enterPlacementMode(ammoToPickup);
                clearAmmoToPickup();
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.R)) {
        
            if (currentWeapon != null) {
                System.out.println("Tipo da arma: " + currentWeapon.getClass().getSimpleName());
                currentWeapon.reload();
            } 
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Weapon currentWeapon = getCurrentWeapon();
            if (currentWeapon != null) {
                Vector2 firePosition = weaponSystem.getTrueMuzzlePosition();
                Vector2 direction = weaponSystem.getAimDirection().cpy().nor();
                currentWeapon.shoot(firePosition, direction);
            }
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

    public void dispose() {
        shapeRenderer.dispose();
    }

}
