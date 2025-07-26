package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;

public class BarrelHandler implements ContactHandler {
    @Override
    public void handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        if ("MELEE_ATTACK".equals(dataA) && dataB instanceof Barrel) {
            ((Barrel) dataB).takeDamage(1);
        } else if ("MELEE_ATTACK".equals(dataB) && dataA instanceof Barrel) {
            ((Barrel) dataA).takeDamage(1);
        }
    }
    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
    }
}