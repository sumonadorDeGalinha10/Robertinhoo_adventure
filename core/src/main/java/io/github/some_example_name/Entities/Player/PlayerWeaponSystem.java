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
    

    if (diff.len() != 0) {
        aimDirection.set(diff.nor());
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
            
       
            Vector2 muzzleOffset = currentWeapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE);
            Vector2 rotatedOffset = new Vector2(muzzleOffset).rotateDeg(getAimAngle());
            
    
            float drawX = baseX + rotatedOffset.x;
            float drawY = baseY + rotatedOffset.y;
            
          
            float originX = 8;
            float originY = 4;
            float angle = getAimAngle();
            boolean flip = angle > 90 && angle < 270;
            float scaleY = flip ? -1 : 1;
    
            batch.draw(
                frame,
                drawX - originX,
                drawY - originY,
                originX,
                originY,
                14,
                8,
                1 ,
                scaleY,
                angle
            );
        }
    }
    public void logAimInfo() {
        Gdx.app.log("AIM_DEBUG", "Mouse World: " + mousePosition.toString());
        Gdx.app.log("AIM_DEBUG", "Direction: " + aimDirection.toString());
        Gdx.app.log("AIM_DEBUG", "Angle: " + aimDirection.angleDeg());
    }
}
