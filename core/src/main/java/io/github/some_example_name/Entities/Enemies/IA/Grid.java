package io.github.some_example_name.Entities.Enemies.IA;
import java.util.ArrayList;
import java.util.List;

import io.github.some_example_name.MapConfig.Mapa;


public class Grid {
    private final int width, height;
    private final Node[][] nodes;
    public Grid(Mapa mapa, int tileSize) {
        this.width  = mapa.mapWidth;
        this.height = mapa.mapHeight;
        this.nodes  = new Node[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean blocked = mapa.isTileBlocked(x, y);
                nodes[x][y] = new Node(x, y, !blocked);
                
                if (blocked) {
                    System.out.println("Tile bloqueado em: (" + x + ", " + y + ")");
                }
            }
        }
    }
    public Node getNode(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        return nodes[x][y];
    }

public List<Node> getNeighbors(Node node) {
    List<Node> neigh = new ArrayList<>();
    // 8 deltas: 4 cardeais + 4 diagonais
    final int[][] deltas = {
        { 1,  0}, {-1,  0}, { 0,  1}, { 0, -1},
        { 1,  1}, { 1, -1}, {-1,  1}, {-1, -1}
    };
    for (int[] d : deltas) {
        Node n = getNode(node.x + d[0], node.y + d[1]);
        if (n != null && n.walkable) {
            if (Math.abs(d[0]) + Math.abs(d[1]) == 2) {
                Node n1 = getNode(node.x + d[0], node.y);
                Node n2 = getNode(node.x, node.y + d[1]);
                if (n1 == null || n2 == null || !n1.walkable || !n2.walkable) 
                    continue;
            }
            neigh.add(n);
        }
    }
    return neigh;
}

}
