package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Base class for machines that consume the block-data object supplied by the
 * historical RC-37 ticker contract.
 *
 * <p>The same Config instance supplied by Slimefun is forwarded as Object to
 * avoid allocating wrappers in machine hot paths. Access is centralized through
 * LegacyTickerDataCompat, keeping the deprecated type out of machine code while
 * preserving exact RC-37 behavior.</p>
 */
@SuppressWarnings("deprecation")
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
            @Nonnull Config data) {
        tickWithData(block, slimefunItem, data);
    }

    protected abstract void tickWithData(
            @Nonnull Block block,
            @Nonnull SlimefunItem slimefunItem,
            @Nonnull Object data);
}
