package io.github.some_example_name.Entities.Itens.CraftinItens;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Item;

public abstract class Polvora implements Item{
    private String qualidade;
    private TextureRegion icon;
    private int gridWidth;
    private int gridHeight;
    private Vector2[] occupiedCells;
    protected Vector2 position;
     public TextureRegion getIcon() {
        return icon;
    }
        public Polvora( String qualidade,TextureRegion icon, int gridWidth, int gridHeight) {
        this.icon = icon;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.occupiedCells = generateOccupiedCells();
    }

    private Vector2[] generateOccupiedCells() {
        Vector2[] cells = new Vector2[gridWidth * gridHeight];
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                cells[y * gridWidth + x] = new Vector2(x, y);
            }
        }
        return cells;
    }

    @Override
    public void rotate() {
        int temp = gridWidth;
        gridWidth = gridHeight;
        gridHeight = temp;
    }

    @Override
    public void setPosition(Vector2 position) {
        this.position = position.cpy();
    }

    public Vector2 getPosition() {
        return position;

    }

    public abstract void createBody(Vector2 position);

    public abstract void destroyBody();

    public Vector2[] getOccupiedCells() {
        return occupiedCells;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public String getQualidade() {
        return qualidade;
    }

}
