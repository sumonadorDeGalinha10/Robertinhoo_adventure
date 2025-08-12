package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.Entities.Inventory.Item;
import com.badlogic.gdx.Input.Keys;
import io.github.some_example_name.Entities.Renderer.RenderInventory.InventoryMouseCursorRenderer;

import java.util.Arrays;
import java.util.List;

public class InventoryContextMenu {

    public interface Listener {
        void onDrop(Item item);
        void onMove(Item item);
        void onCraft(Item item);
    }
private boolean debugMode = false; //
    private final int cellSize;
    private final Listener listener;

    private Item item;
    private boolean visible = false;
     private ShapeRenderer debugRenderer;


    // Layout
    private float x;
    private float y; 
    private float menuWidth = 130f;
    private float padding =8f;
    private float optionHeight = 28f;
    private List<String> options = Arrays.asList("Descartar", "Mover", "Craft");
     private float optionMargin = 20f; 

    // computed
    private float menuHeight;
    private float menuBottomY;
    public InventoryMouseCursorRenderer mouseCursorRenderer;

    // hover
    private int hoverIndex = -1;

    public InventoryContextMenu(int cellSize, Listener listener,
    InventoryMouseCursorRenderer mouseCursorRenderer) {
        this.cellSize = cellSize;
        this.listener = listener;
        this.menuHeight = padding * 2f + options.size() * optionHeight;
        this.mouseCursorRenderer = mouseCursorRenderer;
          this.debugRenderer = new ShapeRenderer();
    }

    /** 
     * Mostra o menu. x,y são coordenadas *absolutas* (world pixels). 
     * x normalmente será a borda direita do item (ex: itemWorldX + itemWidth)
     * y normalmente será o center Y do item.
     */
    public void show(Item item, float x, float y, int gridWidth) {
        this.item = item;
        this.x = x + 5f; // um pequeno gap à direita do item
        this.y = y + 40f;
        this.menuHeight = padding * 2f + options.size() * optionHeight;
        this.menuBottomY = this.y - (this.menuHeight / 2f);
        this.visible = true;
        this.hoverIndex = -1;
    }

    public void hide() {
        this.visible = false;
        this.item = null;
        this.hoverIndex = -1;
    }

    public boolean isVisible() {
        return visible;
    }

    /** 
     * Renderiza o menu. Deve ser chamado com as mesmas projeções/matrices que você usa para desenhar o inventário (camera combined).
     * mouseWorldX/Y são as coordenadas do mouse no mesmo espaço (world) — ex.: camera.unproject(new Vector3(screenX, screenY,0))
     */

       private float hoverOffsetY = -25f; 
    public void render(ShapeRenderer sr, SpriteBatch sb, BitmapFont font, float mouseWorldX, float mouseWorldY) {
        if (!visible || item == null) return;

        // recalcula bottom caso a y tenha mudado
        menuBottomY = y - (menuHeight / 2f);

        // Hover check
        hoverIndex = -1;
        if (mouseWorldX >= x && mouseWorldX <= x + menuWidth) {
            // Aplicar offset ao mouseY para ajuste de alinhamento
            float adjustedMouseY = mouseWorldY + hoverOffsetY;
            
            if (adjustedMouseY >= menuBottomY && adjustedMouseY <= menuBottomY + menuHeight) {
                // Verificar cada item individualmente com offset aplicado
                for (int i = 0; i < options.size(); i++) {
                    float optionTopY = menuBottomY + menuHeight - padding - i * optionHeight;
                    float optionBottomY = optionTopY - optionHeight;
                    
                    if (adjustedMouseY >= optionBottomY && adjustedMouseY <= optionTopY) {
                        hoverIndex = i;
                        break;
                    }
                }
            }
        }

        // Draw background

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.85f);
        sr.rect(x, menuBottomY, menuWidth, menuHeight);
        sr.end();

        // Border
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1f, 1f, 1f, 0.15f);
        sr.rect(x, menuBottomY, menuWidth, menuHeight);
        sr.end();

        // Highlight hovered option
        if (hoverIndex != -1) {
            float optionTopY = menuBottomY + menuHeight - padding - (hoverIndex) * optionHeight;
            float highlightY = optionTopY - optionHeight;
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(new Color(1f, 1f, 1f, 0.06f));
            sr.rect(x, highlightY, menuWidth, optionHeight);
            sr.end();
        }

        // Draw text
        sb.begin();
        font.getData().setScale(1f);
        float textX = x + padding;
        for (int i = 0; i < options.size(); i++) {
            // calc baseline Y for this option
            float optionTopY = menuBottomY + menuHeight - padding - i * optionHeight;
            // baseline = optionTopY - (optionHeight - capHeight)/2
            float baseline = optionTopY - (optionHeight - font.getCapHeight()) / 2f - 4f; // -4 to adjust visually
            // color
            if (i == hoverIndex) {
                font.setColor(Color.YELLOW);
            } else {
                font.setColor(Color.WHITE);
            }
            font.draw(sb, options.get(i), textX, baseline);
        }
        sb.end();
    if (debugMode) {
            renderDebugAreas(mouseWorldX, mouseWorldY, sb, font);
        }
    
        
    }

    /**
     * Deve ser chamado quando ocorrer um clique (em coordenadas world).
     * Retorna true se o clique foi consumido (dentro do menu), false se foi externo.
     */
