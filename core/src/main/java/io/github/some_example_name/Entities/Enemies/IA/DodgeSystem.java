package io.github.some_example_name.Entities.Enemies.IA;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.MapConfig.Mapa;
import java.util.Random;
import com.badlogic.gdx.Gdx;

public class DodgeSystem {
    private final PathfindingSystem pathfindingSystem;
    private final Mapa mapa;
    private final Random random;

    private static final boolean DEBUG_LOGS = true;

    private static final float DODGE_FORCE = 5f;
    private static final float MIN_DODGE_DISTANCE = 3f;
    private static final float MAX_DODGE_DISTANCE = 5f;

    private float dodgeCooldown = 0f;
    private static final float DODGE_COOLDOWN_TIME = 2.0f;

    public DodgeSystem(PathfindingSystem pathfindingSystem, Mapa mapa) {
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.random = new Random();

        if (DEBUG_LOGS) {
            Gdx.app.log("DodgeSystem", "Sistema de esquiva inicializado");
        }
    }

    public void update(float deltaTime) {
        if (dodgeCooldown > 0) {
            dodgeCooldown -= deltaTime;
        }
    }

    /**
     * Executa uma esquiva quando toma hit perto do player
     */
    public boolean executeDodgeOnHit(Body body, Vector2 currentPosition, Vector2 playerPosition) {
        if (DEBUG_LOGS) {
            Gdx.app.log("DodgeSystem", "=== TENTANDO ESQUIVA NO HIT ===");
            Gdx.app.log("DodgeSystem", "Posição atual: " + currentPosition);
            Gdx.app.log("DodgeSystem", "Posição do player: " + playerPosition);
            Gdx.app.log("DodgeSystem", "Cooldown: " + dodgeCooldown + "/" + DODGE_COOLDOWN_TIME);
        }

        if (dodgeCooldown > 0) {
            if (DEBUG_LOGS)
                Gdx.app.log("DodgeSystem", "❌ Cooldown ativo - não pode esquivar");
            return false;
        }

        float distanceToPlayer = currentPosition.dst(playerPosition);
        if (DEBUG_LOGS)
            Gdx.app.log("DodgeSystem", "Distância até player: " + distanceToPlayer);

        // Só esquiva se estiver perto o suficiente do player
        if (distanceToPlayer > 6f) {
            if (DEBUG_LOGS)
                Gdx.app.log("DodgeSystem", "❌ Muito longe do player - não precisa esquivar");
            return false;
        }

        Vector2 dodgeDirection = findSimpleDodgeDirection(currentPosition, playerPosition);

        if (dodgeDirection != null) {
            // Aplica o impulso de esquiva
            Vector2 dodgeImpulse = dodgeDirection.nor().scl(DODGE_FORCE * body.getMass());
            body.applyLinearImpulse(dodgeImpulse, body.getWorldCenter(), true);

            dodgeCooldown = DODGE_COOLDOWN_TIME;

            if (DEBUG_LOGS) {
                Gdx.app.log("DodgeSystem", "✅ ESQUIVA BEM-SUCEDIDA!");
                Gdx.app.log("DodgeSystem", "Direção: " + dodgeDirection);
                Gdx.app.log("DodgeSystem", "Força: " + dodgeImpulse);
            }
            return true;
        } else {
            if (DEBUG_LOGS)
                Gdx.app.log("DodgeSystem", "❌ Nenhuma direção de esquiva segura encontrada");
            return false;
        }
    }

    /**
     * Encontra uma direção simples para esquiva (oposta ao player)
     */
    private Vector2 findSimpleDodgeDirection(Vector2 currentPosition, Vector2 playerPosition) {
        if (DEBUG_LOGS)
            Gdx.app.log("DodgeSystem", "🔍 Buscando direção de esquiva...");

        // Direção principal: oposta ao player
        Vector2 awayFromPlayer = currentPosition.cpy().sub(playerPosition).nor();

        // Tenta a direção oposta primeiro
        if (isDodgeDirectionSafe(currentPosition, awayFromPlayer)) {
            if (DEBUG_LOGS)
                Gdx.app.log("DodgeSystem", "✅ Direção oposta segura");
            return awayFromPlayer;
        }

        // Tenta direções laterais
        Vector2[] sideDirections = {
                awayFromPlayer.cpy().rotateDeg(45f),
                awayFromPlayer.cpy().rotateDeg(-45f),
                awayFromPlayer.cpy().rotateDeg(90f),
                awayFromPlayer.cpy().rotateDeg(-90f)
        };

        for (int i = 0; i < sideDirections.length; i++) {
            if (isDodgeDirectionSafe(currentPosition, sideDirections[i])) {
                if (DEBUG_LOGS)
                    Gdx.app.log("DodgeSystem", "✅ Direção lateral " + (i + 1) + " segura");
                return sideDirections[i];
            }
        }

        // Última tentativa: qualquer direção que não seja para uma parede
        for (int i = 0; i < 8; i++) {
            float angle = i * 45f;
            Vector2 randomDirection = new Vector2(1, 0).rotateDeg(angle);
            if (isDodgeDirectionSafe(currentPosition, randomDirection)) {
                if (DEBUG_LOGS)
                    Gdx.app.log("DodgeSystem", "✅ Direção aleatória " + angle + "° segura");
                return randomDirection;
            }
        }

        return null;
    }

    /**
     * Verifica se uma direção de esquiva é segura
     */
    private boolean isDodgeDirectionSafe(Vector2 currentPosition, Vector2 direction) {
        Vector2 targetPosition = currentPosition.cpy().add(direction.nor().scl(MAX_DODGE_DISTANCE));

        // Verifica se a posição alvo é válida
        if (!isValidPosition(targetPosition)) {
            if (DEBUG_LOGS)
                Gdx.app.log("DodgeSystem", "🚫 Posição alvo inválida: " + targetPosition);
            return false;
        }

        // Verifica se há caminho (usando pathfinding simples)
        if (!hasSimplePath(currentPosition, targetPosition)) {
            if (DEBUG_LOGS)
                Gdx.app.log("DodgeSystem", "🚫 Sem caminho para: " + targetPosition);
            return false;
        }

        if (DEBUG_LOGS)
            Gdx.app.log("DodgeSystem", "✅ Direção segura: " + direction);
        return true;
    }

    /**
     * Verificação simples de caminho
     */
    private boolean hasSimplePath(Vector2 start, Vector2 end) {
        // Verifica alguns pontos ao longo da linha
        int checks = 3;
        for (int i = 1; i <= checks; i++) {
            float t = (float) i / (checks + 1);
            Vector2 checkPoint = start.cpy().lerp(end, t);

            if (!isValidPosition(checkPoint)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se uma posição é válida no mapa
     */
    private boolean isValidPosition(Vector2 position) {
        Vector2 tilePos = mapa.worldToTile(position);
        int tileX = (int) tilePos.x;
        int tileY = (int) tilePos.y;

        boolean isValid = tileX >= 0 && tileX < mapa.mapWidth &&
                tileY >= 0 && tileY < mapa.mapHeight &&
                !mapa.isTileBlocked(tileX, tileY);

        if (DEBUG_LOGS && !isValid) {
            Gdx.app.log("DodgeSystem", "🚫 Tile bloqueado: " + tileX + "," + tileY);
        }

        return isValid;
    }

    /**
     * Verifica se pode executar uma esquiva (sem cooldown)
     */
    public boolean canDodge() {
        return dodgeCooldown <= 0;
    }

    /**
     * Getter para debug
     */
    public float getDodgeCooldown() {
        return dodgeCooldown;
    }

}