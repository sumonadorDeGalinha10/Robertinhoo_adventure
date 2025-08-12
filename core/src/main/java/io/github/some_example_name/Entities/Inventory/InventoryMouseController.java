
package io.github.some_example_name.Entities.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Renderer.RenderInventory.InventoryContextMenu;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class InventoryMouseController implements InputProcessor {
    private final InventoryController controller;
    private final Inventory inventory;

    private boolean dragging = false;
    public boolean rightClickTriggered = false;
    public int rightClickGridX;
    public int rightClickGridY;
    public float rightClickScreenX;
    public float rightClickScreenY;

    public InventoryMouseController(InventoryController controller, Inventory inventory) {
        this.controller = controller;
        this.inventory = inventory;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        boolean isVisible = controller.getContextMenu().isVisible();
        if (isVisible)
            return true;
        if (!controller.isInventoryOpen())
            return false;

        Vector2 gridPos = screenToGrid(screenX, screenY);
        if (gridPos != null) {
            controller.setCursorPosition((int) gridPos.x, (int) gridPos.y);
            return true;
        }

        return false;
    }
   @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Menu visível: tratar cliques de forma especial
        if (controller.getContextMenu().isVisible()) {
            if (button == Buttons.LEFT) {
                // Clique esquerdo: interage com o menu
                Vector2 worldPos = controller.getContextMenu().mouseCursorRenderer.screenToWorld(screenX, screenY);
                return controller.getContextMenu().handleClick(worldPos.x, worldPos.y);
            } 
            else if (button == Buttons.RIGHT) {
                // Clique direito: apenas fecha o menu
                controller.getContextMenu().hide();
                return true;
            }
            return true; // Bloqueia outros botões
        }

        // Menu não visível: comportamento normal
        if (button == Buttons.LEFT) {
            // Comportamento normal para clique esquerdo
            Vector2 gridPos = screenToGrid(screenX, screenY);
            if (gridPos != null) {
                controller.setCursorPosition((int) gridPos.x, (int) gridPos.y);
                
                if (controller.getSelectedItem() == null) {
                    Item item = inventory.getItemAt((int) gridPos.x, (int) gridPos.y);
                    if (item != null) {
                        controller.startDragging(item, (int) gridPos.x, (int) gridPos.y);
                        dragging = true;
                    }
                } else {
                    controller.selectItemAtCursor();
                }
                return true;
            }
        } 
        else if (button == Buttons.RIGHT) {
            // Comportamento para clique direito (abrir menu)
            Vector2 gridPos = screenToGrid(screenX, screenY);
            if (gridPos != null) {
                rightClickTriggered = true;
                rightClickGridX = (int) gridPos.x;
                rightClickGridY = (int) gridPos.y;
                rightClickScreenX = screenX;
                rightClickScreenY = Gdx.graphics.getHeight() - screenY;
                return true;
            }
        }

        return false;
    }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!controller.isInventoryOpen() || !dragging)
            return false;

        Vector2 gridPos = screenToGrid(screenX, screenY);
        if (gridPos != null) {
            controller.setCursorPosition((int) gridPos.x, (int) gridPos.y);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!controller.isInventoryOpen() || button != 0)
            return false;

        if (dragging) {
            Vector2 gridPos = screenToGrid(screenX, screenY);
            if (gridPos != null) {
                controller.completeDrag((int) gridPos.x, (int) gridPos.y);
            }
            dragging = false;
            return true;
        }
        return false;
    }


    public Vector2 screenToGrid(int screenX, int screenY) {

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Obter posição e tamanho do inventário
        float startX = controller.getInventoryStartX();
        float startY = screenHeight - controller.getInventoryStartY(); // Inverter Y
        float cellSize = controller.getCellSize();
        int gridRows = inventory.gridRows;
        int gridCols = inventory.gridCols;
        float inventoryHeight = gridRows * cellSize;

        int gridX = MathUtils.floor((screenX - (startX)) / cellSize);
        int gridY = MathUtils.floor((screenY - (startY - inventoryHeight)) / cellSize);

        gridX = MathUtils.clamp(gridX, 0, gridCols - 1);
        gridY = MathUtils.clamp(gridY, 0, gridRows - 1);

        return new Vector2(gridX, gridY);
    }

    public void renderDebugGridArea(ShapeRenderer shapeRenderer) {
        if (!controller.isInventoryOpen())
            return;

        float startX = controller.getInventoryStartX();
        float startY = controller.getInventoryStartY();
        float cellSize = controller.getCellSize();
        int gridCols = inventory.gridCols;
        int gridRows = inventory.gridRows;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        shapeRenderer.rect(startX, startY, gridCols * cellSize, gridRows * cellSize);

        for (int x = 0; x <= gridCols; x++) {
            float lineX = startX + x * cellSize;
            shapeRenderer.line(lineX, startY, lineX, startY + gridRows * cellSize);
        }
        for (int y = 0; y <= gridRows; y++) {
            float lineY = startY + y * cellSize;
            shapeRenderer.line(startX, lineY, startX + gridCols * cellSize, lineY);
        }

        shapeRenderer.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
}