package io.github.some_example_name.Screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.Main;

public class MainScreen implements Screen {

    private final Main game;
    private Texture backgroundTexture;
    private BitmapFont font;

    public MainScreen(final Main game) {
        this.game = game;
    }


    @Override
    public void show() {
        font = new BitmapFont(); // Fonte padrão do LibGDX
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Renderiza o menu
        game.batch.begin();
        font.draw(game.batch, "Aperte qualquer tecla cria", Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f);
        game.batch.end();

        if (Gdx.input.isTouched()) {
            System.out.println("Tela tocada! Mudando para GameScreen.");
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        font.dispose();
    }
}
