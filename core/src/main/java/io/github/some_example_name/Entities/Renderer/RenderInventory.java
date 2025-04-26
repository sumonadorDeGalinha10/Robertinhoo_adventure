package io.github.some_example_name.Entities.Renderer;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventorySlot;
import io.github.some_example_name.Entities.Inventory.Item;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Fonts.FontsManager;

import com.badlogic.gdx.math.Vector2;

public class RenderInventory {
    private final Inventory inventory;
    private final ShapeRenderer shapeRenderer;
    private final int cellSize;
    private final Vector2 position;
    private final SpriteBatch spriteBatch;
    private BitmapFont inventoryFont;
    
    private Object selectedItem;
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


    private Color selectionColor = new Color(1f, 0.8f, 0.3f, 1f); // Laranja amarelado
private Color hoverColor = new Color(0.6f, 0.8f, 1f, 0.8f);   // Azul claro


    public RenderInventory(Inventory inventory, int cellSize, Vector2 startPosition) {
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
    }

    public void render(Item placementItem, 
                      int placementX, 
                      int placementY, 
                      boolean isValid,
                      Object selectedItem, 
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
        drawBackground(); 


        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawGrid();
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawItems();
        if (placementItem != null) {
            drawPlacement(placementItem, placementX, placementY, isValid);
        }
        drawSelection();
        shapeRenderer.end();
    }
    private void drawGrid() {
        shapeRenderer.setColor(gridLineColor);
     
        for (int y = 0; y <= inventory.getGridRows(); y++) {
            float yPos = position.y + (y * cellSize);
            shapeRenderer.line(
                position.x, 
                yPos, 
                position.x + (inventory.getGridCols() * cellSize), 
                yPos
            );
        }
        
        // Linhas verticais
        for (int x = 0; x <= inventory.getGridCols(); x++) {
            float xPos = position.x + (x * cellSize);
            shapeRenderer.line(
                xPos, 
                position.y, 
                xPos, 
                position.y + (inventory.getGridRows() * cellSize)
            );
        }
    }
private void drawItems() {
    spriteBatch.begin();

    BitmapFont font = new BitmapFont();
    font.getData().setScale(0.7f);
    font.setColor(Color.WHITE);
    

    for (InventorySlot slot : inventory.getSlots()) {
        if(slot.item instanceof Weapon) {
            Weapon weapon = (Weapon) slot.item;
            if (weapon == selectedItem) continue;
            drawItemIcon(weapon.getIcon(), slot.x, slot.y, weapon.getGridWidth(), weapon.getGridHeight(), 1f, Color.WHITE);
        }
        else if(slot.item instanceof Ammo) {
            Ammo ammo = (Ammo) slot.item;
            if (ammo == selectedItem) continue;
       
            drawItemIcon(ammo.getIcon(), slot.x, slot.y, 
                       ammo.getGridWidth(), ammo.getGridHeight(), 
                       1f, Color.WHITE);
            
     
            drawAmmoQuantity(ammo, slot.x, slot.y, 
                           ammo.getGridWidth(), ammo.getGridHeight());
        }
    }


    if (selectedItem != null) {
        float alpha = 0.5f;
        Color tint = Color.RED;
        
        if(selectedItem instanceof Weapon) {
            Weapon weapon = (Weapon) selectedItem;
            drawItemIcon(weapon.getIcon(), cursorGridX, cursorGridY, weapon.getGridWidth(), weapon.getGridHeight(), alpha, tint);
        }
        else if(selectedItem instanceof Ammo) {
            Ammo ammo = (Ammo) selectedItem;
            drawItemIcon(ammo.getIcon(), cursorGridX, cursorGridY, ammo.getGridWidth(), ammo.getGridHeight(), alpha, tint);
        }
    }

    
    
    spriteBatch.end();
    font.dispose();
}
private void drawAmmoQuantity(Ammo ammo, int gridX, int gridY, int gridWidth, int gridHeight) {
  
    float baseRenderX = position.x + (gridX * cellSize);
    float baseRenderY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
    baseRenderY -= (gridHeight - 1) * cellSize;
    int currentQuantity = ammo.getQuantity();

 
    float padding = 5;
    float textX = baseRenderX + padding;
    float textY = baseRenderY + padding + inventoryFont.getCapHeight();


    inventoryFont.setColor(Color.BLACK);
    inventoryFont.draw(
        spriteBatch, 
        String.valueOf(currentQuantity), 
        textX + 1, 
        textY - 1
    );

    inventoryFont.setColor(Color.WHITE);
    inventoryFont.draw(
        spriteBatch, 
        String.valueOf(currentQuantity), 
        textX, 
        textY
    );
}
private void drawItemIcon(TextureRegion icon, int gridX, int gridY, int gridWidth, int gridHeight, float alpha, Color tint) {
    // Calcular posição
    float baseX = position.x + (gridX * cellSize);
    float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
    baseY -= (gridHeight - 1) * cellSize;




    float scale = Math.min(
        (gridWidth * cellSize) / icon.getRegionWidth(),
        (gridHeight * cellSize) / icon.getRegionHeight()
    );
    float scaledWidth = icon.getRegionWidth() * 4;
    float scaledHeight = icon.getRegionHeight() * 3;

    float offsetX = (gridWidth * cellSize - scaledWidth) / 2;
    float offsetY = (gridHeight * cellSize - scaledHeight) / 2;

    Color originalColor = new Color(spriteBatch.getColor());
    spriteBatch.setColor(tint.r, tint.g, tint.b, alpha);
    spriteBatch.draw(
        icon,
        baseX + offsetX, 
        baseY + offsetY,
        scaledWidth,
        scaledHeight
    );
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
        totalHeight + 4
    );
    shapeRenderer.end();
}


    private void drawSelection() {
        // Obtém o item sob o cursor
        Object hoveredItem = inventory.getItemAt(cursorGridX, cursorGridY);
        
        // Desenha o hover effect
        if (hoveredItem != null && hoveredItem != selectedItem) {
            drawItemHoverEffect(hoveredItem, cursorGridX, cursorGridY);
        }
        
    
        drawCursor(hoveredItem);
      
        if (selectedItem != null) {
            drawSelectedItem();
        }
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


    
    // Efeito de pulsação
    float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7);
    shapeRenderer.setColor(selectionColor.r, selectionColor.g, selectionColor.b, pulse);
    
    // Linha superior
    shapeRenderer.rectLine(
        baseX - 2, baseY + height * cellSize + 2,
        baseX + width * cellSize + 2, baseY + height * cellSize + 2,
        3
    );
    
    // Linha inferior
    shapeRenderer.rectLine(
        baseX - 2, baseY - 2,
        baseX + width * cellSize + 2, baseY - 2,
        3
    );
    

}

