package io.github.some_example_name.Entities.Enemies;

import java.util.ArrayList;
import java.util.List;

public class EnemyManager {
    private List<Enemy> enemies;

    public EnemyManager() {
        enemies = new ArrayList<>();
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void update(float deltaTime) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
        }
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
