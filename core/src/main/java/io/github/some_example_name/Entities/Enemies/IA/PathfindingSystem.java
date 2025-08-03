package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;
import java.util.List;
import io.github.some_example_name.Mapa;

public class PathfindingSystem {
    private Grid grid;               // tira o final, começa null
    private final AStarPathFinder pathFinder;
    private final Mapa mapa;
    private final int tileSize = 16;

    public PathfindingSystem(Mapa mapa) {
        this.mapa = mapa;
        // adianta o pathFinder, mas sem grid válido ainda:
        this.grid = null;
        this.pathFinder = new AStarPathFinder(this::getGrid, mapa);
    }

    /** Garante que o grid esteja inicializado com os dados do mapa carregado */
    private Grid getGrid() {
        if (grid == null) {
            System.out.println("🛠️ Inicializando Grid em PathfindingSystem (lazy)…");
            grid = new Grid(mapa, tileSize);
        }
        return grid;
    }

    /** Chamada pelo RatAI */
    public List<Vector2> findPath(Vector2 start, Vector2 end) {
        // no AStarPathFinder, o primeiro parâmetro agora é uma função que retorna o grid certo
        return pathFinder.findPath(start, end, tileSize);
    }

    /** se você quiser forçar recarga manual após um reload do mapa */
    public void updateGrid() {
        this.grid = null;
    }
}
