package io.github.some_example_name.Entities.Enemies.Castor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.Entities.Enemies.IA.ChaseSystem;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Enemies.IA.PatrolSystem;
import io.github.some_example_name.Entities.Enemies.IA.ShootSystem;
import io.github.some_example_name.Entities.Enemies.StateEnemy.StateEnemy;
import io.github.some_example_name.Entities.Player.Robertinhoo;
import io.github.some_example_name.MapConfig.Mapa;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.Entities.Enemies.IA.DodgeSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;

public class CastorIA {
    // Variável de debug - altere para false para desativar os logs
    private static final boolean DEBUG = false;

    private final Robertinhoo target;
    private final PathfindingSystem pathfindingSystem;
    private final Mapa mapa;
    private final Castor castor;
    private final PatrolSystem patrolSystem;
    private List<Vector2> chasePath = new ArrayList<>();
    private final ChaseSystem chaseSystem;
    private final ShootSystem shootSystem;
    private StateEnemy stateEnemy;
    private boolean wasInterruptedByDamage = false;
    private boolean isInvestigatingLastKnown = false;

    private final String castorId; // ID único para identificar cada castor nos logs

    private enum State {
        PATROL, CHASE, SHOOTING
    }

    private State currentState = State.PATROL;

    private float reactionTime = 0f;
    private static final float REACTION_DELAY = 0.7f;
    private static final float REACTION_VARIABILITY = 0.3f;

    private float stateCooldown = 0f;
    private static final float STATE_COOLDOWN_TIME = 3f;

    private static final float LAST_KNOWN_POSITION_UPDATE_INTERVAL = 1.0f;
    private static final float DETECTION_RANGE = 15f;
    private static final float SHOOTING_RANGE = 8f;
    private static final float CHASE_PERSISTENCE_TIME = 15.0f;

    private Vector2 lastKnownTargetPosition = null;
    private float chasePersistenceTimer = 0f;
    private float lastKnownPositionUpdateTimer = 0f;
    private boolean hasRecentSight = false;

    private Random random = new Random();

    public CastorIA(Castor castor, Robertinhoo target, PathfindingSystem pathfindingSystem, Mapa mapa,
            DodgeSystem dodgeSystem) {
        this.castor = castor;
        this.target = target;
        this.pathfindingSystem = pathfindingSystem;
        this.mapa = mapa;
        this.currentState = State.PATROL;
        this.patrolSystem = new PatrolSystem(mapa, castor.getBody(), pathfindingSystem);
        this.chaseSystem = new ChaseSystem(pathfindingSystem, mapa);
        this.shootSystem = new ShootSystem(castor, pathfindingSystem, mapa);
        this.stateEnemy = new StateEnemy();
        this.castorId = "Castor_" + System.identityHashCode(this); // ID único baseado no hash

        if (DEBUG) {
            Gdx.app.log(castorId, "🦫 CastorIA criado");
        }
    }

