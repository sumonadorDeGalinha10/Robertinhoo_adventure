package io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import io.github.some_example_name.Entities.Enemies.Enemy.DeathType;

public class CastorAnimationSystem {
    // Animations
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runLeftAnimation;
    private Animation<TextureRegion> runDownAnimation;
    private Animation<TextureRegion> runUpAnimation;
    private Animation<TextureRegion> shootLeftAnimation;
    private Animation<TextureRegion> shootDownAnimation;
    private Animation<TextureRegion> shootUpAnimation;
    private Animation<TextureRegion> damageAnimation;
    private Animation<TextureRegion> dashAnimation;
    private Animation<TextureRegion> meleeDeathAnimation;
    private Animation<TextureRegion> projectileDeathAnimation;
    private float deathAnimationTime = 0f;

    // Animation states
    private float stateTime;
    private static final float FRAME_DURATION = 0.1f;
    private float shootAnimationTime = 0f;
    private float damageAnimationTime = 0f;
    private float dashAnimationTime = 0f;

    private static final float DASH_PREPARATION_DURATION = 0.3f; // Frames 0-1 mais curtos
    private static final float DASH_EXECUTION_DURATION = 0.5f; // Frames 2-7
    private static final float DASH_TOTAL_DURATION = DASH_PREPARATION_DURATION + DASH_EXECUTION_DURATION;

    // Direction
    public enum Direction {
        LEFT, RIGHT, DOWN, UP
    }

    // Dash phases
    public enum DashPhase {
        PREPARATION, // Primeiros 2 frames
        DASH // Últimos 6 frames
    }

    public CastorAnimationSystem() {
        loadAnimations();
        loadDeathAnimations();
    }

    private void loadAnimations() {
        // Carregar idle
        Texture idleSheet = new Texture(Gdx.files.internal("enemies/castor/idle-Sheet.png"));
        TextureRegion[][] idleFramesGrid = TextureRegion.split(idleSheet,
                idleSheet.getWidth() / 8,
                idleSheet.getHeight());
        idleAnimation = new Animation<>(FRAME_DURATION, idleFramesGrid[0]);

        // Carregar run
        Texture runSheet = new Texture(Gdx.files.internal("enemies/castor/RUN-Sheet.png"));
        int runFrameWidth = runSheet.getWidth() / 5;
        int runFrameHeight = runSheet.getHeight() / 3;
        TextureRegion[][] runFrames = TextureRegion.split(runSheet, runFrameWidth, runFrameHeight);

        runLeftAnimation = new Animation<>(FRAME_DURATION, runFrames[0]);
        runDownAnimation = new Animation<>(FRAME_DURATION, runFrames[1]);
        runUpAnimation = new Animation<>(FRAME_DURATION, runFrames[2]);

        // Carregar shoot
        Texture shootSheet = new Texture(Gdx.files.internal("enemies/castor/all_shoot-Sheet.png"));
        int shootFrameWidth = shootSheet.getWidth() / 6;
        int shootFrameHeight = shootSheet.getHeight() / 3;
        TextureRegion[][] shootFrames = TextureRegion.split(shootSheet, shootFrameWidth, shootFrameHeight);

        shootLeftAnimation = new Animation<>(FRAME_DURATION, shootFrames[0]);
        shootDownAnimation = new Animation<>(FRAME_DURATION, shootFrames[1]);
        shootUpAnimation = new Animation<>(FRAME_DURATION, shootFrames[2]);

        // Carregar damage
        Texture damageSheet = new Texture(Gdx.files.internal("enemies/castor/hit_castor-Sheet.png"));
        int damageFrameWidth = damageSheet.getWidth() / 4;
        int damageFrameHeight = damageSheet.getHeight();

        TextureRegion[] damageFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            damageFrames[i] = new TextureRegion(damageSheet, i * damageFrameWidth, 0, damageFrameWidth,
                    damageFrameHeight);
        }
        damageAnimation = new Animation<>(FRAME_DURATION, damageFrames);

