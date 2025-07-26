package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;


import io.github.some_example_name.Entities.Enemies.Enemy;


public class ProjectileHandler implements ContactHandler {
    @Override
    public void handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        // Colisão com paredes
        if ((dataA instanceof Projectile && "WALL".equals(dataB)) || 
            (dataB instanceof Projectile && "WALL".equals(dataA))) {
            handleProjectileWallCollision(dataA, dataB);
        }
        // Colisão com inimigos
        else if ((dataA instanceof Projectile && dataB instanceof Enemy) || 
                (dataB instanceof Projectile && dataA instanceof Enemy)) {
            handleProjectileEnemyCollision(dataA, dataB);
        }
    }
    
    private void handleProjectileWallCollision(Object dataA, Object dataB) {
        Projectile projectile = (dataA instanceof Projectile) ? 
            (Projectile) dataA : (Projectile) dataB;
        projectile.startDestruction();
    }
    
    private void handleProjectileEnemyCollision(Object dataA, Object dataB) {
        Projectile projectile = (dataA instanceof Projectile) ? 
            (Projectile) dataA : (Projectile) dataB;
        Enemy enemy = (dataB instanceof Enemy) ? 
            (Enemy) dataB : (Enemy) dataA;

        enemy.takeDamage(projectile.getDamage());
        projectile.startDestruction();

        if (enemy.getHealth() <= 0) {
            enemy.destroy();
        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // Nenhuma ação necessária ao sair do contato
    }
}