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
 * Base class for machines whose ticker logic does not consume persisted ticker data.
 *
 * <p>The deprecated RC-37 Config signature is isolated here so data-free machine
 * implementations can remain independent of the legacy storage ABI. Current
 * Slimefun Legacy still reaches this bridge through BlockTicker's compatibility
 * dispatch, preserving identical tick timing and behavior.</p>
 */
@SuppressWarnings("deprecation")
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
            @Nonnull Config ignored) {
        tick(block, slimefunItem);
    }

    protected abstract void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem);
}
