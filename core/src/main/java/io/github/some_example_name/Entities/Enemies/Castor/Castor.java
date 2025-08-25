package io.github.some_example_name.Entities.Enemies.Castor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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



public class Castor extends Enemy  implements ShadowEntity,Steerable<Vector2> {

    private Float damage;
    private final Mapa mapa;
    private final Body body;
    public final Robertinhoo target;
    private boolean markedForDestruction = false;
    private Animation<TextureRegion> ratAnimation;
    private float animationTime = 0f;
    private ShadowComponent shadowComponent;


    private boolean tagged;
    private float boundingRadius = 0.5f;
    private float maxLinearSpeed = 3f;
    private float maxLinearAcceleration = 5f;
    private float maxAngularSpeed = 5f;
    private float maxAngularAcceleration = 10f;
    private float zeroLinearSpeedThreshold = 0.01f;
    private CastorIA ai;

    private float shootCooldown = 0f;
    private static final float SHOOT_COOLDOWN_TIME = 4f;
    private static final float PROJECTILE_SPEED = 12f;
    private static final float PROJECTILE_DAMAGE = 25f;



    public Castor(Mapa mapa, float x, float y, Robertinhoo target) {
        super(x, y, 20, 2);
        this.mapa = mapa;
        this.target = target;
        this.body = createBody(x, y);
        this.damage = 15f;
        this.ai = new CastorIA(this, target, mapa.getPathfindingSystem());
    
        body.setUserData(this);
        this.shadowComponent = new ShadowComponent(
                5,
                2,
                -0.1f,
                0.7f,
                new Color(0.05f, 0.05f, 0.05f, 1f)
        );
    }


        private Body createBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + 0.5f, y + 0.5f);
        bodyDef.fixedRotation = true;

        Body body = mapa.world.createBody(bodyDef);

        // calcula meados baseado no sprite real (12px) e TILE_SIZE (16px)
        float halfWidth = (6f / 3f) / 12; // 6 / 16 = 0.375
        float halfHeight = (6f / 3f) / 12; // 6 / 16 = 0.375

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

public void shootAtPlayer() {
    if (shootCooldown <= 0 && target != null) {
        Vector2 direction = target.getPosition().cpy().sub(body.getPosition()).nor();
        
     
        float offsetDistance = 1.2f;
        Vector2 launchPosition = body.getPosition().cpy().add(direction.scl(offsetDistance));
        
        Missile missile = new Missile(
            mapa, 
            launchPosition,
            direction
        );
        
        mapa.addProjectile(missile);
        shootCooldown = SHOOT_COOLDOWN_TIME;
        
        Gdx.app.log("Castor", "Lançando míssil da posição: " + launchPosition);
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
            Gdx.app.log("Castor-Cooldown", "Cooldown: " + shootCooldown);
        }
        
        // Atualiza a IA
        ai.update(deltaTime, body);
        
        Gdx.app.log("Castor-Update", "Posição: " + body.getPosition() + 
                   ", Cooldown: " + shootCooldown);
    }
    
    public TextureRegion getCurrentFrame(float deltaTime) {
        animationTime += deltaTime;
        return ratAnimation.getKeyFrame(animationTime);
    }

    @Override
    public ShadowComponent getShadowComponent() {
        return shadowComponent;
    }


    /* ============================
       Métodos exigidos por Steerable/Location/Limiter
       ============================ */

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
        return (float)Math.atan2(vector.y, vector.x);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = (float)Math.cos(angle);
        outVector.y = (float)Math.sin(angle);
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
