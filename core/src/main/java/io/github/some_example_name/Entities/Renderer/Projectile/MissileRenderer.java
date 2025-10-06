package io.github.some_example_name.Entities.Renderer.Projectile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Itens.Weapon.Missile;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

public class MissileRenderer {
    private Animation<TextureRegion> missileAnimation;
    private Animation<TextureRegion> explosionAnimation;
    private final Texture missileTexture;
    private final Texture explosionTexture;
    private static final float TILE_SIZE = 64f;

    private final Map<Missile, Float> missileStateTimes = new HashMap<>();
    private final Map<Missile, Vector2> explosionPositions = new HashMap<>();
    private final Map<Missile, Float> explosionStartTimes = new HashMap<>();

    private static final int MISSILE_FRAMES = 3;
    private static final int EXPLOSION_FRAMES = 6;
    private static final float EXPLOSION_TOTAL_DURATION = 0.9f;
    private static final float EXPLOSION_SIZE = 96f;

    public MissileRenderer() {
        missileTexture = new Texture(Gdx.files.internal("enemies/castor/BlueMissile.png"));
        explosionTexture = new Texture(Gdx.files.internal("enemies/castor/explosion_missile-Sheet.png"));

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

        // 🔥 CORREÇÃO: Usar duração total consistente
        float explosionFrameDuration = EXPLOSION_TOTAL_DURATION / EXPLOSION_FRAMES;
        explosionAnimation = new Animation<>(explosionFrameDuration, explosionFrames);
        explosionAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        Gdx.app.log("MissileRenderer", "✅ Renderizador inicializado - Explosão: " +
                EXPLOSION_FRAMES + " frames, " + EXPLOSION_TOTAL_DURATION + "s, Tamanho: " + EXPLOSION_SIZE);
    }

    public void render(SpriteBatch batch, Missile missile, float offsetX, float offsetY) {
        float delta = Gdx.graphics.getDeltaTime();
        float stateTime = missileStateTimes.getOrDefault(missile, 0f) + delta;
        missileStateTimes.put(missile, stateTime);

        Vector2 position = missile.getPosition();
        float x = offsetX + position.x * TILE_SIZE - 8f;
        float y = offsetY + position.y * TILE_SIZE - 8f;

        if (!missile.isDestroying()) {
            TextureRegion frame = missileAnimation.getKeyFrame(stateTime, true);

            batch.draw(
                    frame,
                    x, y,
                    8f, 8f,
                    40f, 40f,
                    1f, 1f,
                    missile.getAngle());

            // 🔥 CORREÇÃO: Armazenar posição atual para explosão
            explosionPositions.put(missile, new Vector2(position));
        } else {
            // 🔥 CORREÇÃO: Inicializar tempo da explosão se necessário
            if (!explosionStartTimes.containsKey(missile)) {
                explosionStartTimes.put(missile, 0f);
                Gdx.app.log("MissileRenderer", "💥 Iniciando animação de explosão");
            }

            float explosionTime = explosionStartTimes.get(missile) + delta;
            explosionStartTimes.put(missile, explosionTime);

            Vector2 explosionPosition = explosionPositions.get(missile);
            if (explosionPosition == null) {
                explosionPosition = position;
            }

            float explosionX = offsetX + explosionPosition.x * TILE_SIZE;
            float explosionY = offsetY + explosionPosition.y * TILE_SIZE;

            TextureRegion explosionFrame = explosionAnimation.getKeyFrame(explosionTime, false);

            // 🔥 SIMPLIFICAÇÃO: Tamanho fixo maior para a explosão
            float explosionSize = EXPLOSION_SIZE;

            // 🔥 CORREÇÃO: Centralizar a explosão
            float drawX = explosionX - explosionSize / 2f;
            float drawY = explosionY - explosionSize / 2f;

            batch.draw(
                    explosionFrame,
                    drawX, drawY,
                    explosionSize, explosionSize);

            // 🔥 CORREÇÃO: Verificar se a animação terminou
            if (explosionAnimation.isAnimationFinished(explosionTime)) {
                missileStateTimes.remove(missile);
                explosionPositions.remove(missile);
                explosionStartTimes.remove(missile);
                Gdx.app.log("MissileRenderer", "💨 Animação de explosão terminada - " + explosionTime + "s");
            } else {
                // 🔥 DEBUG: Log do progresso
                int currentFrame = explosionAnimation.getKeyFrameIndex(explosionTime);
                float progress = explosionTime / EXPLOSION_TOTAL_DURATION;
                if (currentFrame % 2 == 0) { // Log a cada 2 frames
                    Gdx.app.log("MissileRenderer", "💥 Explosão - Frame: " + currentFrame +
                            "/" + (EXPLOSION_FRAMES - 1) + " Progresso: " + (int) (progress * 100) + "%");
                }
            }
        }
    }

    public void dispose() {
        missileTexture.dispose();
        explosionTexture.dispose();
        Gdx.app.log("MissileRenderer", "♻️ Renderizador descartado");
    }
}