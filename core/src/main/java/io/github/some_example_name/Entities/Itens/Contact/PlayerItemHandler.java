package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Inventory.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.math.Vector2;

public class PlayerItemHandler implements ContactHandler {
    private final Robertinhoo player;
    private final List<Item> nearbyItems = new ArrayList<>();
    private final Map<Item, Fixture> itemFixtures = new HashMap<>();

    public PlayerItemHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public boolean  handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        // Identificar qual fixture pertence ao jogador e qual ao item
        Fixture playerFixture = null;
        Fixture itemFixture = null;
        Object itemData = null;

        if ("PLAYER".equals(dataA) && dataB instanceof Item) {
            playerFixture = fixtureA;
            itemFixture = fixtureB;
            itemData = dataB;
        } else if ("PLAYER".equals(dataB) && dataA instanceof Item) {
            playerFixture = fixtureB;
            itemFixture = fixtureA;
            itemData = dataA;
        }

        if (itemData != null) {
            Item item = (Item) itemData;
            
            // Registrar o item e sua fixture
            if (!nearbyItems.contains(item)) {
                nearbyItems.add(item);
                itemFixtures.put(item, itemFixture);
                System.out.println("[DEBUG] Item adicionado à lista: " + item);
            }
      
            updateNearestItem();
        }
        return false;
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        Item item = null;
        if (dataA instanceof Item && "PLAYER".equals(dataB)) {
            item = (Item) dataA;
        } else if (dataB instanceof Item && "PLAYER".equals(dataA)) {
            item = (Item) dataB;
        }

        if (item != null) {
            // Remover o item da lista
            nearbyItems.remove(item);
            itemFixtures.remove(item);
            System.out.println("[DEBUG] Item removido da lista: " + item);
            
            // Atualizar o item mais próximo
            updateNearestItem();
        }
    }

    private void updateNearestItem() {
        if (nearbyItems.isEmpty()) {
            player.clearItemToPickup();
            return;
        }

        // Encontrar o item mais próximo ao jogador
        Vector2 playerPos = player.getPosition();
        Item nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Item item : nearbyItems) {
            float distance = playerPos.dst(item.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = item;
            }
        }

        if (nearest != null) {
            player.setItemToPickup(nearest);
            System.out.println("[DEBUG] Item mais próximo definido: " + nearest);
        }
    }

    public void forceItemContact(Item item) {
        if (!nearbyItems.contains(item)) {
            nearbyItems.add(item);
            System.out.println("[DEBUG] Contato forçado para item: " + item);
            updateNearestItem();
        }
    }
    
    public boolean isPlayerTouching(Item item) {
        return nearbyItems.contains(item);
    }
}