package io.github.some_example_name.Entities.Itens.Ammo;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.MapConfig.Mapa;

public abstract class Ammo implements Item {
    private String caliber;
    protected int quantity;
    protected Mapa mapa;

    private int maxStack;
    private TextureRegion icon;
    private int gridWidth;
    private int gridHeight;
    private Vector2[] occupiedCells;
    protected Vector2 position;
    private String ammoType;

    public Ammo(String caliber, int quantity, int maxStack, TextureRegion icon, int gridWidth, int gridHeight) {
        this.caliber = caliber;
        this.ammoType = caliber;
        this.maxStack = maxStack;
        this.icon = icon;
        this.quantity = quantity;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.occupiedCells = generateOccupiedCells();
    }

    public boolean canMerge(Ammo other) {
        return this.caliber.equals(other.caliber) && this.quantity < this.maxStack;
    }

    public void merge(Ammo other) {
        if (canMerge(other)) {
            int total = this.quantity + other.quantity;
            this.quantity = Math.min(total, maxStack);
        }
    }

    public void use(int amount) {
        quantity = Math.max(0, quantity - amount);
    }

    public String getCaliber() {
        return caliber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        this.quantity = Math.min(this.quantity + amount, maxStack);
    }

    public TextureRegion getIcon() {
        return icon;
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

    public String getAmmoType() {
        return ammoType;
    }

    public void setQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public void reduceQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }
    
    public void setMapa(Mapa mapa) {
        this.mapa = mapa;
    }
}