package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Contact;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.Gdx;

public class ParrySystem {
    private final Robertinhoo player;
    private final Mapa mapa;
    private boolean isParryActive;
    private float parryWindow = 0.15f;
    private float parryTimer = 0f;

    public ParrySystem(Robertinhoo player) {
        this.player = player;
        this.mapa = player.getMap();
        this.isParryActive = false;
    }

    public void update(float deltaTime) {
        if (isParryActive) {
            parryTimer -= deltaTime;
            if (parryTimer <= 0) {
                isParryActive = false;
                Gdx.app.log("ParrySystem", "Parry expirou por tempo.");
            }
        }
    }

    public void activateParry() {
        isParryActive = true;
        parryTimer = parryWindow;
        Gdx.app.log("ParrySystem", "Parry ativado!");
    }

    public boolean checkMissileParry(Contact contact, Fixture fixtureA, Fixture fixtureB) {
        if (!isParryActive) {
            return false;
        }

        Object dataA = fixtureA.getBody().getUserData();
        Object dataB = fixtureB.getBody().getUserData();

        Gdx.app.log("ParrySystem", "Verificando colisão: " + dataA + " vs " + dataB);

        // Verifica se é uma colisão entre MELEE_ATTACK e Missile
        boolean isMeleeVsMissile = ("MELEE_ATTACK".equals(dataA) && dataB instanceof Missile) ||
                ("MELEE_ATTACK".equals(dataB) && dataA instanceof Missile);

        if (isMeleeVsMissile) {
            Missile missile = (Missile) ("MELEE_ATTACK".equals(dataA) ? dataB : dataA);

            Gdx.app.log("ParrySystem", "Colisão MELEE_ATTACK vs Missile detectada");

            // Verifica se o míssil pode ser rebatido (não está já rebatido)
            if (!missile.isReflected() && missile.getOwner() != null) {
                // Calcula direção de retorno (do jogador para o Castor)
                Vector2 returnDirection = missile.getOwner().getPosition()
                        .cpy().sub(missile.getPosition()).nor();

                Gdx.app.log("ParrySystem", "Direção de retorno calculada: " + returnDirection + 
                               ", Dono: " + missile.getOwner().getPosition() + 
                               ", Missil: " + missile.getPosition());

                // Reflete o míssil
                missile.reflect(returnDirection);

                Gdx.app.log("ParrySystem", "Míssil rebatido com sucesso!");
                deactivateParry();
                return true;
            } else {
                Gdx.app.log("ParrySystem", "Míssil não pode ser rebatido - já refletido: " + 
                    missile.isReflected() + ", owner: " + (missile.getOwner() != null));
            }
        }

        return false;
    }

    public boolean isParryActive() {
        return isParryActive;
    }

    public void deactivateParry() {
        isParryActive = false;
        parryTimer = 0f;
        Gdx.app.log("ParrySystem", "Parry desativado.");
    }
    
    public float getParryWindow() {
        return parryWindow;
    }

    public void setParryWindow(float parryWindow) {
        this.parryWindow = parryWindow;
    }
}