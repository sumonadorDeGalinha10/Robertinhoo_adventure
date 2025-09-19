package io.github.some_example_name.Entities.Enemies.Castor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Enemies.Box2dLocation;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;

public class Castor extends Enemy implements ShadowEntity, Steerable<Vector2> {

    private Float damage;
    private final Mapa mapa;
    private final Body body;
    public final Robertinhoo target;

    private Animation<TextureRegion> ratAnimation;
    private float animationTime = 0f;
    private ShadowComponent shadowComponent;
    private boolean isShooting = false;
    private boolean hasShot = false;

    private boolean isTakingDamage = false;
    private float damageTimer = 0f;
    private static final float DAMAGE_ANIMATION_DURATION = 0.5f;
    private boolean isDead = false;
    private boolean markedForDestruction = false;

    private boolean tagged;
    private float boundingRadius = 0.5f;
    private float maxLinearSpeed = 3f;
    private float maxLinearAcceleration = 5f;
    private float maxAngularSpeed = 5f;
    private float maxAngularAcceleration = 10f;
    private float zeroLinearSpeedThreshold = 1f;
    public CastorIA ai;
    private float shootAnimationTime = 0f;
    private boolean wasMovingBeforeShooting = false;
    private Vector2 previousVelocity = new Vector2();

    private float shootCooldown = 0f;
    private static final float SHOOT_COOLDOWN_TIME = 4f;
    private static final float SHOOT_ANIMATION_DURATION = 0.6f;
    private static final float RECOIL_FORCE = 1.5f;
    private static final float RECOIL_DAMPING = 0.9f;

    public Castor(Mapa mapa, float x, float y, Robertinhoo target) {
        super(x, y, 20, 2);
        this.mapa = mapa;
        this.target = target;
        this.body = createBody(x, y);
        this.damage = 15f;
        this.ai = new CastorIA(this, target, mapa.getPathfindingSystem(), mapa);

        body.setUserData(this);
        this.shadowComponent = new ShadowComponent(
                20,
                20,
                -0.25f,
                0.7f,
                new Color(0.05f, 0.05f, 0.05f, 1f));
    }

    public void startShooting() {
        if (!isShooting) {

            wasMovingBeforeShooting = getLinearVelocity().len() > 0.1f;
            previousVelocity.set(getLinearVelocity());

            body.setLinearVelocity(0, 0);

            this.isShooting = true;
            this.hasShot = false;
            this.shootAnimationTime = 0f;
            this.shootCooldown = SHOOT_COOLDOWN_TIME;
            Gdx.app.log("Castor", "startShooting(): animação iniciada, movimento parado");
        }
    }

    public void fireProjectile() {
        if (target != null) {
            Vector2 direction = target.getPosition().cpy().sub(body.getPosition()).nor();
            float offsetDistance = 1.2f;
            Vector2 launchPosition = body.getPosition().cpy().add(direction.scl(offsetDistance));

            Missile missile = new Missile(
                    mapa,
                    launchPosition,
                    direction,
                     this );

            mapa.addProjectile(missile);
            shootCooldown = SHOOT_COOLDOWN_TIME;

            Vector2 recoilDirection = direction.cpy().scl(-1);
            body.applyLinearImpulse(
                    recoilDirection.scl(RECOIL_FORCE * body.getMass() * 0.5f),
                    body.getWorldCenter(),
                    true);

            Gdx.app.log("Castor", "Lançando míssil com recuo suave da posição: " + launchPosition);
        }
    }

    public boolean isShooting() {
        return isShooting;
    }

    public boolean hasShot() {
        return hasShot;
    }

    public void setHasShot(boolean hasShot) {
        this.hasShot = hasShot;
    }

