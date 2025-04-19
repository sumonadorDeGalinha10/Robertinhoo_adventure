package io.github.some_example_name.Entities.Inventory;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

public class InventorySlot {
    public int x;
    public int y;
    public Weapon weapon;

    public InventorySlot(int x, int y, Weapon weapon) {
        this.x = x;
        this.y = y;
        this.weapon = weapon;
    }
}