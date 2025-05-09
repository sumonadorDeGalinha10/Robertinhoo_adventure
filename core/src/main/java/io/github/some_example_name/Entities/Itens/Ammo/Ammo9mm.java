package io.github.some_example_name.Entities.Itens.Ammo;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Mapa;

import com.badlogic.gdx.graphics.Texture;

public class Ammo9mm extends Ammo {
    private Mapa mapa;
    private Body body;

    protected Vector2 setWorldPosition;
    public Ammo9mm(Mapa mapa, int x, int y) {
        super("9mm",15, 25, new TextureRegion(new Texture("ITENS/Ammo/ammo0.9mm.png")),2,1);
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        createBody(this.position);
    }

     public void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set(position.x + 0.5f, position.y + 0.5f);

        body = mapa.world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.4f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public Vector2 getPosition() {
        return position;
    }
    
    public void destroyBody() {
        mapa.world.destroyBody(body);
    }



}