package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.MapConfig.Mapa;

import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;

public class AStarPathFinder {
    private final Supplier<Grid> gridSupplier; // agora recebemos um supplier
    private final Mapa mapa;

    public AStarPathFinder(Supplier<Grid> gridSupplier, Mapa mapa) {
        this.gridSupplier = gridSupplier;
        this.mapa = mapa;
    }

    public List<Vector2> findPath(Vector2 startWorld, Vector2 endWorld, int tileSize) {
        Grid grid = gridSupplier.get();
        Vector2 startTile = mapa.worldToTile(startWorld);
        Vector2 endTile = mapa.worldToTile(endWorld);

        int startX = (int) startTile.x;
        int startY = (int) startTile.y;
        int endX = (int) endTile.x;
        int endY = (int) endTile.y;
        Node start = grid.getNode(startX, startY);
        Node end = grid.getNode(endX, endY);

        if (start == null) {
            System.out.println("   ❌ Start node inválido (fora do grid ou null).");
            return Collections.emptyList();
        }
        if (end == null) {
            System.out.println("   ❌ End node inválido (fora do grid ou null).");
            return Collections.emptyList();
        }
        if (!start.walkable) {
            System.out.println("   ⛔ Start node bloqueado.");
            return Collections.emptyList();
        }
        if (!end.walkable) {

            return Collections.emptyList();
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::fCost));
        HashSet<Node> closedSet = new HashSet<>();

        start.gCost = 0;
        start.hCost = heuristic(start, end);
        start.parent = null;
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current == end) {
                break;
            }

            closedSet.add(current);

            for (Node neighbor : grid.getNeighbors(current)) {
                if (closedSet.contains(neighbor))
                    continue;

                double tentativeG = current.gCost + 1;
                if (tentativeG < neighbor.gCost || !openSet.contains(neighbor)) {
                    neighbor.gCost = tentativeG;
                    neighbor.hCost = heuristic(neighbor, end);
                    neighbor.parent = current;
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        List<Vector2> path = new ArrayList<>();
        Node curr = end;
        while (curr != null) {
            path.add(0, mapa.tileToWorld(curr.x, curr.y));
            curr = curr.parent;
        }

        if (path.size() > 1) {
            path.remove(0);
        }
        return path;
    }

private double heuristic(Node a, Node b) {
    double dx = a.x - b.x;
    double dy = a.y - b.y;
    return dx*dx + dy*dy;
}

}
