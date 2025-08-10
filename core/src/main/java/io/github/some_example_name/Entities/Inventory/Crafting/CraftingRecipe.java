package io.github.some_example_name.Entities.Inventory.Crafting;

import io.github.some_example_name.Entities.Inventory.Inventory;
import io.github.some_example_name.Entities.Inventory.InventoryController;
import io.github.some_example_name.Entities.Inventory.Item;
import java.util.HashMap;
import java.util.Map;

public class CraftingRecipe {
    private final Map<Class<? extends Item>, Integer> ingredients;
    private final Item result;
    private final int resultQuantity;
    

    public CraftingRecipe(Item result, int resultQuantity) {
        this.ingredients = new HashMap<>();
        this.result = result;
        this.resultQuantity = resultQuantity;
    }

    public CraftingRecipe addIngredient(Class<? extends Item> itemClass, int quantity) {
        ingredients.put(itemClass, quantity);
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
}