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




public class Ratinho extends Enemy implements Steerable<Vector2> {
    
    private final Mapa mapa;
    private final Body body;
    private final Pursue<Vector2> pursueBehavior;
    public final Robertinhoo target;
    


    



    private float maxLinearSpeed = 4f;       // Velocidade aumentada
    private float maxLinearAcceleration = 2f; // Resposta mais rápida
    private static final float ZERO_LINEAR_SPEED_THRESHOLD = 0.01f;

    public static final float DETECTION_RANGE = 10f; // Em tiles
    private static final float ATTACK_RANGE = 1.5f;
    public final Vector2 pos = new Vector2();
    // Adicione novos campos na classe Ratinho
    private static final float DASH_DURATION = 0.3f; // Tempo que o dash dura
    private static final float DASH_COOLDOWN = 2f;   // Tempo entre dashes
    private float dashTimer = 0f;
    private static final float DASH_FORCE = 10f;
    private boolean isDashing = false;
    private float dashCooldown = 0f;

    
    private Animation<TextureRegion> ratAnimation;
    private TextureRegion[] ratFrames;
    private float animationTime = 0f;
    
    public Ratinho(Mapa mapa, int x, int y, Robertinhoo target) {
        super(x, y, 20, 2);
        this.mapa = mapa;
        this.target = target;
        this.body = createBody(x, y);
        
        this.pursueBehavior = setupAI();
        loadAnimation();
        this.pursueBehavior.setOwner(this);

        body.setUserData(this); // Identificação do corpo
   
    }
    
    private Body createBody(int x, int y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y );
        
        Body body = mapa.world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.2f);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 2.0f; // Aumente para maior inércia
        fixtureDef.friction = 0.0f; // Reduza o atrito
        body.createFixture(fixtureDef);
        body.setLinearDamping(2f); // Amortecimento mínimo para movimento fluido
        body.setAngularDamping(2f); // Sem amortecimento angular
        shape.dispose();
    
        body.setUserData(this);
        
        return body;
    }

    @Override
    public Body getBody() {
        return this.body;
    }
    
    private Pursue<Vector2> setupAI() {
        Pursue<Vector2> pursue = new Pursue<>(this, target);
        pursue.setMaxPredictionTime(0.1f); // Foco na posição atual do alvo
        return pursue;
    }
    
    
    private void loadAnimation() {
        Texture ratSheet = new Texture("enemies/Enemy_rat-sheet.png");
        int frameWidth = ratSheet.getWidth() / 5;
        int frameHeight = ratSheet.getHeight();
        ratFrames = new TextureRegion[5];
        
        for (int i = 0; i < 5; i++) {
            ratFrames[i] = new TextureRegion(ratSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }
        
        ratAnimation = new Animation<>(0.2f, ratFrames);
        ratAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }



    
    

    public void update(float deltaTime) {
        Vector2 playerPos = target.getPosition();
        Vector2 myPos = body.getPosition();

    
        // 1. Calcula distância
        float distance = myPos.dst(playerPos);
    
        // 2. Atualiza timers
        if (isDashing) {
            dashTimer -= deltaTime;
            if (dashTimer <= 0) {
                isDashing = false;
                // Restaura a velocidade normal após o dash
                body.setLinearVelocity(body.getLinearVelocity().scl(0.2f));
            }
        }
        
        if (dashCooldown > 0) {
            dashCooldown -= deltaTime;
        }
    
        // 3. Lógica de perseguição (só ocorre se não estiver em dash)
        if (!isDashing && dashCooldown <= 0 && distance <= DETECTION_RANGE) {
            SteeringAcceleration<Vector2> steering = new SteeringAcceleration<>(new Vector2());
            pursueBehavior.calculateSteering(steering);
            
            Vector2 force = steering.linear.scl(body.getMass());
            body.applyForceToCenter(force, true);
        }
    
        // 4. Lógica de ataque
        if (!isDashing && dashCooldown <= 0 && distance <= ATTACK_RANGE) {
            executeDashAttack(playerPos);
        }

        
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

    // Adicione este método para debug visual
public void debugDraw(ShapeRenderer shapeRenderer) {
    // Desenha o círculo de detecção
    shapeRenderer.setColor(1, 0, 0, 0.3f); // Vermelho transparente
    shapeRenderer.circle(
        body.getPosition().x, 
        body.getPosition().y, 
        DETECTION_RANGE, 
        32 // Segmentos do círculo
    );

    // Desenha a linha de perseguição (do rato ao alvo)
    shapeRenderer.setColor(1, 1, 0, 1); // Amarelo
    shapeRenderer.line(
        body.getPosition(), 
        target.getPosition()
    );

    // Desenha a velocidade atual (vetor verde)
    shapeRenderer.setColor(0, 1, 0, 1); // Verde
    Vector2 velocity = body.getLinearVelocity().cpy().nor().scl(1f); // Normaliza e escala
    shapeRenderer.line(
        body.getPosition(), 
        body.getPosition().cpy().add(velocity)
    );
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