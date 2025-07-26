package io.github.some_example_name.Entities.Renderer.ItensRenderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;

public abstract class BaseDestructible implements Destructible {
    protected Vector2 position;
    protected TextureRegion intactTexture;
    protected TextureRegion destroyedTexture;
    protected boolean destroyed;
    protected ParticleEffect destructionEffect;

    
    public BaseDestructible(float x, float y, 
                           TextureRegion intactTexture, 
                           TextureRegion destroyedTexture) {
        this.position = new Vector2(x, y);
        this.intactTexture = intactTexture;
        this.destroyedTexture = destroyedTexture;
        this.destroyed = false;
    }
    
    @Override
    public Vector2 getPosition() {
        return position;
    }
    
    @Override
    public TextureRegion getTexture() {
        return destroyed ? destroyedTexture : intactTexture;
    }
    
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    
    @Override
    public void update(float delta) {
        if (destructionEffect != null) {
            destructionEffect.update(delta);
        }
    }
    

    public void setDestructionEffect(ParticleEffect effect) {
        this.destructionEffect = effect;
    }

    
    public void destroy() {
        if (!destroyed) {
            destroyed = true;
            if (destructionEffect != null) {
                destructionEffect.setPosition(position.x, position.y);
                destructionEffect.start();
            }
        }
    }

    public abstract void loadAssets();
    
    public void dispose() {
        if (destructionEffect != null) {
            destructionEffect.dispose();
        }
        intactTexture.getTexture().dispose();
    }
}