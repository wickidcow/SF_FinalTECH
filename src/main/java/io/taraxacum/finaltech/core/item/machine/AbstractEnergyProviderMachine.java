package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Compatibility base for FinalTECH generators that still support the RC-37
 * EnergyNetProvider ABI.
 *
 * <p>The deprecated Config parameter is isolated here. Generator implementations
 * expose the same int output through a config-free method, while current
 * Slimefun Legacy continues to reach this bridge through its compatibility
 * overloads. No per-call wrapper or reflection is used.</p>
 */
@SuppressWarnings("deprecation")
public abstract class AbstractEnergyProviderMachine extends AbstractTickerDataMachine implements EnergyNetProvider {

    protected AbstractEnergyProviderMachine(
            @Nonnull ItemGroup itemGroup,
            @Nonnull SlimefunItemStack item,
            @Nonnull RecipeType recipeType,
            @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public final int getGeneratedOutput(@Nonnull Location location, @Nonnull Config ignored) {
        return getGeneratedOutputCompat(location);
    }

    protected abstract int getGeneratedOutputCompat(@Nonnull Location location);
}
