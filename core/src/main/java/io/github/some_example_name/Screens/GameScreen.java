package io.github.some_example_name.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapRenderer;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol;

public class GameScreen extends CatScreen {

    private Mapa mapa;
    private MapRenderer renderer;
    private Robertinhoo robertinhoo;
    private Pistol pistol;

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        mapa = new Mapa();
        robertinhoo = mapa.robertinhoo; // Robertinhoo é criado dentro do Mapa
        renderer = new MapRenderer(mapa);
    
        // Configurar o MapRenderer no Robertinhoo após a criação
        robertinhoo.setMapRenderer(renderer);
        
    
        System.out.println("MapRenderer configurado no Robertinhoo.");
    }

    @Override
    public void render(float delta) {
        // Limita o delta para evitar variações extremas de tempo de quadro
        delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

        // Limpa a tela
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Atualiza a posição e animação do jogador
        robertinhoo.update(delta);

        mapa.update(delta);

        // Renderiza o mapa e o jogador
        renderer.render(delta, robertinhoo);
    }

    @Override
    public void resize(int width, int height) {
      
        

        renderer.resize(width, height);
    }

    @Override
    public void hide() {
        // Descarrega recursos ao sair da tela
        if (renderer != null) {
            System.out.println("Destruindo MapRenderer...");
            renderer.dispose();
        }
    }
}
