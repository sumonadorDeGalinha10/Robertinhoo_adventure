package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;



public class ProjectileRenderer {
    private final Mapa mapa;
    private final Animation<TextureRegion> shootAnimation;
    private final Texture projectileTexture;
    private final int tileSize;
    private final Animation<TextureRegion> destructionAnimation;

    private final Texture destructionTexture;

    public ProjectileRenderer(Mapa mapa, int tileSize){
        this.mapa = mapa;
        this.tileSize = tileSize;
     
        
        projectileTexture = new Texture("ITENS/Pistol/newShoot.png");
        destructionTexture = new Texture("ITENS/Pistol/SmallExplosion1-Sheet.png");

        System.out.println(projectileTexture);
        this.shootAnimation = createAnimation(projectileTexture, 5, 0.2f);
        this.destructionAnimation = createAnimation(destructionTexture, 8, 0.1f);

    }

    private Animation<TextureRegion> createAnimation(Texture texture, int frameCount, float frameDuration) {
        int frameWidth = texture.getWidth() / frameCount;
        int frameHeight = texture.getHeight();
        
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight);
        }
        
        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL); 
        return animation;
    }

    public void render(SpriteBatch batch, float delta, float offsetX, float offsetY) {
        for (Projectile projectile : mapa.getProjectiles()) {
            // Determina qual animação usar
            Animation<TextureRegion> currentAnimation;
            float animationTime;
            float drawAngle;
    
            if (projectile.isDestroying()) {
                currentAnimation = destructionAnimation;
                animationTime = projectile.getDestructionTime();
                drawAngle = projectile.destructionAngle; // Usa o ângulo no momento da colisão
            } else {
                currentAnimation = shootAnimation;
                animationTime = projectile.getStateTime();
                drawAngle = projectile.getAngle(); // Ângulo normal
            }
    
            // Obtém o frame apropriado
            TextureRegion frame = currentAnimation.getKeyFrame(animationTime, false);
    
            // Configurações de tamanho e posição
            float width = projectile.getWidth() * tileSize;
            float height = projectile.getHeight() * tileSize;
            
            float x = offsetX + projectile.getPosition().x * tileSize - width / 2;
            float y = offsetY + projectile.getPosition().y * tileSize - height / 2;
    
            // Desenha o projétil
            batch.draw(
                frame,
                x,
                y,
                width / 2,
                height / 2,
                width,
                height,
                1, 
                1,
                drawAngle
            );
    
            // Atualiza o stateTime apenas se não estiver destruindo
            if (!projectile.isDestroying()) {
                projectile.updateStateTime(delta);
            }
        }
    }

    public void dispose() {
    
        projectileTexture.dispose();
        destructionTexture.dispose();

        
    }
}