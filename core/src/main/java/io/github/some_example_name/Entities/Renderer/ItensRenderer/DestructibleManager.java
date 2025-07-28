package io.github.some_example_name.Entities.Renderer.ItensRenderer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.Entities.Itens.CenarioItens.Barrel;
import io.github.some_example_name.Mapa;

public class DestructibleManager {
    private final Array<Destructible> destructibles;
    private final World world;
    private Mapa mapa;
    
public DestructibleManager(World world, Mapa mapa) {
    this.destructibles = new Array<>();
    this.world = world;
    this.mapa = mapa;
}
    public void createBarrel(float x, float y, 
                            TextureRegion intactTexture, 
                            TextureRegion destroyedTexture) {
        Barrel barrel = new Barrel(mapa, x, y, intactTexture, destroyedTexture);
        destructibles.add(barrel);
    }
    
    public void createCrate(float x, float y, 
                           TextureRegion intactTexture, 
                           TextureRegion destroyedTexture) {
        // Implementação similar para caixas
    }
    
    public Array<Destructible> getDestructibles() {
        return destructibles;
    }
    
    public void update(float delta) {
        for (Destructible d : destructibles) {
            d.update(delta);
        }
    }
}