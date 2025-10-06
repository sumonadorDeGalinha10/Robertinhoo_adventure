package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

import io.github.some_example_name.Entities.Player.Robertinhoo;

public class RobertinhoFaceHUD {
    private final Texture metalFrame;
    private final Texture faceSheet;
    private final Texture batimentoSheet;

    private final Texture maskMedium;
    private final Texture maskLow;

    private Animation<TextureRegion> maskMediumAnimation;
    private Animation<TextureRegion> maskLowAnimation;
    private StaminaBarRenderer staminaBarRenderer;
    private final Texture exhaustedSheet;  // Nova textura para estado exausto
    private Animation<TextureRegion> exhaustedAnimation;  // Animação de exausto

    private final VidaBatimentoCardiaco batimentoCardiaco;

    private static final float FACE_SIZE = 380;
    private static final float BATIMENTO_WIDTH = 225;
    private static final float BATIMENTO_HEIGHT = 82;
    private static final float SPACING = 2f;
    private static final float STAMINA_WIDTH = 230;
    private static final float STAMINA_HEIGHT = 15;
    private static final float STAMINA_Y_OFFSET = 2;
    private static final float STAMINA_X_OFFSET = 2;
    private float x;
    private float y;
    private float batimentoX;
    private float batimentoY;

    private final Animation<TextureRegion> faceAnimation;
    private final Robertinhoo robertinhoo;
    private float stateTime;
        private boolean lastExhaustedState = false;
    private float faceStateTime = 0f;

