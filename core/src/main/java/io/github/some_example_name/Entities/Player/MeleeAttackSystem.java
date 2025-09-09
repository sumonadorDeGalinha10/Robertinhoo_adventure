package io.github.some_example_name.Entities.Player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;

import io.github.some_example_name.Entities.Itens.Contact.Constants;


public class MeleeAttackSystem {
    private final Robertinhoo player;
    private Body meleeHitboxBody;
    private boolean attackInProgress;
    private float attackDuration;
    private final World world;
    private final ParrySystem parrySystem;

    public MeleeAttackSystem(Robertinhoo player) {
        this.player = player;
        this.world = player.getMap().world;
        this.attackInProgress = false;
        this.attackDuration = 0.2f; // Duração do ataque
        this.attackDuration = player.getMeleeAttackDuration();
          this.parrySystem = new ParrySystem(player);
    }

    public void startAttack(int direction) {
        if (attackInProgress)
            return;

        attackInProgress = true;
        

        parrySystem.activateParry();

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                createMeleeHitbox(direction);

                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        endAttack();
                    }
                }, attackDuration);
            }
        }, 0.1f);
    }

    private void createMeleeHitbox(int direction) {
        // Criar definição do corpo
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(player.body.getPosition());
        bodyDef.fixedRotation = true;

        // Criar o corpo no mundo
        meleeHitboxBody = world.createBody(bodyDef);
        meleeHitboxBody.setUserData("MELEE_ATTACK");

        // Criar a forma do hitbox
        PolygonShape shape = new PolygonShape();
        float halfW = 0.6f / 2f;
        float halfH = 0.3f / 2f;
        Vector2 localCenter = new Vector2(0, 0);

        switch (direction) {
            case Robertinhoo.RIGHT:
                localCenter.set(0.5f + halfW, 0);
                break;
            case Robertinhoo.LEFT:
                localCenter.set(-0.5f - halfW, 0);
                break;
            case Robertinhoo.UP:
                localCenter.set(0, 0.5f + halfH);
                break;
            case Robertinhoo.DOWN:
                localCenter.set(0, -0.5f - halfH);
                break;
            // diagonais: ajuste x e y juntos
        }
        // cria caixa de melee deslocada localmente, sem precisar chamar setTransform
        // depois
        shape.setAsBox(halfW, halfH, localCenter, 0f);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = true;
        fd.filter.categoryBits = Constants.BIT_PLAYER_ATTACK;
        fd.filter.maskBits = Constants.BIT_ENEMY | Constants.BIT_OBJECT;
        meleeHitboxBody.createFixture(fd);
        shape.dispose();

        System.out.println("Criando hitbox de ataque corpo a corpo em: " +
                meleeHitboxBody.getPosition() +
                " | Direção: " + direction);
    }

    private void endAttack() {
        if (meleeHitboxBody != null) {
            world.destroyBody(meleeHitboxBody);
            meleeHitboxBody = null;
            System.out.println("Hitbox de ataque destruída");
        }
        attackInProgress = false;
    }

    public boolean isAttacking() {
        return attackInProgress;
    }

    public Body getMeleeHitboxBody() {
        return meleeHitboxBody;
    }

        public ParrySystem getParrySystem() {
        return parrySystem;
    }

}
