package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.MenuUpdater;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.unit.StatusMenu;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.MachineUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * @author Final_ROOT
 * @since 2.2
 */
public class TimeGenerator extends AbstractEnergyProviderMachine implements RecipeItem, MenuUpdater {
    private final String key = "time";
    private final int interval = ConfigUtil.getOrDefaultItemSetting(1600, this, "interval");
    private final int capacity = ConfigUtil.getOrDefaultItemSetting(16777216, this, "capacity");

    public TimeGenerator(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return MachineUtil.BLOCK_PLACE_HANDLER_PLACER_DENY;
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return MachineUtil.simpleBlockBreakerHandler(this);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new StatusMenu(this);
    }

    @Override
    protected void tickWithData(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Object data) {
        Location location = block.getLocation();
        World world = location.getWorld();
        int charge = LegacySlimefunApiCompat.getCharge(this, location);

        if (world != null) {
            long time = world.getTime() / this.interval;
            String oldTime = LegacyTickerDataCompat.getString(data, this.key);
            if (oldTime != null && !oldTime.equals(String.valueOf(time))) {
                charge *= 2;
            }
            LegacyTickerDataCompat.setValue(data, this.key, String.valueOf(time));
        }

        charge += 1;

        charge = charge > this.capacity ? 0 : charge;
        LegacySlimefunApiCompat.setCharge(this, location, charge);

        BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(location);
        if (blockMenu.hasViewer()) {
            this.updateMenu(blockMenu, 4, this,
                    String.valueOf(charge));
        }
    }

    @Override
    protected boolean isSynchronized() {
        return false;
    }

    @Override
    protected int getGeneratedOutputCompat(@Nonnull Location location) {
        int charge = LegacySlimefunApiCompat.getCharge(this, location);
        LegacySlimefunApiCompat.setCharge(this, location, 0);
        return charge;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this,
                String.valueOf(String.format("%.2f", Slimefun.getTickerTask().getTickRate() / 20.0)),
                String.valueOf(this.capacity));
    }
}
