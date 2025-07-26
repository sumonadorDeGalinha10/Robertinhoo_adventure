package io.github.some_example_name.Entities.Player;

import io.github.some_example_name.MapRenderer;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Renderer.PlayerRenderer;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations;
import io.github.some_example_name.Entities.Renderer.WeaponAnimations.WeaponDirection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

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

    public Vector2 getMousePosition() {
        return mousePosition;
    }

    public void renderMiraArma(ShapeRenderer shapeRenderer) {
        if (mapRenderer == null || player == null)
            return;

        Weapon weapon = player.getInventory().getEquippedWeapon();
        if (weapon == null)
            return;

        // Obtém a posição REAL do cano da arma
        Vector2 muzzleWorldPos = getTrueMuzzlePosition();

        // Converte para coordenadas de tela
        float startX = mapRenderer.offsetX + muzzleWorldPos.x * MapRenderer.TILE_SIZE;
        float startY = mapRenderer.offsetY + muzzleWorldPos.y * MapRenderer.TILE_SIZE;

        // Direção da mira (já normalizada)
        Vector2 direction = getAimDirection();

        // Comprimento máximo da mira (em pixels)
        float maxLength = 500f;

        // Ponto final padrão (sem colisão)
        float endX = startX + direction.x * maxLength;
        float endY = startY + direction.y * maxLength;

        // Verifica colisão com paredes usando raycast
        RayCastResult collision = rayCast(muzzleWorldPos, direction, maxLength / MapRenderer.TILE_SIZE);

        if (collision.hit) {
            // Se houve colisão, ajusta o ponto final
            endX = mapRenderer.offsetX + collision.point.x * MapRenderer.TILE_SIZE;
            endY = mapRenderer.offsetY + collision.point.y * MapRenderer.TILE_SIZE;
        }

        // Renderiza a linha da mira
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.6f);
        shapeRenderer.rectLine(startX, startY, endX, endY, 3f);

        // // Renderiza um marcador no ponto de colisão (opcional)
        // if (collision.hit) {
        // shapeRenderer.setColor(1, 0, 0, 0.8f);
        // shapeRenderer.circle(endX, endY, 5);
        // }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private RayCastResult rayCast(Vector2 start, Vector2 direction, float maxDistance) {
        RayCastResult result = new RayCastResult();
        result.hit = false;

        Vector2 end = new Vector2(
                start.x + direction.x * maxDistance,
                start.y + direction.y * maxDistance);

        player.getMap().world.rayCast((fixture, point, normal, fraction) -> {

            if ("WALL".equals(fixture.getBody().getUserData())) {
                result.hit = true;
                result.point.set(point);
                result.fraction = fraction;
                return fraction; // Retorna a fração da colisão
            }
            return -1; // Continua o raycast
        }, start, end);

        return result;
    }

    private static class RayCastResult {
        public boolean hit = false;
        public Vector2 point = new Vector2();
        public float fraction = 1f;
    }

     public void renderWeapon(SpriteBatch batch, float delta, Robertinhoo player, float playerRenderX, float playerRenderY) {
        Weapon currentWeapon = player.getInventory().getEquippedWeapon();
        if (currentWeapon == null || player.state == Robertinhoo.DASH || player.state == Robertinhoo.MELEE_ATTACK)
            return;

        currentWeapon.update(delta, getAimDirection());

        Vector2 renderPosition = calculateWeaponPosition(currentWeapon, playerRenderX, playerRenderY);

        currentWeapon.getRenderer().render(
                batch,
                renderPosition,
                mapRenderer.offsetX,
                mapRenderer.offsetY);
    }

    

private Vector2 calculateWeaponPosition(Weapon weapon, float playerX, float playerY) {
    Vector2 muzzleOffset = weapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE);

    float playerScale =1.4f;
    
    float playerCenterX = playerX + (player.bounds.width * MapRenderer.TILE_SIZE * playerScale) /2;
    float playerCenterY = playerY + (player.bounds.height * MapRenderer.TILE_SIZE * playerScale) /2;
    
    return new Vector2(
        playerCenterX + muzzleOffset.x - 3.4f,
        playerCenterY + muzzleOffset.y-6.5f
    );
}

    public float getAimAngleForRenderer() {
        return currentAimAngle;
    }

    public Vector2 getMuzzlePosition() {
        Weapon weapon = player.getInventory().getEquippedWeapon();
        if (weapon == null)
            return player.body.getPosition();

        Vector2 playerWorldPos = player.body.getPosition();
        Vector2 muzzleOffset = weapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE * 0.5f);
        Vector2 rotatedOffset = muzzleOffset.cpy().rotateDeg(getAimAngle());

        return playerWorldPos.cpy().add(rotatedOffset.scl(1f / MapRenderer.TILE_SIZE));
    }

    public Vector2 getTrueMuzzlePosition() {
        Weapon weapon = player.getInventory().getEquippedWeapon();
        if (weapon == null)
            return player.body.getPosition();

        // Obtém a direção atual baseada no ângulo de mira
        float aimAngle = getAimAngle();
        WeaponDirection currentDir = DirectionUtils.getDirectionFromAngle(aimAngle);

        Vector2 muzzleOffset = weapon.getMuzzleOffset(currentDir);
        Vector2 playerWorldPos = player.body.getPosition();

        // Converte de pixels para unidades do mundo
        Vector2 worldOffset = new Vector2(
                muzzleOffset.x / MapRenderer.TILE_SIZE,
                muzzleOffset.y / MapRenderer.TILE_SIZE);

        // Rotaciona o offset conforme a direção
        float angleRad = MathUtils.degreesToRadians * aimAngle;
        Vector2 rotatedOffset = new Vector2(
                worldOffset.x * MathUtils.cos(angleRad) - worldOffset.y * MathUtils.sin(angleRad),
                worldOffset.x * MathUtils.sin(angleRad) + worldOffset.y * MathUtils.cos(angleRad));

        return playerWorldPos.cpy().add(rotatedOffset);
    }

    public Vector2 getAimDirection() {
        return aimDirection.cpy();
    }

    public boolean isFlipped() {
        return getAimAngle() > 90 && getAimAngle() < 270;
    }

    public void renderMuzzleDebug(ShapeRenderer shapeRenderer) {
        Vector2 muzzlePos = getTrueMuzzlePosition();
        Vector2 screenPos = new Vector2(
                muzzlePos.x * MapRenderer.TILE_SIZE + mapRenderer.offsetX,
                muzzlePos.y * MapRenderer.TILE_SIZE + mapRenderer.offsetY);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(screenPos.x, screenPos.y, 5);
        shapeRenderer.end();
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
