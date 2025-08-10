package io.github.some_example_name.Entities.Renderer.RenderInventory;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.InventoryMouseController;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Fonts.FontsManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class RenderInventory {
    public final Inventory inventory;
    private final ShapeRenderer shapeRenderer;
    public final int cellSize;
    public final Vector2 position;
    private final SpriteBatch spriteBatch;
    private final InventoryGridRenderer gridRenderer;
    private final InventoryItemRenderer itemRenderer;
    private final InventoryCursorRenderer cursorRenderer;
    private final CraftingRenderer craftingRenderer;
    private BitmapFont inventoryFont;
    public final InventoryMouseCursorRenderer mouseCursorRenderer;

    private Item selectedItem;
    private int originalGridX;
    private int originalGridY;
    private int cursorGridX;
    private int cursorGridY;

    private Color backgroundColor = new Color(0.15f, 0.15f, 0.15f, 1); // Cinza escuro
    private Color gridLineColor = new Color(0.9f, 0.9f, 0.9f, 0.6f);

    private Color gridColor = new Color(0.2f, 0.2f, 0.2f, 1);
    private Color validColor = new Color(0, 1, 0, 0.4f);
    private Color invalidColor = new Color(1, 0, 0, 0.4f);
    private Color itemColor = new Color(0.4f, 0.4f, 0.8f, 1);
    private Vector2 offset = new Vector2(50, 50);

    private final InventoryContextMenu contextMenu;

    private Color selectionColor = new Color(1f, 0.8f, 0.3f, 1f);
    private Color hoverColor = new Color(0.6f, 0.8f, 1f, 0.8f);

    private boolean menuOpen = false;
    private Item menuItem = null;
    private float menuScreenX;
    private float menuScreenY;

    public RenderInventory(Inventory inventory, int cellSize, Vector2 startPosition,
            InventoryController inventoryController) {
        this.inventory = inventory;
        this.cellSize = cellSize;
        this.position = startPosition;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.inventoryFont = FontsManager.createInventoryFont();
        this.selectedItem = null;
        this.originalGridX = 0;
        this.originalGridY = 0;
        this.cursorGridX = 0;
        this.cursorGridY = 0;
        this.gridRenderer = new InventoryGridRenderer(inventory, position, cellSize);
        this.itemRenderer = new InventoryItemRenderer(inventory, position, cellSize);
        this.cursorRenderer = new InventoryCursorRenderer(inventory, position, cellSize);
        this.craftingRenderer = new CraftingRenderer(spriteBatch, shapeRenderer, position);
        this.mouseCursorRenderer = new InventoryMouseCursorRenderer(
                inventoryController,
                position,
                cellSize,
                inventory);
        inventoryController.setInventoryPosition(startPosition.x, startPosition.y);
        this.contextMenu = new InventoryContextMenu(cellSize, new InventoryContextMenu.Listener() {
            @Override
            public void onDrop(Item item) {
                System.out.println("Drop -> " + item.getName());
                // chama seu código de drop aqui
                inventoryController.dropItem(item);
            }

            @Override
            public void onMove(Item item) {
                System.out.println("Move -> " + item.getName());
                // iniciar modo mover (por ex. colocar item no cursor)
                // inventoryController.startMove(item);
            }

            @Override
            public void onCraft(Item item) {
                System.out.println("Craft -> " + item.getName());
                // abrir crafting relacionado, ou começar crafting
                // inventoryController.openCraftFor(item);
            }
        },mouseCursorRenderer);
    }

    public void render(Item placementItem,
            int placementX,
            int placementY,
            boolean isValid,
            Item selectedItem,
            int originalGridX,
            int originalGridY,
            int cursorGridX,
            int cursorGridY,
            boolean craftingMode,
            List<CraftingRecipe> availableRecipes,
            CraftingRecipe selectedRecipe) {

        this.selectedItem = selectedItem;
        this.originalGridX = originalGridX;
        this.originalGridY = originalGridY;
        this.cursorGridX = cursorGridX;
        this.cursorGridY = cursorGridY;
        drawBackground();
        gridRenderer.render();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        itemRenderer.renderItems(selectedItem, isValid, cursorGridX, cursorGridY);

        if (placementItem != null) {
            drawPlacement(placementItem, placementX, placementY, isValid);
        }
        mouseCursorRenderer.setFreeCursorMode(contextMenu.isVisible());

        drawSelection();
        shapeRenderer.end();

        if (craftingMode && selectedItem != null) {
            craftingRenderer.render(availableRecipes, selectedRecipe, cursorGridX, cursorGridY, selectedItem);
        }
        checkRightClick();
        Vector2 mouseWorld = getMouseWorldFromScreen();

        contextMenu.render(shapeRenderer, spriteBatch, inventoryFont, mouseWorld.x + cellSize / 2f,
                mouseWorld.y + cellSize / 2f);

        mouseCursorRenderer.renderMouseCursor();

        if (craftingMode && selectedItem != null) {
            craftingRenderer.render(availableRecipes, selectedRecipe, cursorGridX, cursorGridY, selectedItem);
        }

    }

    private void drawItemIcon(TextureRegion icon, int gridX, int gridY, int gridWidth, int gridHeight, float alpha,
            Color tint) {
        // Calcular posição
        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (gridHeight - 1) * cellSize;

        float scale = Math.min(
                (gridWidth * cellSize) / icon.getRegionWidth(),
                (gridHeight * cellSize) / icon.getRegionHeight());
        float scaledWidth = icon.getRegionWidth() * scale;
        float scaledHeight = icon.getRegionHeight() * scale;

        float offsetX = (gridWidth * cellSize - scaledWidth) / 2;
        float offsetY = (gridHeight * cellSize - scaledHeight) / 2;

        Color originalColor = new Color(spriteBatch.getColor());
        spriteBatch.setColor(tint.r, tint.g, tint.b, alpha);
        spriteBatch.draw(
                icon,
                baseX + offsetX,
                baseY + offsetY,
                scaledWidth,
                scaledHeight);
        spriteBatch.setColor(originalColor);
    }

    private void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);

        float totalWidth = inventory.getGridCols() * cellSize;
        float totalHeight = inventory.getGridRows() * cellSize;

        shapeRenderer.rect(
                position.x - 2,
                position.y - 2,
                totalWidth + 4,
                totalHeight + 4);
        shapeRenderer.end();
    }

    private void drawSelection() {
        Object hoveredItem = inventory.getItemAt(cursorGridX, cursorGridY);
        if (hoveredItem != null && hoveredItem != selectedItem) {
            drawItemHoverEffect(hoveredItem, cursorGridX, cursorGridY);
        }
        drawCursor(hoveredItem);
        if (selectedItem != null) {
            drawSelectedItem();
        }
        cursorRenderer.render(selectedItem, originalGridX, originalGridY, cursorGridX, cursorGridY);

    }

    private void drawCursor(Object hoveredItem) {
        int width = 1;
        int height = 1;

        if (hoveredItem instanceof Item) {
            Item item = (Item) hoveredItem;
            width = item.getGridWidth();
            height = item.getGridHeight();
        }

        float baseX = position.x + (cursorGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - cursorGridY) * cellSize);
        baseY -= (height - 1) * cellSize;
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7);
        shapeRenderer.setColor(selectionColor.r, selectionColor.g, selectionColor.b, pulse);

        shapeRenderer.rectLine(
                baseX - 2, baseY + height * cellSize + 2,
                baseX + width * cellSize + 2, baseY + height * cellSize + 2,
                3);

        shapeRenderer.rectLine(
                baseX - 2, baseY - 2,
                baseX + width * cellSize + 2, baseY - 2,
                3);

    }

    private void drawItemHoverEffect(Object item, int gridX, int gridY) {
        int width = item instanceof Weapon ? ((Weapon) item).getGridWidth() : 1;
        int height = item instanceof Weapon ? ((Weapon) item).getGridHeight() : 1;

        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        shapeRenderer.setColor(hoverColor);

        // Linha superior sutil
        shapeRenderer.rectLine(
                baseX, baseY + height * cellSize,
                baseX + width * cellSize, baseY + height * cellSize,
                2);

        // Linha inferior sutil
        shapeRenderer.rectLine(
                baseX, baseY,
                baseX + width * cellSize, baseY,
                2);

    }

    private void drawSelectedItem() {
        int width = selectedItem instanceof Weapon ? ((Weapon) selectedItem).getGridWidth() : 1;
        int height = selectedItem instanceof Weapon ? ((Weapon) selectedItem).getGridHeight() : 1;

        float baseX = position.x + (originalGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - originalGridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);

        float pulse = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.005));
        shapeRenderer.rectLine(
                baseX - 2, baseY + height * cellSize + 2,
                baseX + width * cellSize + 2, baseY + height * cellSize + 2,
                3 * pulse);

        shapeRenderer.rectLine(
                baseX - 2, baseY - 2,
                baseX + width * cellSize + 2, baseY - 2,
                3 * pulse);

    }

    private void drawPlacement(Item item, int x, int y, boolean isValid) {
        int renderY = inventory.getGridRows() - 1 - y - (item.getGridHeight() - 1);

        shapeRenderer.setColor(isValid ? validColor : invalidColor);

        shapeRenderer.rectLine(
                position.x + x * cellSize,
                position.y + renderY * cellSize + item.getGridHeight() * cellSize,
                position.x + (x + item.getGridWidth()) * cellSize,
                position.y + renderY * cellSize + item.getGridHeight() * cellSize,
                3);

        shapeRenderer.rectLine(
                position.x + x * cellSize,
                position.y + renderY * cellSize,
                position.x + (x + item.getGridWidth()) * cellSize,
                position.y + renderY * cellSize,
                3);

        spriteBatch.begin();
        drawItemIcon(
                item.getIcon(),
                x,
                y,
                item.getGridWidth(),
                item.getGridHeight(),
                0.5f,
                isValid ? new Color(1, 1, 1, 0.7f) : new Color(1, 0.5f, 0.5f, 0.7f));
        spriteBatch.end();
    }

    // public void debugRenderInteractionArea() {
    // float startX =
    // mouseCursorRenderer.getInventoryController().getInventoryStartX();
    // float startY =
    // mouseCursorRenderer.getInventoryController().getInventoryStartY();
    // float cellSize = mouseCursorRenderer.getInventoryController().getCellSize();
    // int rows = inventory.gridRows;
    // int cols = inventory.gridCols;

    // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    // shapeRenderer.setColor(Color.MAGENTA);

    // // Desenhar área total
    // shapeRenderer.rect(startX, startY, cols * cellSize, rows * cellSize);

    // // Desenhar células
    // for (int y = 0; y < rows; y++) {
    // for (int x = 0; x < cols; x++) {
    // float cellX = startX + x * cellSize;
    // float cellY = startY + y * cellSize;
    // shapeRenderer.rect(cellX, cellY, cellSize, cellSize);
    // }
    // }
    // shapeRenderer.end();
    // }

    // public void debugRenderInventoryBounds() {
    // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    // shapeRenderer.setColor(Color.RED);

    // float width = inventory.getGridCols() * cellSize;
    // float height = inventory.getGridRows() * cellSize;

    // // Desenhar retângulo ao redor do inventário
    // shapeRenderer.rect(position.x, position.y, width, height);

    // // Desenhar ponto de origem
    // shapeRenderer.setColor(Color.GREEN);
    // shapeRenderer.circle(position.x, position.y, 5);

    // shapeRenderer.end();

    // spriteBatch.begin();
    // inventoryFont.draw(spriteBatch, "Pos: " + position.x + ", " + position.y,
    // position.x, position.y - 20);
    // spriteBatch.end();
    // }

    public void setGridColor(Color color) {
        this.gridColor = color;
    }

    public void setValidColor(Color color) {
        this.validColor = color;
    }

    public void setInvalidColor(Color color) {
        this.invalidColor = color;
    }

    public void setItemColor(Color color) {
        this.itemColor = color;
    }

    private void checkRightClick() {
        InventoryMouseController mouseController = mouseCursorRenderer
                .getInventoryController()
                .getMouseController();

        if (mouseController.rightClickTriggered) {
            mouseController.rightClickTriggered = false;

            handleRightClick(
                    mouseController.rightClickGridX,
                    mouseController.rightClickGridY,
                    mouseController.rightClickScreenX,
                    mouseController.rightClickScreenY);
        }
    }

    public void handleRightClick(int gridX, int gridY, float screenX, float screenY) {
        Item clickedItem = inventory.getItemAt(gridX, gridY);
        if (clickedItem != null) {
            // Calcular posição do centro do item
            float itemCenterX = position.x + (gridX * cellSize) + (clickedItem.getGridWidth() * cellSize / 2f);
            float itemCenterY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize) -
                    (clickedItem.getGridHeight() * cellSize / 2f);

            // Mostrar menu no centro do item
            contextMenu.show(clickedItem, itemCenterX + (clickedItem.getGridWidth() * cellSize / 2f),
                    itemCenterY, clickedItem.getGridWidth());
        } else {
            contextMenu.hide();
        }
    }



    private Vector2 getMouseWorldFromScreen() {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        return mouseCursorRenderer.screenToWorld(screenX, screenY);
    }

    public InventoryContextMenu getContextMenu() {
        return contextMenu;
    }

    public void dispose() {
        inventoryFont.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
        gridRenderer.dispose();
        itemRenderer.dispose();
        cursorRenderer.dispose();
        mouseCursorRenderer.dispose();
    }

}