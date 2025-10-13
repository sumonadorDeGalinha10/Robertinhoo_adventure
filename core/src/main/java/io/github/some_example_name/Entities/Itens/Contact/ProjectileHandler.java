package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Inventory.Item;

public class ProjectileHandler implements ContactHandler {
    private final Robertinhoo player;

    public ProjectileHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        Gdx.app.log("ProjectileHandler", "Contato detectado entre: " + dataA + " e " + dataB);

        if (player.getMeleeAttackSystem().getParrySystem().isParryActive() &&
                ((dataA instanceof Missile && "PLAYER".equals(dataB)) ||
                        (dataB instanceof Missile && "PLAYER".equals(dataA)))) {
            Gdx.app.log("ProjectileHandler", "Parry ativo - ignorando colisão com jogador");
            return false;
        }

        if ((dataA instanceof Projectile && "WALL".equals(dataB)) ||
                (dataB instanceof Projectile && "WALL".equals(dataA))) {
            handleProjectileWallCollision(dataA, dataB);
        }

        else if ((dataA instanceof Projectile && dataB instanceof Enemy) ||
                (dataB instanceof Projectile && dataA instanceof Enemy)) {

            handleProjectileEnemyCollision(dataA, dataB);
        } else if ((dataA instanceof Projectile && "PLAYER".equals(dataB)) ||
                (dataB instanceof Projectile && "PLAYER".equals(dataA))) {
            System.out.println("Colisão detectada entre projétil e player.");
            handleProjectilePlayerCollision(dataA, dataB);
        }

        else if ((dataA instanceof Projectile && dataB instanceof Barrel) ||
                (dataB instanceof Projectile && dataA instanceof Barrel)) {
            handleProjectileBarrelCollision(dataA, dataB);
        }
        return false;
    }

    private void handleProjectileWallCollision(Object dataA, Object dataB) {
        Projectile projectile = (dataA instanceof Projectile) ? (Projectile) dataA : (Projectile) dataB;
        projectile.startDestruction();
    }

    private void handleProjectilePlayerCollision(Object dataA, Object dataB) {
        Projectile projectile = (dataA instanceof Projectile) ? (Projectile) dataA : (Projectile) dataB;
        player.takeDamage(projectile.getDamage());
        projectile.startDestruction();
    }

    private void handleProjectileEnemyCollision(Object dataA, Object dataB) {
        Projectile projectile = (dataA instanceof Projectile) ? (Projectile) dataA : (Projectile) dataB;
        Enemy enemy = (dataB instanceof Enemy) ? (Enemy) dataB : (Enemy) dataA;

        enemy.takeDamage(projectile.getDamage());
        projectile.startDestruction();

        if (enemy.getHealth() <= 0) {

            enemy.die(Enemy.DeathType.PROJECTILE);

            enemy.isToBeDestroyed();
        }

    }

    private void handleProjectileBarrelCollision(Object dataA, Object dataB) {
        Projectile projectile = (dataA instanceof Projectile) ? (Projectile) dataA : (Projectile) dataB;
        Barrel barrel = (dataB instanceof Barrel) ? (Barrel) dataB : (Barrel) dataA;

        barrel.takeDamage(projectile.getDamage());
        projectile.startDestruction();
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {

    }
}