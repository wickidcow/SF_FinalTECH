package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Generator base using the modern Slimefun Legacy data-container callback.
 * Existing int output calculations remain unchanged.
 */
public abstract class AbstractEnergyProviderMachine extends AbstractTickerDataMachine implements EnergyNetProvider {

    protected AbstractEnergyProviderMachine(
            @Nonnull ItemGroup itemGroup,
            @Nonnull SlimefunItemStack item,
            @Nonnull RecipeType recipeType,
            @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public final int getGeneratedOutput(@Nonnull Location location, @Nonnull ASlimefunDataContainer ignored) {
        return getGeneratedOutputCompat(location);
    }

    @Override
    public boolean willExplode(@Nonnull Location location, @Nonnull ASlimefunDataContainer ignored) {
        return false;
    }

    @Override
    protected boolean requiresLocationOwnedTick() {
        return true;
    }

    protected abstract int getGeneratedOutputCompat(@Nonnull Location location);
}
