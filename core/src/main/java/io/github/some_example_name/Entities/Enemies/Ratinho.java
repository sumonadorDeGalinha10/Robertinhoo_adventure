package io.github.some_example_name.Entities.Enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Mapa;
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

    private static final float DASH_DURATION = 0.2f;
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

  
    
    public Ratinho(Mapa mapa, int x, int y, Robertinhoo target) {
        super(x, y, 20, 2);
        this.mapa = mapa;
        this.target = target;
        this.body = createBody(x, y);
        
        this.pursueBehavior = setupAI();
        this.pursueBehavior.setOwner(this);

        body.setUserData(this); // Identificação do corpo
    }
    
    private Body createBody(int x, int y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f);
        
        Body body = mapa.world.createBody(bodyDef);
        
      
        float halfWidth = 0.2f;
        float halfHeight = 0.2f;
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 2.0f;
        fixtureDef.friction = 0.0f;
        
        body.createFixture(fixtureDef);
        shape.dispose();
        
        body.setLinearDamping(2f);
        body.setAngularDamping(2f);
        body.setUserData("ENEMY");
        
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
        IDLE, RUNNING_HORIZONTAL, RUNNING_DOWN,GOT_DAMAGE
    }
    

    public float getAnimationTime() {
        return animationTime;
    }

    public void update(float deltaTime) {
        Vector2 playerPos = target.getPosition();
        Vector2 myPos = body.getPosition();

    

        float distance = myPos.dst(playerPos);

  
           if(isTakingDamage) {
            damageTimer -= deltaTime;
            if(damageTimer <= 0) {
                isTakingDamage = false;
                state = State.IDLE; 
            }
        }

        if(!isTakingDamage) {

        

    
  
        if (isDashing) {
            dashTimer -= deltaTime;
            if (dashTimer <= 0) {
                isDashing = false;
              
                body.setLinearVelocity(body.getLinearVelocity().scl(0.2f));
            }
        }
        
        if (dashCooldown > 0) {
            dashCooldown -= deltaTime;
        }
    

        if (!isDashing && dashCooldown <= 0 && distance <= DETECTION_RANGE) {
            SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
            pursueBehavior.calculateSteering(steering);
            
            Vector2 force = steering.linear.scl(body.getMass());
            body.applyForceToCenter(force, true);
        }

        if (!isDashing && dashCooldown <= 0 && distance <= ATTACK_RANGE) {
            executeDashAttack(playerPos);
        }

    }

        animationTime += deltaTime;
        updateState();

        
    }


    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        // Reinicia o timer e ativa o estado
        damageTimer = damageAnimationDuration;
        isTakingDamage = true;
        state = State.GOT_DAMAGE;
    }

 



    public void updateState() {
        if(isTakingDamage) return;
        Vector2 velocity = body.getLinearVelocity();
        
        if (velocity.isZero(ZERO_LINEAR_SPEED_THRESHOLD)) {
            state = State.IDLE;
        } else {
            // Verifica se o movimento é predominante vertical
            boolean isVerticalMovement = Math.abs(velocity.y) > Math.abs(velocity.x);
            
            if(isVerticalMovement) {
                state = State.RUNNING_DOWN;
                // Define direção Y baseada no sinal da velocidade
                directionY = velocity.y > 0 ? -1 : 1;
            } else {
                state = State.RUNNING_HORIZONTAL;
                directionX = velocity.x > 0 ? 1 : -1;
            }
        }
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
        Vector2 direction = targetPos.cpy().sub(body.getPosition()).nor();
        
        // Defina uma velocidade fixa para o dash
        body.setLinearVelocity(direction.scl(maxLinearSpeed * 2)); // 5x a velocidade normal
        
        isDashing = true;
        dashTimer = DASH_DURATION; // Inicia a duração do dash
        dashCooldown = DASH_COOLDOWN; // Inicia o cooldown
    }
    public TextureRegion getCurrentFrame(float deltaTime) {
        animationTime += deltaTime;
        return ratAnimation.getKeyFrame(animationTime);
    }


    public void debugDraw(ShapeRenderer shapeRenderer) {
      
        Vector2 position = body.getPosition();
        float angle = body.getAngle();
        
      
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        
   
        PolygonShape shape = (PolygonShape) body.getFixtureList().first().getShape();
        
  
        Vector2[] vertices = new Vector2[shape.getVertexCount()];
        for(int i = 0; i < shape.getVertexCount(); i++) {
            Vector2 vertex = new Vector2();
            shape.getVertex(i, vertex);
            // Aplica rotação e posição
            vertices[i] = vertex.cpy().rotateRad(angle).add(position);
        }
        
    
        for(int i = 0; i < vertices.length; i++) {
            Vector2 current = vertices[i];
            Vector2 next = vertices[(i + 1) % vertices.length];
            shapeRenderer.line(current, next);
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
    public void setTagged(boolean tagged) {}
    
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
    public void setZeroLinearSpeedThreshold(float value) {}
    
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
    public float getMaxAngularSpeed() { return 0; }
    
    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {}
    
    @Override
    public float getMaxAngularAcceleration() { return 0; }
    
    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {}
    
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