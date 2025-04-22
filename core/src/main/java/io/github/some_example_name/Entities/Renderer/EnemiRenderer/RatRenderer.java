package io.github.some_example_name.Entities.Renderer.EnemiRenderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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


    
    public RatRenderer() {
        idleSheet = new Texture("enemies/rat/MouseIdle.png");
        runHorizontalSheet = new Texture("enemies/rat/MouseRun.png");
        runDownSheet = new Texture("enemies/rat/rat_run_downimg.png");
        gotDamageSheet = new Texture("enemies/rat/MouseDamage.png");
        
        idleAnimation = createAnimation(idleSheet, 6, 0.2f);
        runHorizontalAnimation = createAnimation(runHorizontalSheet, 6, 0.1f);
        runDownAnimation = createAnimation(runDownSheet, 6, 0.1f);
        gotDamage = createAnimation(gotDamageSheet, 4, 0.1f);
    }
    
    private Animation<TextureRegion> createAnimation(Texture sheet, int frames, float speed) {
        TextureRegion[] regions = new TextureRegion[frames];
        int frameHeight = sheet.getHeight() / frames;
        
        for (int i = 0; i < frames; i++) {
            regions[i] = new TextureRegion(sheet, 0, i * frameHeight, sheet.getWidth(), frameHeight);
        }
        
        return new Animation<>(speed, regions);
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
            default:
                originalFrame = idleAnimation.getKeyFrame(rat.getAnimationTime(), true);
        }
        
        TextureRegion frame = new TextureRegion(originalFrame);
        
        // Flip horizontal
        if (flip && rat.getState() == State.RUNNING_HORIZONTAL) {
            frame.flip(true, false);
        }
        // Flip vertical corrigido
        else if (rat.getState() == State.RUNNING_DOWN) {
            if(rat.getDirectionY() > 0) {
                frame.flip(false, false); 
            } else {
                frame.flip(false, true); // Inverte verticalmente
            }
        }
        
        return frame;
    }
    

    public void dispose() {
        idleSheet.dispose();
        runHorizontalSheet.dispose();
        runDownSheet.dispose();
    }
}