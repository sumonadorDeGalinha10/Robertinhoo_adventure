package io.github.some_example_name.Entities.Itens.Weapon;


import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Mapa;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;

public abstract class Weapon {
    protected float fireRate; // Tiros por segundo
    protected float damage;
    protected int ammo;
    protected float timeSinceLastShot = 0f;
    public boolean canShoot = true;
    protected Vector2 position;
    public Body body;
    private float pulseTimer = 0;
    private Mapa mapa;


    

    public abstract void shoot(Vector2 position, Vector2 direction);
    public abstract void update(float delta);



    public float getFireRate() { return fireRate; }
    public float getDamage() { return damage; }
    public int getAmmo() { return ammo; }

    public abstract TextureRegion getCurrentFrame(float delta);
    public abstract Vector2 getPosition();
    public abstract void createBody(Vector2 position);
    

    public void destroyBody() {
      
    }

    public abstract Vector2 getMuzzleOffset();
    


}