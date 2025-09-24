package io.github.some_example_name.Entities.Itens.CenarioItens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.some_example_name.Entities.Itens.Contact.Constants;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Renderer.ItensRenderer.BaseDestructible;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowComponent;
import io.github.some_example_name.Entities.Renderer.Shadow.ShadowEntity;
import io.github.some_example_name.MapConfig.Mapa;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.Gdx;

public class Barrel extends BaseDestructible implements ShadowEntity {
    private Body body;
    private int health;
    private final Mapa mapa;
    private static Texture textureSheet;
    private boolean bodyMarkedForDestruction = false;
    private ShadowComponent shadowComponent;
    private boolean hasDropped = false;

    private static final float SINGLE_HIT_PROBABILITY = 0.3f;

    private float flashTimer = 0f;
    private static final float FLASH_DURATION = 0.15f;
    private boolean flashActive = false;

    public Barrel(Mapa mapa, float x, float y,
            TextureRegion intactTexture,
            TextureRegion destroyedTexture) {
        super(x, y, intactTexture, destroyedTexture);
        this.mapa = mapa;
        this.health = 2;
        Gdx.app.log("Barrel", "Instanciando Barrel em " + x + "," + y);
        this.loadAssets();
        createPhysicsBody();
        this.shadowComponent = new ShadowComponent(
                6, 3, -0.2f, 0.6f,
                new Color(0.1f, 0.1f, 0.1f, 1));
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.2f, 0.2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = Constants.BIT_OBJECT;
        fixtureDef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_PLAYER_ATTACK | Constants.BIT_PROJECTILE | Constants.BIT_ENEMY;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        body.setUserData(this);
    }

    @Override
    public void loadAssets() {
        if (textureSheet == null) {
            textureSheet = new Texture(Gdx.files.internal("ITENS/DestrutiveItens/barril-Sheet.png"));
        }

        int frameWidth = textureSheet.getWidth() / 6;
        int frameHeight = textureSheet.getHeight();

        this.intactTexture = new TextureRegion(textureSheet, 0, 0, frameWidth, frameHeight);

        TextureRegion[] destructionFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            destructionFrames[i] = new TextureRegion(textureSheet, (i + 1) * frameWidth, 0, frameWidth, frameHeight);
        }
        this.destructionAnimation = new Animation<>(0.08f, destructionFrames);
        this.destroyedTexture = destructionFrames[4];
    }

    public void takeDamage(float damage) {
        if (destroyed || isAnimating) {
            return;
        }
        flashTimer = FLASH_DURATION;
        flashActive = true;
        if (MathUtils.random() < SINGLE_HIT_PROBABILITY) {
            Gdx.app.log("Barrel", "Tiro crítico! Destruição em 1 hit.");
            health = 0;
        } else {
            health -= damage;
            Gdx.app.log("Barrel", "Recebeu " + damage + " de dano. Vida agora: " + health);
        }

        if (health <= 0) {
            super.startDestructionAnimation();
            destroy();
            dropGunpowder();
        }
    }

    public void startDestructionAnimation() {
        isAnimating = true;
        animationTime = 0f;
    }

    private void dropGunpowder() {
        if (!hasDropped) {
            hasDropped = true;
            final Vector2 barrelBodyPos = body.getPosition().cpy();
            Gdx.app.log("Barrel", "Posição do barril: " + barrelBodyPos.x + ", " + barrelBodyPos.y);

            mapa.addPendingAction(() -> {
                float x = barrelBodyPos.x - 0.54f;
                float y = barrelBodyPos.y - 0.54f;
                Gdx.app.log("Barrel", "Dropando polvora em: " + x + ", " + y);

                PolvoraBruta polvoraBruta = new PolvoraBruta(
                        mapa.world,
                        x,
                        y);

                polvoraBruta.createBody(new Vector2(x, y));
                mapa.addCraftItem(polvoraBruta);
            });
        }
    }

    public void destroyBody() {
        if (body != null) {
            mapa.world.destroyBody(body);
            body = null;
        }
    }

    public void setBodyMarkedForDestruction(boolean bodyMarkedForDestruction) {
        this.bodyMarkedForDestruction = bodyMarkedForDestruction;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (flashTimer > 0f) {
            flashTimer -= delta;
            if (flashTimer <= 0f)
                flashActive = false;
        }
        // Verifica fim de animação de destruição
        if (isAnimating && destructionAnimation.isAnimationFinished(animationTime)) {
            bodyMarkedForDestruction = true;
            Gdx.app.log("Barrel", "Animação finalizada. Marcando corpo para destruição.");
        }
    }

    public boolean isFlashActive() {
        return flashActive;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public ShadowComponent getShadowComponent() {
        return shadowComponent;
    }

    public boolean isBodyMarkedForDestruction() {
        return bodyMarkedForDestruction;
    }
}