private void drawItemHoverEffect(Object item, int gridX, int gridY) {
    int width = item instanceof Weapon ? ((Weapon)item).getGridWidth() : 1;
    int height = item instanceof Weapon ? ((Weapon)item).getGridHeight() : 1;
    
    float baseX = position.x + (gridX * cellSize);
    float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
    baseY -= (height - 1) * cellSize;

    
    shapeRenderer.setColor(hoverColor);
    
    // Linha superior sutil
    shapeRenderer.rectLine(
        baseX, baseY + height * cellSize,
        baseX + width * cellSize, baseY + height * cellSize,
        2
    );
    
    // Linha inferior sutil
    shapeRenderer.rectLine(
        baseX, baseY,
        baseX + width * cellSize, baseY,
        2
    );
    
 
}

private void drawSelectedItem() {
    int width = selectedItem instanceof Weapon ? 
               ((Weapon)selectedItem).getGridWidth() : 1;
    int height = selectedItem instanceof Weapon ? 
                ((Weapon)selectedItem).getGridHeight() : 1;
    
    float baseX = position.x + (originalGridX * cellSize);
    float baseY = position.y + ((inventory.getGridRows() - 1 - originalGridY) * cellSize);
    baseY -= (height - 1) * cellSize;

  
    shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
    
    float pulse = (float) Math.abs(Math.sin(System.currentTimeMillis() * 0.005));
    shapeRenderer.rectLine(
        baseX - 2, baseY + height * cellSize + 2,
        baseX + width * cellSize + 2, baseY + height * cellSize + 2,
        3 * pulse
    );
    
    shapeRenderer.rectLine(
        baseX - 2, baseY - 2,
        baseX + width * cellSize + 2, baseY - 2,
        3 * pulse
    );
    
  
}

  
    private void drawPlacement(Item item, int x, int y, boolean isValid) {
        int renderY = inventory.getGridRows() - 1 - y - (item.getGridHeight() - 1);
        
   
        shapeRenderer.setColor(isValid ? validColor : invalidColor);
        

        shapeRenderer.rectLine(
            position.x + x * cellSize, 
            position.y + renderY * cellSize + item.getGridHeight() * cellSize,
            position.x + (x + item.getGridWidth()) * cellSize,
            position.y + renderY * cellSize + item.getGridHeight() * cellSize,
            3
        );

        shapeRenderer.rectLine(
            position.x + x * cellSize, 
            position.y + renderY * cellSize,
            position.x + (x + item.getGridWidth()) * cellSize,
            position.y + renderY * cellSize,
            3
        );
        
   
    

        spriteBatch.begin();
        drawItemIcon(
            item.getIcon(), 
            x, 
            y, 
            item.getGridWidth(), 
            item.getGridHeight(), 
            0.5f, 
            isValid ? new Color(1, 1, 1, 0.7f) : new Color(1, 0.5f, 0.5f, 0.7f)
        );
        spriteBatch.end();
    }
    public void setGridColor(Color color) { this.gridColor = color; }
    public void setValidColor(Color color) { this.validColor = color; }
    public void setInvalidColor(Color color) { this.invalidColor = color; }
    public void setItemColor(Color color) { this.itemColor = color; }

    public void dispose() {
        inventoryFont.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }

    
}