package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.item.machine.range.point.EquivalentConcept;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class EntropySeed extends AbstractConfigFreeMachine implements RecipeItem {
    private final double equivalentConceptLife = ConfigUtil.getOrDefaultItemSetting(8.0, this, "life");
    private final int equivalentConceptRange = ConfigUtil.getOrDefaultItemSetting(4, this, "range");
    private final String key = "key";
    private final String value = "value";

    public EntropySeed(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                Location location = e.getBlock().getLocation();
                LegacyBlockDataCompat.setValue(location, EntropySeed.this.key, EntropySeed.this.value);
            }
        };
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new BlockBreakHandler(false, true) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent blockBreakEvent, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                blockBreakEvent.setDropItems(false);
                drops.clear();
            }
        };
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return null;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem) {
        Location location = block.getLocation();
        LegacySlimefunApiCompat.runAt(location, () -> transformAt(block));
    }

    private void transformAt(@Nonnull Block block) {
        Location location = block.getLocation();
        if (!this.getId().equals(LegacyBlockDataCompat.getSlimefunId(location))) {
            return;
        }

        String marker = LegacyBlockDataCompat.getValue(block.getLocation(), this.key);
        if (marker != null && this.value.equals(marker)) {
            LegacyBlockDataCompat.setValue(location, this.key, null);
            SlimefunItem sfItem = SlimefunItem.getByItem(FinalTechItemStacks.EQUIVALENT_CONCEPT);
            if (sfItem != null) {
                LegacyBlockDataCompat.removeBlock(location);
                LegacySlimefunApiCompat.runAt(location, () -> {
                    if (location.getBlock().getType().equals(EntropySeed.this.getItem().getType())
                            && !LegacyBlockDataCompat.hasBlockData(location)) {
                        LegacyBlockDataCompat.setSlimefunId(location, FinalTechItemStacks.EQUIVALENT_CONCEPT.getItemId());
                        LegacyBlockDataCompat.setValue(location, EquivalentConcept.KEY_LIFE, String.valueOf(EntropySeed.this.equivalentConceptLife));
                        LegacyBlockDataCompat.setValue(location, EquivalentConcept.KEY_RANGE, String.valueOf(EntropySeed.this.equivalentConceptRange));
                    }
                }, Slimefun.getTickerTask().getTickRate() + 1L);
            }
        } else {
            LegacyBlockDataCompat.removeBlock(location);
            LegacySlimefunApiCompat.runAt(location, () -> {
                if (location.getBlock().getType().equals(EntropySeed.this.getItem().getType())
                        && !LegacyBlockDataCompat.hasBlockData(location)) {
                    LegacyBlockDataCompat.setSlimefunId(location, FinalTechItemStacks.JUSTIFIABILITY.getItemId());
                }
            }, Slimefun.getTickerTask().getTickRate() + 1L);
        }
    }

    @Override
    protected boolean isSynchronized() {
        return false;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this);
    }
}
