package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

    private final int cellSize;
    private final Listener listener;

    private Item item;
    private boolean visible = false;

    // Layout
    private float x; // left edge world X
    private float y; // center Y where menu is vertically centered on this value
    private float menuWidth = 140f;
    private float padding = 8f;
    private float optionHeight = 28f;
    private List<String> options = Arrays.asList("Descartar", "Mover", "Craft");

    // computed
    private float menuHeight;
    private float menuBottomY;
    private InventoryMouseCursorRenderer mouseCursorRenderer;

    // hover
    private int hoverIndex = -1;

    public InventoryContextMenu(int cellSize, Listener listener,
    InventoryMouseCursorRenderer mouseCursorRenderer) {
        this.cellSize = cellSize;
        this.listener = listener;
        this.menuHeight = padding * 2f + options.size() * optionHeight;
        this.mouseCursorRenderer = mouseCursorRenderer;
    }

    /** 
     * Mostra o menu. x,y são coordenadas *absolutas* (world pixels). 
     * x normalmente será a borda direita do item (ex: itemWorldX + itemWidth)
     * y normalmente será o center Y do item.
     */
    public void show(Item item, float x, float y, int gridWidth) {
        this.item = item;
        this.x = x + 6f; // um pequeno gap à direita do item
        this.y = y;
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
    public void render(ShapeRenderer sr, SpriteBatch sb, BitmapFont font, float mouseWorldX, float mouseWorldY) {
        if (!visible || item == null) return;

        // recalcula bottom caso a y tenha mudado
        menuBottomY = y - (menuHeight / 2f);

        // Hover check
        hoverIndex = -1;
        if (mouseWorldX >= x && mouseWorldX <= x + menuWidth &&
            mouseWorldY >= menuBottomY && mouseWorldY <= menuBottomY + menuHeight) {

            float relativeY = mouseWorldY - menuBottomY; // 0..menuHeight
            // invert index because we draw from top to bottom
            int idxFromTop = (int) ((menuHeight - relativeY - padding) / optionHeight);
            if (idxFromTop >= 0 && idxFromTop < options.size()) {
                hoverIndex = idxFromTop;
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
    }

    /**
     * Deve ser chamado quando ocorrer um clique (em coordenadas world).
     * Retorna true se o clique foi consumido (dentro do menu), false se foi externo.
     */
    public boolean handleClick(float worldX, float worldY) {
        if (!visible) return false;

        if (worldX >= x && worldX <= x + menuWidth &&
            worldY >= menuBottomY && worldY <= menuBottomY + menuHeight) {

            // clicou dentro -> descobrir opção
            float relativeY = worldY - menuBottomY;
            int idxFromTop = (int) ((menuHeight - relativeY - padding) / optionHeight);
            if (idxFromTop >= 0 && idxFromTop < options.size()) {
                String opt = options.get(idxFromTop);
                // chama listener
                if (listener != null && item != null) {
                    switch (opt) {
                        case "Descartar":
                            listener.onDrop(item);
                            break;
                        case "Mover":
                            listener.onMove(item);
                            break;
                        case "Craft":
                            listener.onCraft(item);
                            break;
                    }
                }
            }
            hide();
            return true; // consumiu clique
        } else {
            // clique fora -> fecha e não consome
            hide();
            return false;
        }
    }

      public void scroll(int amount) {
        if (!visible) return;
        
        // Navega pelas opções (amount: positivo = scroll up, negativo = scroll down)
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
}
