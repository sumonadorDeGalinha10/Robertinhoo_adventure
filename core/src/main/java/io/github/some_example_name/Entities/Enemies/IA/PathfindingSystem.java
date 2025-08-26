package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.MapConfig.Mapa;

import java.util.List;

public class PathfindingSystem {
    private Grid grid;
    private final AStarPathFinder pathFinder;
    private final Mapa mapa;
    private final int tileSize = 16;

    public PathfindingSystem(Mapa mapa) {
        this.mapa = mapa;
        this.grid = null;
        this.pathFinder = new AStarPathFinder(this::getGrid, mapa);
    }

    private Grid getGrid() {
        if (grid == null) {
            System.out.println("🛠️ Inicializando Grid em PathfindingSystem (lazy)…");
            grid = new Grid(mapa, tileSize);
        }
        return grid;
    }


    public List<Vector2> findPath(Vector2 start, Vector2 end) {
        return pathFinder.findPath(start, end, tileSize);
    }

    public void updateGrid() {
        this.grid = null;
    }
}
