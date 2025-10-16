package io.github.some_example_name.Entities.Renderer.EnemiRenderer.Castor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import io.github.some_example_name.Entities.Enemies.Enemy.DeathType;
import io.github.some_example_name.Entities.Enemies.Castor.CastorAnimationState;

public class CastorAnimationSystem {
    // Animations (permanecem compartilhadas - são apenas os recursos)
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

    private static final float FRAME_DURATION = 0.1f;
    private static final float DASH_PREPARATION_DURATION = 0.3f;
    private static final float DASH_EXECUTION_DURATION = 0.5f;
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

        // Carregar dash
        Texture dashSheet = new Texture(Gdx.files.internal("enemies/castor/recue_dash-Sheet.png"));
        int dashFrameWidth = dashSheet.getWidth() / 8;
        int dashFrameHeight = dashSheet.getHeight();

        TextureRegion[] dashFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            dashFrames[i] = new TextureRegion(dashSheet, i * dashFrameWidth, 0, dashFrameWidth, dashFrameHeight);
        }

        dashAnimation = new Animation<>(0.1f, dashFrames);
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

    /**
     * MÉTODO ATUALIZADO: Obtém o frame atual baseado no estado do castor e seu
     * estado de animação
     */
    public AnimationFrame getCurrentFrame(CastorState state, Direction direction,
            Vector2 velocity, Vector2 targetPosition,
            Vector2 currentPosition, CastorAnimationState animState) {

        TextureRegion currentFrame = null;
        boolean flipX = false;
        DashPhase dashPhase = DashPhase.PREPARATION;

        switch (state) {
            case DASHING:
                currentFrame = getDashFrame(direction, animState.dashAnimationTime);
                dashPhase = getDashPhase(animState.dashAnimationTime);
                if (direction == Direction.LEFT) {
                    flipX = true;
                }
                break;

            case TAKING_DAMAGE:
                currentFrame = getDamageFrame(direction, animState.damageAnimationTime);
                if (direction == Direction.RIGHT) {
                    flipX = true;
                }
                break;

            case SHOOTING:
                currentFrame = getShootFrame(direction, animState.shootAnimationTime);
                if (direction == Direction.RIGHT) {
                    flipX = true;
                }
                break;

            case MOVING:
                currentFrame = getRunFrame(direction, animState.stateTime);
                if (direction == Direction.RIGHT) {
                    flipX = true;
                }
                break;

            default:
                currentFrame = getIdleFrame(animState.stateTime);
                break;
        }

        return new AnimationFrame(currentFrame, flipX, dashPhase);
    }

    /**
     * MÉTODO ATUALIZADO: Obtém frame de dash com tempo específico
     */
    private TextureRegion getDashFrame(Direction direction, float dashAnimationTime) {
        int frameIndex;

        if (dashAnimationTime < DASH_PREPARATION_DURATION) {
            // Fase de preparação - frames 0 e 1
            float preparationProgress = dashAnimationTime / DASH_PREPARATION_DURATION;
            frameIndex = (int) (preparationProgress * 2);
            frameIndex = Math.min(frameIndex, 1);
        } else {
            // Fase de dash - frames 2 a 7
            float dashProgress = (dashAnimationTime - DASH_PREPARATION_DURATION) / DASH_EXECUTION_DURATION;
            frameIndex = 2 + (int) (dashProgress * 6);
            frameIndex = Math.min(frameIndex, 7);
        }

        frameIndex = Math.min(frameIndex, dashAnimation.getKeyFrames().length - 1);
        return dashAnimation.getKeyFrames()[frameIndex];
    }

    private DashPhase getDashPhase(float dashAnimationTime) {
        return (dashAnimationTime < DASH_PREPARATION_DURATION) ? DashPhase.PREPARATION : DashPhase.DASH;
    }

    private TextureRegion getIdleFrame(float stateTime) {
        return idleAnimation.getKeyFrame(stateTime, true);
    }

    private TextureRegion getRunFrame(Direction direction, float stateTime) {
        switch (direction) {
            case LEFT:
                return runLeftAnimation.getKeyFrame(stateTime, true);
            case RIGHT:
                return runLeftAnimation.getKeyFrame(stateTime, true);
            case DOWN:
                return runDownAnimation.getKeyFrame(stateTime, true);
            case UP:
                return runUpAnimation.getKeyFrame(stateTime, true);
            default:
                return idleAnimation.getKeyFrame(stateTime, true);
        }
    }

    private TextureRegion getShootFrame(Direction direction, float shootAnimationTime) {
        switch (direction) {
            case LEFT:
                return shootLeftAnimation.getKeyFrame(shootAnimationTime, false);
            case RIGHT:
                return shootLeftAnimation.getKeyFrame(shootAnimationTime, false);
            case DOWN:
                return shootDownAnimation.getKeyFrame(shootAnimationTime, false);
            case UP:
                return shootUpAnimation.getKeyFrame(shootAnimationTime, false);
            default:
                return idleAnimation.getKeyFrame(0, true);
        }
    }

    private TextureRegion getDamageFrame(Direction direction, float damageAnimationTime) {
        TextureRegion frame = damageAnimation.getKeyFrame(damageAnimationTime, false);

        if (damageAnimation.isAnimationFinished(damageAnimationTime)) {
            frame = damageAnimation.getKeyFrames()[damageAnimation.getKeyFrames().length - 1];
        }

        return frame;
    }

    /**
     * MÉTODO ATUALIZADO: Obtém frame de morte com estado específico
     */
    public TextureRegion getDeathFrame(DeathType deathType, Direction direction, CastorAnimationState animState) {
        TextureRegion frame = null;

        switch (deathType) {
            case MELEE:
                frame = meleeDeathAnimation.getKeyFrame(animState.deathAnimationTime, false);
                break;
            case PROJECTILE:
                frame = projectileDeathAnimation.getKeyFrame(animState.deathAnimationTime, false);
                break;
        }

        if (isDeathAnimationFinished(deathType, animState.deathAnimationTime)) {
            frame = getLastDeathFrame(deathType);
        }

        return frame;
    }

    public TextureRegion getLastDeathFrame(DeathType deathType) {
        switch (deathType) {
            case MELEE:
                return meleeDeathAnimation.getKeyFrames()[9];
            case PROJECTILE:
                return projectileDeathAnimation.getKeyFrames()[9];
            default:
                return idleAnimation.getKeyFrames()[0];
        }
    }

    /**
     * MÉTODO ATUALIZADO: Verifica se animação de morte terminou com tempo
     * específico
     */
    public boolean isDeathAnimationFinished(DeathType deathType, float deathAnimationTime) {
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

    /**
     * Classe para encapsular o resultado da animação
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
     * Enum para estados do castor
     */
    public enum CastorState {
        IDLE, MOVING, SHOOTING, TAKING_DAMAGE, DASHING
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