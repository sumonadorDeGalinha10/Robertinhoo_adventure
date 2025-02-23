package io.github.some_example_name.Entities.Enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class Enemy {
    

    protected Vector2 position;
    protected float health;
    protected float speed;
    protected boolean isAlive;

    public Enemy(float x, float y, float health, float speed) {
        this.position = new Vector2(x, y);
        this.health = health;
        this.speed = speed;
        this.isAlive = true;


        
    }
    public abstract void update(float deltaTime);
    public abstract TextureRegion getCurrentFrame(float deltaTime);

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            isAlive = false;
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isAlive() {
        return isAlive;
    }
}