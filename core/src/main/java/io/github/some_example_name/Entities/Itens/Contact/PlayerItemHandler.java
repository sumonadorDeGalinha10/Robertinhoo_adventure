package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Inventory.Item;

public class PlayerItemHandler implements ContactHandler {
    private final Robertinhoo player;

    public PlayerItemHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public void handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        if (dataA != null && dataB != null) {
            // Coleta de armas
            if (dataA.equals("PLAYER") && dataB instanceof Weapon) {
                player.setWeaponToPickup((Weapon) dataB);
            } else if (dataB.equals("PLAYER") && dataA instanceof Weapon) {
                player.setWeaponToPickup((Weapon) dataA);
            }
            
            // Coleta de munição
            if (dataA instanceof Ammo && "PLAYER".equals(dataB)) {
                player.setAmmoToPickup((Ammo) dataA);
            } else if (dataB instanceof Ammo && "PLAYER".equals(dataA)) {
                player.setAmmoToPickup((Ammo) dataB);
            }
        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object userDataA = fixtureA.getUserData();
        Object userDataB = fixtureB.getUserData();
        
        if (userDataA != null && userDataB != null) {
            // Limpar referência a itens
            if (userDataA.equals("PLAYER") && userDataB instanceof Weapon) {
                player.clearWeaponToPickup();
            } else if (userDataB.equals("PLAYER") && userDataA instanceof Weapon) {
                player.clearWeaponToPickup();
            }
            
            if (userDataA.equals("PLAYER") && userDataB instanceof Item) {
                player.clearItemToPickup();
            } else if (userDataB.equals("PLAYER") && userDataA instanceof Item) {
                player.clearItemToPickup();
            }
        }

    }
    
}