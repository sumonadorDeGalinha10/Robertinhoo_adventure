package io.github.some_example_name.Entities.Itens.Weapon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Inventory.Inventory;

public class Pistol extends Weapon {

    private Mapa mapa;
    private Inventory inventory;
    private float verticalSpeed = 0f;
    private Texture idleTexture;
    private Texture shootTexture;
    private Texture reloadTexture;
    private float animationTime = 0f;
    private int maxAmmo;
    private int pistolMaxAmmo = 15;
   

    private Animation<TextureRegion> shootAnim;
    private Animation<TextureRegion> reloadAnim;

    @Override
    public TipoMao getTipoMao() {
        return TipoMao.UMA_MAO;
    }

    protected Vector2 position;

    private enum State {
        IDLE,
        SHOOTING,
        RELOADING
    }

    private State currentState = State.IDLE;

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
        this.occupiedCells = new Vector2[]{
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
    public void shoot(Vector2 position, Vector2 direction) {
        if (canShoot && ammo > 0) {
            System.out.println("ATIROU");

            new Projectile(mapa, position, direction.nor().scl(15f), damage);
            currentState = State.SHOOTING;
            ammo--;
            canShoot = false;
            System.out.println("renderizou tiro");
        }
    }

    @Override
    public int getMaxAmmo() {
        return pistolMaxAmmo;
    }

 
    @Override
    public void reload() {
        if (currentState != State.RELOADING && inventory != null) {
            int needed = maxAmmo - ammo;
            String requiredType = "9mm";
            int available = inventory.getAmmoCount(requiredType);
            int toReload = Math.min(needed, available);
            
            if (toReload > 0) {
                inventory.consumeAmmo(requiredType, toReload);
                ammo += toReload;
                currentState = State.RELOADING;
                animationTime = 0f;
            }
        }
    }

    @Override
    public void update(float delta) {

        updateFloatation(delta); 
        timeSinceLastShot += delta;
        if (timeSinceLastShot >= 1 / fireRate) {
            canShoot = true;
        }

        if (currentState == State.SHOOTING) {
            animationTime += delta;
            if (shootAnim.isAnimationFinished(animationTime)) {
                currentState = State.IDLE;
                animationTime = 0f;
            }
        } else if (currentState == State.RELOADING) {
            animationTime += delta;
            if (reloadAnim.isAnimationFinished(animationTime)) {
                ammo = maxAmmo;
                currentState = State.IDLE;
                animationTime = 0f;
                System.out.println("Recarregado");
            }
        }

        if (reloading) {
            reloadProgress += delta / reloadAnim.getAnimationDuration();
            if (reloadProgress >= 1) {
                reloading = false;
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

        reloadTexture = new Texture("ITENS/Pistol/GUN_01_[square_frame]_01_V1.00.png");

        shootAnim = createAnimation(shootTexture, 10, 0.1f);
        reloadAnim = createAnimation(reloadTexture, 6, 0.15f);
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
        if (currentState == State.SHOOTING) {
            animationTime += delta;
            TextureRegion frame = shootAnim.getKeyFrame(animationTime, false);
            if (shootAnim.isAnimationFinished(animationTime)) {
                currentState = State.IDLE;
                animationTime = 0f;
            }
            return frame;
        } else if (currentState == State.RELOADING) {
            animationTime += delta;
            TextureRegion frame = reloadAnim.getKeyFrame(animationTime, false);
            if (reloadAnim.isAnimationFinished(animationTime)) {
                ammo = maxAmmo;
                currentState = State.IDLE;
                animationTime = 0f;
            }
            return frame;
        }
        return shootAnim.getKeyFrame(0);
    }

    public Vector2 getPosition() {
        if (body != null) {
            return body.getPosition();
        }
        return position;
    }

    public Vector2 getMuzzleOffset() {
        return new Vector2(0.8f, 0.1f);
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
        if (reloadTexture != null)
            reloadTexture.dispose();
    }

}