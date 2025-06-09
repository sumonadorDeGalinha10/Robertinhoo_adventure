package io.github.some_example_name.Entities.Itens.Weapon.Pistol;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon.TipoMao;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;

import java.util.HashMap;
import java.util.Map;

public class Pistol extends Weapon {

    private Mapa mapa;
    private Inventory inventory;
    private float verticalSpeed = 0f;
    private Texture idleTexture;
    private Texture shootTexture;

    private float animationTime = 0f;
    private int maxAmmo;
    private int pistolMaxAmmo = 15;

    private float reloadTime = 0;
    private float reloadDuration = 2.1f; // Tempo de duração da recarga em segundos

    private Animation<TextureRegion> shootAnim;
    private Animation<TextureRegion> reloadAnim;

    @Override
    public TipoMao getTipoMao() {
        return TipoMao.UMA_MAO;
    }

    protected Vector2 position;

    public Pistol(Mapa mapa, int x, int y, Inventory inventory) {
        super();
        this.maxAmmo = 15;
        this.ammo = this.maxAmmo;
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        this.inventory = inventory;
        this.fireRate = 2f;
        this.damage = 10f;
        this.icon = new TextureRegion(new Texture("ITENS/Pistol/GUN_01_[square_frame]_01_V1.00.png"));
        createBody(this.position);
        loadTexturesAndAnimations();
        this.gridWidth = 2;
        this.gridHeight = 2;
        this.occupiedCells = new Vector2[] {
                new Vector2(0, 0),
                new Vector2(1, 0),
                new Vector2(0, 1)
        };

    }

    @Override
    public Vector2[] getOccupiedCells() {
        return occupiedCells;
    }

    @Override
    public int getMaxAmmo() {
        return pistolMaxAmmo;
    }

    @Override
    public void reload() {
        System.out.println("recarregando");

        // Não permitir recarregar se já estiver recarregando ou sem inventário
        if (currentState == WeaponState.RELOADING || inventory == null) {
            return;
        }

        int needed = maxAmmo - ammo;
        String requiredType = "9mm";
        int available = inventory.getAmmoCount(requiredType);

        if (available <= 0) {
            System.out.println("Sem munição para recarregar");
            return;
        }

        int toReload = Math.min(needed, available);

        if (toReload > 0) {
            inventory.consumeAmmo(requiredType, toReload);
            ammo += toReload;
            currentState = WeaponState.RELOADING;
            reloadTime = 0;
            
            reloadJustTriggered = true;
            System.out.println("Recarregou " + toReload + " balas");
        } else {
            System.out.println("Carregador já está cheio");
        }
        
      
    }

    @Override
    public void update(float delta) {
        updateFloatation(delta);
        timeSinceLastShot += delta;

        if (timeSinceLastShot >= 1 / fireRate) {
            canShoot = true;
        }

        if (currentState == WeaponState.RELOADING) {
            reloadTime += delta;

            if (reloadTime >= reloadDuration) {
                currentState = WeaponState.IDLE;
                reloadTime = 0;
            }
        }
    }

    public void createBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KinematicBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        body.setLinearVelocity(0, verticalSpeed);
        body.setUserData(this);
    }

    private void loadTexturesAndAnimations() {
        idleTexture = new Texture("ITENS/Pistol/GUN_01_[square_frame]_01_V1.00.png");

        shootTexture = new Texture("ITENS/Pistol/[FULL]PistolV1.01.png");

        shootAnim = createAnimation(shootTexture, 10, 0.1f);
    }

    private Animation<TextureRegion> createAnimation(Texture texture, int frameCount, float frameDuration) {
        int frameWidth = texture.getWidth() / frameCount;
        int frameHeight = texture.getHeight();
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight);
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.NORMAL);
        return anim;
    }

    public TextureRegion getCurrentFrame(float delta) {
        if (currentState == WeaponState.SHOOTING) {
            animationTime += delta;
            TextureRegion frame = shootAnim.getKeyFrame(animationTime, false);
            if (shootAnim.isAnimationFinished(animationTime)) {
                currentState = WeaponState.IDLE;
                animationTime = 0f;
            }
            return frame;
        }

        return shootAnim.getKeyFrame(0);
    }

    @Override
    public void shoot(Vector2 position, Vector2 direction) {
         if (currentState == WeaponState.RELOADING) {
            return;
        }
        if (canShoot && ammo > 0) {
            new Projectile(mapa, position, direction.nor().scl(35f), damage);

            shotTriggered = true;

            ammo--;
            canShoot = false;
            timeSinceLastShot = 0f;
        }
    }

    @Override
    public WeaponState getCurrentState() {
        return currentState;
    }

    public Vector2 getPosition() {
        if (body != null) {
            return body.getPosition();
        }
        return position;
    }

    @Override
    public Vector2 getMuzzleOffset() {

        return new Vector2(0.001f, 0.001f);
    }

    @Override
    public void rotate() {
        int temp = gridWidth;
        gridWidth = gridHeight;
        gridHeight = temp;
    }

    @Override
    public void destroyBody() {
        if (this.body != null) {
            this.body.getWorld().destroyBody(this.body);
            this.body = null;
        }
    }

    public void dispose() {
        if (idleTexture != null)
            idleTexture.dispose();
        if (shootTexture != null)
            shootTexture.dispose();

    }

}