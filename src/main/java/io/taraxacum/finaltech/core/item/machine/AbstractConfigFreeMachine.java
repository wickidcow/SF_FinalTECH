package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Base class for machines whose ticker logic does not consume persisted data.
 * The modern block-data callback forwards to the existing data-free tick.
 */
public abstract class AbstractConfigFreeMachine extends AbstractMachine {

    protected AbstractConfigFreeMachine(
            @Nonnull ItemGroup itemGroup,
            @Nonnull SlimefunItemStack item,
            @Nonnull RecipeType recipeType,
            @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    protected final void tick(
            @Nonnull Block block,
            @Nonnull SlimefunItem slimefunItem,
            @Nonnull SlimefunBlockData ignored) {
        tick(block, slimefunItem);
    }

    @Override
    protected boolean requiresLocationOwnedTick() {
        return true;
    }

    protected abstract void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem);
}
