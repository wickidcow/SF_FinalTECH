package io.taraxacum.finaltech.core.item.machine.electric.capacitor.expanded;

import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.common.util.StringNumberUtil;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.MenuUpdater;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.item.machine.electric.capacitor.AbstractElectricCapacitor;
import io.taraxacum.finaltech.core.listener.ExpandedElectricCapacitorEnergyListener;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.unit.StatusMenu;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.slimefun.util.EnergyUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nonnull;

/**
 * @author Final_ROOT
 * @since 1.0
 */
public abstract class AbstractExpandedElectricCapacitor extends AbstractElectricCapacitor implements RecipeItem, MenuUpdater {
    private static boolean registerListener = false;
    protected final String key = "s";

    public AbstractExpandedElectricCapacitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void register(@Nonnull SlimefunAddon addon) {
        super.register(addon);
        if (!this.isDisabled() && !registerListener) {
            PluginManager pluginManager = addon.getJavaPlugin().getServer().getPluginManager();
            pluginManager.registerEvents(new ExpandedElectricCapacitorEnergyListener(), addon.getJavaPlugin());
            registerListener = true;
        }
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent blockPlaceEvent) {
                LegacyBlockDataCompat.setValue(blockPlaceEvent.getBlock().getLocation(), AbstractExpandedElectricCapacitor.this.key, StringNumberUtil.ZERO);
            }
        };
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new StatusMenu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData config) {
        String energyStr = EnergyUtil.getCharge(config);
        String energyStackStr = JavaUtil.getFirstNotNull(config.getData(this.key), StringNumberUtil.ZERO);
        long energy = Integer.parseInt(energyStr);
        long energyStack = Integer.parseInt(energyStackStr);

        long allEnergy = energyStack * (this.getCapacity() / 2 + 1) + energy;

        this.setEnergy(block.getLocation(), allEnergy);

        BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(block.getLocation());
        if (blockMenu.hasViewer()) {
            this.updateMenu(blockMenu, StatusMenu.STATUS_SLOT, this, String.valueOf(energy), energyStackStr);
        }
    }

    @Override
    public abstract int getCapacity();

    public abstract int getMaxStack();

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this,
                String.valueOf((this.getCapacity() / 2)),
                String.valueOf(this.getMaxStack()),
                String.format("%.2f", Slimefun.getTickerTask().getTickRate() / 20.0));
    }

    public int getStack(@Nonnull SlimefunBlockData config) {
        return Integer.parseInt(JavaUtil.getFirstNotNull(config.getData(this.key), StringNumberUtil.ZERO));
    }

    public long getMaxEnergy() {
        return (long) this.getCapacity() / 2 * this.getMaxStack() + this.getCapacity();
    }

    public long calEnergy(int energy, int stack) {
        return (long) this.getCapacity() / 2 * stack + energy;
    }

    public void setEnergy(@Nonnull Location location, long energy) {
        long stack = energy / (this.getCapacity() / 2);
        stack = Math.min(stack, this.getMaxStack());
        long lastEnergy = energy - this.getCapacity() / 2 * stack;
        lastEnergy = Math.min(lastEnergy, this.getCapacity());

        if (lastEnergy < this.getCapacity() / 4 && stack > 0) {
            lastEnergy += this.getCapacity() / 2;
            stack--;
        } else if (lastEnergy > this.getCapacity() / 4 * 3 && stack < this.getMaxStack()) {
            lastEnergy -= this.getCapacity() / 2;
            stack++;
        }

        LegacyBlockDataCompat.setValue(location, this.key, String.valueOf(stack));
        // Energy charge remains managed by the Slimefun energy compatibility boundary below.
        LegacySlimefunApiCompat.setCharge(this, location, (int) (lastEnergy % Integer.MAX_VALUE));
    }
}
