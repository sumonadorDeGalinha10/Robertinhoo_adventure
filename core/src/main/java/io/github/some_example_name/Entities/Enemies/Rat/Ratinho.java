package io.github.some_example_name.Entities.Enemies.Rat;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Enemies.Box2dLocation;
import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.MapConfig.Mapa;
import io.github.some_example_name.Entities.Renderer.EnemiRenderer.Rat.RatRenderer;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

public class Ratinho extends Enemy implements Steerable<Vector2>, ShadowEntity {

    private final Mapa mapa;
    private final Body body;
    public final Robertinhoo target;
    private boolean markedForDestruction = false;

    private float maxLinearSpeed = 4f;
    private float maxLinearAcceleration = 2f;
    private static final float ZERO_LINEAR_SPEED_THRESHOLD = 0.01f;

    public static final float DETECTION_RANGE = 400f;
    private static final float ATTACK_RANGE = 1.5f;
    public final Vector2 pos = new Vector2();

    private static final float DASH_DURATION = 0.4f;
    private static final float DASH_COOLDOWN = 2f;
    private float dashTimer = 0f;

    private boolean isDashing = false;
    private float dashCooldown = 0f;

    private Animation<TextureRegion> ratAnimation;

    private float animationTime = 0f;
    private State state = State.IDLE;
    private int directionX = 1;
    private int directionY = 1;

    private boolean movingDown = false;
    private float damageAnimationDuration = 1f;
    private float damageTimer = 0f;
    private boolean isTakingDamage = false;

    private static final float PREPARE_DASH_DURATION = 0.8f;
    private static final float DASH_SPEED_MULTIPLIER = 1.5f;
    private float prepareDashTimer = 0f;
    private Vector2 dashTargetDirection; //
    private ShadowComponent shadowComponent;

    private RatAI ai;
    public RatRenderer renderer = new RatRenderer();
    private Rectangle homeRoom;
    private int TILE_SIZE = 64;

    public enum State {
        IDLE, RUNNING_HORIZONTAL, RUNNING_DOWN, GOT_DAMAGE,
        PREPARING_DASH,
        DASHING, MELEE_DEATH,
        PROJECTILE_DEATH
    }

    private DeathType deathType = DeathType.NONE;
    private float deathAnimationTime = 0;
    public boolean isDead = false;
    private boolean shouldDeactivate = false;

    public Ratinho(Mapa mapa, float x, float y, Robertinhoo target, Rectangle homeRoom) {
        super(x, y, 20, 2);
        this.mapa = mapa;
        this.target = target;
        this.homeRoom = homeRoom;
        this.body = createBody(x, y);
        this.ai = new RatAI(this, target, mapa.getPathfindingSystem(), mapa, homeRoom);

        body.setUserData(this);
        this.shadowComponent = new ShadowComponent(
                5,
                2,
                -0.1f,

                0.7f,
                new Color(0.05f, 0.05f, 0.05f, 1f));
    }

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
        fd.density = 2f;
        fd.friction = 0f;
        fd.filter.categoryBits = Constants.BIT_ENEMY;

        body.createFixture(fd);
        shape.dispose();

        body.setLinearDamping(2f);
        body.setAngularDamping(2f);
        body.setUserData(this);

        return body;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public void update(float deltaTime) {
        Vector2 playerPos = target.getPosition();
        Vector2 myPos = body.getPosition();
        float distance = myPos.dst(playerPos);

        if (isDead) {
            deathAnimationTime += deltaTime;
            return;
        }
        animationTime += deltaTime;

        if (isTakingDamage) {
            damageTimer -= deltaTime;
            if (damageTimer <= 0) {
                isTakingDamage = false;
                state = State.IDLE;
            }
            return;
        }

        if (state == State.PREPARING_DASH) {
            prepareDashTimer -= deltaTime;
            if (prepareDashTimer <= 0) {
                state = State.DASHING;
                dashTimer = DASH_DURATION;
                body.setLinearVelocity(dashTargetDirection.scl(maxLinearSpeed * DASH_SPEED_MULTIPLIER));
            }
        } else if (state == State.DASHING) {
            dashTimer -= deltaTime;
            if (dashTimer <= 0) {
                state = State.IDLE;
                dashCooldown = DASH_COOLDOWN;
                body.setLinearVelocity(body.getLinearVelocity().scl(0.1f));
            }
        }

        if (dashCooldown > 0) {
            dashCooldown -= deltaTime;
        }

        if (state == State.IDLE || state == State.RUNNING_HORIZONTAL || state == State.RUNNING_DOWN) {
            if (dashCooldown <= 0 && distance <= DETECTION_RANGE) {
                SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());

                Vector2 force = steering.linear.scl(body.getMass());
                body.applyForceToCenter(force, true);

                updateMovementState();
            }

            ai.update(deltaTime, body, myPos);
            updateMovementState();
            if (dashCooldown <= 0 && distance <= ATTACK_RANGE) {
                executeDashAttack(playerPos);
            }

        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        damageTimer = damageAnimationDuration;
        isTakingDamage = true;
        state = State.GOT_DAMAGE;
    }

