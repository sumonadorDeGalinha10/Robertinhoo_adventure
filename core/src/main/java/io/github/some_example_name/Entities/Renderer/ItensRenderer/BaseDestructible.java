package io.github.some_example_name.Entities.Renderer.ItensRenderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;

public abstract class BaseDestructible implements Destructible {
    protected Vector2 position;
    protected TextureRegion intactTexture;
    protected TextureRegion destroyedTexture;
    protected boolean destroyed;
    protected ParticleEffect destructionEffect;
    protected Animation<TextureRegion> destructionAnimation;
    protected float animationTime = 0;
    protected boolean isAnimating = false;
    protected float flashTimer;
    protected boolean flashActive;
    public boolean isFlashActive() { return flashActive; }

    
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
    
public TextureRegion getTexture() {
    if (isAnimating) {
        TextureRegion frame = destructionAnimation.getKeyFrame(animationTime, false);
        if (frame == null) {
            Gdx.app.error("Barrel", "Frame de animação null! Tempo: " + animationTime);
        }
        return frame;
    }
    
    if (destroyed) {
        if (destroyedTexture == null) {
            Gdx.app.error("Barrel", "DestroyedTexture é null!");
        }
        return destroyedTexture != null ? destroyedTexture : intactTexture;
    }
    
    if (intactTexture == null) {
        Gdx.app.error("Barrel", "IntactTexture é null!");
    }
    return intactTexture;
}
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    
    @Override
    public void update(float delta) {
        if (isAnimating) {
            animationTime += delta;
        }
        
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

    public boolean isAnimationFinished() {
        return isAnimating && destructionAnimation.isAnimationFinished(animationTime);
    }
    
    public void startDestructionAnimation() {
        isAnimating = true;
        animationTime = 0;
    }
    
    public void dispose() {
        if (destructionEffect != null) {
            destructionEffect.dispose();
        }
        intactTexture.getTexture().dispose();
    }
}