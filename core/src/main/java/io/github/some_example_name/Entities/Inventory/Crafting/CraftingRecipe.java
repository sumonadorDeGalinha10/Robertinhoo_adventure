package io.github.some_example_name.Entities.Inventory.Crafting;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.Item;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CraftingRecipe {
    private final Map<Class<? extends Item>, Integer> ingredients;
    private final Item result;
    private final int resultQuantity;
      private TextureRegion resultIcon;
      private final Map<Class<? extends Item>, TextureRegion> ingredientIcons = new HashMap<>();
    
    

    public CraftingRecipe(Item result, int resultQuantity) {
        this.ingredients = new HashMap<>();
        this.result = result;
        this.resultQuantity = resultQuantity;
          this.resultIcon = result.getIcon();
        
    }

      public CraftingRecipe addIngredient(Class<? extends Item> itemClass, int quantity, TextureRegion icon) {
        ingredients.put(itemClass, quantity);
        ingredientIcons.put(itemClass, icon); 
        return this;
    }

    public boolean canCraft(Inventory inventory) {
        for (Map.Entry<Class<? extends Item>, Integer> entry : ingredients.entrySet()) {
            int count = inventory.getItemCount(entry.getKey());
            if (count < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public boolean craft(Inventory inventory, InventoryController inventoryController) {
        if (!canCraft(inventory)) return false;
        
        for (Map.Entry<Class<? extends Item>, Integer> entry : ingredients.entrySet()) {
            inventory.removeItems(entry.getKey(), entry.getValue());
        }
        
        for (int i = 0; i < resultQuantity; i++) {
            inventory.addItem(result.copy());
        }
        inventoryController.selectedItem= null;
        
        return true;
    }

    public Item getResult() {
        return result;
    }

    public Map<Class<? extends Item>, Integer> getIngredients() {
        return ingredients;
    }
    
    public int getResultQuantity() {
        return resultQuantity;
    }
       public TextureRegion getResultIcon() {
        return resultIcon;
    }
    
    public TextureRegion getIngredientIcon(Class<? extends Item> itemClass) {
        return ingredientIcons.get(itemClass);
    }
}