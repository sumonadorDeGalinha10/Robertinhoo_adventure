package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public interface ContactHandler {
    void handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB);
    void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB);
}