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
 * Base class for machines that consume the canonical block-data record.
 * The same record supplied by Slimefun is forwarded without a wrapper.
 */
public abstract class AbstractTickerDataMachine extends AbstractMachine {

    protected AbstractTickerDataMachine(
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
            @Nonnull SlimefunBlockData data) {
        tickWithData(block, slimefunItem, data);
    }

    protected abstract void tickWithData(
            @Nonnull Block block,
            @Nonnull SlimefunItem slimefunItem,
            @Nonnull SlimefunBlockData data);
}