    public boolean isTakingDamage() {
        return isTakingDamage;
    }

    private void updateMovementState() {
        Vector2 velocity = body.getLinearVelocity();

        if (velocity.isZero(ZERO_LINEAR_SPEED_THRESHOLD)) {
            state = State.IDLE;
        } else {
            boolean isVerticalMovement = Math.abs(velocity.y) > Math.abs(velocity.x);

            if (isVerticalMovement) {
                state = State.RUNNING_DOWN;
                directionY = velocity.y > 0 ? -1 : 1;
            } else {
                state = State.RUNNING_HORIZONTAL;
                directionX = velocity.x > 0 ? 1 : -1;
            }
        }
    }

    @Override
    public float getAttackDamage() {
        return 10f;
    }

    public float getDamageAnimationTime() {
        return damageAnimationDuration - damageTimer;
    }

    public State getState() {
        return state;
    }

    public int getDirectionX() {
        return directionX;
    }

    public int getDirectionY() {
        return directionY;
    }

    public boolean isMovingDown() {
        return movingDown;
    }

    private void executeDashAttack(Vector2 targetPos) {
        dashTargetDirection = targetPos.cpy().sub(body.getPosition()).nor();
        state = State.PREPARING_DASH;
        prepareDashTimer = PREPARE_DASH_DURATION;
        body.setLinearVelocity(0, 0);
    }

    public TextureRegion getCurrentFrame(float deltaTime) {
        animationTime += deltaTime;
        return ratAnimation.getKeyFrame(animationTime);
    }

    public void debugDraw(ShapeRenderer renderer, float offsetX, float offsetY) {
        Vector2 position = body.getPosition(); // em metros
        float angle = body.getAngle();

        // Pega shape do fixture
        PolygonShape shape = (PolygonShape) body.getFixtureList().first().getShape();

        int vcount = shape.getVertexCount();
        Vector2[] verts = new Vector2[vcount];

        // Converte cada vértice de “metros” para “pixels” e aplica rotação
        for (int i = 0; i < vcount; i++) {
            Vector2 local = new Vector2();
            shape.getVertex(i, local);

            // Rotaciona em torno da origem do body e converte pra pixel
            float worldX = (local.x * TILE_SIZE) * MathUtils.cos(angle)
                    - (local.y * TILE_SIZE) * MathUtils.sin(angle);
            float worldY = (local.x * TILE_SIZE) * MathUtils.sin(angle)
                    + (local.y * TILE_SIZE) * MathUtils.cos(angle);

            verts[i] = new Vector2(
                    offsetX + (position.x * TILE_SIZE) + worldX,
                    offsetY + (position.y * TILE_SIZE) + worldY);
        }

        renderer.setColor(Color.RED);
        // Desenha linhas entre vértices
        for (int i = 0; i < vcount; i++) {
            Vector2 a = verts[i];
            Vector2 b = verts[(i + 1) % vcount];
            renderer.line(a, b);
        }
    }

    @Override
    public void die(DeathType type) {
        if (isDead)
            return;

        isDead = true;
        deathType = type;
        deathAnimationTime = 0;
        state = type == DeathType.MELEE ? State.MELEE_DEATH : State.PROJECTILE_DEATH;
        disableCollisions();
    }

    public DeathType getDeathType() {
        return deathType;
    }

    public float getDeathAnimationTime() {
        return deathAnimationTime;
    }

    public boolean isDead() {
        return isDead;
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
        return 0.5f;
    }

    @Override
    public boolean isTagged() {
        return false;
    }

    @Override
    public void setTagged(boolean tagged) {
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(body.getPosition(), orientation);
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return ZERO_LINEAR_SPEED_THRESHOLD;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return 0;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
    }

    @Override
    public float getMaxAngularAcceleration() {
        return 0;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(-vector.x, vector.y);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.set((float) Math.cos(angle), (float) Math.sin(angle));
        return outVector;
    }

    @Override
    public ShadowComponent getShadowComponent() {
        return shadowComponent;
    }

    public void safeDeactivate() {
        if (shouldDeactivate) {
            getBody().setActive(false);
            shouldDeactivate = false;
        }
    }

    public void setRenderer(RatRenderer renderer) {
        this.renderer = renderer;
    }

    public void markForDestruction() {
        this.markedForDestruction = true;
    }

    public boolean isMarkedForDestruction() {
        return markedForDestruction;
    }

    public boolean isDying() {
        return isDead && !isDeathAnimationFinished();
    }

    public List<Vector2> getCurrentPath() {
        return ai != null ? ai.getCurrentPath() : null;
    }

    @Override
    protected void disableCollisions() {
        super.disableCollisions();

        state = (deathType == DeathType.MELEE) ? State.MELEE_DEATH : State.PROJECTILE_DEATH;
    }

    @Override
    public boolean isDeathAnimationFinished() {
        float duration = (deathType == DeathType.MELEE) ? renderer.getMeleeDeathDuration()
                : renderer.getProjectileDeathDuration();
        return deathAnimationTime >= duration;
    }

}