public boolean handleClick(float worldX, float worldY) {
    if (!visible) return false;

    System.out.printf("Click at: X=%.1f, Y=%.1f%n", worldX, worldY);
    
    // Área total do menu
    if (worldX < x || worldX > x + menuWidth || 
        worldY < menuBottomY || worldY > menuBottomY + menuHeight) {
        hide();
        return false;
    }
    
    // Calcula a posição relativa dentro do menu
    float relativeY = worldY - menuBottomY;
    
    // Inverte Y (porque o menu é desenhado de cima para baixo)
    float invertedY = menuHeight - relativeY;
    
    // Calcula o índice diretamente
    int index = (int) ((invertedY - padding) / optionHeight);
    index = MathUtils.clamp(index, 0, options.size() - 1);
    
    System.out.println("Selected index: " + index);
    
    // Executa a ação
    String opt = options.get(index);
    if (listener != null && item != null) {
        System.out.println("Triggering: " + opt);
        switch (opt) {
            case "Descartar": listener.onDrop(item); break;
            case "Mover": listener.onMove(item); break;
            case "Craft": listener.onCraft(item); break;
        }
    }
    
    hide();
    return true;
}
      public void scroll(int amount) {
        if (!visible) return;

        if (amount > 0) {
            hoverIndex = Math.max(0, hoverIndex - 1);
        } else {
            hoverIndex = Math.min(options.size() - 1, hoverIndex + 1);
        }
    }

    public boolean handleKeyPress(int keycode) {
        if (!visible) return false;

        switch (keycode) {
            case Keys.UP:
                hoverIndex = Math.max(0, hoverIndex - 1);
                return true;
            case Keys.DOWN:
                hoverIndex = Math.min(options.size() - 1, hoverIndex + 1);
                return true;
            case Keys.ENTER:
                if (hoverIndex >= 0) {
                    triggerOption(hoverIndex);
                    hide();
                }
                return true;
            case Keys.ESCAPE:
                hide();
                return true;
        }
        return false;
    }

    private void triggerOption(int index) {
        String opt = options.get(index);
        if (listener != null && item != null) {
            switch (opt) {
                case "Descartar": listener.onDrop(item); break;
                case "Mover": listener.onMove(item); break;
                case "Craft": listener.onCraft(item); break;
            }
        }
    }

    /** Se precisar só checar se um ponto está dentro sem consumir. */
    public boolean isPointInside(float worldX, float worldY) {
        if (!visible) return false;
        return worldX >= x && worldX <= x + menuWidth &&
               worldY >= menuBottomY && worldY <= menuBottomY + menuHeight;
    }
    public boolean handleLeftClick(float screenX, float screenY) {
    if (visible) {
        Vector2 worldPos = mouseCursorRenderer.screenToWorld((int) screenX, (int) screenY);
        return handleClick(worldPos.x, worldPos.y);
    }
    return false;
}



    private void renderDebugAreas(float mouseWorldX, float mouseWorldY,SpriteBatch sb, BitmapFont font) {
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        for (int i = 0; i < options.size(); i++) {
            float optionTopY = menuBottomY + menuHeight - padding - i * optionHeight;
            float optionBottomY = optionTopY - optionHeight;
            
            // Verifica se o mouse está sobre esta opção
            boolean isHovered = (i == hoverIndex);
            
            // Define a cor baseada no hover
            debugRenderer.setColor(isHovered ? Color.RED : Color.GREEN);
            
            // Desenha a área da opção
            debugRenderer.rect(x, optionBottomY, menuWidth, optionHeight);
            
            // Desenha texto de debug
            debugRenderer.end();
            sb.begin();
            font.setColor(Color.WHITE);
            font.draw(sb, "Option " + i + ": " + options.get(i), x, optionBottomY - 110);
            font.draw(sb, "Top: " + optionTopY, x, optionBottomY - 240);
            font.draw(sb, "Bottom: " + optionBottomY, x, optionBottomY - 360);
            font.draw(sb, "MouseY: " + mouseWorldY, x, optionBottomY - 480);
            sb.end();
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        }
        
        debugRenderer.end();
    }


}