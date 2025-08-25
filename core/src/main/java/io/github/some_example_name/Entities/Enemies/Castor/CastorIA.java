package io.github.some_example_name.Entities.Enemies.Castor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.some_example_name.Entities.Enemies.IA.PathfindingSystem;
import io.github.some_example_name.Entities.Player.Robertinhoo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;

public class CastorIA {
    private final Robertinhoo target;
    private final PathfindingSystem pathfindingSystem;
    private List<Vector2> currentPath;
    private int currentPathIndex;
    private float repathTimer = 0;
    private static final float REPATH_INTERVAL = 0.5f;

    // Estados da IA
    private enum State {
        PATROL, CHASE, SHOOTING
    }

    private State currentState = State.PATROL;

    // Parâmetros de detecção
    private static final float DETECTION_RANGE = 10f;
    private static final float CHASE_RANGE = 15f;
    private static final float LOSE_SIGHT_RANGE = 20f;

    // Waypoints para patrulha
    private List<Vector2> patrolWaypoints = new ArrayList<>();
    private int currentWaypointIndex = 0;
    private float waypointWaitTimer = 0;
    private static final float WAYPOINT_WAIT_TIME = 2f;
    private static final float SHOOTING_RANGE = 12f;
    private static final float IDEAL_SHOOTING_DISTANCE = 8f;

    // Referência ao castor
    private Castor castor;

    // Aleatório para waypoints
    private Random random = new Random();

    public CastorIA(Castor castor, Robertinhoo target, PathfindingSystem pathfindingSystem) {
        this.castor = castor;
        this.target = target;
        this.pathfindingSystem = pathfindingSystem;

        // Inicializa waypoints de patrulha
        initializePatrolWaypoints();
    }

     public void update(float deltaTime, Body body) {
        Vector2 currentPosition = body.getPosition();
        Vector2 targetPosition = target.getPosition();
        float distanceToTarget = currentPosition.dst(targetPosition);


        if (target == null || target.getPosition() == null) {
            Gdx.app.log("CastorIA-ERROR", "Target ou posição do target é nula!");
            return;
        }

        Gdx.app.log("CastorIA", "Estado atual: " + currentState + 
                   ", Distância: " + distanceToTarget +
                   ", Posição: " + currentPosition);

        // Verifica transições de estado
        checkStateTransitions(distanceToTarget);

        // Executa comportamento baseado no estado atual
        switch (currentState) {
            case PATROL:
                Gdx.app.log("CastorIA", "Executando PATRULHA");
                updatePatrol(deltaTime, body, currentPosition);
                break;
            case CHASE:
                Gdx.app.log("CastorIA", "Executando PERSEGUIR");
                updateChase(deltaTime, body, currentPosition, targetPosition);
                break;
            case SHOOTING:
                Gdx.app.log("CastorIA", "Executando ATIRAR");
                updateShooting(deltaTime, body, currentPosition, targetPosition);
                break;
        }
    }


private void updateShooting(float deltaTime, Body body, Vector2 currentPosition, Vector2 targetPosition) {
    float distanceToTarget = currentPosition.dst(targetPosition);
    
    // Tenta manter a distância ideal para atirar
    if (distanceToTarget < IDEAL_SHOOTING_DISTANCE - 2f) {
        // Está muito perto, recua
        Vector2 retreatDirection = currentPosition.cpy().sub(targetPosition).nor();
        Vector2 desiredVelocity = retreatDirection.scl(3f);
        body.setLinearVelocity(desiredVelocity);
        Gdx.app.log("CastorIA-SHOOTING", "Recuando - Distância: " + distanceToTarget);
    } else if (distanceToTarget > IDEAL_SHOOTING_DISTANCE + 2f) {
        // Está muito longe, avança
        Vector2 approachDirection = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = approachDirection.scl(3f);
        body.setLinearVelocity(desiredVelocity);
        Gdx.app.log("CastorIA-SHOOTING", "Avançando - Distância: " + distanceToTarget);
    } else {
        // Está na distância ideal, para e atira
        body.setLinearVelocity(0, 0);
        Gdx.app.log("CastorIA-SHOOTING", "Distância ideal - Atirando: " + distanceToTarget);
        castor.shootAtPlayer();
    }
    
    // Mesmo se estiver se movendo, tenta atirar se estiver no alcance
    if (distanceToTarget <= SHOOTING_RANGE && castor.canShoot()) {
        castor.shootAtPlayer();
    }
}

private void checkStateTransitions(float distanceToTarget) {
    switch (currentState) {
        case PATROL:
            if (distanceToTarget <= DETECTION_RANGE) {
                currentState = State.CHASE;
                Gdx.app.log("CastorIA", "Modo: PERSEGUIR");
            }
            break;

        case CHASE:
            if (distanceToTarget > LOSE_SIGHT_RANGE) {
                currentState = State.PATROL;
                currentPath = null;
                Gdx.app.log("CastorIA", "Modo: PATRULHA");
                initializePatrolWaypoints();
            } else if (distanceToTarget <= SHOOTING_RANGE) {
                currentState = State.SHOOTING;
                Gdx.app.log("CastorIA", "Modo: ATIRANDO");
            }
            break;

        case SHOOTING:
            // Adicione uma margem de hysteresis para evitar oscilação
            if (distanceToTarget > SHOOTING_RANGE * 1.1f) { // 10% a mais
                currentState = State.CHASE;
                Gdx.app.log("CastorIA", "Modo: PERSEGUIR (saiu do alcance)");
            } else if (distanceToTarget > LOSE_SIGHT_RANGE) {
                currentState = State.PATROL;
                currentPath = null;
                Gdx.app.log("CastorIA", "Modo: PATRULHA (perdeu visão)");
                initializePatrolWaypoints();
            }
            break;
    }
}

