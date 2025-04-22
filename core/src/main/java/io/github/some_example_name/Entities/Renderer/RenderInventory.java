package io.github.some_example_name.Entities.Renderer;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventorySlot;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import com.badlogic.gdx.math.Vector2;

public class RenderInventory {
    private final Inventory inventory;
    private final ShapeRenderer shapeRenderer;
    private final int cellSize;
    private final Vector2 position;
    private final SpriteBatch spriteBatch;
    
    // Remova o 'final' para permitir modificação
    private Weapon selectedItem;
    private int originalGridX;
    private int originalGridY;
    private int cursorGridX;
    private int cursorGridY;
    
    private Color gridColor = new Color(0.2f, 0.2f, 0.2f, 1);
    private Color validColor = new Color(0, 1, 0, 0.4f);
    private Color invalidColor = new Color(1, 0, 0, 0.4f);
    private Color itemColor = new Color(0.4f, 0.4f, 0.8f, 1);
    private Vector2 offset = new Vector2(50, 50);

    public RenderInventory(Inventory inventory, int cellSize, Vector2 startPosition) {
        this.inventory = inventory;
        this.cellSize = cellSize;
        this.position = startPosition;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        
        // Inicialize com valores padrão
        this.selectedItem = null;
        this.originalGridX = 0;
        this.originalGridY = 0;
        this.cursorGridX = 0;
        this.cursorGridY = 0;
    }

    public void render(Weapon placementWeapon, 
                      int placementX, 
                      int placementY, 
                      boolean isValid,
                      Weapon selectedItem,
                      int originalGridX,
                      int originalGridY,
                      int cursorGridX,
                      int cursorGridY) {
        
        // Atualize os valores a cada renderização
        this.selectedItem = selectedItem;
        this.originalGridX = originalGridX;
        this.originalGridY = originalGridY;
        this.cursorGridX = cursorGridX;
        this.cursorGridY = cursorGridY;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawGrid();
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawItems();
        if (placementWeapon != null) {
            drawPlacement(placementWeapon, placementX, placementY, isValid);
        }
        drawSelection();
        shapeRenderer.end();
    }
    private void drawGrid() {
        shapeRenderer.setColor(gridColor);
        for (int y = 0; y < inventory.getGridRows(); y++) {
            for (int x = 0; x < inventory.getGridCols(); x++) {
                shapeRenderer.rect(
                    position.x + (x * cellSize),
                    position.y + (y * cellSize),
                    cellSize,
                    cellSize
                );
            }
        }
    }
    private void drawItems() {
        spriteBatch.begin();
        // Desenha todos os itens normais
        for (InventorySlot slot : inventory.getSlots()) {
            TextureRegion icon = slot.weapon.getIcon();
            
            // Pula o item selecionado (será desenhado depois)
            if (slot.weapon == selectedItem) continue;
            
            drawWeaponIcon(slot.weapon, slot.x, slot.y, 1f, Color.WHITE);
        }

        // Desenha o item selecionado por último (se houver)
        if (selectedItem != null) {
            drawWeaponIcon(selectedItem, cursorGridX, cursorGridY, 0.5f, Color.RED);
        }
        
        spriteBatch.end();
    }

    private void drawWeaponIcon(Weapon weapon, int gridX, int gridY, float alpha, Color tint) {
        float baseRenderX = position.x + (gridX * cellSize);
        float baseRenderY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseRenderY -= (weapon.getGridHeight() - 1) * cellSize;

        // Aplica transparência e cor
        Color originalColor = new Color(spriteBatch.getColor());
        spriteBatch.setColor(tint.r, tint.g, tint.b, alpha);
        
        spriteBatch.draw(
            weapon.getIcon(),
            baseRenderX,
            baseRenderY,
            weapon.getGridWidth() * cellSize,
            weapon.getGridHeight() * cellSize
        );
        
        // Restaura cor original
        spriteBatch.setColor(originalColor);
    }

    private void drawSelection() {
        // Desenha contorno no item sob o cursor
        Weapon hoveredWeapon = inventory.getWeaponAt(cursorGridX, cursorGridY);
        if (hoveredWeapon != null && hoveredWeapon != selectedItem) {
            drawWeaponOutline(hoveredWeapon, cursorGridX, cursorGridY, new Color(1, 1, 1, 0.7f));
        }

        // Desenha cursor amarelo
        float cursorScreenX = position.x + (cursorGridX * cellSize);
        float cursorScreenY = position.y + ((inventory.getGridRows() - 1 - cursorGridY) * cellSize);
        shapeRenderer.setColor(new Color(1, 1, 0, 0.5f));
        shapeRenderer.rect(cursorScreenX - 2, cursorScreenY - 2, cellSize + 4, cellSize + 4);


        if (selectedItem != null) {
            float originScreenX = position.x + (originalGridX * cellSize);
            float originScreenY = position.y + ((inventory.getGridRows() - 1 - originalGridY) * cellSize);
            drawWeaponOutline(selectedItem, originalGridX, originalGridY, new Color(1, 0, 0, 0.5f));
        }
    }

    private void drawWeaponOutline(Weapon weapon, int gridX, int gridY, Color color) {
        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (weapon.getGridHeight() - 1) * cellSize;

        shapeRenderer.setColor(color);
        shapeRenderer.rect(
            baseX - 2,
            baseY - 2,
            (weapon.getGridWidth() * cellSize) + 4,
            (weapon.getGridHeight() * cellSize) + 4
        );
    }
    private void drawPlacement(Weapon weapon, int x, int y, boolean isValid) {
        int renderY = inventory.getGridRows() - 1 - y - (weapon.getGridHeight() - 1);
        shapeRenderer.setColor(isValid ? validColor : invalidColor);
        shapeRenderer.rect(
            position.x + (x * cellSize) + 1,
            position.y + (renderY * cellSize) + 1,
            (weapon.getGridWidth() * cellSize) - 2,
            (weapon.getGridHeight() * cellSize) - 2
        );
        
        spriteBatch.begin();
        drawWeaponIcon(weapon, x, y, 0.7f, isValid ? Color.GREEN : Color.RED);
        spriteBatch.end();
    }
    public void setGridColor(Color color) { this.gridColor = color; }
    public void setValidColor(Color color) { this.validColor = color; }
    public void setInvalidColor(Color color) { this.invalidColor = color; }
    public void setItemColor(Color color) { this.itemColor = color; }

    public void dispose() {
        shapeRenderer.dispose();
    }
}