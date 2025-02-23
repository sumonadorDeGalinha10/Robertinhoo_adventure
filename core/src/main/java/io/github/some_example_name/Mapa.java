package io.github.some_example_name;




import io.github.some_example_name.Otimizations.WallOtimizations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Enemies.Enemy;
import io.github.some_example_name.Entities.Enemies.Ratinho;
import io.github.some_example_name.Entities.Itens.Contact.GameContactListener;
import io.github.some_example_name.Entities.Itens.Contact.ProjectileContactListenter;
import io.github.some_example_name.Entities.Itens.Weapon.Pistol;
import io.github.some_example_name.Entities.Itens.Weapon.Projectile;
import io.github.some_example_name.Entities.Itens.Weapon.Weapon;
import io.github.some_example_name.Entities.Player.Robertinhoo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class Mapa {

 private List<Enemy> enemies;
 private List<Weapon> weapons;
 private List<Projectile> projectiles = new ArrayList<>();
   private boolean toBeDestroyed = false;

    public World world;
    public WallOtimizations agruparParedes;

  // Valores corrigidos para RGBA8888
  static int TILE = 0x000000; // #000000 (tiles normais)
    static int START = 0xFF0000; // #FF0000 (ponto de início)
    public static int PAREDE = 0x00FFF4; // #00FFF4 (paredes)
    public static int ENEMY = 0X913d77; // #913d77 (inimigos)
    public static  int REVOLVER = 0X22ff00; //#22ff00

    ArrayList<Vector2> wallPositions = new ArrayList<>(); // Lista de coordenadas das paredes

    public int mapWidth;
    public int mapHeight;

    public int[][] tiles;
    public Vector2 startPosition;

    public Robertinhoo robertinhoo;
    public Ratinho ratinho;

    public Mapa() {
        enemies = new ArrayList<>();
        world = new World(new Vector2(0, 0), true);
        weapons = new ArrayList<>();
        agruparParedes = new WallOtimizations(this);


        loadImageMap("assets/maps/TesteMap.png");
        world.setContactListener(new GameContactListener(robertinhoo));
      
    }


    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }
    private void loadImageMap(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            mapWidth = image.getWidth();
            mapHeight = image.getHeight();
            tiles = new int[mapWidth][mapHeight];
            Vector2 tempVector = new Vector2();
    
            // --- PRIMEIRA ETAPA: PROCURA O TILE START ---
            boolean spawnEncontrado = false;
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int color = image.getRGB(x, y) & 0xFFFFFF;
                    System.out.println(color);
                    if (color == START) {
                        if (spawnEncontrado) {
                            Gdx.app.error("Mapa", "Mapa tem múltiplos pontos de início (START)!");
                        }
                        startPosition = new Vector2(x, y);
                        robertinhoo = new Robertinhoo(this, x, y,null);
                        tiles[x][y] = TILE;
                        spawnEncontrado = true;
                    }
                }
            }
    
            if (!spawnEncontrado) {
                throw new RuntimeException("Mapa não tem ponto de início (START)!");
            }
    
            // --- SEGUNDA ETAPA: PAREDES E INIMIGOS ---
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    int color = image.getRGB(x, y) & 0xFFFFFF;
    
                    if (color == PAREDE) {
                        tiles[x][y] = PAREDE;
                        tempVector.set(x, y);
                        wallPositions.add(tempVector.cpy());
                    } else if (color == ENEMY) {
                        Ratinho ratinho = new Ratinho(this, x, y, robertinhoo);
                        enemies.add(ratinho);
                        tiles[x][y] = TILE;
                    }
                    else if(color == REVOLVER){
                        Pistol pistol = new Pistol(this,x,y);
                        weapons.add(pistol);
                        
                        tiles[x][y] = PAREDE;

                    }
                }
            }
    
            agruparEPCriarParedes();
        } catch (IOException e) {
            Gdx.app.error("Mapa", "Erro ao carregar imagem: " + e.getMessage());
        }
    }

    private void agruparEPCriarParedes() {
        List<Rectangle> retangulos = agruparParedes.optimizeWalls(wallPositions);
        for (Rectangle ret : retangulos) {
            createWallBody(ret);
        }
    }

    private void createWallBody(Rectangle ret) {
        float escala = 1.0f; // Alterado para 1.0f
    
        BodyDef bodyDef = new BodyDef();
        
        bodyDef.type = BodyType.StaticBody;
        // Se necessário, inverta o eixo Y (exemplo para mapas com Y começando no topo):
        float posY = (mapHeight - ret.y - ret.height/2) * escala;
        bodyDef.position.set(
            (ret.x + ret.width/2) * escala, 
            posY // Aplicar inversão se necessário
        );
    
        Body body = world.createBody(bodyDef);
        body.setUserData("WALL");
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            (ret.width/2) * escala, 
            (ret.height/2) * escala
        );
    
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        shape.dispose();
    }


    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }    



    public void update(float deltaTime) {
        robertinhoo.update(deltaTime);
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
        }
        for (Weapon weapon : weapons) {
            weapon.update(deltaTime);
        }
    
    
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : projectiles) {
            if (p.isMarkedForDestruction()) {
                toRemove.add(p);
            }
        }
        for (Projectile p : toRemove) {
            p.destroy();
            projectiles.remove(p);
        }
    }
    

    boolean match(int src, int dst) {
        return src == dst;
    }
}
