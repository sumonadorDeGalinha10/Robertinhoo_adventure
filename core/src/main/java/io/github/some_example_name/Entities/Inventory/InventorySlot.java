package io.github.some_example_name.Entities.Inventory;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

public class InventorySlot {
    public int x, y;
    public Item item;

    public InventorySlot(int x, int y, Item item) {
        this.x = x;
        this.y = y;
        this.item = item;
    }
}