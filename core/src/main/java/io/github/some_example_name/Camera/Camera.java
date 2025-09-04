package io.github.some_example_name.Camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.MapRenderer;

public class Camera {
    public OrthographicCamera camera;
    public static final int TILE_SIZE = 16;
    private float zoom;
    public MapRenderer mapRenderer;

    public Camera() {
        camera = new OrthographicCamera();
        zoom = 0.7f;
        camera.setToOrtho(
                false,
                Gdx.graphics.getWidth() * zoom / TILE_SIZE,
                Gdx.graphics.getHeight() * zoom / TILE_SIZE);
    }

    public void centerOnPlayer(Robertinhoo player, float offsetX, float offsetY) {
        Vector2 playerPos = player.body.getPosition();

        float playerWorldX = playerPos.x * MapRenderer.TILE_SIZE + offsetX;
        float playerWorldY = playerPos.y * MapRenderer.TILE_SIZE + offsetY;

        camera.position.set(
                playerWorldX,
                playerWorldY,
                0);

        camera.zoom = 3.0f;
        camera.update();
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;

        camera.viewportWidth = (width * zoom) / TILE_SIZE;
        camera.viewportHeight = (height * zoom) / TILE_SIZE;

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