        // 🔥 CARREGAMENTO REFINADO: Animação de dash com fases
        Texture dashSheet = new Texture(Gdx.files.internal("enemies/castor/recue_dash-Sheet.png"));
        int dashFrameWidth = dashSheet.getWidth() / 8;
        int dashFrameHeight = dashSheet.getHeight();

        TextureRegion[] dashFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            dashFrames[i] = new TextureRegion(dashSheet, i * dashFrameWidth, 0, dashFrameWidth, dashFrameHeight);
        }

        // 🔥 DURAÇÕES DIFERENCIADAS: Preparação mais lenta, dash mais rápido
        float[] frameDurations = {
                0.15f, // Frame 0: Preparação lenta
                0.15f, // Frame 1: Preparação lenta
                0.08f, // Frame 2: Dash rápido
                0.08f, // Frame 3: Dash rápido
                0.08f, // Frame 4: Dash rápido
                0.08f, // Frame 5: Dash rápido
                0.08f, // Frame 6: Dash rápido
                0.08f // Frame 7: Dash rápido
        };

        dashAnimation = new Animation<>(0.1f, dashFrames); // Duração base, vamos controlar manualmente
    }

    private void loadDeathAnimations() {
        // Carregar spritesheet de morte (5 colunas x 4 linhas)
        Texture deathSheet = new Texture(Gdx.files.internal("enemies/castor/CastorDeads.png"));

        int deathFrameWidth = deathSheet.getWidth() / 5;
        int deathFrameHeight = deathSheet.getHeight() / 4;

        // Projetil death - primeiras 2 linhas (10 frames)
        TextureRegion[] projectileFrames = new TextureRegion[10];
        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                projectileFrames[index++] = new TextureRegion(
                        deathSheet,
                        col * deathFrameWidth,
                        row * deathFrameHeight,
                        deathFrameWidth,
                        deathFrameHeight);
            }
        }
        projectileDeathAnimation = new Animation<>(0.1f, projectileFrames);

        // Melee death - últimas 2 linhas (10 frames)
        TextureRegion[] meleeFrames = new TextureRegion[10];
        index = 0;
        for (int row = 2; row < 4; row++) {
            for (int col = 0; col < 5; col++) {
                meleeFrames[index++] = new TextureRegion(
                        deathSheet,
                        col * deathFrameWidth,
                        row * deathFrameHeight,
                        deathFrameWidth,
                        deathFrameHeight);
            }
        }
        meleeDeathAnimation = new Animation<>(0.1f, meleeFrames);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    public void updateShootTime(float deltaTime) {
        shootAnimationTime += deltaTime;
    }

    public void resetShootTime() {
        shootAnimationTime = 0f;
    }

    public void updateDamageTime(float deltaTime) {
        damageAnimationTime += deltaTime;
    }

    public void resetDamageTime() {
        damageAnimationTime = 0f;
    }

    public void updateDashTime(float deltaTime) {
        dashAnimationTime += deltaTime;
    }

    public void resetDashTime() {
        dashAnimationTime = 0f;
    }

    /**
     * 🔥 MÉTODO REFINADO: Obtém o frame atual baseado no estado do castor
     */
    public AnimationFrame getCurrentFrame(CastorState state, Direction direction,
            Vector2 velocity, Vector2 targetPosition,
            Vector2 currentPosition) {

        TextureRegion currentFrame = null;
        boolean flipX = false;
        DashPhase dashPhase = DashPhase.PREPARATION;

        switch (state) {
            case DASHING:
                currentFrame = getDashFrame(direction);
                dashPhase = getDashPhase();
                // 🔥 CORREÇÃO DO FLIP:
                // - LEFT: flipX = false (normal - sprite virado para esquerda)
                // - RIGHT: flipX = true (espelhado - sprite virado para direita)
                // - UP/DOWN: sem flip
                if (direction == Direction.LEFT) {
                    flipX = true;
                }
                break;

            case TAKING_DAMAGE:
                currentFrame = getDamageFrame(direction);
                // Manter flip consistente com outras animações
                if (direction == Direction.RIGHT) {
                    flipX = true;
                }
                break;

            case SHOOTING:
                currentFrame = getShootFrame(direction);
                if (direction == Direction.RIGHT) {
                    flipX = true;
                }
                break;

            case MOVING:
                currentFrame = getRunFrame(direction);
                if (direction == Direction.RIGHT) {
                    flipX = true;
                }
                break;

            default:
                currentFrame = getIdleFrame();
                break;
        }

        return new AnimationFrame(currentFrame, flipX, dashPhase);
    }

    /**
     * 🔥 MÉTODO REFINADO: Obtém frame de dash com fases
     */
    private TextureRegion getDashFrame(Direction direction) {
        int frameIndex;

        if (dashAnimationTime < DASH_PREPARATION_DURATION) {
            // Fase de preparação - frames 0 e 1
            float preparationProgress = dashAnimationTime / DASH_PREPARATION_DURATION;
            frameIndex = (int) (preparationProgress * 2); // 0 ou 1
            frameIndex = Math.min(frameIndex, 1);
        } else {
            // Fase de dash - frames 2 a 7
            float dashProgress = (dashAnimationTime - DASH_PREPARATION_DURATION) / DASH_EXECUTION_DURATION;
            frameIndex = 2 + (int) (dashProgress * 6); // 2 a 7
            frameIndex = Math.min(frameIndex, 7);
        }

        // Garantir que não ultrapassa o último frame
        frameIndex = Math.min(frameIndex, dashAnimation.getKeyFrames().length - 1);

        return dashAnimation.getKeyFrames()[frameIndex];
    }

    private DashPhase getDashPhase() {
        return (dashAnimationTime < DASH_PREPARATION_DURATION) ? DashPhase.PREPARATION : DashPhase.DASH;
    }

    private TextureRegion getIdleFrame() {
        return idleAnimation.getKeyFrame(stateTime, true);
    }

    private TextureRegion getRunFrame(Direction direction) {
        switch (direction) {
            case LEFT:
                return runLeftAnimation.getKeyFrame(stateTime, true);
            case RIGHT:
                return runLeftAnimation.getKeyFrame(stateTime, true); // Será flipado
            case DOWN:
                return runDownAnimation.getKeyFrame(stateTime, true);
            case UP:
                return runUpAnimation.getKeyFrame(stateTime, true);
            default:
                return idleAnimation.getKeyFrame(stateTime, true);
        }
    }

    private TextureRegion getShootFrame(Direction direction) {
        switch (direction) {
            case LEFT:
                return shootLeftAnimation.getKeyFrame(shootAnimationTime, false);
            case RIGHT:
                return shootLeftAnimation.getKeyFrame(shootAnimationTime, false); // Será flipado
            case DOWN:
                return shootDownAnimation.getKeyFrame(shootAnimationTime, false);
            case UP:
                return shootUpAnimation.getKeyFrame(shootAnimationTime, false);
            default:
                return idleAnimation.getKeyFrame(stateTime, true);
        }
    }

    private TextureRegion getDamageFrame(Direction direction) {
        TextureRegion frame = damageAnimation.getKeyFrame(damageAnimationTime, false);

        // Se a animação terminou, manter o último frame
        if (damageAnimation.isAnimationFinished(damageAnimationTime)) {
            frame = damageAnimation.getKeyFrames()[damageAnimation.getKeyFrames().length - 1];
        }

        return frame;
    }

    public boolean isShootAtFirePoint() {
        return shootAnimationTime >= 0.5f;
    }

    public boolean isDamageAnimationFinished() {
        return damageAnimation.isAnimationFinished(damageAnimationTime);
    }

    public void updateDeathTime(float deltaTime) {
        deathAnimationTime += deltaTime;
    }

    public void resetDeathTime() {
        deathAnimationTime = 0f;
    }

    public TextureRegion getDeathFrame(DeathType deathType, Direction direction) {
        TextureRegion frame = null;

        switch (deathType) {
            case MELEE:
                frame = meleeDeathAnimation.getKeyFrame(deathAnimationTime, false);
                break;
            case PROJECTILE:
                frame = projectileDeathAnimation.getKeyFrame(deathAnimationTime, false);
                break;
        }

        // Se animação acabou, pega o último frame
        if (isDeathAnimationFinished(deathType)) {
            frame = getLastDeathFrame(deathType);
        }

        return frame;
    }

    public TextureRegion getLastDeathFrame(DeathType deathType) {
        switch (deathType) {
            case MELEE:
                return meleeDeathAnimation.getKeyFrames()[9]; // Último frame
            case PROJECTILE:
                return projectileDeathAnimation.getKeyFrames()[9]; // Último frame
            default:
                return idleAnimation.getKeyFrames()[0];
        }
    }

    public boolean isDeathAnimationFinished(DeathType deathType) {
        switch (deathType) {
            case MELEE:
                return meleeDeathAnimation.isAnimationFinished(deathAnimationTime);
            case PROJECTILE:
                return projectileDeathAnimation.isAnimationFinished(deathAnimationTime);
            default:
                return true;
        }
    }

    public float getMeleeDeathDuration() {
        return meleeDeathAnimation.getAnimationDuration();
    }

    public float getProjectileDeathDuration() {
        return projectileDeathAnimation.getAnimationDuration();
    }

    public float getDeathTime() {
        return deathAnimationTime;
    }

    /**
     * 🔥 NOVO: Classe para encapsular o resultado da animação
     */
    public static class AnimationFrame {
        public final TextureRegion frame;
        public final boolean flipX;
        public final DashPhase dashPhase;

        public AnimationFrame(TextureRegion frame, boolean flipX, DashPhase dashPhase) {
            this.frame = frame;
            this.flipX = flipX;
            this.dashPhase = dashPhase;
        }
    }

    /**
     * 🔥 NOVO: Enum para estados do castor
     */
    public enum CastorState {
        IDLE, MOVING, SHOOTING, TAKING_DAMAGE, DASHING
    }

    public float getDamageTime() {
        return damageAnimationTime;
    }

    public float getDashTime() {
        return dashAnimationTime;
    }

    public float getDashProgress() {
        return Math.min(dashAnimationTime / DASH_TOTAL_DURATION, 1.0f);
    }

    // 🔥 CORREÇÃO: Método para verificar se está na fase de execução do dash
    public boolean isDashInExecutionPhase() {
        return dashAnimationTime >= DASH_PREPARATION_DURATION;
    }

    // 🔥 CORREÇÃO: Método para verificar se o dash terminou
    public boolean isDashAnimationFinished() {
        return dashAnimationTime >= DASH_TOTAL_DURATION;
    }

    public void dispose() {
        if (idleAnimation != null && idleAnimation.getKeyFrames().length > 0) {
            idleAnimation.getKeyFrames()[0].getTexture().dispose();
        }
        if (runLeftAnimation != null && runLeftAnimation.getKeyFrames().length > 0) {
            runLeftAnimation.getKeyFrames()[0].getTexture().dispose();
        }
        if (shootLeftAnimation != null && shootLeftAnimation.getKeyFrames().length > 0) {
            shootLeftAnimation.getKeyFrames()[0].getTexture().dispose();
        }
        if (damageAnimation != null && damageAnimation.getKeyFrames().length > 0) {
            damageAnimation.getKeyFrames()[0].getTexture().dispose();
        }
        if (dashAnimation != null && dashAnimation.getKeyFrames().length > 0) {
            dashAnimation.getKeyFrames()[0].getTexture().dispose();
        }

        if (meleeDeathAnimation != null && meleeDeathAnimation.getKeyFrames().length > 0) {
            meleeDeathAnimation.getKeyFrames()[0].getTexture().dispose();
        }
        if (projectileDeathAnimation != null && projectileDeathAnimation.getKeyFrames().length > 0) {
            projectileDeathAnimation.getKeyFrames()[0].getTexture().dispose();
        }
    }

}