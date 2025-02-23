package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.some_example_name.Screens.MainScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */



public class Main  extends Game {

	public SpriteBatch batch;
	public BitmapFont font;
	public FitViewport viewport;

	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		viewport = new FitViewport(8, 5);
	
		font.setUseIntegerPositions(false);
		font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());
	
		// Adicionando um log para verificar a transição
		Gdx.app.log("Main", "Inicializando tela principal");
		this.setScreen(new MainScreen(this));
	}
	

	public void render() {
		super.render(); // important!
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
	}
}
