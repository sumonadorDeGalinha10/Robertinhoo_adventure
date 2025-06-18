package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

public class PlayerController {
    private final Robertinhoo player;
    private final Vector2 moveDir = new Vector2();
    private boolean isMoving = false;

    private float dashTime = 0;
    private float dashCooldownTime = 0;
    public Weapon weaponToPickup;
    private InventoryController inventoryController;

    // Constantes
    public static final float DASH_DURATION = 0.4f;
    public static final float DASH_COOLDOWN = 1f;
    public static final float DASH_SPEED = 4f;
    public static final float ACCELERATION = 1.5f;

    public PlayerController(Robertinhoo player) {
        this.player = player;
        this.inventoryController = player.inventoryController;
    }

    public void update(float deltaTime) {
        if (dashCooldownTime > 0)
            dashCooldownTime -= deltaTime;

        if (dashTime > 0) {
            dashTime -= deltaTime;
            if (dashTime <= 0)
                endDash();
        } else {
            processMovement();
            processActions();
        }
    }

    private void endDash() {
        player.state = Robertinhoo.IDLE;
        player.setInvulnerable(false);
        player.body.setLinearVelocity(0, 0);
    }

    private void processMovement() {
        moveDir.set(0, 0);
        isMoving = false;

        boolean wPressed = Gdx.input.isKeyPressed(Keys.W);
        boolean sPressed = Gdx.input.isKeyPressed(Keys.S);
        boolean aPressed = Gdx.input.isKeyPressed(Keys.A);
        boolean dPressed = Gdx.input.isKeyPressed(Keys.D);

        // Processa direções
        if (wPressed && dPressed) {
            player.dir = Robertinhoo.NORTH_EAST;
            moveDir.set(1, 1);
            isMoving = true;
            player.lastDir = Robertinhoo.NORTH_EAST;
        } else if (wPressed && aPressed) {
            player.dir = Robertinhoo.NORTH_WEST;
            moveDir.set(-1, 1);
            isMoving = true;
            player.lastDir = Robertinhoo.NORTH_WEST;
        } else if (sPressed && dPressed) {
            player.dir = Robertinhoo.SOUTH_EAST;
            moveDir.set(1, -1);
            isMoving = true;
            player.lastDir = Robertinhoo.SOUTH_EAST;
        } else if (sPressed && aPressed) {
            player.dir = Robertinhoo.SOUTH_WEST;
            moveDir.set(-1, -1);
            isMoving = true;
            player.lastDir = Robertinhoo.SOUTH_WEST;
        } else if (wPressed) {
            player.dir = Robertinhoo.UP;
            moveDir.set(0, 1);
            isMoving = true;
            player.lastDir = Robertinhoo.UP;
        } else if (sPressed) {
            player.dir = Robertinhoo.DOWN;
            moveDir.set(0, -1);
            isMoving = true;
            player.lastDir = Robertinhoo.DOWN;
        } else if (dPressed) {
            player.dir = Robertinhoo.RIGHT;
            moveDir.set(1, 0);
            isMoving = true;
            player.lastDir = Robertinhoo.RIGHT;
        } else if (aPressed) {
            player.dir = Robertinhoo.LEFT;
            moveDir.set(-1, 0);
            isMoving = true;
            player.lastDir = Robertinhoo.LEFT;
        }

        if (isMoving) {
            player.lastDir = player.dir;
        } else {
            player.dir = Robertinhoo.IDLE;
        }

        // Dash
        if (Gdx.input.isKeyPressed(Keys.SPACE) &&
                dashCooldownTime <= 0 &&
                player.state != Robertinhoo.DASH) {

            if (!moveDir.isZero()) {
                moveDir.nor();
                Vector2 dashVector = new Vector2(moveDir.x, moveDir.y);
                player.state = Robertinhoo.DASH;
                dashTime = DASH_DURATION;
                dashCooldownTime = DASH_COOLDOWN;
                player.setInvulnerable(true);
                player.body.setLinearVelocity(dashVector.scl(DASH_SPEED));
            }
        }
        // Movimento normal
        else if (!moveDir.isZero()) {
            moveDir.nor();
            player.state = Robertinhoo.RUN;
            player.body.setLinearVelocity(moveDir.scl(ACCELERATION));
        } else {
            player.state = Robertinhoo.IDLE;
            player.body.setLinearVelocity(0, 0);
        }
    }

    private void processActions() {
        // Pickup/Placement
        if (Gdx.input.isKeyJustPressed(Keys.T)) {
            if (player.weaponToPickup != null) {
                player.inventoryController.enterPlacementMode(player.weaponToPickup);
                player.clearWeaponToPickup();
            } else if (player.ammoToPickup != null) {
                player.inventoryController.enterPlacementMode(player.ammoToPickup);
                player.clearAmmoToPickup();
            }
        }

        // Recarregar
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            Weapon currentWeapon = player.getCurrentWeapon();
            if (currentWeapon != null) {
                currentWeapon.reload();
            }
        }

        // Disparar
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Weapon currentWeapon = player.getCurrentWeapon();
            if (currentWeapon != null) {
                Vector2 firePosition = player.weaponSystem.getTrueMuzzlePosition();
                Vector2 direction = player.weaponSystem.getAimDirection().cpy().nor();
                currentWeapon.shoot(firePosition, direction);
            }
        }
    }
}