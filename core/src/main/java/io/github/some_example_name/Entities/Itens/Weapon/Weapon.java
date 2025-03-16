package io.github.some_example_name.Entities.Itens.Weapon;


import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.Vector2;


import com.badlogic.gdx.physics.box2d.Body;


import io.github.some_example_name.Mapa;


public abstract class Weapon {
    protected float fireRate; // Tiros por segundo
    protected float damage;
    protected int ammo;
    protected float timeSinceLastShot = 0f;
    public boolean canShoot = true;
    protected Vector2 position;
    public Body body;

    protected TextureRegion icon;
    protected boolean reloading = false;
    protected float reloadProgress = 0;
    protected int maxAmmo; 

    public TextureRegion getIcon() {
        return icon;
    }

    public boolean isReloading() {
        return reloading;
    }

    public float getReloadProgress() {
        return reloadProgress;
    }
    public int getMaxAmmo() {
        return maxAmmo;
    }


    protected Weapon() {
        this.maxAmmo = 30;
    }

    

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