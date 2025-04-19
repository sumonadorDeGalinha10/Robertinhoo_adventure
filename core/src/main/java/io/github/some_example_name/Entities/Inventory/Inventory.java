package io.github.some_example_name.Entities.Inventory;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;

public class Inventory {
    private List<Weapon> weapons = new ArrayList<>();
    private Weapon equippedWeapon;
    private Robertinhoo robertinhoo;
    public int gridCols = 8;
    public int gridRows = 6;
    private List<InventorySlot> slots = new ArrayList<>();
    private boolean[][] grid;
    public int getGridCols() { return gridCols; }
    public int getGridRows() { return gridRows; }

    public List<InventorySlot> getSlots() { return slots; }


    public Inventory(Robertinhoo player) {
        this.robertinhoo = player;
        this.grid = new boolean[gridRows][gridCols];
    }

    public boolean isEquipped(Weapon weapon) {
        return equippedWeapon == weapon;
    }
    
    public void markGrid(int startX, int startY, Weapon weapon, boolean value) {
        for (int y = startY; y < startY + weapon.getGridHeight(); y++) {
            for (int x = startX; x < startX + weapon.getGridWidth(); x++) {
                grid[y][x] = value;
            }
        }
    }

    public Boolean addWeapon(Weapon weapon) {
        int[] position = findAvailablePosition(weapon);
        if (position != null) {
            markGrid(position[0], position[1], weapon, true);
            slots.add(new InventorySlot(position[0], position[1], weapon));
            
            if (equippedWeapon == null) {
                equipWeapon(weapon);
            }
            return true;
        }
        return false;
    }


    private int[] findAvailablePosition(Weapon weapon) {
        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                if (canPlaceAt(x, y, weapon)) {
                    return new int[]{x, y};
                }
            }
        }
        return null;
    }

    public boolean canPlaceAt(int startX, int startY, Weapon weapon) {
        int width = weapon.getGridWidth();
        int height = weapon.getGridHeight();

        if (startX + width > gridCols || startY + height > gridRows) {
            return false;
        }

        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                if (grid[y][x]) return false;
            }
        }
        return true;
    }


    public void equipWeapon(Weapon weapon) {
        this.equippedWeapon = weapon;
        robertinhoo.equipWeapon(weapon);
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public void dropWeapon() {
        if (equippedWeapon != null) {
            Vector2 dropPosition = robertinhoo.getPosition().cpy();
            
            switch (robertinhoo.lastDir) {
                case Robertinhoo.LEFT:
                    dropPosition.x -= 0.1;
                    break;
                case Robertinhoo.RIGHT:
                    dropPosition.x += 0.1;
                    break;
                case Robertinhoo.UP:
                    dropPosition.y += 0.1;
                    break;
                case Robertinhoo.DOWN:
                    dropPosition.y -= 0.1;
                    break;
            }
            equippedWeapon.setPosition(dropPosition);
            equippedWeapon.createBody(dropPosition);
            robertinhoo.map.getWeapons().add(equippedWeapon);
            
            weapons.remove(equippedWeapon);
            equippedWeapon = null;
        }
    }

    public boolean removeWeapon(Weapon weapon) {
        for (InventorySlot slot : new ArrayList<>(slots)) {
            if (slot.weapon == weapon) {
                markGrid(slot.x, slot.y, weapon, false);
                slots.remove(slot);
                if (equippedWeapon == weapon) {
                    equippedWeapon = null;
                }
                return true;
            }
        }
        return false;
    }

    public void dropWeapon2(Weapon weapon, Vector2 dropPosition) {
        if (removeWeapon(weapon)) {
            weapon.setPosition(dropPosition);
            weapon.createBody(dropPosition);
            robertinhoo.map.getWeapons().add(weapon);
        }
    }

    public void placeWeapon(Weapon weapon, int x, int y) {
        if (!canPlaceAt(x, y, weapon)) return;

        for (int iy = y; iy < y + weapon.getGridHeight(); iy++) {
            for (int ix = x; ix < x + weapon.getGridWidth(); ix++) {
                grid[iy][ix] = true;
            }
        }
        weapons.add(weapon);
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }
}