    public RobertinhoFaceHUD(float screenWidth, float screenHeight, Robertinhoo robertinhoo) {
        this.robertinhoo = robertinhoo;
        this.staminaBarRenderer = new StaminaBarRenderer(robertinhoo);
        recalculatePosition(screenWidth, screenHeight);

        metalFrame = new Texture("rober/interface/molde.png");
        faceSheet = new Texture("rober/interface/robertinhoo_idle-Sheet.png");
        batimentoSheet = new Texture("rober/interface/vida-full-Sheet.png");
        maskMedium = new Texture("rober/interface/medium-Sheet.png");
        maskLow = new Texture("rober/interface/low-Sheet.png");
        exhaustedSheet = new Texture("rober/interface/cansado-Sheet.png");

        maskMediumAnimation = createMaskAnimation(maskMedium, 8);
        maskLowAnimation = createMaskAnimation(maskLow, 8);

        batimentoCardiaco = new VidaBatimentoCardiaco(
                batimentoSheet,
                robertinhoo,
                batimentoX,
                batimentoY,
                BATIMENTO_WIDTH,
                BATIMENTO_HEIGHT);

        int FRAME_COLS = 8;
        int FRAME_ROWS = 1;

        TextureRegion[][] tmp = TextureRegion.split(
                faceSheet,
                faceSheet.getWidth() / FRAME_COLS,
                faceSheet.getHeight() / FRAME_ROWS);

        TextureRegion[] idleFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                idleFrames[index++] = tmp[i][j];
            }
        }

        faceAnimation = new Animation<>(0.4f, idleFrames);
        exhaustedAnimation = createFaceAnimation(exhaustedSheet, 5);
        lastExhaustedState = robertinhoo.getStaminaSystem().isExhausted();

        stateTime = 0f;
    }

    private Animation<TextureRegion> createMaskAnimation(Texture sheet, int frameCount) {
        int FRAME_COLS = frameCount;
        int FRAME_ROWS = 1;

        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / FRAME_COLS,
                sheet.getHeight() / FRAME_ROWS);

        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        System.arraycopy(tmp[0], 0, frames, 0, FRAME_COLS);

        return new Animation<>(0.4f, frames);
    }

    private void recalculatePosition(float screenWidth, float screenHeight) {
        float margin = 10f;

        this.x = screenWidth - FACE_SIZE - margin;
        this.y = margin;
        this.batimentoX = x + (FACE_SIZE - BATIMENTO_WIDTH) / 2;
        this.batimentoY = y + FACE_SIZE + SPACING;

        float staminaX = screenWidth - FACE_SIZE - 10 + STAMINA_X_OFFSET;
        float staminaY = 10 + STAMINA_Y_OFFSET;
        
        staminaBarRenderer.setPosition(staminaX, staminaY);

        Gdx.app.log("HUD Position",
                String.format("Tela: %.0fx%.0f | Face: (%.0f, %.0f) | Batimento: (%.0f, %.0f)",
                        screenWidth, screenHeight, x, y, batimentoX, batimentoY));
    }

     public void update(float delta) {
        stateTime += delta;
        faceStateTime += delta;
        
        // Verifica mudança de estado
        boolean currentExhausted = robertinhoo.getStaminaSystem().isExhausted();
        if (currentExhausted != lastExhaustedState) {
            faceStateTime = 0f; // Reseta o tempo da animação
            lastExhaustedState = currentExhausted;
        }
        
        if (batimentoCardiaco != null) {
            batimentoCardiaco.update(delta);
        }
    }
    public void draw(SpriteBatch batch , float delta) {
        Matrix4 originalMatrix = batch.getProjectionMatrix().cpy();
        Matrix4 hudMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudMatrix);

        float scale = Math.min(1.0f, Gdx.graphics.getWidth() / 1280f);
        float scaledFaceSize = FACE_SIZE * scale;
        float scaledX = Gdx.graphics.getWidth() - scaledFaceSize - (10f * scale);
        float scaledY = 10f * scale;

        batch.draw(metalFrame, scaledX, scaledY, scaledFaceSize, scaledFaceSize);

          Animation<TextureRegion> currentAnimation = robertinhoo.getStaminaSystem().isExhausted() 
            ? exhaustedAnimation 
            : faceAnimation;
        
        TextureRegion currentFrame = currentAnimation.getKeyFrame(faceStateTime, true);
        

        float faceMargin = scaledFaceSize * 0.001f;
        float faceRenderSize = scaledFaceSize - 2 * faceMargin;

        float faceX = scaledX + faceMargin;
        float faceY = scaledY + faceMargin;
        batch.draw(currentFrame, faceX, faceY, faceRenderSize, faceRenderSize);

        float vida = robertinhoo.getLife();
        if (vida < 30) {
            TextureRegion maskFrame = maskLowAnimation.getKeyFrame(stateTime, true);
            batch.draw(maskFrame, faceX, faceY, faceRenderSize, faceRenderSize);
        } else if (vida < 70) {
            TextureRegion maskFrame = maskMediumAnimation.getKeyFrame(stateTime, true);
            batch.draw(maskFrame, faceX, faceY, faceRenderSize, faceRenderSize);
        }

        if (batimentoCardiaco != null) {
            float batimentoScaleX = BATIMENTO_WIDTH * scale;
            float batimentoScaleY = BATIMENTO_HEIGHT * scale;

            float batimentoX = scaledX + (scaledFaceSize - batimentoScaleX) / 2;

            float horizontalOffset = -7.15f * scale;
            batimentoX += horizontalOffset;

            float offsetVertical = scaledFaceSize * 0.175f;
            float batimentoY = scaledY + offsetVertical - (batimentoScaleY / 2);

            batimentoCardiaco.setPosition(batimentoX, batimentoY);
            batimentoCardiaco.setSize(batimentoScaleX, batimentoScaleY);
            batimentoCardiaco.draw(batch);
        }

        if (staminaBarRenderer != null) {
           
            float staminaScaleX = STAMINA_WIDTH * scale;
            float staminaScaleY = STAMINA_HEIGHT * scale;
            
      
            float staminaX = scaledX + (scaledFaceSize - staminaScaleX) / 2;
            
  
            float horizontalOffset = 3.00f * scale;
            staminaX += horizontalOffset;

            float offsetVertical = scaledFaceSize * 0.100f;
      
            float staminaY = scaledY + offsetVertical - (staminaScaleY / 2);
            
            staminaBarRenderer.setPosition(staminaX, staminaY);
            staminaBarRenderer.setSize(staminaScaleX, staminaScaleY);
            staminaBarRenderer.draw(batch,delta);
        }


        batch.setProjectionMatrix(originalMatrix);
    }

    public void updateScreenSize(float width, float height) {
        recalculatePosition(width, height);
        batimentoCardiaco.setPosition(batimentoX, batimentoY);
    }

        private Animation<TextureRegion> createFaceAnimation(Texture sheet, int frameCount) {
        int FRAME_COLS = frameCount;
        int FRAME_ROWS = 1;

        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / FRAME_COLS,
                sheet.getHeight() / FRAME_ROWS);

        TextureRegion[] frames = new TextureRegion[FRAME_COLS];
        System.arraycopy(tmp[0], 0, frames, 0, FRAME_COLS);

        return new Animation<>(0.4f, frames);
    }

    public void dispose() {
        metalFrame.dispose();
        faceSheet.dispose();
        batimentoSheet.dispose();
        maskMedium.dispose();
        maskLow.dispose();
        staminaBarRenderer.dispose();
        exhaustedSheet.dispose();
        if (faceAnimation != null) {
            faceAnimation.getKeyFrames()[0].getTexture().dispose();

        if (batimentoCardiaco != null) {
            batimentoCardiaco.dispose();
        }
    }
}
}