    public void update(float deltaTime, Body body) {
        if (castor.isDead() || castor.isTakingDamage()) {
            return;
        }

        if (wasInterruptedByDamage && !castor.isTakingDamage()) {
            wasInterruptedByDamage = false;
            resetAIState();
        }

        if (castor.isDead() || castor.isTakingDamage()) {
            wasInterruptedByDamage = true;
            return;
        }

        stateEnemy.update(deltaTime);

        // Atualizar estado visual
        switch (currentState) {
            case PATROL:
                stateEnemy.setState(StateEnemy.StateIcon.PATROL);
                break;
            case CHASE:
                stateEnemy.setState(StateEnemy.StateIcon.CHASE);
                break;
            case SHOOTING:
                stateEnemy.setState(StateEnemy.StateIcon.SHOOTING);
                break;
        }

        Vector2 currentPosition = body.getPosition();
        Vector2 targetPosition = target.getPosition();
        float distanceToTarget = currentPosition.dst(targetPosition);

        // Atualizar temporizadores - apenas se não estiver no modo PATROL
        if (stateCooldown > 0) {
            stateCooldown -= deltaTime;
        }

        // Só decrementar o timer de persistência se não tiver visão direta
        boolean hasLOS = hasLineOfSight(currentPosition, targetPosition);
        if (!hasLOS && currentState == State.CHASE) {
            chasePersistenceTimer -= deltaTime;
        } else if (hasLOS && currentState == State.CHASE) {
            // Reset do timer se tiver visão durante a perseguição
            chasePersistenceTimer = CHASE_PERSISTENCE_TIME;
        }

        lastKnownPositionUpdateTimer -= deltaTime;
        reactionTime -= deltaTime;

        if (reactionTime > 0) {
            return;
        }
        reactionTime = REACTION_DELAY + (random.nextFloat() * 2 - 1) * REACTION_VARIABILITY;

        if (target == null || target.getPosition() == null) {
            if (DEBUG)
                Gdx.app.log(castorId, "❌ Target ou posição do target é nula!");
            return;
        }

        // Atualizar última posição conhecida se tiver visão
        if (hasLOS) {
            lastKnownTargetPosition = new Vector2(targetPosition);
            hasRecentSight = true;
            chasePersistenceTimer = CHASE_PERSISTENCE_TIME; // Reset do temporizador
            lastKnownPositionUpdateTimer = LAST_KNOWN_POSITION_UPDATE_INTERVAL;
        } else if (lastKnownPositionUpdateTimer <= 0 && hasRecentSight) {
            // Atualizar periodicamente a última posição conhecida mesmo sem visão
            lastKnownTargetPosition = new Vector2(targetPosition);
            lastKnownPositionUpdateTimer = LAST_KNOWN_POSITION_UPDATE_INTERVAL;
        }

        if (DEBUG) {
            Gdx.app.log(castorId, "Estado atual: " + currentState +
                    ", Distância: " + distanceToTarget +
                    ", Posição: " + currentPosition +
                    ", Tempo Persistência: " + chasePersistenceTimer +
                    ", Tempo Cooldown: " + stateCooldown);
        }

        if (stateCooldown <= 0) {
            checkStateTransitions(distanceToTarget, hasLOS);
        }

        switch (currentState) {
            case PATROL:
                if (DEBUG)
                    Gdx.app.log(castorId, "Executando PATRULHA");
                updatePatrolWithForces(deltaTime, body, currentPosition);
                break;

            case CHASE:
                if (DEBUG)
                    Gdx.app.log(castorId, "Executando PERSEGUIR");

                Vector2 chaseTarget = hasLOS ? targetPosition : lastKnownTargetPosition;
                updateChaseState(deltaTime, body, currentPosition, chaseTarget, hasLOS);

                // Só volta a patrulhar se chegou na última posição conhecida E não tem mais
                // visão
                if (chaseSystem.hasReachedLastKnown() && !hasLOS && isInvestigatingLastKnown) {
                    if (DEBUG)
                        Gdx.app.log(castorId,
                                "Chegou na última posição conhecida sem encontrar jogador - voltando a PATROL");
                    currentState = State.PATROL;
                    patrolSystem.reset();
                    stateCooldown = STATE_COOLDOWN_TIME;
                    hasRecentSight = false;
                    lastKnownTargetPosition = null;
                    isInvestigatingLastKnown = false;
                    chaseSystem.clearReachedLastKnown();
                }
                break;

            case SHOOTING:
                if (DEBUG)
                    Gdx.app.log(castorId, "Executando ATIRAR");

                // Verificação crítica: só continua atirando se ainda tiver linha de visão
                boolean stillHasLOS = hasLineOfSight(currentPosition, targetPosition);

                if (!stillHasLOS) {
                    if (DEBUG)
                        Gdx.app.log(castorId, "Perdeu linha de visão, voltando a perseguir");
                    currentState = State.CHASE;
                    stateCooldown = STATE_COOLDOWN_TIME;
                    break;
                }

                boolean shouldChase = shootSystem.update(deltaTime, body, currentPosition, targetPosition);

                if (castor.canShoot() && !castor.isShooting() && stillHasLOS) {
                    if (DEBUG)
                        Gdx.app.log(castorId, "Iniciando animação de tiro via CastorIA");
                    castor.startShooting();
                }
                if (shouldChase) {
                    currentState = State.CHASE;
                    stateCooldown = STATE_COOLDOWN_TIME;
                }
                break;
        }
    }

    private void resetAIState() {
        currentState = State.CHASE;
        stateCooldown = 0f;
        // REMOVER: chasePersistenceTimer = CHASE_PERSISTENCE_TIME;
        hasRecentSight = true;
        lastKnownTargetPosition = new Vector2(target.getPosition());
        isInvestigatingLastKnown = false;
        if (DEBUG)
            Gdx.app.log(castorId, "Reiniciando IA após dano");
    }

    private void updateChaseState(float deltaTime, Body body, Vector2 currentPosition,
            Vector2 targetPosition, boolean hasLOS) {
        chaseSystem.update(deltaTime, body, currentPosition, targetPosition, hasLOS);
    }

