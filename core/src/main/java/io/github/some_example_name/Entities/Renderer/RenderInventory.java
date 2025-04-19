package io.github.some_example_name.Entities.Renderer;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventorySlot;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import com.badlogic.gdx.math.Vector2;

public class RenderInventory {
    private final Inventory inventory;
    private final ShapeRenderer shapeRenderer;
    private final int cellSize;
    private final Vector2 position;

    // Cores ajustadas para melhor visibilidade
    private Color gridColor = new Color(0.2f, 0.2f, 0.2f, 1); // Cinza escuro
    private Color validColor = new Color(0, 1, 0, 0.4f);       // Verde semi-transparente
    private Color invalidColor = new Color(1, 0, 0, 0.4f);     // Vermelho semi-transparente
    private Color itemColor = new Color(0.4f, 0.4f, 0.8f, 1);  // Azul escuro para itens

    public RenderInventory(Inventory inventory, int cellSize, Vector2 startPosition) {
        this.inventory = inventory;
        this.cellSize = cellSize;
        this.position = startPosition;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(Weapon placementWeapon, int placementX, int placementY, boolean isValid) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawGrid();
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawItems();
        if (placementWeapon != null) {
            drawPlacement(placementWeapon, placementX, placementY, isValid);
        }
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
        shapeRenderer.setColor(itemColor);
        for (InventorySlot slot : inventory.getSlots()) {
            shapeRenderer.rect(
                position.x + (slot.x * cellSize) + 1, // +1 para margem interna
                position.y + (slot.y * cellSize) + 1,
                (slot.weapon.getGridWidth() * cellSize) - 2,
                (slot.weapon.getGridHeight() * cellSize) - 2
            );
        }
    }

    private void drawPlacement(Weapon weapon, int x, int y, boolean isValid) {
        shapeRenderer.setColor(isValid ? validColor : invalidColor);
        shapeRenderer.rect(
            position.x + (x * cellSize) + 1,
            position.y + (y * cellSize) + 1,
            (weapon.getGridWidth() * cellSize) - 2,
            (weapon.getGridHeight() * cellSize) - 2
        );
    }

    // Métodos para alterar configurações
    public void setGridColor(Color color) { this.gridColor = color; }
    public void setValidColor(Color color) { this.validColor = color; }
    public void setInvalidColor(Color color) { this.invalidColor = color; }
    public void setItemColor(Color color) { this.itemColor = color; }

    public void dispose() {
        shapeRenderer.dispose();
    }
}