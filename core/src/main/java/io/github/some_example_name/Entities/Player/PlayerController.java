package io.github.some_example_name.Entities.Player;

import java.security.Key;

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
    private float meleeCooldown = 0;

    // Constantes
    public static final float DASH_DURATION = 0.57f;
    public static final float DASH_COOLDOWN = 1f;
    public static final float DASH_SPEED = 3f;
    public static final float ACCELERATION = 1.5f;
    private boolean canMeleeAttack = true;
    public static final float MELEE_COOLDOWN = 0.5f;

    public static final float DASH_STAMINA_COST = 40f;
    public static final float MELEE_STAMINA_COST = 30f;
    public static final float ROLL_STAMINA_COST = 40f;

    private boolean dashKeyWasPressed = false;

    public PlayerController(Robertinhoo player) {
        this.player = player;
        this.inventoryController = player.inventoryController;
    }

    public void update(float deltaTime) {
        if (dashCooldownTime > 0)
            dashCooldownTime -= deltaTime;

        // Atualizar cooldown do ataque corpo a corpo
        if (meleeCooldown > 0) {
            meleeCooldown -= deltaTime;
            canMeleeAttack = false;
        } else {
            canMeleeAttack = true;
        }

        if (dashTime > 0) {
            dashTime -= deltaTime;
            if (dashTime <= 0)
                endDash();
        } else {
            processMovement();
            processActions(deltaTime);
        }
        player.getStaminaSystem().update(deltaTime);
    }

    private void endDash() {
        player.state = Robertinhoo.IDLE;
        player.setInvulnerable(false);
        player.body.setLinearVelocity(0, 0);
    }

    private void processMovement() {
        boolean spacePressed = Gdx.input.isKeyPressed(Keys.SPACE);
        boolean spaceJustPressed = Gdx.input.isKeyJustPressed(Keys.SPACE);
        moveDir.set(0, 0);
        isMoving = false;
        if (player.state == Robertinhoo.MELEE_ATTACK) {
            player.body.setLinearVelocity(0, 0);
            return;
        }

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

        boolean canDash = (spaceJustPressed || (!dashKeyWasPressed && spacePressed)) &&
                dashCooldownTime <= 0 &&
                player.state != Robertinhoo.DASH &&
                !moveDir.isZero();

        if (canDash) {
            if (player.getStaminaSystem().consumeStamina(DASH_STAMINA_COST)) {
                moveDir.nor();
                Vector2 dashVector = new Vector2(moveDir.x, moveDir.y);
                player.state = Robertinhoo.DASH;
                player.dashDirection = player.dir;

                dashTime = DASH_DURATION;
                dashCooldownTime = DASH_COOLDOWN;
                player.setInvulnerable(true);
                player.body.setLinearVelocity(dashVector.scl(DASH_SPEED));

                System.out.println("DASH ACTIVATED");
            } else {
                System.out.println("DASH BLOCKED: Not enough stamina");
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

        dashKeyWasPressed = spacePressed;
    }

    private void processActions(float deltaTime) {
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
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) &&
                canMeleeAttack &&
                player.state != Robertinhoo.DASH &&
                player.state != Robertinhoo.MELEE_ATTACK) {

            // Tenta consumir stamina somente se puder atacar
            if (player.getStaminaSystem().consumeStamina(MELEE_STAMINA_COST)) {
                player.startMeleeAttack();
                meleeCooldown = MELEE_COOLDOWN;
                canMeleeAttack = false;
                System.out.println("MELEE ATTACK ACTIVATED");
            } else {
                System.out.println("MELEE ATTACK BLOCKED: Insufficient stamina");
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