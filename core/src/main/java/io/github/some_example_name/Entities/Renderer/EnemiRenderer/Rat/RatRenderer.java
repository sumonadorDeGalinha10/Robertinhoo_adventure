package io.github.some_example_name.Entities.Renderer.EnemiRenderer.Rat;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Enemies.Rat.Ratinho;
import io.github.some_example_name.Entities.Enemies.Rat.Ratinho.State;

import com.badlogic.gdx.graphics.Color;

public class RatRenderer {
    private final Texture ratSpriteSheet;
    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runHorizontalAnimation;
    private final Animation<TextureRegion> gotDamageAnimation;
    private final Animation<TextureRegion> prepareDashAnimation;
    private final Animation<TextureRegion> dashAnimation;
    private final Texture deathSheet;
    private final Animation<TextureRegion> meleeDeathAnimation;
    private final Animation<TextureRegion> projectileDeathAnimation;
    private static final float TILE_SIZE =64f;

    private static final float RAT_WIDTH = TILE_SIZE;
    private static final float RAT_HEIGHT = TILE_SIZE;
    private static final float RAT_RENDER_WIDTH = 40f;
    private static final float RAT_RENDER_HEIGHT = 40f;
    private static final int SHEET_COLS = 6;
    private static final int SHEET_ROWS = 5;

    public RatRenderer() {
        ratSpriteSheet = new Texture("enemies/rat/NewRat.png");
        deathSheet = new Texture("enemies/rat/rat_die.png");

        idleAnimation = createAnimation(2, 6, 0.2f);
        runHorizontalAnimation = createAnimation(3, 6, 0.05f);
        gotDamageAnimation = createAnimation(4, 4, 0.1f);
        prepareDashAnimation = createAnimation(1, 6, 0.17f);
        dashAnimation = createAnimation(0, 6, 0.08f);
        meleeDeathAnimation = createDeathAnimation(0, 7, 0.1f);
        projectileDeathAnimation = createDeathAnimation(1, 7, 0.1f);
    }

    private Animation<TextureRegion> createAnimation(int row, int frames, float speed) {
        TextureRegion[] regions = new TextureRegion[frames];
        int frameWidth = ratSpriteSheet.getWidth() / SHEET_COLS;
        int frameHeight = ratSpriteSheet.getHeight() / SHEET_ROWS;

        for (int i = 0; i < frames; i++) {
            regions[i] = new TextureRegion(
                    ratSpriteSheet,
                    i * frameWidth,
                    row * frameHeight,
                    frameWidth,
                    frameHeight);
        }
        return new Animation<>(speed, regions);
    }

    private Animation<TextureRegion> createDeathAnimation(int row, int frames, float speed) {
        TextureRegion[] regions = new TextureRegion[frames];
        int frameWidth = deathSheet.getWidth() / 7;
        int frameHeight = deathSheet.getHeight() / 2;

        for (int i = 0; i < frames; i++) {
            regions[i] = new TextureRegion(
                    deathSheet,
                    i * frameWidth,
                    row * frameHeight,
                    frameWidth,
                    frameHeight);
        }
        return new Animation<>(speed, regions);
    }

    public void render(SpriteBatch batch, float delta, Ratinho rat, float offsetX, float offsetY) {
        rat.update(delta);
        boolean flip = shouldFlip(rat);

       if (!rat.isDead() || rat.isDying() )  {
            TextureRegion frame = getFrame(rat, flip);
            Vector2 renderPos = calculateRenderPosition(rat);
            Vector2 renderSize = calculateRenderSize(rat);

            if (rat.isTakingDamage()) {
            float t = rat.getDamageAnimationTime();
            float flashPeriod = 0.1f;
            int phase = (int)(t / flashPeriod) % 2;
            if (phase == 0) {
                batch.setColor(1f, 0.5f, 0.5f, 1f);
            }
        }
            batch.draw(
                    frame,
                    offsetX + renderPos.x * TILE_SIZE,
                    offsetY + renderPos.y * TILE_SIZE,
                    renderSize.x,
                    renderSize.y);

            batch.setColor(Color.WHITE);
        }

    }

