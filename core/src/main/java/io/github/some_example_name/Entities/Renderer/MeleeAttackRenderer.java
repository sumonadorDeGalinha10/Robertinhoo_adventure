// package io.github.some_example_name.Entities.Renderer;



// import com.badlogic.gdx.graphics.Color;
// import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
// import com.badlogic.gdx.math.Vector2;
// import com.badlogic.gdx.physics.box2d.Body;
// import com.badlogic.gdx.physics.box2d.Fixture;
// import com.badlogic.gdx.physics.box2d.PolygonShape;
// import io.github.some_example_name.Entities.Player.MeleeAttackSystem;
// import io.github.some_example_name.Entities.Player.Robertinhoo;

// public class MeleeAttackRenderer {
//     private final ShapeRenderer shapeRenderer;
//     private final Robertinhoo player;

//     public MeleeAttackRenderer(Robertinhoo player) {
//         this.player = player;
//         this.shapeRenderer = new ShapeRenderer();
//     }

//     public void render(float delta) {
//         MeleeAttackSystem attackSystem = player.getMeleeAttackSystem();
        
//         if (attackSystem.isAttacking() && attackSystem.getMeleeHitboxBody() != null) {
//             Body hitboxBody = attackSystem.getMeleeHitboxBody();
//             Vector2 position = hitboxBody.getPosition();
            
//             shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//             shapeRenderer.setColor(Color.RED);
            
//             // Renderiza todas as fixtures do corpo
//             for (Fixture fixture : hitboxBody.getFixtureList()) {
//                 if (fixture.getShape() instanceof PolygonShape) {
//                     PolygonShape polygon = (PolygonShape) fixture.getShape();
//                     Vector2[] vertices = new Vector2[polygon.getVertexCount()];
                    
//                     // Obtém os vértices transformados para posição mundial
//                     for (int i = 0; i < vertices.length; i++) {
//                         vertices[i] = new Vector2();
//                         polygon.getVertex(i, vertices[i]);
//                         hitboxBody.getWorldPoint(vertices[i]);
//                     }
                    
//                     // Desenha o polígono
//                     for (int i = 0; i < vertices.length; i++) {
//                         Vector2 v1 = vertices[i];
//                         Vector2 v2 = vertices[(i + 1) % vertices.length];
//                         shapeRenderer.line(v1, v2);
//                     }
//                 }
//             }
            
//             shapeRenderer.end();
//         }
//     }

//     public void dispose() {
//         shapeRenderer.dispose();
//     }
// } 