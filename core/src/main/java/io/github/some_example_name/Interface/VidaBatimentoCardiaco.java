package io.github.some_example_name.Interface;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.Entities.Player.Robertinhoo;

public class VidaBatimentoCardiaco {
    private final Robertinhoo robertinhoo;

    // Animações para cada estado
    private Animation<TextureRegion> animacaoFull;
    private Animation<TextureRegion> animacaoMedio;
    private Animation<TextureRegion> animacaoBaixo;

    private BatimentoCardiaco estadoAtual;

    private float stateTime;

    private float x, y;
    private float width, height;

    public enum BatimentoCardiaco {
        FULL,
        MEDIO,
        BAIXO
    }

    public VidaBatimentoCardiaco(Texture batimentoSheet, Robertinhoo robertinhoo, float x, float y, float width,
            float height) {
        this.robertinhoo = robertinhoo;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.stateTime = 0f;

        criarAnimacoes(batimentoSheet);

        // Estado inicial
        estadoAtual = BatimentoCardiaco.FULL;
    }

    private void criarAnimacoes(Texture batimentoSheet) {
        int TOTAL_FRAMES = 33;
        int FRAMES_POR_ESTADO = 11;
        
        TextureRegion[] allFrames = TextureRegion.split(
            batimentoSheet,
            batimentoSheet.getWidth() / TOTAL_FRAMES,
            batimentoSheet.getHeight()
        )[0]; 
        animacaoFull = criarAnimacao(allFrames, 0, 10, 0.1f);
        animacaoMedio = criarAnimacao(allFrames, 11, 21, 0.1f);
        animacaoBaixo = criarAnimacao(allFrames, 22, 32, 0.1f);
    }


        private Animation<TextureRegion> criarAnimacao(TextureRegion[] allFrames, int start, int end, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[end - start + 1];
        for (int i = start; i <= end; i++) {
            frames[i - start] = allFrames[i];
        }
        return new Animation<>(frameDuration, frames);
    }


    public void update(float delta) {
        stateTime += delta;

        float vidaPercentual = robertinhoo.getLife();

        if (vidaPercentual >= 70) {
            estadoAtual = BatimentoCardiaco.FULL;

        } else if (vidaPercentual >= 30) {
            estadoAtual = BatimentoCardiaco.MEDIO;
        } else {
            estadoAtual = BatimentoCardiaco.BAIXO;
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = null;

        switch (estadoAtual) {
            case FULL:
                frame = animacaoFull.getKeyFrame(stateTime, true);
                break;
            case MEDIO:
                frame = animacaoMedio.getKeyFrame(stateTime, true);
                break;
            case BAIXO:
                frame = animacaoBaixo.getKeyFrame(stateTime, true);
                break;
        }

        if (frame != null) {
            batch.draw(frame, x, y, width, height);
        }
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

        public float getWidth() {
        return width;
    }

    public float getY() {
        return y;
    }
    
    public float getHeight() {
        return height;
    }

    public void dispose() {
        if (animacaoFull != null) {
            animacaoFull.getKeyFrames()[0].getTexture().dispose();
        }
        if (animacaoMedio != null) {
            animacaoMedio.getKeyFrames()[0].getTexture().dispose();
        }
        if (animacaoBaixo != null) {
            animacaoBaixo.getKeyFrames()[0].getTexture().dispose();
        }
    }

}