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
    public int gridCols = 6;
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


    public boolean moveWeapon(Weapon weapon, int newX, int newY) {
        boolean wasEquipped = (equippedWeapon == weapon);
        
        removeWeapon(weapon);
        

        boolean success = addWeaponAt(weapon, newX, newY);
        
        if (success && wasEquipped) {
            equipWeapon(weapon);
        }
        
        return success;
    }

        public void unequipWeapon() {
        if (this.equippedWeapon != null) {
            this.equippedWeapon = null;
            if (robertinhoo != null) {
                robertinhoo.unequipWeapon();
            }
        }
    }
    private boolean addWeaponAt(Weapon weapon, int x, int y) {
        if (!canPlaceAt(x, y, weapon)) return false;
        
        markGrid(x, y, weapon, true);
        slots.add(new InventorySlot(x, y, weapon));
        return true;
    }
    public Weapon getWeaponAt(int gridX, int gridY) {
        for (InventorySlot slot : slots) {
            for (Vector2 cell : slot.weapon.getOccupiedCells()) {
                int slotX = slot.x + (int) cell.x;
                int slotY = slot.y + (int) cell.y;
                
                if (slotX == gridX && slotY == gridY) {
                    return slot.weapon;
                }
            }
        }
        return null;
    }
    public boolean isEquipped(Weapon weapon) {
        return equippedWeapon == weapon;
    }
    
    public void markGrid(int startX, int startY, Weapon weapon, boolean value) {
        for (InventorySlot slot : slots) {
            if (slot.weapon == weapon) {
                for (Vector2 cell : weapon.getOccupiedCells()) {
                    int x = slot.x + (int) cell.x;
                    int y = slot.y + (int) cell.y;
                    grid[gridRows - 1 - y][x] = false;
                }
            }
        }
        for (Vector2 cell : weapon.getOccupiedCells()) {
            int x = startX + (int) cell.x;
            int y = startY + (int) cell.y;
            if (x < gridCols && y < gridRows) {
                grid[gridRows - 1 - y][x] = value;
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
        for (Vector2 cell : weapon.getOccupiedCells()) {
            int x = startX + (int) cell.x;
            int y = startY + (int) cell.y;
            if (x < 0 || x >= gridCols || y < 0 || y >= gridRows) {
                return false;
            }
            if (grid[gridRows - 1 - y][x]) {
                return false;
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


    public void placeWeapon(Weapon weapon, int newX, int newY) {
        if (weapons.contains(weapon)) {
            removeWeapon(weapon);
        }
        
        if (!canPlaceAt(newX, newY, weapon)) return;
        
        markGrid(newX, newY, weapon, true);
        slots.add(new InventorySlot(newX, newY, weapon));
        weapons.add(weapon);
        
        if (equippedWeapon == null) {
            equipWeapon(weapon);
        }
    }
    public List<Weapon> getWeapons() {
        return weapons;
    }
}