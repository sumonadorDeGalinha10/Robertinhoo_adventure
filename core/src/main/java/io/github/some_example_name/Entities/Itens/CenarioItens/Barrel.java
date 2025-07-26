package io.github.some_example_name.Entities.Itens.CenarioItens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.BaseDestructible;

public class Barrel extends BaseDestructible {
    private Body body;
    private int health;
    private final World world;
    private static Texture textureSheet;
    private boolean bodyMarkedForDestruction = false;

    public Barrel(World world, float x, float y, 
                 TextureRegion intactTexture, 
                 TextureRegion destroyedTexture) {
        super(x, y, intactTexture, destroyedTexture);
        this.world = world;
        this.health = 3;
        this.loadAssets(); 
        createPhysicsBody();
         Gdx.app.log("Barrel", "Instanciando Barrel em " + x + "," + y);
    }
    
    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = Constants.BIT_OBJECT;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_PLAYER_ATTACK;
        
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();
        
        body.setUserData(this);
    }
@Override
public void loadAssets() {
    if (textureSheet == null) {
        Gdx.app.log("Barrel", "Carregando textura do barril...");
        textureSheet = new Texture(Gdx.files.internal("ITENS/DestrutiveItens/barril-Sheet.png"));
        Gdx.app.log("Barrel", "Textura carregada com sucesso.");
    } else {
        Gdx.app.log("Barrel", "Textura do barril já carregada anteriormente (usando cache).");
    }

    this.intactTexture = new TextureRegion(textureSheet);
    this.destroyedTexture = this.intactTexture; // ou região diferente
    Gdx.app.log("Barrel", "Texturas atribuídas a intactTexture e destroyedTexture.");
}

    public void takeDamage(int damage) {
        if (destroyed) return;
        
        health -= damage;
        if (health <= 0) {
            destroy();
            // Marca o corpo para destruição posterior em vez de destruir imediatamente
            bodyMarkedForDestruction = true;
        }
            }


            
    public boolean isBodyMarkedForDestruction() {
        return bodyMarkedForDestruction;
    }

    public void destroyBody() {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }

    public void setBodyMarkedForDestruction(boolean bodyMarkedForDestruction) {
        this.bodyMarkedForDestruction = bodyMarkedForDestruction;
    }

}