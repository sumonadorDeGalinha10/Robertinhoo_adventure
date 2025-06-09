package io.github.some_example_name.Entities.Itens.Weapon.Pistol;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;




public class PistolWithRober {
       public TextureRegion texture;
    public Vector2 muzzleOffset;

    public PistolWithRober(TextureRegion texture, Vector2 muzzleOffset) {
        this.texture = texture;
        this.muzzleOffset = muzzleOffset;
    }

    public enum Direction {
    SOUTH,
   
}
}
