package io.taraxacum.finaltech.core.item.machine.electric;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.common.util.StringNumberUtil;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.MenuUpdater;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.unit.StatusMenu;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.slimefun.util.EnergyUtil;
import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class VariableWireCapacitor extends AbstractElectricMachine implements RecipeItem, MenuUpdater {
    private final int capacity = ConfigUtil.getOrDefaultItemSetting(65536, this, "capacity");

    public VariableWireCapacitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new StatusMenu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData config) {
        Location location = block.getLocation();
        String charge = EnergyUtil.getCharge(location);
        if (StringNumberUtil.ZERO.equals(charge)) {
            LegacySlimefunApiCompat.runAt(location, () -> {
                if (!this.getId().equals(LegacyBlockDataCompat.getSlimefunId(location))) {
                    return;
                }

                LegacyBlockDataCompat.removeBlock(location);
                LegacyBlockDataCompat.setSlimefunId(location, FinalTechItemStacks.VARIABLE_WIRE_RESISTANCE.getItemId());
                Slimefun.getNetworkManager().updateAllNetworks(location);
                LegacySlimefunApiCompat.runAt(location, () -> {
                    if (!location.getBlock().getType().isAir() && FinalTechItemStacks.VARIABLE_WIRE_RESISTANCE.getItemId().equals(LegacyBlockDataCompat.getSlimefunId(location))) {
                        block.setType(FinalTechItemStacks.VARIABLE_WIRE_RESISTANCE.getType());
                    }
                });
            });
        } else {
            BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(location);
            if (blockMenu.hasViewer()) {
                this.updateMenu(blockMenu, StatusMenu.STATUS_SLOT, this, charge);
            }
        }
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CAPACITOR;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this,
                String.valueOf(this.capacity));
    }
}