    // No método createBody do Castor.java
    private Body createBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f);
        bodyDef.fixedRotation = true;

        Body body = mapa.world.createBody(bodyDef);

        float halfWidth = (6f / 3f) / 12;
        float halfHeight = (6f / 3f) / 12;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = 4f;
        fd.friction = 3f;
        
        fd.filter.categoryBits = Constants.BIT_ENEMY;
        fd.filter.maskBits = (short) (Constants.BIT_GROUND
                | Constants.BIT_WALL
                | Constants.BIT_PLAYER
                | Constants.BIT_OBJECT
                | Constants.BIT_PLAYER_ATTACK
                | Constants.BIT_PROJECTILE);

        body.createFixture(fd);
        shape.dispose();

        body.setLinearDamping(1f);
        body.setAngularDamping(1f);
        body.setUserData(this);

        return body;
    }

    public void shootAtPlayer() {
        // Se a animação já está em progresso, ignora chamadas diretas
        if (isShooting) {
            Gdx.app.log("Castor", "shootAtPlayer() ignorado — animação já em progresso");
            return;
        }

        // Se pode atirar, pede pra iniciar a animação (que chamará fireProjectile no
        // tempo certo)
        if (shootCooldown <= 0 && target != null) {
            Gdx.app.log("Castor", "shootAtPlayer(): redirecionando para startShooting() para respeitar animação");
            startShooting();
        } else {
            Gdx.app.log("Castor", "shootAtPlayer(): não pode atirar ainda (cooldown=" + shootCooldown + ")");
        }
    }

    public boolean canShoot() {
        return shootCooldown <= 0;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public float getAttackDamage() {
        return damage;
    }

    @Override
    public void update(float deltaTime) {
        if (shootCooldown > 0) {
            shootCooldown -= deltaTime;
        }
             if (isDead) {
            return;
        }
        

        if (isTakingDamage) {
            damageTimer -= deltaTime;
            if (damageTimer <= 0) {
                isTakingDamage = false;
            }
            
            return;
        }

        // Aplica damping ao recuo para suavizar o movimento
        if (isShooting && shootAnimationTime >= 0.5f) {
            Vector2 velocity = body.getLinearVelocity();
            velocity.scl(RECOIL_DAMPING);
            body.setLinearVelocity(velocity);
        }

        // Atualiza o tempo da animação de tiro
        if (isShooting) {
            shootAnimationTime += deltaTime;

            if (shootAnimationTime < 0.5f) {
                body.setLinearVelocity(0, 0);
            }

            if (shootAnimationTime >= 0.5f && !hasShot) {
                fireProjectile();
                hasShot = true;
            }

            // Se a animação de tiro terminou, desativa o estado de shooting
            if (shootAnimationTime >= SHOOT_ANIMATION_DURATION) {
                isShooting = false;
                Gdx.app.log("Castor", "Animação de tiro terminada");
            }
        }

        ai.update(deltaTime, body);
    }
        public boolean isMarkedForDestruction() {
        return markedForDestruction;
    }


    public float getShootAnimationTime() {
        return shootAnimationTime;
    }

    public boolean isAnimationFinished() {
        return shootAnimationTime >= SHOOT_ANIMATION_DURATION;
    }

    public TextureRegion getCurrentFrame(float deltaTime) {
        animationTime += deltaTime;
        return ratAnimation.getKeyFrame(animationTime);
    }

    @Override
    public ShadowComponent getShadowComponent() {
        return shadowComponent;
    }

public void debugRender(ShapeRenderer shapeRenderer, Vector2 cameraOffset, float tileSize) {
    if (ai != null) {
        ai.debugRender(shapeRenderer);
        ai.debugRenderChaseSystem(shapeRenderer, cameraOffset, tileSize);
    }
}
   
    @Override
    public void takeDamage(float damage) {
        if (isDead) return;
        
        super.takeDamage(damage);
        damageTimer = DAMAGE_ANIMATION_DURATION;
        isTakingDamage = true;
        
        if (isShooting) {
            isShooting = false;
            hasShot = false;
            shootAnimationTime = 0f;
        }
        
        Vector2 knockbackDirection = target.getPosition().cpy().sub(body.getPosition()).nor().scl(-1);
        body.applyLinearImpulse(
            knockbackDirection.scl(3f * body.getMass()),
            body.getWorldCenter(),
            true
        );
        
        Gdx.app.log("Castor", "Levou " + damage + " de dano. Saúde: " + health);
        
        // Verifica se morreu
        if (health <= 0) {
            die();
        }
    }
        private void die() {
        isDead = true;
        Gdx.app.log("Castor", "Morreu!");
        
        // Desativa colisões
        for (Fixture fixture : body.getFixtureList()) {
            fixture.setSensor(true);
        }
        
        // Para o movimento
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
        
        markedForDestruction = true;
    }
        public boolean isTakingDamage() {
        return isTakingDamage;
    }
    
    public boolean isDead() {
        return isDead;
    }
    

    /*
     * ============================
     * Métodos exigidos por Steerable/Location/Limiter
     * ============================
     */

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(vector.y, vector.x);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = (float) Math.cos(angle);
        outVector.y = (float) Math.sin(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }

    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    // ===== Limiter =====
    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroLinearSpeedThreshold;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        this.zeroLinearSpeedThreshold = value;
    }
}
