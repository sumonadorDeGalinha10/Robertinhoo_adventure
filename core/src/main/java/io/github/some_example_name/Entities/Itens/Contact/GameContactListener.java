package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import java.util.ArrayList;
import java.util.List;

public class GameContactListener implements ContactListener {
    private final List<ContactHandler> handlers = new ArrayList<>();
    
    public GameContactListener(Robertinhoo player) {
        handlers.add(new PlayerItemHandler(player));
        handlers.add(new ProjectileHandler());
        handlers.add(new MeleeAttackHandler());
        handlers.add(new EnemyHandler(player));
        handlers.add(new BarrelHandler());
    }
    
    public void addHandler(ContactHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
        for (ContactHandler handler : handlers) {
            handler.handleBeginContact(contact, fixtureA, fixtureB);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        
        for (ContactHandler handler : handlers) {
            handler.handleEndContact(contact, fixtureA, fixtureB);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}