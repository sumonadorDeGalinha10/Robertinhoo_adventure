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

    private final DashSystem dashSystem;
    private InventoryController inventoryController;
    private float meleeCooldown = 0;

    // Constantes
    public static final float ACCELERATION = 1.5f;
    private boolean canMeleeAttack = true;
    public static final float MELEE_COOLDOWN = 0.5f;
    public static final float MELEE_STAMINA_COST = 30f;
    public static final float ROLL_STAMINA_COST = 40f;

    public PlayerController(Robertinhoo player) {
        this.player = player;
        this.inventoryController = player.inventoryController;
        this.dashSystem = new DashSystem(player);
    }

    public void update(float deltaTime) {
        dashSystem.update(deltaTime);

        // Atualizar cooldown do ataque corpo a corpo
        if (meleeCooldown > 0) {
            meleeCooldown -= deltaTime;
            canMeleeAttack = false;
        } else {
            canMeleeAttack = true;
        }

        if (dashSystem.shouldApplyPostDashImpulse()) {
            dashSystem.applyPostDashImpulse();
        }

        if (!dashSystem.isDashing() && !dashSystem.isApplyingPostDashImpulse()) {
            processMovement();
        }

        processActions(deltaTime);
        player.getStaminaSystem().update(deltaTime);
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

        if (!dashSystem.isDashing()) {
            if (!moveDir.isZero()) {
                moveDir.nor();
                player.state = Robertinhoo.RUN;
                player.body.setLinearVelocity(moveDir.scl(ACCELERATION));
            } else {
                player.state = Robertinhoo.IDLE;
                player.body.setLinearVelocity(0, 0);
            }
        }
        dashSystem.handleDashInput(spacePressed, spaceJustPressed, moveDir);

    }
    // Movimento normal

    private void processActions(float deltaTime) {
        // Pickup/Placement
        if (Gdx.input.isKeyJustPressed(Keys.T)) {
            System.out.println("[DEBUG] Tecla T pressionada");
            if (player.weaponToPickup != null) {
                System.out.println("[DEBUG] Pegando arma: " + player.weaponToPickup);
                player.inventoryController.enterPlacementMode(player.weaponToPickup);
                player.clearWeaponToPickup();
            } else if (player.ammoToPickup != null) {
                System.out.println("[DEBUG] Pegando munição: " + player.ammoToPickup);
                player.inventoryController.enterPlacementMode(player.ammoToPickup);
                player.clearAmmoToPickup();
            }
            else if (player.itemToPickup != null) {
                System.out.println("[DEBUG] Pegando item: " + player.itemToPickup);
                player.inventoryController.enterPlacementMode(player.itemToPickup);
                player.clearItemToPickup();
            } else {
                System.out.println("[DEBUG] Nenhum item para pegar!");
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

    public DashSystem getDashSystem() {
        return dashSystem;
    }
}