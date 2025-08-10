package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.InventoryMouseController;

public class InventoryMouseCursorRenderer {
    private final ShapeRenderer shapeRenderer;
    private final InventoryController inventoryController;
    private final Vector2 position;
    private final int cellSize;
    private final Inventory inventory;
    private boolean freeCursorMode = false; // Novo campo para controlar o modo

    public InventoryMouseCursorRenderer(InventoryController inventoryController,
            Vector2 position,
            int cellSize,
            Inventory inventory

    ) {
        this.inventoryController = inventoryController;
        this.position = position;
        this.cellSize = cellSize;
        this.inventory = inventory;
        this.shapeRenderer = new ShapeRenderer();

    }

    public void renderMouseCursor() {
        if (!inventoryController.isInventoryOpen())
            return;

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        if (freeCursorMode) {
            Vector2 worldPos = screenToWorld(mouseX, mouseY);
            if (worldPos != null) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.circle(worldPos.x, worldPos.y, 4);
                shapeRenderer.end();
            }
        } else {
    
            Vector2 gridPos = inventoryController.getMouseController().screenToGrid(mouseX, mouseY);
            if (gridPos != null) {
                int gridX = (int) gridPos.x;
                int gridY = (int) gridPos.y;
                float baseX = inventoryController.getInventoryPosition().x + (gridX * cellSize);
                float baseY = inventoryController.getInventoryPosition().y +
                        ((inventory.getGridRows() - 1 - gridY) * cellSize);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.rect(baseX, baseY, cellSize, cellSize);
                shapeRenderer.end();
            }
        }
    }

    public Vector2 screenToWorld(int screenX, int screenY) {
        float startX = inventoryController.getInventoryStartX();
        float startY = inventoryController.getInventoryStartY();
        float cellSize = this.cellSize;
        int gridRows = inventory.getGridRows();
        int gridCols = inventory.getGridCols();

        // Altura total do inventário em pixels
        float inventoryHeight = gridRows * cellSize;

        // Converter coordenadas de tela para coordenadas do mundo do inventário
        float worldX = startX + (screenX - startX);
        float worldY = startY + inventoryHeight - (screenY - (Gdx.graphics.getHeight() - startY - inventoryHeight));

        return new Vector2(worldX, worldY);
    }

    public InventoryController getInventoryController() {
        return this.inventoryController;
    }

    public void setFreeCursorMode(boolean freeMode) {
        this.freeCursorMode = freeMode;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}