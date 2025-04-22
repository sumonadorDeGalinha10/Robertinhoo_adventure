package io.github.some_example_name.Entities.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;


import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;

import io.github.some_example_name.Mapa;

public class InventoryController {
    private final Robertinhoo player;
    private final Inventory inventory;
    private final Mapa mapa;

    private boolean isOpen = false;
    private int selectedSlot = 0;
    private boolean placementMode = false;
    private Weapon currentPlacementWeapon;
    private int placementGridX = 0;
    private int placementGridY = 0;
    private boolean validPlacement = false;

    private int lastPlacementX = 0;
    private int lastPlacementY = 0;

    private boolean itemSelected = false;
    private int originalSlotX, originalSlotY;
    private int cursorGridX = 0;
    private int cursorGridY = 0;
    private int originalGridX, originalGridY;
    private Weapon selectedItem = null;


    public InventoryController(Robertinhoo player, Inventory inventory, Mapa mapa) {
        this.player = player;
        this.inventory = inventory;
        this.mapa = mapa;

    }

    public void update(float deltaTime) {

        if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
            System.out.println("carregando");
            toggleInventory();
        }

        if (isOpen && !placementMode) {
            handleInventorySelection();
        }

        if (placementMode) {
            updatePlacementMode();
            return;
        }

    }
    private void handleInventorySelection() {
        // Movimento do cursor
        int gridCols = inventory.gridCols;
        int gridRows = inventory.gridRows;
        
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            cursorGridX = Math.max(0, cursorGridX - 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            cursorGridX = Math.min(gridCols - 1, cursorGridX + 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            cursorGridY = Math.min(gridRows - 1, cursorGridY + 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            cursorGridY = Math.max(0, cursorGridY - 1);
        }
    
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (selectedItem == null) {
                selectedItem = inventory.getWeaponAt(cursorGridX, cursorGridY);
                if (selectedItem != null) {
                    originalGridX = cursorGridX;
                    originalGridY = cursorGridY;
                }
            } else {
                if (inventory.moveWeapon(selectedItem, cursorGridX, cursorGridY)) {
                    selectedItem = null;
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            if (selectedItem != null) {
                selectedItem.rotate();
                inventory.moveWeapon(selectedItem, cursorGridX, cursorGridY);
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            selectedItem = null;
        }

        if (Gdx.input.isKeyJustPressed(Keys.Q)) {
            if (selectedItem != null) {
                dropItem(selectedItem);
                player.IsUsingOneHandWeapon = false;
                selectedItem = null;
            }
        }
    }
    private void rotateItem() {
        if (currentPlacementWeapon != null) {
            currentPlacementWeapon.rotate();
            if (placementGridX + currentPlacementWeapon.getGridWidth() > inventory.gridCols) {
                placementGridX = inventory.gridCols - currentPlacementWeapon.getGridWidth();
            }
            if (placementGridY + currentPlacementWeapon.getGridHeight() > inventory.gridRows) {
                placementGridY = inventory.gridRows - currentPlacementWeapon.getGridHeight();
            }

            updatePlacementValidity();
        }
    }

    public void toggleInventory() {
        if (placementMode)
            return;
        isOpen = !isOpen;
        System.out.println("Inventário " + (isOpen ? "aberto" : "fechado"));
        if (isOpen) {
            player.state = Robertinhoo.IDLE;
            player.body.setLinearVelocity(Vector2.Zero);
            Gdx.input.setInputProcessor(null);
        }
    }

    private void updatePlacementMode() {

        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            rotateItem();
        }

        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            placementGridX = Math.max(0, placementGridX - 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            placementGridX = Math.min(inventory.gridCols - currentPlacementWeapon.getGridWidth(), placementGridX + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            placementGridY = Math.min(inventory.gridRows - currentPlacementWeapon.getGridHeight(), placementGridY + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            placementGridY = Math.max(0, placementGridY - 1);
            updatePlacementValidity();
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (validPlacement) {
                if (itemSelected) {
                    inventory.removeWeapon(selectedItem);
                    itemSelected = false;
                }

                inventory.placeWeapon(currentPlacementWeapon, placementGridX, placementGridY);

                exitPlacementMode(true);
            }
        }
    }

    private void dropItem(Weapon weapon) {
        if (inventory.removeWeapon(weapon)) {
            if (inventory.getEquippedWeapon() == weapon) {
                inventory.unequipWeapon();
            }
            Vector2 dropPosition = player.getPosition().cpy();
            weapon.setPosition(dropPosition);
            weapon.createBody(dropPosition);
            mapa.getWeapons().add(weapon);
        }
    }

    private void updatePlacementValidity() {
        validPlacement = inventory.canPlaceAt(placementGridX, placementGridY, currentPlacementWeapon);
    }

    private void exitPlacementMode(boolean success) {
        placementMode = false;
        if (success) {
            if (player.weaponToPickup != null) {
                player.weaponToPickup.destroyBody();
                mapa.getWeapons().remove(player.weaponToPickup);
                player.clearWeaponToPickup();
                lastPlacementX = placementGridX;
                lastPlacementY = placementGridY;
            }
            if (currentPlacementWeapon != null) {
                inventory.equipWeapon(currentPlacementWeapon);
            }
        }
        currentPlacementWeapon = null;
    }

    public void enterPlacementMode(Weapon weapon) {
        placementMode = true;
        currentPlacementWeapon = weapon;
        placementGridX = lastPlacementX;
        placementGridY = lastPlacementY;
        updatePlacementValidity();
    }

    public boolean GetIsOpen() {
        return isOpen;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public boolean isInPlacementMode() {
        return placementMode;
    }

    public Weapon getCurrentPlacementWeapon() {
        return currentPlacementWeapon;
    }

    public int getPlacementGridX() {
        return placementGridX;
    }

    public int getPlacementGridY() {
        return placementGridY;
    }

    public boolean isValidPlacement() {
        return validPlacement;
    }


    public Weapon getSelectedItem() {
        return selectedItem;
    }

    public int getOriginalGridX() {
        return originalGridX;
    }

    public int getOriginalGridY() {
        return originalGridY;
    }

    public int getCursorGridX() {
        return cursorGridX;
    }

    public int getCursorGridY() {
        return cursorGridY;
    }

}
