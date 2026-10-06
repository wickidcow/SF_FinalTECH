package io.taraxacum.finaltech.core.item.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.taraxacum.finaltech.FinalTechChanged;
import io.taraxacum.finaltech.core.item.AbstractMySlimefunItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Final_ROOT
 * @since 1.0
 */
// TODO: Optimization
/**
 * Machine base using the canonical Slimefun Legacy block-data callback.
 * Scheduling and machine-specific tick behavior are preserved.
 */
public abstract class AbstractMachine extends AbstractMySlimefunItem {
    private AbstractMachineMenu menu;

    public AbstractMachine(@Nonnull ItemGroup itemGroup, @Nonnull SlimefunItemStack item, @Nonnull RecipeType recipeType, @Nonnull ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        super.preRegister();
        this.addItemHandler(this.onBlockBreak());
        this.addItemHandler(this.onBlockPlace());
        this.menu = this.setMachineMenu();

        boolean requiresLocationOwnedTick = this.requiresLocationOwnedTick();
        boolean forceAsync = FinalTechChanged.getMultiThreadLevel() == 2 && !requiresLocationOwnedTick;

        if (forceAsync) {
            this.getAddon().getJavaPlugin().getLogger().info(this.getId() + "(" + this.getItemName() + ")" + " is optimized for multi-thread！！！");
        } else if (!requiresLocationOwnedTick && !this.isSynchronized()
                && (FinalTechChanged.getMultiThreadLevel() == 1 || FinalTechChanged.isAsyncSlimefunItem(this.getId()))) {
            this.getAddon().getJavaPlugin().getLogger().info(this.getId() + "(" + this.getItemName() + ")" + " is optimized for multi-thread！！！");
        }

        BlockTicker blockTicker;
        if (forceAsync) {
            blockTicker = new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return false;
                }

                @Override
                public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                    AbstractMachine.this.tick(b, item, data);
                }

                @Override
                public void uniqueTick() {
                    AbstractMachine.this.uniqueTick();
                }
            };
            FinalTechChanged.addAsyncSlimefunItem(this.getId());
        } else {
            blockTicker = new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return requiresLocationOwnedTick || AbstractMachine.this.isSynchronized();
                }

                @Override
                public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                    AbstractMachine.this.tick(b, item, data);
                }

                @Override
                public void uniqueTick() {
                    AbstractMachine.this.uniqueTick();
                }
            };
            if (!requiresLocationOwnedTick && !this.isSynchronized() && FinalTechChanged.getMultiThreadLevel() >= 1) {
                FinalTechChanged.addAsyncSlimefunItem(this.getId());
            }
        }
        this.addItemHandler(blockTicker);
    }

    @Nonnull
    public final int[] getInputSlot() {
        return this.menu == null ? new int[0] : this.menu.getInputSlot();
    }

    @Nonnull
    public final int[] getOutputSlot() {
        return this.menu == null ? new int[0] : this.menu.getOutputSlot();
    }

    protected void uniqueTick() {

    }

    /**
     * Returns whether this machine must remain on Slimefun's location-owned ticker even when
     * FinalTECH's global multi-thread level requests asynchronous ticking.
     */
    protected boolean requiresLocationOwnedTick() {
        return false;
    }

    @Nonnull
    protected abstract BlockPlaceHandler onBlockPlace();

    @Nonnull
    protected abstract BlockBreakHandler onBlockBreak();

    @Nullable
    protected abstract AbstractMachineMenu setMachineMenu();

    protected abstract void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData config);

    protected abstract boolean isSynchronized();
}
