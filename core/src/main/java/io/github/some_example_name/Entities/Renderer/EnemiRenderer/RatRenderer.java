package io.github.some_example_name.Entities.Renderer.EnemiRenderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.Entities.Enemies.Ratinho;
import io.github.some_example_name.Entities.Enemies.Ratinho.State;

public class RatRenderer {
    private final Texture idleSheet;
    private final Texture runHorizontalSheet;
    private final Texture runDownSheet;
    private final Texture gotDamageSheet;
    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runHorizontalAnimation;
    private final Animation<TextureRegion> runDownAnimation;
    private final Animation<TextureRegion> gotDamage;

    private static final float RAT_WIDTH = 16f;
    private static final float RAT_HEIGHT = 16f;
    private static final float RAT_RENDER_WIDTH = 12f;
    private static final float RAT_RENDER_HEIGHT = 12f;


    private final Texture prepareDashSheet;
    private final Texture dashSheet;
    private final Animation<TextureRegion> prepareDashAnimation;
    private final Animation<TextureRegion> dashAnimation;

    public RatRenderer() {
        idleSheet = new Texture("enemies/rat/MouseIdle.png");
        runHorizontalSheet = new Texture("enemies/rat/MouseRun.png");
        runDownSheet = new Texture("enemies/rat/rat_run_downimg.png");
        gotDamageSheet = new Texture("enemies/rat/MouseDamage.png");

        idleAnimation = createAnimation(idleSheet, 6, 0.2f);
        runHorizontalAnimation = createAnimation(runHorizontalSheet, 6, 0.1f);
        runDownAnimation = createAnimation(runDownSheet, 6, 0.1f);
        gotDamage = createAnimation(gotDamageSheet, 4, 0.1f);

        prepareDashSheet = new Texture("enemies/rat/rat_prepare_to_atacck -Sheet.png");
        dashSheet = new Texture("enemies/rat/atakk rat -Sheet.png");
        prepareDashAnimation = createAnimation(prepareDashSheet, 6, 0.1f);
        dashAnimation = createAnimation(dashSheet, 6, 0.08f);
    }

    private Animation<TextureRegion> createAnimation(Texture sheet, int frames, float speed) {
        TextureRegion[] regions = new TextureRegion[frames];
        int frameHeight = sheet.getHeight() / frames;

        for (int i = 0; i < frames; i++) {
            regions[i] = new TextureRegion(sheet, 0, i * frameHeight, sheet.getWidth(), frameHeight);
        }

        return new Animation<>(speed, regions);
    }

     public void render(SpriteBatch batch, float delta, Ratinho rat, float offsetX, float offsetY) {
        // Atualiza o tempo da animação
        rat.update(delta);
        
        // Determina se precisa virar o sprite
        boolean flip = shouldFlip(rat);
        
        // Obtém o frame de animação
        TextureRegion frame = getFrame(rat, flip);
        
        // Calcula posição e tamanho
        Vector2 renderPos = calculateRenderPosition(rat);
        Vector2 renderSize = calculateRenderSize(rat);
        
        // Aplica efeito de dano se necessário
        if (rat.isTakingDamage()) {
            batch.setColor(1, 0.5f, 0.5f, 1);
        }
        
        // Renderiza o rato
        batch.draw(
            frame,
            offsetX + renderPos.x * 16,
            offsetY + renderPos.y * 16,
            renderSize.x,
            renderSize.y
        );
        
        // Reseta a cor
        batch.setColor(Color.WHITE);
    }
    
    private boolean shouldFlip(Ratinho rat) {
        if (rat.getDirectionX() < 0 &&
            (rat.getState() == State.RUNNING_HORIZONTAL ||
             rat.getState() == State.PREPARING_DASH ||
             rat.getState() == State.DASHING)) {
            return true;
        }
        return false;
    }

public TextureRegion getFrame(Ratinho rat, boolean flip) {
    TextureRegion originalFrame;

    switch (rat.getState()) {
        case RUNNING_HORIZONTAL:
            originalFrame = runHorizontalAnimation.getKeyFrame(rat.getAnimationTime(), true);
            break;
        case RUNNING_DOWN:
            originalFrame = runDownAnimation.getKeyFrame(rat.getAnimationTime(), true);
            break;
        case GOT_DAMAGE:
            originalFrame = gotDamage.getKeyFrame(rat.getDamageAnimationTime(), false);
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

  

    TextureRegion frame = new TextureRegion(originalFrame);
    
    if (rat.getState() == State.RUNNING_HORIZONTAL || 
        rat.getState() == State.PREPARING_DASH || 
        rat.getState() == State.DASHING) {
        

        if (flip) {
            frame.flip(true, false);
        }
    } 
    else if (rat.getState() == State.RUNNING_DOWN) {
        if (rat.getDirectionY() < 0) {
            frame.flip(false, true);
        }
    }
    
    return frame;
}


   private Vector2 calculateRenderPosition(Ratinho rat) {
        Vector2 position = new Vector2(rat.getPosition());
        
        // Ajustes de posição baseados no estado
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
        
        // Centralização básica
        position.x -= (RAT_RENDER_WIDTH / RAT_WIDTH) / 2f;
        position.y -= (RAT_RENDER_HEIGHT / RAT_HEIGHT) / 2f;
        
        return position;
    }
    
    private Vector2 calculateRenderSize(Ratinho rat) {
        Vector2 size = new Vector2(RAT_RENDER_WIDTH, RAT_RENDER_HEIGHT);
        
   
        if (rat.getState() == State.DASHING) {
            size.x *= 1.1f;
            size.y *= 0.95f;
        }
        
        return size;
    }

    public void dispose() {
        idleSheet.dispose();
        runHorizontalSheet.dispose();
        runDownSheet.dispose();
    }
}