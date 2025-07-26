package io.github.some_example_name.Entities.Enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import com.badlogic.gdx.graphics.Color;

public class Ratinho extends Enemy implements Steerable<Vector2> {

    private final Mapa mapa;
    private final Body body;
    private final Pursue<Vector2> pursueBehavior;
    public final Robertinhoo target;

    private float maxLinearSpeed = 4f;
    private float maxLinearAcceleration = 2f;
    private static final float ZERO_LINEAR_SPEED_THRESHOLD = 0.01f;

    public static final float DETECTION_RANGE = 10f;
    private static final float ATTACK_RANGE = 1.5f;
    public final Vector2 pos = new Vector2();

    private static final float DASH_DURATION = 0.4f;
    private static final float DASH_COOLDOWN = 2f;
    private float dashTimer = 0f;
    private static final float DASH_FORCE = 10f;
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

    private static final float PREPARE_DASH_DURATION = 0.8f; // Tempo de preparação
    private static final float DASH_SPEED_MULTIPLIER = 1.5f;
    private float prepareDashTimer = 0f;
    private Vector2 dashTargetDirection; //

    public Ratinho(Mapa mapa, int x, int y, Robertinhoo target) {
        super(x, y, 20, 2);
        this.mapa = mapa;
        this.target = target;
        this.body = createBody(x, y);

        this.pursueBehavior = setupAI();
        this.pursueBehavior.setOwner(this);

        body.setUserData(this);
    }

  private Body createBody(int x, int y) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.set(x + 0.5f, y + 0.5f);
    bodyDef.fixedRotation = true;  

    Body body = mapa.world.createBody(bodyDef);

    // calcula meados baseado no sprite real (12px) e TILE_SIZE (16px)
    float halfWidth  = (6f / 3f) / 12;  // 6 / 16 = 0.375
    float halfHeight = (6f / 3f) / 12;  // 6 / 16 = 0.375

    PolygonShape shape = new PolygonShape();
    shape.setAsBox(halfWidth, halfHeight);

    FixtureDef fd = new FixtureDef();
    fd.shape = shape;
    fd.density  = 2f;
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

    private Pursue<Vector2> setupAI() {
        Pursue<Vector2> pursue = new Pursue<>(this, target);
        pursue.setMaxPredictionTime(0.1f);
        return pursue;
    }

    public enum State {
        IDLE, RUNNING_HORIZONTAL, RUNNING_DOWN, GOT_DAMAGE,
        PREPARING_DASH,
        DASHING
    }

    public float getAnimationTime() {
        return animationTime;
    }

public void update(float deltaTime) {
    Vector2 playerPos = target.getPosition();
    Vector2 myPos = body.getPosition();
    float distance = myPos.dst(playerPos);

    // Sempre atualiza o tempo de animação
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
    } 
    else if (state == State.DASHING) {
        dashTimer -= deltaTime;
        if (dashTimer <= 0) {
            state = State.IDLE;
            dashCooldown = DASH_COOLDOWN;
  body.setLinearVelocity(body.getLinearVelocity().scl(0.1f)); // Reduzido de 0.2 para 0.1
        }
    }

    if (dashCooldown > 0) {
        dashCooldown -= deltaTime;
    }

    if (state == State.IDLE || state == State.RUNNING_HORIZONTAL || state == State.RUNNING_DOWN) {
        if (dashCooldown <= 0 && distance <= DETECTION_RANGE) {
            SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
            pursueBehavior.calculateSteering(steering);

            Vector2 force = steering.linear.scl(body.getMass());
            body.applyForceToCenter(force, true);
            
            updateMovementState();
        }

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
        return 10f; // Dano base do ratinho
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
    // Calcula direção do ataque
    dashTargetDirection = targetPos.cpy().sub(body.getPosition()).nor();
    
    // Entra no estado de preparação
    state = State.PREPARING_DASH;
    prepareDashTimer = PREPARE_DASH_DURATION;
    
    // Para o movimento durante a preparação
    body.setLinearVelocity(0, 0);
}

    public TextureRegion getCurrentFrame(float deltaTime) {
        animationTime += deltaTime;
        return ratAnimation.getKeyFrame(animationTime);
    }

public void debugDraw(ShapeRenderer renderer, float offsetX, float offsetY) {
    Vector2 position = body.getPosition();    // em metros
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
        float worldX = (local.x * 16) * MathUtils.cos(angle)
                     - (local.y * 16) * MathUtils.sin(angle);
        float worldY = (local.x * 16) * MathUtils.sin(angle)
                     + (local.y * 16) * MathUtils.cos(angle);

        verts[i] = new Vector2(
            offsetX + (position.x * 16) + worldX,
            offsetY + (position.y * 16) + worldY
        );
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
}