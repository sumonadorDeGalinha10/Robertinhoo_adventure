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
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor.CastorRenderer;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Rat.RatRenderer;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import io.github.some_example_name.Entities.Enemies.IA.DodgeSystem;

public class Castor extends Enemy implements ShadowEntity, Steerable<Vector2> {

    private Float damage;
    private final Mapa mapa;
    private final Body body;
    public final Robertinhoo target;
    public CastorRenderer renderer = new CastorRenderer();
    private CastorAnimationState animationState;

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

    private int directionX = 1;
    private int directionY = 1;

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
    private DodgeSystem dodgeSystem;

    private boolean scheduledDodge = false;
    private Vector2 scheduledDodgePlayerPosition = null;
    private static final float DODGE_AFTER_DAMAGE_DELAY = 0.4f;
    private float dodgeScheduleTimer = 0f;

    private boolean isDashing = false;

    private static final float DASH_TOTAL_DURATION = 0.8f;

    public enum State {
        IDLE, MOVING, SHOOTING, TAKING_DAMAGE, DASHING,
        MELEE_DEATH, PROJECTILE_DEATH
    }

    private State state = State.IDLE;

    public Castor(Mapa mapa, float x, float y, Robertinhoo target) {
        super(x, y, 3, 2);
        this.mapa = mapa;
        this.target = target;
        this.body = createBody(x, y);
        this.damage = 15f;
        this.dodgeSystem = new DodgeSystem(mapa.getPathfindingSystem(), mapa);
        this.ai = new CastorIA(this, target, mapa.getPathfindingSystem(), mapa, dodgeSystem);
        this.animationState = new CastorAnimationState();

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
                    this);

            mapa.addProjectile(missile);
            shootCooldown = SHOOT_COOLDOWN_TIME;

            Vector2 recoilDirection = direction.cpy().scl(-1);
            body.applyLinearImpulse(
                    recoilDirection.scl(RECOIL_FORCE * body.getMass() * 0.5f),
                    body.getWorldCenter(),
                    true);
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

