package io.github.some_example_name.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.Interface.RobertinhoFaceHUD;
import io.github.some_example_name.Interface.WeaponHUD;
import io.github.some_example_name.MapRenderer;

public class GameScreen extends CatScreen {

    private Mapa mapa;
    private MapRenderer renderer;
    private Robertinhoo robertinhoo;
    private WeaponHUD weaponHUD;
    private SpriteBatch hudBatch;
    private OrthographicCamera hudCamera;
    private RobertinhoFaceHUD robertinhoFaceHUD;


    public GameScreen(Game game) {
        super(game);
    }

    @Override
    public void show() {
        mapa = new Mapa();
        robertinhoo = mapa.robertinhoo;
        renderer = new MapRenderer(mapa);

        hudBatch = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudBatch.setProjectionMatrix(hudCamera.combined);

        weaponHUD = new WeaponHUD(robertinhoo);
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight(); // Corrigido para height

        robertinhoFaceHUD = new RobertinhoFaceHUD(width, height, robertinhoo);

        robertinhoo.setMapRenderer(renderer);
        weaponHUD.setBatch(hudBatch);

        System.out.println("MapRenderer configurado no Robertinhoo.");
    }

    @Override
    public void render(float delta) {
        delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        robertinhoo.update(delta);
        mapa.update(delta);

        renderer.render(delta, robertinhoo);

        weaponHUD.update(delta);
        robertinhoFaceHUD.update(delta);

        hudBatch.begin();
        weaponHUD.draw();
        robertinhoFaceHUD.draw(hudBatch);
        hudBatch.end();
    }

@Override
public void resize(int width, int height) {
    // Atualize PRIMEIRO a câmera do jogo principal
    renderer.resize(width, height);
    
    // Atualize AGORA a câmera HUD
    hudCamera.setToOrtho(false, width, height);
    hudCamera.update();
    hudBatch.setProjectionMatrix(hudCamera.combined);
    
    // Atualize os elementos HUD
    if (weaponHUD != null) {
        weaponHUD.resize(width, height);
    }
    
    robertinhoFaceHUD.updateScreenSize(width, height);
    
    System.out.println("[RESIZE] Tela: " + width + "x" + height);
}

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

        if (renderer != null) {
            renderer.dispose();
        }
        if (weaponHUD != null) {
            weaponHUD.dispose();
        }
        if (hudBatch != null) {
            hudBatch.dispose();
        }

        if (robertinhoFaceHUD != null) {
            robertinhoFaceHUD.dispose();
        }
    }
}