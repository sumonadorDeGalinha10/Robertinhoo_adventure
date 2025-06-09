package io.github.some_example_name.Entities.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
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
    private Item currentPlacementItem; 
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
    private Item selectedItem = null;


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

                selectedItem = inventory.getItemAt(cursorGridX, cursorGridY);
                if (selectedItem != null) {
                    originalGridX = cursorGridX;
                    originalGridY = cursorGridY;
                }
            } else {
                if (inventory.moveItem(selectedItem, cursorGridX, cursorGridY)) {
                    selectedItem = null;
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            if (selectedItem != null) {
                selectedItem.rotate();
                inventory.moveItem(selectedItem, cursorGridX, cursorGridY);
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
        if (currentPlacementItem != null) {
            currentPlacementItem.rotate();
            int newWidth = currentPlacementItem.getGridWidth();
            int newHeight = currentPlacementItem.getGridHeight();
            
            placementGridX = Math.min(placementGridX, inventory.gridCols - newWidth);
            placementGridY = Math.min(placementGridY, inventory.gridRows - newHeight);
            
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
            placementGridX = Math.min(inventory.gridCols - currentPlacementItem.getGridWidth(), placementGridX + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            placementGridY = Math.min(inventory.gridRows - currentPlacementItem.getGridHeight(), placementGridY + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            placementGridY = Math.max(0, placementGridY - 1);
            updatePlacementValidity();
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (validPlacement) {
                if (itemSelected) {
                    inventory.removeItem(selectedItem);
                    itemSelected = false;
                }

   
                if (inventory.placeItem(currentPlacementItem, placementGridX, placementGridY)) {
                    
                    if (currentPlacementItem instanceof Weapon) {
                        ((Weapon) currentPlacementItem).destroyBody();
                        mapa.getWeapons().remove(currentPlacementItem);
                    }
                    else if (currentPlacementItem instanceof Ammo) {
                        Ammo ammo = (Ammo) currentPlacementItem;
                        ammo.destroyBody();
                        mapa.getAmmo().remove(ammo);
                        System.out.println("Munição " + ammo.getCaliber() + " adicionada ao inventário!");
                    }
                    
                    exitPlacementMode(true);
                }
            }
        }
    
    }

    private void dropItem(Item item) {
        if (inventory.removeItem(item)) {
    
            if (item instanceof Weapon && inventory.getEquippedWeapon() == item) {
                inventory.unequipWeapon();
            }
            Vector2 dropPosition = player.getPosition().cpy();
            
            if (item instanceof Weapon) {
                Weapon weapon = (Weapon) item;
                weapon.setPosition(dropPosition);
                weapon.createBody(dropPosition);
                mapa.getWeapons().add(weapon);
            }
            else if (item instanceof Ammo) {
                Ammo ammo = (Ammo) item;
                ammo.setPosition(dropPosition);
                ammo.createBody(dropPosition);
                mapa.getAmmo().add(ammo);
            }
        }
    }
    private void updatePlacementValidity() {
        validPlacement = inventory.canPlaceAt(placementGridX, placementGridY, currentPlacementItem);
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
            if (currentPlacementItem != null && currentPlacementItem instanceof Weapon) {
                inventory.equipWeapon((Weapon)currentPlacementItem);
            }
        }
        currentPlacementItem = null;
    }

    public void enterPlacementMode(Item item) {
        if (item != null) {
            placementMode = true;
            currentPlacementItem = item;
            
            if (item instanceof Weapon) {
                mapa.getWeapons().remove(item);
            } else if (item instanceof Ammo) {
                mapa.getAmmo().remove(item);
            }
            
            placementGridX = lastPlacementX;
            placementGridY = lastPlacementY;
            updatePlacementValidity();
        }
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

    public Item getCurrentPlacementItem() {
        return currentPlacementItem;
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


    public Item getSelectedItem() {
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
