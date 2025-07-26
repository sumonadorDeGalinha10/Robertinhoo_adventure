package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import io.github.some_example_name.Entities.Player.Robertinhoo;

/**
 * Stamina bar com visual melhorado: cores vibrantes, gradiente, glow e efeitos de animação.
 */
public class StaminaBarRenderer {
    private final Robertinhoo robertinhoo;
    private final ShapeRenderer shapeRenderer;

    private static final float BORDER_THICKNESS = 1f;
    private static final float PULSE_SPEED = 6f;
    private static final float JITTER_INTENSITY = 3f;
    private static final float INTERPOLATION_SPEED = 5f;

    private float x, y, width, height;
    private float elapsed;
    private float visualStaminaPercent = 1f;

    public StaminaBarRenderer(Robertinhoo robertinhoo) {
        this.robertinhoo = robertinhoo;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void draw(SpriteBatch batch, float deltaTime) {
        if (robertinhoo == null || robertinhoo.getStaminaSystem() == null) return;

        elapsed += deltaTime;

        float currentStamina = robertinhoo.getStaminaSystem().getCurrentStamina();
        float maxStamina = robertinhoo.getStaminaSystem().getMaxStamina();
        boolean isExhausted = robertinhoo.getStaminaSystem().isExhausted();

        float staminaPercent = MathUtils.clamp(currentStamina / maxStamina, 0f, 1f);

        // Suaviza a transição visual da barra
        visualStaminaPercent += (staminaPercent - visualStaminaPercent) * INTERPOLATION_SPEED * deltaTime;

        // Escolhe cor base mais vibrante
        Color baseColor;
        if (isExhausted) {
            baseColor = new Color(0.9f, 0.1f, 0.1f, 1f);
        } else if (staminaPercent < 0.3f) {
            baseColor = new Color(1f, 0.65f, 0f, 1f);
        } else {
            baseColor = new Color(0.1f, 1f, 0.3f, 1f);
        }

        // Efeito de pulsação
        float alpha = 1f;
        if (isExhausted || staminaPercent < 0.2f) {
            alpha = 0.5f + 0.5f * MathUtils.sin(elapsed * PULSE_SPEED);
        }

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Tremor quando exausto
        float drawX = x;
        float drawY = y;
        if (isExhausted) {
            drawX += MathUtils.random(-JITTER_INTENSITY, JITTER_INTENSITY);
            drawY += MathUtils.random(-JITTER_INTENSITY, JITTER_INTENSITY);
        }

        shapeRenderer.begin(ShapeType.Filled);

        // Borda externa
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(drawX - BORDER_THICKNESS, drawY - BORDER_THICKNESS,
                           width + BORDER_THICKNESS * 2, height + BORDER_THICKNESS * 2);

        // Fundo interno
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(drawX, drawY, width, height);

        if (staminaPercent > 0.9f) {
            shapeRenderer.setColor(new Color(baseColor.r, baseColor.g, baseColor.b, 0.3f));
            shapeRenderer.rect(drawX - 2, drawY - 2, width + 4, height + 4);
        }
        Color topColor = baseColor.cpy();
        Color bottomColor = baseColor.cpy().lerp(Color.BLACK, 0.4f);

        float barWidth = width * visualStaminaPercent;

        shapeRenderer.rect(drawX, drawY, barWidth, height / 2f, bottomColor, bottomColor, topColor, topColor);
        shapeRenderer.setColor(new Color(baseColor.r, baseColor.g, baseColor.b, alpha));
        shapeRenderer.rect(drawX, drawY + height / 2f, barWidth, height / 2f);

        if (currentStamina <= 0f) {
            shapeRenderer.setColor(Color.RED);
            for (int i = 0; i < 6; i++) {
                float fx = drawX + MathUtils.random(barWidth);
                float fy = drawY + MathUtils.random(height);
                shapeRenderer.rect(fx, fy, 1f, 1f);
            }
        }

        shapeRenderer.end();
        batch.begin();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
