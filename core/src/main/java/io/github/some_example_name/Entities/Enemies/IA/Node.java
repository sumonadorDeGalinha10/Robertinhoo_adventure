package io.github.some_example_name.Entities.Enemies.IA;

public  class Node {
    public final int x, y;
    public boolean walkable;
    public double gCost, hCost;
    public Node parent;

    public Node(int x, int y, boolean walkable) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
    }

    public double fCost() {
        return gCost + hCost;
    }
}
