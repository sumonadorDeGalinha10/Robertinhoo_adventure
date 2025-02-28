package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;;

public class ProjectileRenderer {
    private final Mapa mapa;
    private final Animation<TextureRegion> shootAnimation;
    private final Texture projectileTexture;
    private final int tileSize;
    private final Texture glowTexture; // Nova textura para o brilho
    private final float GLOW_SCALE = 1.2f; // Escala do brilho em relação ao projétil
    private final TextureRegion glowRegion;

    public ProjectileRenderer(Mapa mapa, int tileSize) {
        this.mapa = mapa;
        this.tileSize = tileSize;
        
        projectileTexture = new Texture("ITENS/Pistol/shoot.png");
        System.out.println(projectileTexture);
        this.shootAnimation = createAnimation(projectileTexture, 4, 0.2f);
        glowTexture = new Texture("ITENS/Pistol/glow_yellow.png");
        glowRegion = new TextureRegion(glowTexture); 
        
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
            TextureRegion frame = shootAnimation.getKeyFrame(projectile.getStateTime(), false);
            float pulse = (float) (Math.sin(projectile.getStateTime() * 10) * 0.2f + 0.8f);
               // Renderiza o brilho primeiro
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // Modo de blending aditivo
            renderGlow(batch, projectile, offsetX, offsetY, pulse);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Reset par
            
            float width = projectile.getWidth() * tileSize;
            float height = projectile.getHeight() * tileSize;
            
        
            float x = offsetX + projectile.getPosition().x * tileSize - width / 2;
            float y = offsetY + projectile.getPosition().y * tileSize - height / 2;
            
   
            batch.draw(
                frame,
                x,
                y,
                width / 2,
                height / 2,
                width,
                height,
                1, 1,
                projectile.getAngle()
            );
            
            projectile.updateStateTime(delta);
        }
    }


    private void renderGlow(SpriteBatch batch, Projectile projectile, float offsetX, float offsetY, float alphaMultiplier) {
        float scaleFactor = 0.5f; // Reduz o tamanho em 20%
        float baseSize = Math.max(projectile.getWidth(), projectile.getHeight()) * tileSize * scaleFactor;
        float glowWidth = baseSize * GLOW_SCALE;  // GLOW_SCALE pode ser ajustado para 1.2f, por exemplo
        float glowHeight = baseSize * GLOW_SCALE;
    
        float x = offsetX + (projectile.getPosition().x * tileSize) - glowWidth / 2;
        float y = offsetY + (projectile.getPosition().y * tileSize) - glowHeight / 2;

        batch.setColor(1, 0.2f, 0.9f, 0.4f * alphaMultiplier); // RGBA (amarelo + transparência)
        
        batch.draw(
            glowRegion,
            x,
            y,
            glowWidth / 2,
            glowHeight / 2,
            glowWidth,
            glowHeight,
            1,
            1,
            projectile.getAngle()
        );
        
        batch.setColor(1, 1, 1, 1); // Reset da cor
    }
    

    public void dispose() {
        glowTexture.dispose();
        projectileTexture.dispose();
    }
}