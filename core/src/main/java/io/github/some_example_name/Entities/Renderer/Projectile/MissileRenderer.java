package io.github.some_example_name.Entities.Renderer.Projectile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MissileRenderer {
    private Animation<TextureRegion> missileAnimation;
    private Animation<TextureRegion> explosionAnimation;
    private final Texture missileTexture;
    private final Texture explosionTexture;


    private final Map<Missile, Float> missileStateTimes = new HashMap<>();

    private static final int MISSILE_FRAMES = 3;    // sua informação: "roll com 3 colunas"
    private static final int EXPLOSION_FRAMES = 6;  // "roll com 6 colunas"
    private static final float EXPLOSION_TOTAL_DURATION = 0.7f; // duração total da explosão (você já usava 0.5f)

    public MissileRenderer() {
        missileTexture = new Texture(Gdx.files.internal("enemies/castor/BlueMissile.png"));
        explosionTexture = new Texture(Gdx.files.internal("enemies/castor/explosion_missile-Sheet.png"));

        // --- Criar animação do míssil (loop) ---
        int mfW = missileTexture.getWidth() / MISSILE_FRAMES;
        int mfH = missileTexture.getHeight();
        TextureRegion[][] missTmp = TextureRegion.split(missileTexture, mfW, mfH);
        TextureRegion[] missileFrames = new TextureRegion[MISSILE_FRAMES];
   
        System.arraycopy(missTmp[0], 0, missileFrames, 0, MISSILE_FRAMES);
        float missileFrameDuration = 0.1f;
        missileAnimation = new Animation<>(missileFrameDuration, missileFrames);
        missileAnimation.setPlayMode(Animation.PlayMode.LOOP);

        int efW = explosionTexture.getWidth() / EXPLOSION_FRAMES;
        int efH = explosionTexture.getHeight();
        TextureRegion[][] expTmp = TextureRegion.split(explosionTexture, efW, efH);
        TextureRegion[] explosionFrames = new TextureRegion[EXPLOSION_FRAMES];
        System.arraycopy(expTmp[0], 0, explosionFrames, 0, EXPLOSION_FRAMES);
        float explosionFrameDuration = EXPLOSION_TOTAL_DURATION / EXPLOSION_FRAMES;
        explosionAnimation = new Animation<>(explosionFrameDuration, explosionFrames);
        explosionAnimation.setPlayMode(Animation.PlayMode.NORMAL);
    }

    public void render(SpriteBatch batch, Missile missile, float offsetX, float offsetY) {
        // atualizar state time do míssil (guarda por instância)
        float delta = Gdx.graphics.getDeltaTime();
        missileStateTimes.put(missile, missileStateTimes.getOrDefault(missile, 0f) + delta);

        Vector2 position = missile.getPosition();
        float x = offsetX + position.x * 16f - 8f;
        float y = offsetY + position.y * 16f - 8f;

        if (!missile.isDestroying()) {
            // pega o frame atual da animação do míssil
            float stateTime = missileStateTimes.get(missile);
            TextureRegion frame = missileAnimation.getKeyFrame(stateTime, true);

            // desenha com rotação (origem no centro)
            batch.draw(
                frame,
                x, y,
                8f, 8f,    // origem (centro para rotação)
                9f, 9f,  // largura e altura desejadas
                1f, 1f,    // escala X e Y
                missile.getAngle() // rotação em graus
            );
        } else {
            float destroyTime = missile.getDestructionTime(); // tempo desde que começou a explodir
            TextureRegion explosionFrame = explosionAnimation.getKeyFrame(destroyTime, false);


            float explosionProgress = destroyTime / EXPLOSION_TOTAL_DURATION;
            if (explosionProgress > 1f) explosionProgress = 1f;

            // tamanho alvo da explosão (ajuste se quiser maior/menor)
            float maxExplosionSize = 32f;
            float explosionSize = maxExplosionSize * explosionProgress;
            if (explosionSize < 1f) explosionSize = 1f; // evita 0

            // desenha o frame centralizado, escalando conforme o tamanho
            float frameW = explosionFrame.getRegionWidth();
            float frameH = explosionFrame.getRegionHeight();
            float scaleX = explosionSize / frameW;
            float scaleY = explosionSize / frameH;

            // posição centralizada
            float explosionX = x + 8f - (frameW * scaleX) / 2f;
            float explosionY = y + 8f - (frameH * scaleY) / 2f;

            batch.draw(
                explosionFrame,
                explosionX, explosionY,
                0f, 0f, // origem interna (já centralizamos via explosionX/Y)
                frameW, frameH,
                scaleX, scaleY,
                0f
            );

            // se a animação de explosão terminou, podemos limpar o stateTime deste míssil
            if (explosionAnimation.isAnimationFinished(destroyTime)) {
                missileStateTimes.remove(missile);
            }
        }
    }

    public void dispose() {
        missileTexture.dispose();
        explosionTexture.dispose();
    }
}
