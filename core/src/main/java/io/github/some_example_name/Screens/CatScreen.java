package io.github.some_example_name.Screens;



import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public abstract class CatScreen implements Screen {
	Game game;

	public CatScreen (Game game) {
		this.game = game;
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void show () {
	}

	@Override
	public void hide () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
