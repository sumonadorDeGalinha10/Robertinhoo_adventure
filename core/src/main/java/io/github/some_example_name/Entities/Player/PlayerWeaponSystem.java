package io.github.some_example_name.Entities.Player;

import io.github.some_example_name.MapRenderer;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PlayerWeaponSystem {
    private final Robertinhoo player;
    private final Vector2 aimDirection = new Vector2();
    private final Vector2 mousePosition = new Vector2();
    private float currentAimAngle = 0; 





    private final MapRenderer mapRenderer;
    public PlayerWeaponSystem(Robertinhoo player, MapRenderer mapRenderer) {
        this.player = player;
        this.mapRenderer = mapRenderer;
    }

    public void update(float deltaTime) {
        updateAimDirection();
   
    }


private void updateAimDirection() {
    float centerX = Gdx.graphics.getWidth() / 2f;
    float centerY = Gdx.graphics.getHeight() / 2f;
    
    int mouseX = Gdx.input.getX();
    int mouseY = Gdx.input.getY();
    
    float correctedMouseY = Gdx.graphics.getHeight() - mouseY;
    Vector2 diff = new Vector2(mouseX - centerX, correctedMouseY - centerY);
    


 if (!diff.isZero()) {
    aimDirection.set(diff.nor());
    currentAimAngle = (float) Math.toDegrees(Math.atan2(aimDirection.y, aimDirection.x));
    currentAimAngle = (currentAimAngle + 360) % 360;
} else {
    aimDirection.set(1, 0);
    currentAimAngle = 0;
}
    
}

public float getAimAngle() {
    float angle = aimDirection.angleDeg();
    return angle < 0 ? angle + 360 : angle;
}
    

    
    public Vector2 getAimDirection() {
        return aimDirection;
    }

    public Vector2 getMousePosition() {
        return mousePosition;
    }
    public void renderMiraArma(ShapeRenderer shapeRenderer) {
        if (mapRenderer == null) return;
    
        Vector2 playerWorldPos = player.body.getPosition();
        Weapon weapon = player.getInventory().getEquippedWeapon();
        
        float baseX = mapRenderer.offsetX + (playerWorldPos.x * MapRenderer.TILE_SIZE);
        float baseY = mapRenderer.offsetY + (playerWorldPos.y * MapRenderer.TILE_SIZE);
        
        Vector2 muzzleOffset = weapon != null ?
            weapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE) :
            new Vector2(0, 0);
        
        Vector2 rotatedOffset = new Vector2(muzzleOffset).rotateDeg(getAimAngle());
        
        float startX = baseX + rotatedOffset.x;
        float startY = baseY + rotatedOffset.y;
        
        Vector2 aimEnd = aimDirection.cpy().scl(150f);
        float endX = startX + aimEnd.x;
        float endY = startY + aimEnd.y;
    
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.3f);
        shapeRenderer.rectLine(startX, startY, endX, endY, 3f);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void renderWeapon(SpriteBatch batch, float delta) {
        Weapon currentWeapon = player.getInventory().getEquippedWeapon();
        if (currentWeapon != null) {
            TextureRegion frame = currentWeapon.getCurrentFrame(delta);
            Vector2 playerWorldPos = player.body.getPosition();
            
            float baseX = mapRenderer.offsetX + (playerWorldPos.x * MapRenderer.TILE_SIZE);
            float baseY = mapRenderer.offsetY + (playerWorldPos.y * MapRenderer.TILE_SIZE);
            Vector2 muzzleOffset = currentWeapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE * 0.5f);
            
            float originX = frame.getRegionWidth() / 5f;
            float originY = frame.getRegionHeight() / 5f;
            
            float angle = getAimAngle();
            boolean flip = angle > 90 && angle < 270;
            float scaleY = flip ? -0.2f : 0.2f;
            
            if (flip) {
                muzzleOffset.x *= -1;
            }
            
            float posX = baseX + muzzleOffset.x;
            float posY = baseY + muzzleOffset.y;
            float ajustx = flip ? -1.9f : 1.9f;
            
            batch.draw(
                frame,
                posX - originX -ajustx,
                posY - originY -2,
                originX,
                originY,
                frame.getRegionWidth(),
                frame.getRegionHeight(),
                0.2f,
                scaleY,
                angle
            );
        }
    }

    public float getAimAngleForRenderer() {
        return currentAimAngle;
    }
    public boolean isAiming() {
        float centerX = Gdx.graphics.getWidth() / 2f;
    float centerY = Gdx.graphics.getHeight() / 2f;
    int mouseX = Gdx.input.getX();
    int mouseY = Gdx.input.getY();
    float correctedMouseY = Gdx.graphics.getHeight() - mouseY; 

    
    float distance = Vector2.dst(mouseX, correctedMouseY, centerX, centerY);
    return distance > 10f;
    }

    public void logAimInfo() {
        Gdx.app.log("AIM_DEBUG", "Mouse World: " + mousePosition.toString());
        Gdx.app.log("AIM_DEBUG", "Direction: " + aimDirection.toString());
        Gdx.app.log("AIM_DEBUG", "Angle: " + aimDirection.angleDeg());
    }
}
