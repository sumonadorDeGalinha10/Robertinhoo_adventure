package io.github.some_example_name.Entities.Itens.Weapon.Calibre12;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.Mapa;
import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon.TipoMao;

public class Calibre12 extends Weapon {
    
    private Mapa mapa;
    private Inventory inventory;
    private Texture iconTexture;
    private float reloadTime = 0;
    private float reloadDuration = 3.0f; // Tempo maior de recarga
    private int shotgunMaxAmmo = 6; // Menor capacidade
    protected Vector2 position;

    public Calibre12(Mapa mapa, int x, int y, Inventory inventory) {
        super();
        this.maxAmmo = shotgunMaxAmmo;
        this.ammo = this.maxAmmo;
        this.position = new Vector2(x, y);
        this.mapa = mapa;
        this.inventory = inventory;
        this.fireRate = 0.8f; // Cadência mais lenta
        this.damage = 25f; // Dano maior por projetil
        this.iconTexture = new Texture("ITENS/Shotgun/shotgun_icon.png");
        this.icon = new TextureRegion(iconTexture);
        
        createBody(this.position);
        
        this.gridWidth = 3;
        this.gridHeight = 2;
        this.occupiedCells = new Vector2[] {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(2, 0),
            new Vector2(0, 1),
            new Vector2(1, 1)
        };
    }

    @Override
    public TipoMao getTipoMao() {
        return TipoMao.DUAS_MAOS; // Shotgun requer duas mãos
    }

    @Override
    public Vector2[] getOccupiedCells() {
        return occupiedCells;
    }

    @Override
    public int getMaxAmmo() {
        return shotgunMaxAmmo;
    }

    @Override
    public void reload() {
        if (currentState == WeaponState.RELOADING || inventory == null) {
            return;
        }

        int needed = maxAmmo - ammo;
        String requiredType = "12gauge"; // Tipo de munição diferente
        int available = inventory.getAmmoCount(requiredType);

        if (available <= 0) {
            return;
        }

        int toReload = Math.min(needed, available);

        if (toReload > 0) {
            inventory.consumeAmmo(requiredType, toReload);
            ammo += toReload;
            currentState = WeaponState.RELOADING;
            reloadTime = 0;
            reloadJustTriggered = true;
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
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position);

        body = mapa.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.7f, 0.7f); // Tamanho maior

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;
        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData(this);
    }

    @Override
    public TextureRegion getCurrentFrame(float delta) {
        // Implementação similar à Pistol se necessário
        return null;
    }

    @Override
    public void shoot(Vector2 position, Vector2 direction) {
        if (currentState == WeaponState.RELOADING || !canShoot || ammo <= 0) {
            return;
        }

        // Disparar múltiplos projéteis (padrão de shotgun)
        int pelletCount = 8; // Número de pelotas
        float spreadAngle = 20f; // Ângulo de dispersão
        
        for (int i = 0; i < pelletCount; i++) {
            // Calcular direção com dispersão
            float angleVariation = (float) Math.toRadians(spreadAngle * (Math.random() - 0.5));
            Vector2 pelletDir = new Vector2(direction).rotateRad(angleVariation);
            
            new Projectile(mapa, position, pelletDir.nor().scl(30f), damage * 0.7f); // Dano reduzido por pelota
        }

        shotTriggered = true;
        ammo--;
        canShoot = false;
        timeSinceLastShot = 0f;
    }

    @Override
    public WeaponState getCurrentState() {
        return currentState;
    }

    @Override
    public Vector2 getPosition() {
        return body != null ? body.getPosition() : position;
    }

    @Override
    public Vector2 getMuzzleOffset() {
        return new Vector2(0.5f, 0.1f); // Offset diferente
    }

    @Override
    public void rotate() {
        int temp = gridWidth;
        gridWidth = gridHeight;
        gridHeight = temp;
    }

    @Override
    public void destroyBody() {
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }


    public void dispose() {
        if (iconTexture != null) {
            iconTexture.dispose();
        }
    }
}
