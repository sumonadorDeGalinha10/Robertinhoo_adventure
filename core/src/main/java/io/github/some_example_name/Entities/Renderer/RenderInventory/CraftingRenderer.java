package io.github.some_example_name.Entities.Renderer.RenderInventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.Entities.Inventory.Crafting.CraftingRecipe;
import io.github.some_example_name.Entities.Inventory.Item;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Fonts.FontsManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;
import java.util.Map;

public class CraftingRenderer {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Vector2 inventoryPosition;

    private final float recipeSpacing = 35f;
    private final float iconSize = 24f;
    private final float padding = 8f;
    private final float arrowWidth = 20f;

    public CraftingRenderer(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer, Vector2 inventoryPosition) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
        this.font = FontsManager.createInventoryFont();
        this.inventoryPosition = inventoryPosition;
    }

    public void render(List<CraftingRecipe> recipes, CraftingRecipe selected, int selectedGridX, int selectedGridY, Item selectedItem) {
        if (selectedItem == null) return;

        float menuX = inventoryPosition.x;
        float menuY = inventoryPosition.y + 150; 
        float menuWidth = 320f;
        float menuHeight = 200f;

        // Fase 1: Renderizar fundo e destaques
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.85f);
        shapeRenderer.rect(menuX, menuY, menuWidth, menuHeight);
        shapeRenderer.end();

        // Fase 2: Renderizar bordas
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.5f);
        shapeRenderer.rect(menuX, menuY, menuWidth, menuHeight);
        shapeRenderer.end();

        // Fase 3: Renderizar destaques das receitas selecionadas
        renderSelectionHighlights(recipes, selected, menuX + padding, menuY + padding, menuWidth - 2*padding);

        // Fase 4: Renderizar texto e ícones juntos no mesmo SpriteBatch
        spriteBatch.begin();
        renderRecipeList(recipes, selected, menuX + padding, menuY + padding, menuWidth - 2*padding);
        renderInstructions(menuX + padding, menuY );
        spriteBatch.end();
    }

    private void renderSelectionHighlights(List<CraftingRecipe> recipes, CraftingRecipe selected, 
                                          float startX, float startY, float width) {
        float yPos = startY + 140;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe recipe = recipes.get(i);
            if (recipe == selected) {
                shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.7f);
                shapeRenderer.rect(startX - 5, yPos - 25, width, recipeSpacing);
            }
            yPos -= recipeSpacing;
        }
        
        shapeRenderer.end();
    }

    private void renderRecipeList(List<CraftingRecipe> recipes, CraftingRecipe selected, 
                                 float startX, float startY, float width) {
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "RECEITAS DISPONÍVEIS:", startX, startY + 170);

        float yPos = startY + 140;
        
        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe recipe = recipes.get(i);
            boolean isSelected = (recipe == selected);
            renderRecipe(recipe, startX, yPos, isSelected);
            yPos -= recipeSpacing;
        }
    }

    private void renderRecipe(CraftingRecipe recipe, float x, float y, boolean isSelected) {
        float currentX = x;
        Color textColor = isSelected ? Color.YELLOW : Color.LIGHT_GRAY;
        font.setColor(textColor);

        // Ingredientes
        boolean firstIngredient = true;
        for (Map.Entry<Class<? extends Item>, Integer> entry : recipe.getIngredients().entrySet()) {
            if (!firstIngredient) {
                font.draw(spriteBatch, "+", currentX, y);
                currentX += 15;
            }
            firstIngredient = false;

            // Quantidade
            font.draw(spriteBatch, entry.getValue() + "x", currentX, y);
            currentX += 25;

            // Ícone do item
            TextureRegion icon = recipe.getIngredientIcon(entry.getKey());
            if (icon != null) {
                spriteBatch.draw(icon, currentX, y - iconSize/2 -5, iconSize, iconSize);
            }
            currentX += iconSize + 5;

            // Nome do item
            font.draw(spriteBatch, getItemName(entry.getKey()), currentX, y);
            currentX += getItemName(entry.getKey()).length() * 7 + 10;
        }

        // Seta (→)
        font.draw(spriteBatch, "-->", currentX, y);
        currentX += arrowWidth;

        // Resultado
        font.draw(spriteBatch, recipe.getResultQuantity() + "x", currentX, y);
        currentX += 25;
        
        // Ícone do item resultante
        TextureRegion resultIcon = recipe.getResultIcon();
        if (resultIcon != null) {
            spriteBatch.draw(resultIcon, currentX, y - iconSize/2 -5 , iconSize, iconSize);
        }
        currentX += iconSize + 5;
        
        // Nome do item resultante
        font.setColor(isSelected ? Color.GREEN : Color.WHITE);
        font.draw(spriteBatch, recipe.getResult().getName(), currentX, y);
    }

    private void renderInstructions(float x, float y) {
        font.setColor(Color.LIGHT_GRAY);
        font.draw(spriteBatch, "↑↓: Navegar", x, y);
        font.draw(spriteBatch, "ENTER: Craftar", x + 120, y);
        font.draw(spriteBatch, "ESC: Sair", x + 240, y);
    }

    private String getItemName(Class<? extends Item> itemClass) {
        if (itemClass == PolvoraBruta.class) {
            return "Pólvora Bruta";
        } else if (itemClass == Ammo9mm.class) {
            return "Munição 9mm";
        }
        return "Item";
    }
}