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

import java.util.List;
import java.util.Map;

public class CraftingRenderer {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Vector2 inventoryPosition;

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
        float menuWidth = 200;
        float menuHeight = 150;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(menuX, menuY, menuWidth, menuHeight);
        shapeRenderer.end();

        spriteBatch.begin();
        renderTextContent(recipes, selected, menuX, menuY);
        spriteBatch.end();
    }

    private void renderTextContent(List<CraftingRecipe> recipes, CraftingRecipe selected, float menuX, float menuY) {
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "Receitas de Craft:", menuX + 10, menuY + 130);

        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe recipe = recipes.get(i);
            String recipeText = getRecipeDescription(recipe);

            if (recipe == selected) {
                font.setColor(Color.YELLOW);
                font.draw(spriteBatch, "> " + recipeText, menuX + 20, menuY + 100 - i * 30);
            } else {
                font.setColor(Color.WHITE);
                font.draw(spriteBatch, recipeText, menuX + 30, menuY + 100 - i * 30);
            }
        }

        font.setColor(Color.LIGHT_GRAY);
        font.draw(spriteBatch, "Setas: Navegar  |  Enter: Craftar  |  ESC: Sair",
                menuX + 10, menuY + 10);
    }

    private String getRecipeDescription(CraftingRecipe recipe) {
        StringBuilder sb = new StringBuilder();

        sb.append(recipe.getResultQuantity())
                .append("x ")
                .append(recipe.getResult().getName())
                .append(" (");

        boolean first = true;
        for (Map.Entry<Class<? extends Item>, Integer> entry : recipe.getIngredients().entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            String itemName = getItemName(entry.getKey());
            sb.append(entry.getValue())
                    .append("x ")
                    .append(itemName);
        }

        sb.append(")");
        return sb.toString();
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