    private void updatePatrol(float deltaTime, Body body, Vector2 currentPosition) {
            Gdx.app.log("CastorIA-PATROL", "Waypoints: " + patrolWaypoints.size() +
                   ", Waypoint atual: " + currentWaypointIndex +
                   ", Wait timer: " + waypointWaitTimer);
        // Se não tem waypoints, inicializa
        if (patrolWaypoints.isEmpty()) {
            initializePatrolWaypoints();
            return;
        }

        // Espera no waypoint atual por um tempo
        if (waypointWaitTimer > 0) {
            waypointWaitTimer -= deltaTime;
            body.setLinearVelocity(0, 0); // Fica parado
            return;
        }

        // Se não tem caminho ou chegou ao waypoint, calcula novo caminho
        if (currentPath == null || currentPath.isEmpty() || currentPathIndex >= currentPath.size()) {
            Vector2 targetWaypoint = patrolWaypoints.get(currentWaypointIndex);
            currentPath = pathfindingSystem.findPath(currentPosition, targetWaypoint);
            currentPathIndex = 0;

            // Se não encontrou caminho, escolhe outro waypoint
            if (currentPath == null || currentPath.isEmpty()) {
                selectNextWaypoint();
                return;
            }
        }

        // Segue o caminho
        followPath(body, currentPosition, 2f); // Velocidade mais lenta na patrulha

        // Verifica se chegou ao waypoint
        Vector2 currentWaypoint = patrolWaypoints.get(currentWaypointIndex);
        if (currentPosition.dst(currentWaypoint) < 1f) {
            waypointWaitTimer = WAYPOINT_WAIT_TIME;
            selectNextWaypoint();
        }
    }

private void updateChase(float deltaTime, Body body, Vector2 currentPosition, Vector2 targetPosition) {
    repathTimer += deltaTime;

    // Recalcula o caminho periodicamente
    if (repathTimer >= REPATH_INTERVAL) {
        repathTimer = 0;
        calculateNewPath(currentPosition, targetPosition);
    }

    // Verifica se há um caminho válido
    if (currentPath == null || currentPath.isEmpty()) {
        Gdx.app.log("CastorIA-CHASE", "Sem caminho válido, tentando movimento direto");
        // Movimento direto como fallback
        Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
        Vector2 desiredVelocity = direction.scl(4f);
        body.setLinearVelocity(desiredVelocity);
    } else {
        // Segue o caminho com velocidade maior
        followPath(body, currentPosition, 4f);
    }
}
    private void calculateNewPath(Vector2 start, Vector2 end) {
        currentPath = pathfindingSystem.findPath(start, end);
        currentPathIndex = 0;
    }

private void followPath(Body body, Vector2 currentPosition, float speed) {
    if (currentPath == null || currentPath.isEmpty() || currentPathIndex >= currentPath.size()) {
        Gdx.app.log("CastorIA-FOLLOW", "Sem caminho válido para seguir");
        return;
    }

    Vector2 targetPosition = currentPath.get(currentPathIndex);
    float distance = currentPosition.dst(targetPosition);
    

   

    if (distance < 0.5f) {
        currentPathIndex++;
        Gdx.app.log("CastorIA-FOLLOW", "Ponto alcançado, próximo índice: " + currentPathIndex);

        if (currentPathIndex >= currentPath.size()) {
            Gdx.app.log("CastorIA-FOLLOW", "Fim do caminho");
            return;
        }

        targetPosition = currentPath.get(currentPathIndex);
    }

    Vector2 direction = targetPosition.cpy().sub(currentPosition).nor();
    Vector2 desiredVelocity = direction.scl(speed);
    Vector2 currentVelocity = body.getLinearVelocity();
    Vector2 steering = desiredVelocity.sub(currentVelocity);


    body.applyForceToCenter(steering.scl(body.getMass()), true);
    
    Gdx.app.log("CastorIA-FOLLOW", "Velocidade após força: " + body.getLinearVelocity());
}

    private void initializePatrolWaypoints() {
        patrolWaypoints.clear();

        // Gera waypoints aleatórios em uma área ao redor da posição inicial do castor
        Vector2 initialPosition = castor.getBody().getPosition();

        for (int i = 0; i < 4; i++) {
            float angle = (float) (2 * Math.PI * i / 4);
            float distance = 5f + random.nextFloat() * 5f; // Entre 5 e 10 unidades
            float x = initialPosition.x + (float) Math.cos(angle) * distance;
            float y = initialPosition.y + (float) Math.sin(angle) * distance;

            patrolWaypoints.add(new Vector2(x, y));
        }

        currentWaypointIndex = 0;
        waypointWaitTimer = 0;
    }

    private void selectNextWaypoint() {
        currentWaypointIndex = (currentWaypointIndex + 1) % patrolWaypoints.size();
        currentPath = null; // Força recálculo do caminho
    }

    public List<Vector2> getCurrentPath() {
        return currentPath;
    }

    public State getCurrentState() {
        return currentState;
    }
}