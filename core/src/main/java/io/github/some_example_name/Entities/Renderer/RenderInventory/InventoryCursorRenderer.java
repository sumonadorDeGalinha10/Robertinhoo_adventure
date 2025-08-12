package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.Item;

public class InventoryCursorRenderer {
    private final ShapeRenderer shapeRenderer;
    private final Inventory inventory;
    private final Vector2 position;
    private final int cellSize;
    
    private Color selectionColor = new Color(1f, 0.8f, 0.3f, 1f);
    private Color hoverColor = new Color(0.6f, 0.8f, 1f, 0.8f);
    
    // Variáveis de estado (serão passadas no render)
    private Item selectedItem;
    private int originalGridX;
    private int originalGridY;
    private int cursorGridX;
    private int cursorGridY;

    public InventoryCursorRenderer(Inventory inventory, Vector2 position, int cellSize) {
        this.inventory = inventory;
        this.position = position;
        this.cellSize = cellSize;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(Item selectedItem, int originalGridX, int originalGridY, 
                       int cursorGridX, int cursorGridY) {
        // Armazenar estado atual
        this.selectedItem = selectedItem;
        this.originalGridX = originalGridX;
        this.originalGridY = originalGridY;
        this.cursorGridX = cursorGridX;
        this.cursorGridY = cursorGridY;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Obtém o item sob o cursor (para hover)
        Item hoveredItem = inventory.getItemAt(cursorGridX, cursorGridY);
        
        if (hoveredItem != null && hoveredItem != selectedItem) {
            drawItemHoverEffect(hoveredItem);
        }
        
        drawCursor();
        
        if (selectedItem != null) {
            drawSelectedItem();
        }
        
        shapeRenderer.end();
    }

    private void drawItemHoverEffect(Item item) {
        int width = item.getGridWidth();
        int height = item.getGridHeight();

        float baseX = position.x + (cursorGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - cursorGridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        shapeRenderer.setColor(hoverColor);

        // Linha superior
        shapeRenderer.rectLine(
            baseX, 
            baseY + height * cellSize,
            baseX + width * cellSize, 
            baseY + height * cellSize,
            2
        );

        // Linha inferior
        shapeRenderer.rectLine(
            baseX, 
            baseY,
            baseX + width * cellSize, 
            baseY,
            2
        );
    }

    private void drawCursor() {
        Item hoveredItem = inventory.getItemAt(cursorGridX, cursorGridY);
        int width = 1;
        int height = 1;

        if (hoveredItem != null) {
            width = hoveredItem.getGridWidth();
            height = hoveredItem.getGridHeight();
        }

        float baseX = position.x + (cursorGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - cursorGridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        // Efeito de pulsação
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7);
        shapeRenderer.setColor(selectionColor.r, selectionColor.g, selectionColor.b, pulse);

        // Linha superior
        shapeRenderer.rectLine(
            baseX - 2, 
            baseY + height * cellSize + 2,
            baseX + width * cellSize + 2, 
            baseY + height * cellSize + 2,
            3
        );

        // Linha inferior
        shapeRenderer.rectLine(
            baseX - 2, 
            baseY - 2,
            baseX + width * cellSize + 2, 
            baseY - 2,
            3
        );
    }

    private void drawSelectedItem() {
        int width = selectedItem.getGridWidth();
        int height = selectedItem.getGridHeight();

        float baseX = position.x + (originalGridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - originalGridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        float pulse = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.005));
        shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);

        shapeRenderer.rectLine(
            baseX - 2, 
            baseY + height * cellSize + 2,
            baseX + width * cellSize + 2, 
            baseY + height * cellSize + 2,
            3 * pulse
        );

        shapeRenderer.rectLine(
            baseX - 2, 
            baseY - 2,
            baseX + width * cellSize + 2, 
            baseY - 2,
            3 * pulse
        );
    }

    public void setSelectionColor(Color color) {
        this.selectionColor = color;
    }

    public void setHoverColor(Color color) {
        this.hoverColor = color;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}