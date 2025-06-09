package io.github.some_example_name.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol.Pistol;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.WeaponHUD;
import io.github.some_example_name.MapRenderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class GameScreen extends CatScreen {

    private Mapa mapa;
    private MapRenderer renderer;
    private Robertinhoo robertinhoo;
    private WeaponHUD weaponHUD;
    private SpriteBatch hudBatch;
    private OrthographicCamera hudCamera;

    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        mapa = new Mapa();
        robertinhoo = mapa.robertinhoo;
        renderer = new MapRenderer(mapa);
        hudBatch = new SpriteBatch();
        weaponHUD = new WeaponHUD(hudBatch, robertinhoo);
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudBatch = new SpriteBatch();
        hudBatch.setProjectionMatrix(hudCamera.combined);
        
        weaponHUD = new WeaponHUD(hudBatch, robertinhoo);
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
        hudBatch.setProjectionMatrix(hudCamera.combined);
        weaponHUD.update(delta);
        weaponHUD.draw();
        
    }

    @Override
    public void resize(int width, int height) {
      
        
        renderer.resize(width, height);
        weaponHUD.resize(width, height);
        

        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
    }

    @Override
    public void hide() {
        if (renderer != null) {
            System.out.println("Destruindo MapRenderer...");
            renderer.dispose();
        }
        if (weaponHUD != null) {
            weaponHUD.dispose();
        }
        if (hudBatch != null) {
            hudBatch.dispose();
        }
    }
}