    private Body createBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f);
        bodyDef.fixedRotation = true;

        Body body = mapa.world.createBody(bodyDef);

        // AJUSTE: Corrigir as dimensões para a nova escala (64 pixels)
        // Antes: (6f / 3f) / 12 = muito pequeno para 64px
        // Agora: usar dimensões proporcionais à nova escala
        float halfWidth = (16f / 64f) / 2f; // 16 pixels em unidades de mundo (64px = 1 unidade)
        float halfHeight = (16f / 64f) / 2f;

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
        if (isShooting) {
            return;
        }
        if (shootCooldown <= 0 && target != null) {
            startShooting();
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
        // 🔥 ATUALIZAR: animationState primeiro
        animationState.update(deltaTime, this);
        dodgeSystem.update(deltaTime);

        if (isDashing) {
            // 🔥 CORREÇÃO: Usar animationState.dashAnimationTime
            if (shouldApplyDashForce()) {
                applyDashViaDodgeSystem();
            }

            // Finalizar dash quando a animação acabar
            if (animationState.dashAnimationTime >= DASH_TOTAL_DURATION) {
                isDashing = false;
                animationState.dashAnimationTime = 0f;
            }
        }

        if (shootCooldown > 0) {
            shootCooldown -= deltaTime;
        }

        if (isDead) {
            return;
        }

        if (isTakingDamage) {
            damageTimer -= deltaTime;

            if (scheduledDodge) {
                dodgeScheduleTimer -= deltaTime;

                if (dodgeScheduleTimer <= 0 && damageTimer <= DAMAGE_ANIMATION_DURATION * 0.3f) {
                    executeScheduledDodge();
                }
            }

            if (damageTimer <= 0) {
                isTakingDamage = false;
                if (scheduledDodge) {
                    executeScheduledDodge();
                }
            }
            return;
        }

        if (isShooting && animationState.shootAnimationTime >= 0.5f) {
            Vector2 velocity = body.getLinearVelocity();
            velocity.scl(RECOIL_DAMPING);
            body.setLinearVelocity(velocity);
        }

        if (isShooting) {
            if (animationState.shootAnimationTime < 0.5f) {
                body.setLinearVelocity(0, 0);
            }
            if (animationState.shootAnimationTime >= 0.5f && !hasShot) {
                fireProjectile();
                hasShot = true;
            }
            if (animationState.shootAnimationTime >= SHOOT_ANIMATION_DURATION) {
                isShooting = false;
            }
        }
        ai.update(deltaTime, body);
    }

    private void executeScheduledDodge() {
        if (scheduledDodge && scheduledDodgePlayerPosition != null) {
            startDashAnimation();
            scheduledDodge = false;
            dodgeScheduleTimer = 0f;
        }
    }

    public boolean isMarkedForDestruction() {
        return markedForDestruction;
    }

    public void markForDestruction() {
        this.markedForDestruction = true;
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
        if (isDead)
            return;

        super.takeDamage(damage);
        damageTimer = DAMAGE_ANIMATION_DURATION;
        isTakingDamage = true;
        state = State.TAKING_DAMAGE;
        if (target != null) {
            float distanceToTarget = getPosition().dst(target.getPosition());

            if (distanceToTarget < 8f && dodgeSystem.canDodge()) {
                scheduledDodge = true;
                scheduledDodgePlayerPosition = new Vector2(target.getPosition());
                dodgeScheduleTimer = DODGE_AFTER_DAMAGE_DELAY;
                applyReducedKnockback();
            } else {
                applyNormalKnockback();
            }
        } else {
            applyNormalKnockback();
        }

        if (isShooting) {
            isShooting = false;
            hasShot = false;
            shootAnimationTime = 0f;
        }
    }

    private void applyReducedKnockback() {
        if (target != null) {
            Vector2 knockbackDirection = target.getPosition().cpy().sub(body.getPosition()).nor().scl(-1);
            body.applyLinearImpulse(
                    knockbackDirection.scl(0.3f * body.getMass()),
                    body.getWorldCenter(),
                    true);
        }
    }

    /**
     * Knockback normal quando não esquiva
     */
    private void applyNormalKnockback() {
        if (target != null) {
            Vector2 knockbackDirection = target.getPosition().cpy().sub(body.getPosition()).nor().scl(-1);
            body.applyLinearImpulse(
                    knockbackDirection.scl(1.0f * body.getMass()),
                    body.getWorldCenter(),
                    true);
        }
    }

    @Override
    public void die(DeathType type) {
        if (isDead)
            return;
        isDead = true;
        deathType = type;

        animationState.deathAnimationTime = 0f;
        state = type == DeathType.MELEE ? State.MELEE_DEATH : State.PROJECTILE_DEATH;

        for (Fixture fixture : body.getFixtureList()) {
            fixture.setSensor(true);
        }
        body.setLinearVelocity(0, 0);
        body.setAngularVelocity(0);
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

    public float getDamageTimer() {
        return damageTimer;
    }

    public boolean isDodgeScheduled() {
        return scheduledDodge;
    }

    private void startDashAnimation() {
        isDashing = true;
        animationState.dashAnimationTime = 0f;
    }

    private boolean shouldApplyDashForce() {
        return isDashing && animationState.dashAnimationTime >= 0.3f && animationState.dashAnimationTime < 0.35f;
    }

    public boolean isDashing() {
        return isDashing;
    }

    private void applyDashViaDodgeSystem() {
        if (scheduledDodgePlayerPosition != null) {

            // Usar o DodgeSystem existente - ele já calcula direção e aplica força
            boolean dashApplied = dodgeSystem.executeDodgeOnHit(body, getPosition(), scheduledDodgePlayerPosition);

            if (dashApplied) {
                Gdx.app.log("Castor", "✅ Dash aplicado com sucesso via DodgeSystem");
            } else {
                Gdx.app.log("Castor", "❌ Falha ao aplicar dash via DodgeSystem");
            }
        }
    }

    public int getDirectionX() {
        return directionX;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean isDeathAnimationFinished() {
        if (deathType == null) {
            return true;
        }

        float duration = (deathType == DeathType.MELEE) ? renderer.getMeleeDeathDuration()
                : renderer.getProjectileDeathDuration();

        // 🔥 CORREÇÃO: Usar animationState.deathAnimationTime
        boolean finished = animationState.deathAnimationTime >= duration;
        return finished;
    }

    public boolean isDying() {
        boolean dying = isDead && deathType != null && !isDeathAnimationFinished();
        return dying;
    }

    public CastorAnimationState getAnimationState() {
        return animationState;
    }
}
