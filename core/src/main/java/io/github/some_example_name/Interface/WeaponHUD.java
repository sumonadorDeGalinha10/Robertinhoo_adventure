package io.github.some_example_name.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;

public class WeaponHUD {
    private final Image weaponIcon;
    private final Label ammoLabel;
    private final ProgressBar reloadBar;
    private final Robertinhoo player;
    private final Stage stage;
    private final Skin skin;

    public WeaponHUD(SpriteBatch batch, Robertinhoo player) {
        this.player = player;
        this.skin = createBasicSkin();
        setupProgressBarStyle(skin);
        setupLabelStyle(skin);

        stage = new Stage(new FitViewport(1920, 1080), batch);
        
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left().pad(20); 
        
        
        // Inicializa os componentes corretamente
        this.weaponIcon = new Image();
        this.ammoLabel = new Label("", skin);
        this.reloadBar = new ProgressBar(0, 1, 0.01f, false, skin, "default-horizontal");
        reloadBar.setSize(300, 20); // Tamanho maior
        
        // Configura layout
        table.add(weaponIcon).size(100, 100).padRight(10);
        table.add(ammoLabel).width(150).left(); // Alinhar texto à esquerda
        table.row().padTop(5);
        table.add(reloadBar).colspan(2).width(300).height(20);


        
        
        stage.addActor(table);
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();
        
        // Textura da barra de fundo (CORREÇÃO)
        Pixmap barBg = new Pixmap(300, 20, Pixmap.Format.RGBA8888);
        barBg.setColor(new Color(0.2f, 0.2f, 0.2f, 1)); // Cinza escuro
        barBg.fill();
        skin.add("progress-bg", new Texture(barBg));
        
        // Textura da barra de preenchimento (CORREÇÃO)
        Pixmap barFg = new Pixmap(300, 20, Pixmap.Format.RGBA8888);
        barFg.setColor(new Color(0.9f, 0.7f, 0.1f, 1));
        barFg.fill();
        skin.add("progress-fg", new Texture(barFg));
        
        barBg.dispose();
        barFg.dispose();
        
        return skin;
    }
    
    private void setupProgressBarStyle(Skin skin) {
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = skin.getDrawable("progress-bg"); // Usar textura correta
        barStyle.knobBefore = skin.getDrawable("progress-fg"); // Usar textura correta
        skin.add("default-horizontal", barStyle);
    }

    private void setupLabelStyle(Skin skin) {
        BitmapFont font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
    }

    public void update(float delta) {
        Weapon weapon = player.getCurrentWeapon();
        
        if(weapon != null) {
            System.out.println("Tipo da arma: " + weapon.getClass().getSimpleName());
            System.out.println("MaxAmmo via getter: " + weapon.getMaxAmmo());
            
            weaponIcon.setDrawable(new TextureRegionDrawable(weapon.getIcon()));
            ammoLabel.setText(String.format("AMMO: %d/%d", weapon.getAmmo(), weapon.getMaxAmmo()));
            
          
            reloadBar.setRange(0, weapon.getMaxAmmo());
            reloadBar.setValue(weapon.getAmmo());
            
          
            reloadBar.setVisible(true);
        }
        stage.act(delta);
    }

    public void draw() {
    
   
    stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}