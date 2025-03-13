package io.github.some_example_name.Entities.Renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapRenderer;

public class PlayerRenderer {
   
    private float animationTime = 0f; // Tempo de animação
    private Texture idleDowSheet; // Textura padrão
    private Texture leftSheet; // Textura para esquerda
    private Texture rigthSheet; // Textura para direita
    private Texture runUpSheet;
    private Texture runDownSheet;
    private Animation<TextureRegion> playerAnimation; // Animação padrão (idle)
    private Animation<TextureRegion> playerAnimationleft; // Animação para esquerda
    private Animation<TextureRegion> playerAnimationRigth; // An

    private Animation<TextureRegion> runUpAnimation;
    private Animation<TextureRegion> runDownAnimation;
    private Animation<TextureRegion> runSideAnimation;
    
    // Animações idle
    private Animation<TextureRegion> idleUpAnimation;
    private Animation<TextureRegion> idleDownAnimation;
    private Animation<TextureRegion> idleSideAnimation;
    private Texture idleUpSheet;
    private Texture idleDownSheet;
    private Texture idleSideSheet;

    public PlayerRenderer() {
        // Carregar as texturas
        idleDowSheet = new Texture("rober/idle/1_Template_Idle_Down-Sheet.png");
        leftSheet = new Texture("rober/run/2_Template_Run_Left-Sheet.png");
        rigthSheet = new Texture("rober/run/2_Template_Run_Left-Sheet.png");
        runUpSheet = new Texture ("rober/run/2_Template_Run_Up-Sheet.png");
        runDownSheet = new Texture ("rober/run/2_Template_Run_Down-Sheet.png");


        idleUpSheet = new Texture("rober/idle/1_Template_Idle_Up-Sheet.png");
        idleDownSheet = new Texture("rober/idle/1_Template_Idle_Down-Sheet.png");
        idleSideSheet = new Texture("rober/idle/1_Template_Idle_Left-Sheet.png");

    // Criar animação idle para cima
        int frameWidthIdleUp = idleUpSheet.getWidth() / 6;
        int frameHeightIdleUp = idleUpSheet.getHeight();
        TextureRegion[] idleUpFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            idleUpFrames[i] = new TextureRegion(idleUpSheet, i * frameWidthIdleUp, 0, frameWidthIdleUp, frameHeightIdleUp);
        }
        idleUpAnimation = new Animation<>(0.2f, idleUpFrames);
        idleUpAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Criar animação idle para baixo
        int frameWidthIdleDown = idleDownSheet.getWidth() / 6;
        int frameHeightIdleDown = idleDownSheet.getHeight();
        TextureRegion[] idleDownFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            idleDownFrames[i] = new TextureRegion(idleDownSheet, i * frameWidthIdleDown, 0, frameWidthIdleDown, frameHeightIdleDown);
        }
        idleDownAnimation = new Animation<>(0.2f, idleDownFrames);
        idleDownAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Criar animação idle para os lados (esquerda)
        int frameWidthIdleSide = idleSideSheet.getWidth() / 6;
        int frameHeightIdleSide = idleSideSheet.getHeight();
        TextureRegion[] idleSideFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            idleSideFrames[i] = new TextureRegion(idleSideSheet, i * frameWidthIdleSide, 0, frameWidthIdleSide, frameHeightIdleSide);
        }
        idleSideAnimation = new Animation<>(0.2f, idleSideFrames);
        idleSideAnimation.setPlayMode(Animation.PlayMode.LOOP);

        int frameWidthRunUp = runUpSheet.getWidth() / 6;
        int frameHeightRunUp = runUpSheet.getHeight();
        TextureRegion[] runUpFrames = new TextureRegion[6];

        int frameWidthRunDown = runDownSheet.getWidth() / 6;
        int frameHeightRunDown = runDownSheet.getHeight();
        TextureRegion[] runDownFrames = new TextureRegion[6];

        for (int i = 0; i < 6; i++) {
            runDownFrames[i] = new TextureRegion(runDownSheet, i * frameWidthRunDown, 0, frameWidthRunDown, frameHeightRunDown);
        }
        runDownAnimation = new Animation<>(0.1f, runDownFrames);
        runDownAnimation.setPlayMode(Animation.PlayMode.LOOP);

        for (int i = 0; i < 6; i++) {
            runUpFrames[i] = new TextureRegion(runUpSheet, i * frameWidthRunUp, 0, frameWidthRunUp, frameHeightRunUp);
        }
        runUpAnimation = new Animation<>(0.1f, runUpFrames);;;
        runUpAnimation.setPlayMode(Animation.PlayMode.LOOP);

     
        int frameWidth = idleDowSheet.getWidth() / 6;
        int frameHeight = idleDowSheet.getHeight();
        TextureRegion[] animationFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            animationFrames[i] = new TextureRegion(idleDowSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }
        playerAnimation = new Animation<>(0.2f, animationFrames);
        playerAnimation.setPlayMode(Animation.PlayMode.LOOP);

     
        int frameWidthLeft = leftSheet.getWidth() / 6;
        int frameHeightLeft = leftSheet.getHeight();
        TextureRegion[] animationleft = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            animationleft[i] = new TextureRegion(leftSheet, i * frameWidthLeft, 0, frameWidthLeft, frameHeightLeft);
            animationleft[i].flip(true, false);
        }
        playerAnimationleft = new Animation<>(0.1f, animationleft);
        playerAnimationleft.setPlayMode(Animation.PlayMode.LOOP);

        int frameWidthRigth = rigthSheet.getWidth() / 6;
        int frameHeightRight = rigthSheet.getHeight();
        TextureRegion[] animationRigth = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            animationRigth[i] = new TextureRegion(rigthSheet, i * frameWidthRigth, 0, frameWidthRigth, frameHeightRight);
        }
        playerAnimationRigth = new Animation<>(0.1f, animationRigth);
        playerAnimationRigth.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void render(SpriteBatch batch, Robertinhoo player, float delta, float offsetX, float offsetY) {
        animationTime += delta;

        Animation<TextureRegion> selectedAnimation;
        if (player.dir == player.RIGHT) {
            selectedAnimation = playerAnimationleft;

        } else if (player.dir == player.LEFT) {
            selectedAnimation = playerAnimationRigth;

        } 
        else if (player.dir == player.UP){
            selectedAnimation = runUpAnimation;

        }

        else if (player.dir == player.DOWN){
            selectedAnimation = runDownAnimation;

        }
        else {
            selectedAnimation = playerAnimation;
        }

  
        TextureRegion frame = selectedAnimation.getKeyFrame(animationTime, true);

   
        float x = offsetX + player.bounds.x * MapRenderer.TILE_SIZE;
        float y = offsetY + player.bounds.y * MapRenderer.TILE_SIZE;

      
        batch.draw(frame, x, y, player.bounds.width * MapRenderer.TILE_SIZE, player.bounds.height * MapRenderer.TILE_SIZE);
    }

    public void dispose() {
     
        idleDowSheet.dispose();
        leftSheet.dispose();
        rigthSheet.dispose();
    }
}