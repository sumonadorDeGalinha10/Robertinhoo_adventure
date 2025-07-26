package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import com.badlogic.gdx.math.Vector2;

public class EnemyHandler implements ContactHandler {
    private final Robertinhoo player;

    public EnemyHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public void handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        // Inimigo atacando jogador
        if (dataA instanceof Enemy && "PLAYER".equals(dataB)) {
            handleEnemyAttack((Enemy) dataA);
        } else if (dataB instanceof Enemy && "PLAYER".equals(dataA)) {
            handleEnemyAttack((Enemy) dataB);
        }
        
        // Colisão entre inimigos (opcional)
        if (dataA instanceof Enemy && dataB instanceof Enemy) {
            handleEnemyCollision((Enemy) dataA, (Enemy) dataB);
        }
    }
    
    private void handleEnemyAttack(Enemy enemy) {
        player.takeDamage(enemy.getAttackDamage());

    }
    
    private void handleEnemyCollision(Enemy enemy1, Enemy enemy2) {
        // Empurrão entre inimigos para evitar empilhamento
        Vector2 direction = new Vector2(enemy1.getBody().getPosition())
            .sub(enemy2.getBody().getPosition())
            .nor();
        
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // Nenhuma ação necessária ao sair do contato
    }
}