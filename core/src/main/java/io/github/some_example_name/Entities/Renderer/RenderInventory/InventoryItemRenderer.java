package io.github.some_example_name.Entities.Renderer.RenderInventory;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventorySlot;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Fonts.FontsManager;

import com.badlogic.gdx.math.Vector2;

public class InventoryItemRenderer {
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;
    private final Inventory inventory;
    private final Vector2 position;
    private final int cellSize;


    public InventoryItemRenderer(Inventory inventory, Vector2 position, int cellSize) {
        this.inventory = inventory;
        this.position = position;
        this.cellSize = cellSize;
        this.spriteBatch = new SpriteBatch();
        this.font = FontsManager.createInventoryFont();
    }

    public void renderItems(Item selectedItem, boolean validPlacement, 
                            int cursorGridX, int cursorGridY) {
        spriteBatch.begin();
        
        for (InventorySlot slot : inventory.getSlots()) {
            if (slot.item != null && slot.item != selectedItem) {
                renderItem(slot.item, slot.x, slot.y, 1f, Color.WHITE);
                
                if (slot.item instanceof Ammo) {
                    renderAmmoQuantity((Ammo) slot.item, slot.x, slot.y);
                }
            }
        }

        if (selectedItem != null) {
            renderItem(selectedItem, cursorGridX, cursorGridY, 0.5f, 
                      validPlacement ? Color.GREEN : Color.RED);
        }
        
        spriteBatch.end();
    }

    private void renderItem(Item item, int gridX, int gridY, float alpha, Color tint) {
        TextureRegion icon = item.getIcon();
        int width = item.getGridWidth();
        int height = item.getGridHeight();
        
        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (height - 1) * cellSize;

        float scale = Math.min(
            (width * cellSize) / icon.getRegionWidth(),
            (height * cellSize) / icon.getRegionHeight()
        );
        
        float scaledWidth = icon.getRegionWidth() * scale;
        float scaledHeight = icon.getRegionHeight() * scale;
        float offsetX = (width * cellSize - scaledWidth) / 2;
        float offsetY = (height * cellSize - scaledHeight) / 2;

        Color originalColor = spriteBatch.getColor();
        spriteBatch.setColor(tint.r, tint.g, tint.b, alpha);
        spriteBatch.draw(icon, baseX + offsetX, baseY + offsetY, scaledWidth, scaledHeight);
        spriteBatch.setColor(originalColor);
    }

    private void renderAmmoQuantity(Ammo ammo, int gridX, int gridY) {
        int width = ammo.getGridWidth();
        int height = ammo.getGridHeight();
        
        float baseX = position.x + (gridX * cellSize);
        float baseY = position.y + ((inventory.getGridRows() - 1 - gridY) * cellSize);
        baseY -= (height - 1) * cellSize;
        
        int currentQuantity = ammo.getQuantity();
        float padding = 5;
        float textX = baseX + padding;
        float textY = baseY + padding + font.getCapHeight();

        font.setColor(Color.BLACK);
        font.draw(
            spriteBatch,
            String.valueOf(currentQuantity),
            textX + 1,
            textY - 1
        );

        font.setColor(Color.WHITE);
        font.draw(
            spriteBatch,
            String.valueOf(currentQuantity),
            textX,
            textY
        );
    }

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
    }
}