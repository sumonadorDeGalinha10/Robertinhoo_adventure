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


    public Inventory(Robertinhoo player) {
        this.robertinhoo = player;
    }

    public Boolean addWeapon(Weapon weapon) {
        if (weapons.size() < 5) { // Capacidade máxima exemplo
            weapons.add(weapon);
            if (equippedWeapon == null) {
                equipWeapon(weapon);
            }
            return true;
        }
        return false;
      
    }

    public void equipWeapon(Weapon weapon) {
        this.equippedWeapon = weapon;
        robertinhoo.equipWeapon(weapon); // Sincroniza com o Robertinhoo
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }
    public void dropWeapon() {
        if (equippedWeapon != null) {
            Vector2 dropPosition = robertinhoo.getPosition().cpy().add(0.5f, 0.5f);
            equippedWeapon.createBody(dropPosition); // Agora usando Vector2
            weapons.remove(equippedWeapon);
            equippedWeapon = null;
        }
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }
}