package io.github.some_example_name.Entities.Itens.Contact;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import com.badlogic.gdx.Gdx;

public class MeleeAttackHandler implements ContactHandler {
    private final Robertinhoo player;
    
    public MeleeAttackHandler(Robertinhoo player) {
        this.player = player;
    }

    @Override
    public boolean handleBeginContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        System.out.println("Início de contato entre: " + 
                          fixtureA.getBody().getUserData() + 
                          " e " + 
                          fixtureB.getBody().getUserData());
        
        // Primeiro verifica se é uma colisão com míssil para parry
        if (checkMissileParry(contact, fixtureA, fixtureB)) {
            return true; // Contato totalmente tratado - para processamento
        }
        
        // Depois verifica colisões com inimigos
        if (("MELEE_ATTACK".equals(dataA) && dataB instanceof Enemy)) {
            handleMeleeAttack((Enemy) dataB, fixtureA.getBody().getPosition());
        } else if ("MELEE_ATTACK".equals(dataB) && dataA instanceof Enemy) {
            handleMeleeAttack((Enemy) dataA, fixtureB.getBody().getPosition());
        }
        
        return false; // Continua o processamento com outros handlers
    }
    
    private boolean checkMissileParry(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();
        
        // Verifica se é uma colisão entre MELEE_ATTACK e Missile
        boolean isMeleeVsMissile = ("MELEE_ATTACK".equals(dataA) && dataB instanceof Missile) ||
                                  ("MELEE_ATTACK".equals(dataB) && dataA instanceof Missile);
        
        if (isMeleeVsMissile && player.getMeleeAttackSystem().getParrySystem().isParryActive()) {
            Missile missile = (Missile) ("MELEE_ATTACK".equals(dataA) ? dataB : dataA);
            
            // Verifica se o míssil pode ser rebatido (não está já rebatido)
            if (!missile.isReflected() && missile.getOwner() != null) {
                // Calcula direção de retorno (do jogador para o Castor)
                Vector2 returnDirection = missile.getOwner().getPosition()
                    .cpy().sub(player.getPosition()).nor();
                
                // Reflete o míssil
                missile.reflect(returnDirection);
                
                Gdx.app.log("MeleeAttackHandler", "Míssil rebatido com sucesso!");
                player.getMeleeAttackSystem().getParrySystem().deactivateParry();
                return true; // Parry bem-sucedido - contato totalmente tratado
            }
        }
        
        return false;
    }
    private void handleMeleeAttack(Enemy enemy, Vector2 attackPosition) {
        // Ignora inimigos já mortos
        if (enemy.isDead()) return;
        
        enemy.takeDamage(15);
        Vector2 direction = new Vector2(enemy.getBody().getPosition()).sub(attackPosition).nor();
        enemy.getBody().applyLinearImpulse(direction.scl(1f), enemy.getBody().getWorldCenter(), true);
        
        if (enemy.getHealth() <= 0) {
            if (enemy instanceof Ratinho) {
                ((Ratinho) enemy).die(Ratinho.DeathType.MELEE);
            }
            // Não marca para destruição aqui! Apenas inicia a morte
        }
    }

    @Override
    public void handleEndContact(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        // Nenhuma ação necessária ao sair do contato
    }
}