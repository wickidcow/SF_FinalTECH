package io.taraxacum.finaltech.core.item.machine.manual.craft;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.slimefun.compat.LegacyRecipeApiCompat;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

public class ManualCraftingTable extends AbstractManualCraftMachine {
    public ManualCraftingTable(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void registerDefaultRecipes() {
        Iterator<Recipe> recipeIterator = this.getAddon().getJavaPlugin().getServer().recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe next = recipeIterator.next();
            if (next instanceof ShapedRecipe shapedRecipe) {
                List<ItemStack> input = new ArrayList<>(shapedRecipe.getChoiceMap().size());
                for (var choice : shapedRecipe.getChoiceMap().values()) {
                    input.add(LegacyRecipeApiCompat.getRepresentativeIngredient(choice));
                }
                this.registerRecipeInCard(0, ItemStackUtil.getNoNullItemArray(input), new ItemStack[]{next.getResult()});
            } else if (next instanceof ShapelessRecipe shapelessRecipe) {
                List<ItemStack> input = new ArrayList<>(shapelessRecipe.getChoiceList().size());
                for (var choice : shapelessRecipe.getChoiceList()) {
                    input.add(LegacyRecipeApiCompat.getRepresentativeIngredient(choice));
                }
                this.registerRecipeInCard(0, ItemStackUtil.getNoNullItemArray(input), new ItemStack[]{next.getResult()});
            }
        }
    }
}