    private boolean shouldFlip(Ratinho rat) {
        return rat.getDirectionX() < 0 &&
                (rat.getState() == State.RUNNING_HORIZONTAL ||
                        rat.getState() == State.PREPARING_DASH ||
                        rat.getState() == State.DASHING);
    }

public TextureRegion getFrame(Ratinho rat, boolean flip) {
    TextureRegion originalFrame = null;

    // Primeiro verifique se está morrendo
    if (rat.isDying()) {
        switch (rat.getState()) {
            case MELEE_DEATH:
                originalFrame = meleeDeathAnimation.getKeyFrame(rat.getDeathAnimationTime(), true);
                break;
            case PROJECTILE_DEATH:
                originalFrame = projectileDeathAnimation.getKeyFrame(rat.getDeathAnimationTime(), true);
                break;
        }
    }
    
    // Se não está morrendo ou não encontrou frame de morte, verifica outros estados
    if (originalFrame == null) {
        switch (rat.getState()) {
            case RUNNING_HORIZONTAL:
                originalFrame = runHorizontalAnimation.getKeyFrame(rat.getAnimationTime(), true);
                break;

            case RUNNING_DOWN:
                originalFrame = runHorizontalAnimation.getKeyFrame(rat.getAnimationTime(), true);
                break;
            case GOT_DAMAGE:
                originalFrame = gotDamageAnimation.getKeyFrame(rat.getDamageAnimationTime(), false);
                break;
            case PREPARING_DASH:
                originalFrame = prepareDashAnimation.getKeyFrame(rat.getAnimationTime(), true);
                break;
            case DASHING:
                originalFrame = dashAnimation.getKeyFrame(rat.getAnimationTime(), true);
                break;
            default:
                originalFrame = idleAnimation.getKeyFrame(rat.getAnimationTime(), true);
        }
    }

    // Garante que sempre teremos um frame
    if (originalFrame == null) {
        originalFrame = idleAnimation.getKeyFrame(0, true);
    }

    TextureRegion frame = new TextureRegion(originalFrame);
    if (flip) {
        frame.flip(true, false);
    }

    return frame;
}

    public Vector2 calculateRenderPosition(Ratinho rat) {
        Vector2 position = new Vector2(rat.getPosition());

        switch (rat.getState()) {
            case DASHING:
                position.x -= 0.1f;
                position.y -= 0.05f;
                break;
            case PREPARING_DASH:
                position.x += 0.05f;
                break;
            case GOT_DAMAGE:
                position.y -= 0.03f;
                break;
        }

        position.x -= (RAT_RENDER_WIDTH / RAT_WIDTH) / 2f;
        position.y -= (RAT_RENDER_HEIGHT / RAT_HEIGHT) / 2f;

        return position;
    }

    public Vector2 calculateRenderSize(Ratinho rat) {
        Vector2 size = new Vector2(RAT_RENDER_WIDTH, RAT_RENDER_HEIGHT);

        if (rat.getState() == State.DASHING) {
            size.x *= 1.1f;
            size.y *= 0.95f;
        }

        return size;
    }

    public TextureRegion getCorpseFrame(Ratinho rat) {

        switch (rat.getDeathType()) {
            case MELEE:
                return meleeDeathAnimation.getKeyFrames()[6]; // Último frame (índice 6)
            case PROJECTILE:
                return projectileDeathAnimation.getKeyFrames()[6]; // Último frame (índice 6)
            default:
                return idleAnimation.getKeyFrames()[0];
        }
    }

    public void dispose() {
        ratSpriteSheet.dispose();
    }

    public float getMeleeDeathDuration() {
        return meleeDeathAnimation.getAnimationDuration();
    }

    public float getProjectileDeathDuration() {
        return projectileDeathAnimation.getAnimationDuration();
    }

    public float getRatRenderWidth() {
        return RAT_RENDER_WIDTH;
    }

    public float getRatRenderHeight() {
        return RAT_RENDER_HEIGHT;
    }

    public boolean flipou(Ratinho rat) {
        // Implementação real da lógica de flip
        return rat.getDirectionX() < 0;
    }

    private Vector2 calculateCorpsePosition(Ratinho rat) {
    Vector2 position = new Vector2(rat.getPosition());
    position.x -= (RAT_RENDER_WIDTH / RAT_WIDTH) / 2f;
    position.y -= (RAT_RENDER_HEIGHT / RAT_HEIGHT) / 2f;
    return position;
}

private Vector2 calculateCorpseSize(Ratinho rat) {
    return new Vector2(RAT_RENDER_WIDTH, RAT_RENDER_HEIGHT);
}


public Vector2 calculateRenderOffset(Ratinho rat) {
    Vector2 offset = new Vector2();
    
    // Aplica os mesmos ajustes de posição usados na renderização
    switch (rat.getState()) {
        case DASHING:
            offset.x = -0.1f;
            offset.y = -0.05f;
            break;
        case PREPARING_DASH:
            offset.x = 0.05f;
            break;
        case GOT_DAMAGE:
            offset.y = -0.03f;
            break;
    }
    
    // Aplica o offset centralizador
    offset.x -= (RAT_RENDER_WIDTH / RAT_WIDTH) / 2f;
    offset.y -= (RAT_RENDER_HEIGHT / RAT_HEIGHT) / 2f;
    
    return offset;
}

}