package io.github.some_example_name.Entities.Inventory;


import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public interface Item {
    int getGridWidth();
    int getGridHeight();
    void rotate();
    Vector2[] getOccupiedCells();
    void setPosition(Vector2 position);
    void destroyBody();
    TextureRegion getIcon();
}