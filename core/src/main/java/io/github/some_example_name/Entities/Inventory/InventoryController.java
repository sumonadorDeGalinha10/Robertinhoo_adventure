package io.github.some_example_name.Entities.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.RenderInventory.InventoryContextMenu;
import io.github.some_example_name.Entities.Renderer.RenderInventory.InventoryContextMenu;

import java.util.List;

import io.github.some_example_name.Mapa;

public class InventoryController {
    private final Robertinhoo player;
    private final Inventory inventory;
    private final Mapa mapa;
    private InventoryMouseController mouseController;

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
    private int cursorGridX = 0;
    private int cursorGridY = 0;
    private int originalGridX, originalGridY;
    public Item selectedItem = null;

    public boolean craftingMode = false;
    private CraftingRecipe selectedRecipe;
    private List<CraftingRecipe> availableRecipes;
    private InventoryContextMenu inventoryContextMenu;

    private Vector2 inventoryPosition;

    public InventoryController(Robertinhoo player, Inventory inventory, Mapa mapa) {
        this.player = player;
        this.inventory = inventory;
        this.mapa = mapa;
        this.mouseController = new InventoryMouseController(this, inventory);

        this.inventoryPosition = new Vector2(50, 50);

    }

    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
            toggleInventory();
        }
        if (placementMode) {
            updatePlacementMode();
            return;
        }
        if (isOpen) {

            if (Gdx.input.isKeyJustPressed(Keys.C)) {
                toggleCraftingMode();
            }
            if (craftingMode) {
                handleCraftingMode();
            } else {
                handleInventorySelection();
            }
        }
    }

    private void toggleCraftingMode() {

        craftingMode = !craftingMode;

        if (craftingMode) {
            availableRecipes = inventory.getAvailableRecipes();

            selectedRecipe = availableRecipes.isEmpty() ? null : availableRecipes.get(0);
        }
    }

    private void handleCraftingMode() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectPreviousRecipe();
        } else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectNextRecipe();
        } else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            executeCraft();
        } else if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            craftingMode = false;
        } else {
        }
    }

    private void selectNextRecipe() {
        if (availableRecipes.isEmpty())
            return;
        int index = availableRecipes.indexOf(selectedRecipe);
        index = (index + 1) % availableRecipes.size();
        selectedRecipe = availableRecipes.get(index);
    }

    private void selectPreviousRecipe() {
        if (availableRecipes.isEmpty())
            return;
        int index = availableRecipes.indexOf(selectedRecipe);
        index = (index - 1 + availableRecipes.size()) % availableRecipes.size();
        selectedRecipe = availableRecipes.get(index);
    }

    private void executeCraft() {
        if (selectedRecipe != null && inventory.craftRecipe(selectedRecipe)) {
            System.out.println("Item craftado com sucesso!");
            craftingMode = false;
        }
    }

    private void handleInventorySelection() {
        boolean isVisible = getContextMenu().isVisible();
        System.out.println("isVisible: " + isVisible);
        if (isVisible)
         return;
        int gridCols = inventory.gridCols;
        int gridRows = inventory.gridRows;

        int maxX = gridCols - 1;
        int maxY = gridRows - 1;

        if (selectedItem != null) {
            maxX = gridCols - selectedItem.getGridWidth();
            maxY = gridRows - selectedItem.getGridHeight();
        }

        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            cursorGridX = Math.max(0, cursorGridX - 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            cursorGridX = Math.min(maxX, cursorGridX + 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            cursorGridY = Math.max(0, cursorGridY - 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            cursorGridY = Math.min(maxY, cursorGridY + 1);
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

    private InputProcessor previousInputProcessor;

    private void toggleInventory() {
        if (placementMode)
            return;

        isOpen = !isOpen;

        if (isOpen) {
            player.state = Robertinhoo.IDLE;
            player.body.setLinearVelocity(Vector2.Zero);

            // Salva o processador atual
            previousInputProcessor = Gdx.input.getInputProcessor();

            // Cria novo multiplexer
            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(mouseController);

            // Mantém o processador anterior se existir
            if (previousInputProcessor != null) {
                multiplexer.addProcessor(previousInputProcessor);
            }

            Gdx.input.setInputProcessor(multiplexer);

            // Reseta estados
            selectedItem = null;
            cursorGridX = 0;
            cursorGridY = 0;
            craftingMode = false;
        } else {
            // Restaura o processador anterior
            Gdx.input.setInputProcessor(previousInputProcessor);

            // Reseta estados
            craftingMode = false;
            selectedItem = null;
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
            placementGridX = Math.min(
                    inventory.gridCols - currentPlacementItem.getGridWidth(),
                    placementGridX + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            placementGridY = Math.min(
                    inventory.gridRows - currentPlacementItem.getGridHeight(),
                    placementGridY + 1);
            updatePlacementValidity();
        }
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            placementGridY = Math.max(0, placementGridY - 1);
            updatePlacementValidity();
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (validPlacement &&
                    placementGridX >= 0 &&
                    placementGridY >= 0 &&
                    placementGridX + currentPlacementItem.getGridWidth() <= inventory.gridCols &&
                    placementGridY + currentPlacementItem.getGridHeight() <= inventory.gridRows) {
                if (itemSelected) {
                    inventory.removeItem(selectedItem);
                    itemSelected = false;

                }

                if (inventory.placeItem(currentPlacementItem, placementGridX, placementGridY)) {

                    if (currentPlacementItem instanceof Weapon) {
                        ((Weapon) currentPlacementItem).destroyBody();
                        mapa.getWeapons().remove(currentPlacementItem);
                    } else if (currentPlacementItem instanceof Ammo) {
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

    public void dropItem(Item item) {
        if (inventory.removeItem(item)) {
            if (item instanceof Weapon && inventory.getEquippedWeapon() == item) {
                inventory.unequipWeapon();
            }
            Vector2 dropPosition = player.getPosition().cpy();

            if (item instanceof Ammo) {
                ((Ammo) item).setMapa(mapa);
            }
            item.setPosition(dropPosition);
            if (item instanceof Weapon) {
                Weapon weapon = (Weapon) item;
                weapon.createBody(dropPosition);
                mapa.getWeapons().add(weapon);
            } else if (item instanceof Ammo) {
                Ammo ammo = (Ammo) item;
                ammo.createBody(dropPosition);
                mapa.getAmmo().add(ammo);
            } else {
                item.createBody(dropPosition);
                mapa.getCraftItems().add(item);
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
                inventory.equipWeapon((Weapon) currentPlacementItem);
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
            } else {
                mapa.getCraftItems().remove(item);
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

    public List<CraftingRecipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public CraftingRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setCursorGridPosition(int x, int y) {
        this.cursorGridX = x;
        this.cursorGridY = y;
    }

    public void setSelectedItem(Item item, int gridX, int gridY) {
        this.selectedItem = item;
        this.originalGridX = gridX;
        this.originalGridY = gridY;
    }

    public void moveSelectedItem(int gridX, int gridY) {
        if (inventory.moveItem(selectedItem, gridX, gridY)) {
            selectedItem = null;
        }
    }

    // InventoryController.java
    public float getInventoryStartX() {
        return inventoryPosition.x;
    }

    public float getInventoryStartY() {
        return inventoryPosition.y;
    }

    public float getCellSize() {
        return 40; // Tamanho da célula (deve ser igual ao usado no render)
    }

    public boolean isInventoryOpen() {
        return isOpen;
    }

    public void setCursorPosition(int x, int y) {
        // Garante que o cursor fique dentro dos limites
        this.cursorGridX = MathUtils.clamp(x, 0, inventory.gridCols - 1);
        this.cursorGridY = MathUtils.clamp(y, 0, inventory.gridRows - 1);
    }

    public void selectItemAtCursor() {
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

    public void startDragging(Item item, int gridX, int gridY) {
        this.selectedItem = item;
        this.originalGridX = gridX;
        this.originalGridY = gridY;
    }

    public void completeDrag(int gridX, int gridY) {
        if (selectedItem != null) {
            if (inventory.moveItem(selectedItem, gridX, gridY)) {
                selectedItem = null;
            }
        }
    }

    public Vector2 getInventoryPosition() {
        return inventoryPosition;
    }

    public void setInventoryPosition(float x, float y) {
        this.inventoryPosition.set(x, y);
    }

    public InventoryMouseController getMouseController() {
        return mouseController;
    }

    public InventoryContextMenu getContextMenu() {
        return inventoryContextMenu;
    }

    public void setContextMenu(InventoryContextMenu contextMenu) {
        this.inventoryContextMenu = contextMenu;
    }

    
}
