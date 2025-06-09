package io.github.some_example_name.Entities.Player;

import io.github.some_example_name.MapRenderer;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
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

    public Vector2 getMousePosition() {
        return mousePosition;
    }

    public void renderMiraArma(ShapeRenderer shapeRenderer) {
        if (mapRenderer == null)
            return;

        Vector2 playerWorldPos = player.body.getPosition();
        Weapon weapon = player.getInventory().getEquippedWeapon();

        float baseX = mapRenderer.offsetX + (playerWorldPos.x * MapRenderer.TILE_SIZE);
        float baseY = mapRenderer.offsetY + (playerWorldPos.y * MapRenderer.TILE_SIZE);

        Vector2 muzzleOffset = weapon != null ? weapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE) : new Vector2(0, 0);

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
        if (currentWeapon == null || player.state == Robertinhoo.DASH)
            return;

        // Atualiza a arma (que atualiza automaticamente o renderer)
        currentWeapon.update(delta, getAimDirection());

        Vector2 renderPosition = calculateWeaponPosition(currentWeapon);

        // Renderiza usando o renderer da arma
        currentWeapon.getRenderer().render(
                batch,
                renderPosition,
                mapRenderer.offsetX,
                mapRenderer.offsetY);
    }

    private Vector2 calculateWeaponPosition(Weapon weapon) {
        Vector2 playerWorldPos = player.body.getPosition();
        Vector2 muzzleOffset = weapon.getMuzzleOffset().scl(MapRenderer.TILE_SIZE);

        return new Vector2(
                (playerWorldPos.x * MapRenderer.TILE_SIZE) + muzzleOffset.x,
                (playerWorldPos.y * MapRenderer.TILE_SIZE) + muzzleOffset.y);
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

        Vector2 playerWorldPos = player.body.getPosition();
        Vector2 direction = getAimDirection().cpy().nor();
        Vector2 muzzleOffset = weapon.getMuzzleOffset();

        float angle = direction.angleRad();
        Vector2 rotatedOffset = new Vector2(
                muzzleOffset.x * MathUtils.cos(angle) - muzzleOffset.y * MathUtils.sin(angle),
                muzzleOffset.x * MathUtils.sin(angle) + muzzleOffset.y * MathUtils.cos(angle));

        float safetyMargin = 0.05f;
        Vector2 safetyOffset = direction.cpy().scl(safetyMargin);

        return playerWorldPos.cpy()
                .add(rotatedOffset)
                .add(safetyOffset);
    }

    public Vector2 getAimDirection() {
        return aimDirection.cpy();
    }

    public boolean isFlipped() {
        return getAimAngle() > 90 && getAimAngle() < 270;
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