    private void checkStateTransitions(float distanceToTarget, boolean hasLOS) {
        State previousState = currentState;
        Vector2 currentPosition = castor.getBody().getPosition();

        switch (currentState) {
            case PATROL:
                if (distanceToTarget <= DETECTION_RANGE && hasLOS) {
                    currentState = State.CHASE;
                    chasePath.clear();
                    stateCooldown = STATE_COOLDOWN_TIME;
                    hasRecentSight = true;
                    lastKnownTargetPosition = new Vector2(target.getPosition());
                    isInvestigatingLastKnown = false;
                    if (DEBUG)
                        Gdx.app.log(castorId, "Modo: PERSEGUIR (jogador visível)");
                }
                break;

            case CHASE:
                // Se perdeu a visão, marca que está investigando a última posição conhecida
                if (!hasLOS && lastKnownTargetPosition != null) {
                    isInvestigatingLastKnown = true;

                    if (DEBUG)
                        Gdx.app.log(castorId,
                                "Perdeu visão - investigando última posição conhecida: " + lastKnownTargetPosition);
                }

                // Se tem visão, atualiza a última posição conhecida
                if (hasLOS) {
                    lastKnownTargetPosition = new Vector2(target.getPosition());
                    isInvestigatingLastKnown = false; // Para de investigar pois tem visão novamente
                }

                // Se está no alcance de tiro e tem visão, atira
                if (distanceToTarget <= SHOOTING_RANGE && hasLOS) {
                    currentState = State.SHOOTING;
                    stateCooldown = STATE_COOLDOWN_TIME;
                    isInvestigatingLastKnown = false;
                    if (DEBUG)
                        Gdx.app.log(castorId, "Modo: ATIRANDO (jogador visível)");
                }
                break;

            case SHOOTING:
                // Volta a perseguir se sair do alcance de tiro
                if (distanceToTarget > SHOOTING_RANGE * 1.2f) {
                    currentState = State.CHASE;
                    stateCooldown = STATE_COOLDOWN_TIME;
                    if (DEBUG)
                        Gdx.app.log(castorId, "Modo: PERSEGUIR (saiu do alcance de tiro)");
                }
                break;
        }

        if (previousState != currentState && DEBUG) {
            Gdx.app.log(castorId, "Transição de estado: " + previousState + " -> " + currentState);
        }
    }

    private void updatePatrolWithForces(float deltaTime, Body body, Vector2 currentPosition) {
        patrolSystem.update(deltaTime, currentPosition);
    }

    public void debugMovementInfo() {
        if (DEBUG) {
            Vector2 velocity = castor.getBody().getLinearVelocity();
            Vector2 position = castor.getBody().getPosition();

            Gdx.app.log(castorId,
                    "Velocidade: " + String.format("%.2f", velocity.len()) +
                            ", Estado: " + currentState +
                            ", Posição: " + String.format("(%.2f, %.2f)", position.x, position.y) +
                            ", Massa: " + castor.getBody().getMass() +
                            ", Damping: " + castor.getBody().getLinearDamping());
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public void debugRender(ShapeRenderer shapeRenderer) {
        if (patrolSystem != null) {
            patrolSystem.debugRender(shapeRenderer);
        }

        // Adicionar renderização do sistema de perseguição

        Vector2 currentPos = castor.getBody().getPosition();
        Vector2 targetPos = target.getPosition();

        if (hasLineOfSight(currentPos, targetPos)) {
            shapeRenderer.setColor(Color.GREEN);
        } else {
            shapeRenderer.setColor(Color.RED);
        }

        shapeRenderer.line(currentPos.x, currentPos.y, targetPos.x, targetPos.y);
    }

    private boolean hasDetailedLineOfSight(Vector2 start, Vector2 end) {
        float distance = start.dst(end);
        int samples = (int) (distance * 2);

        for (int i = 1; i <= samples; i++) {
            float t = (float) i / samples;
            Vector2 point = start.cpy().lerp(end, t);
            Vector2 tilePoint = mapa.worldToTile(point);

            int tileX = (int) tilePoint.x;
            int tileY = (int) tilePoint.y;

            if (mapa.isTileBlocked(tileX, tileY)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasLineOfSight(Vector2 start, Vector2 end) {
        return hasDetailedLineOfSight(start, end);
    }

    public void debugRenderVision(ShapeRenderer shapeRenderer) {
        if (patrolSystem != null) {
            patrolSystem.debugRender(shapeRenderer);
        }

        Vector2 currentPos = castor.getBody().getPosition();
        Vector2 targetPos = target.getPosition();

        if (hasLineOfSight(currentPos, targetPos)) {
            shapeRenderer.setColor(Color.GREEN);
        } else {
            shapeRenderer.setColor(Color.RED);
        }

        shapeRenderer.line(currentPos.x, currentPos.y, targetPos.x, targetPos.y);
    }

    public void debugRenderChaseSystem(ShapeRenderer shapeRenderer, Vector2 cameraOffset, float tileSize) {
        if (chaseSystem != null) {
            chaseSystem.debugRender(shapeRenderer, cameraOffset, tileSize);
        }
    }

    public StateEnemy getStateEnemy() {
        return stateEnemy;
    }
}