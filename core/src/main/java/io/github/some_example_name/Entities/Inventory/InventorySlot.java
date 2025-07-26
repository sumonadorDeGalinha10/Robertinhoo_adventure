package io.github.some_example_name.Entities.Inventory;


public class InventorySlot {
    public int x, y;
    public Item item;

    public InventorySlot(int x, int y, Item item) {
        this.x = x;
        this.y = y;
        this.item = item;
    }
}