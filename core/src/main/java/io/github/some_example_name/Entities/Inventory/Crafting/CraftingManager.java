package io.github.some_example_name.Entities.Inventory.Crafting;


import io.github.some_example_name.Entities.Inventory.Inventory;
import java.util.ArrayList;
import java.util.List;
import io.github.some_example_name.Entities.Itens.Ammo.Ammo9mm;
import io.github.some_example_name.Entities.Itens.CraftinItens.PolvoraBruta;
import io.github.some_example_name.Entities.Inventory.InventoryController;

public class CraftingManager {
    private final List<CraftingRecipe> recipes = new ArrayList<>();
    
    public CraftingManager() {
        initializeRecipes();
    }
    
    private void initializeRecipes() {
        CraftingRecipe gunpowderRecipe = new CraftingRecipe(new Ammo9mm(), 1)
            .addIngredient(PolvoraBruta.class, 2);
        
        recipes.add(gunpowderRecipe);
    }
    
    public List<CraftingRecipe> getAvailableRecipes(Inventory inventory) {
        List<CraftingRecipe> available = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            if (recipe.canCraft(inventory)) {
                available.add(recipe);
            }
        }
        return available;
    }
    
    public boolean craftRecipe(CraftingRecipe recipe, Inventory inventory, InventoryController inventoryController) {
        return recipe.craft(inventory, inventoryController);
    }
}