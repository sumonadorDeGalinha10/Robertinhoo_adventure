package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Ratinho;

public class MeleeAttackHandler implements ContactHandler {
    @Override
    public void handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        // Detectar colisão entre ataque corpo a corpo e inimigo
        if (("MELEE_ATTACK".equals(dataA) && dataB instanceof Enemy)) {
            handleMeleeAttack((Enemy) dataB, fixtureA.getBody().getPosition());
              
        System.out.println("Início de contato entre: " + 
                          fixtureA.getBody().getUserData() + 
                          " e " + 
                          fixtureB.getBody().getUserData());
        } else if ("MELEE_ATTACK".equals(dataB) && dataA instanceof Enemy) {
            handleMeleeAttack((Enemy) dataA, fixtureB.getBody().getPosition());
        }
    }
    
    private void handleMeleeAttack(Enemy enemy, Vector2 attackPosition) {
        enemy.takeDamage(15);
        Vector2 direction = new Vector2(enemy.getBody().getPosition()).sub(attackPosition).nor();
        enemy.getBody().applyLinearImpulse(direction.scl(1f), enemy.getBody().getWorldCenter(), true);
        if (enemy.getHealth() <= 0) {
            enemy.destroy();
        }
    }

    

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // Nenhuma ação necessária ao sair do contato
    }
}