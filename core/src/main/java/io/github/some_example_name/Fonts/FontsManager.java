package io.github.some_example_name.Fonts;
import java.util.BitSet;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;


public class FontsManager {
    public static BitmapFont createInventoryFont() {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        
   
        font.getRegion().getTexture().setFilter(
            TextureFilter.Linear, 
            TextureFilter.Linear
        );
        return font;
    }
}

