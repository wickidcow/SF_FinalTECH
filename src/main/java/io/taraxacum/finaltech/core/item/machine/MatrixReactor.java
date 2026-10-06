package io.taraxacum.finaltech.core.item.machine;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
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
import io.taraxacum.finaltech.core.menu.machine.MatrixReactorMenu;
import io.taraxacum.finaltech.setup.FinalTechItems;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.ConstantTableUtil;
import io.taraxacum.finaltech.util.MachineUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class MatrixReactor extends AbstractTickerDataMachine implements RecipeItem, MenuUpdater {
    private final String keyItem = "item";
    private final String keyCount = "count";
    private final int difficulty = ConfigUtil.getOrDefaultItemSetting(80, this, "difficulty");
    private final Set<String> notAllowedIdList = new HashSet<>(ConfigUtil.getItemStringList(this, "not-allowed-id"));

    public MatrixReactor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
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
        return MachineUtil.simpleBlockBreakerHandler(this, MatrixReactorMenu.ITEM_PHONY_INPUT_SLOT);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new MatrixReactorMenu(this);
    }

    @Override
    protected void tickWithData(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData data) {
        BlockMenu blockMenu = LegacyBlockDataCompat.getMenu(block.getLocation());
        Location location = block.getLocation();
        ItemStack itemStack = blockMenu.getItemInSlot(MatrixReactorMenu.ITEM_INPUT_SLOT[0]);

        if (ItemStackUtil.isItemNull(itemStack)) {
            LegacyTickerDataCompat.setValue(data, keyItem, null);
            LegacyTickerDataCompat.setValue(data, keyCount, "0");
            if (blockMenu.hasViewer()) {
                this.updateMenu(blockMenu, MatrixReactorMenu.STATUS_SLOT, this,
                        "0",
                        String.valueOf(difficulty));
            }
            return;
        } else if (!this.allowedItem(itemStack)) {
            LegacyTickerDataCompat.setValue(data, keyItem, null);
            LegacyTickerDataCompat.setValue(data, keyCount, "0");
            if (blockMenu.hasViewer()) {
                this.updateMenu(blockMenu, MatrixReactorMenu.STATUS_SLOT, this,
                        "0",
                        String.valueOf(difficulty));
            }
            blockMenu.dropItems(location, MatrixReactorMenu.ITEM_INPUT_SLOT);
            return;
        }

        ItemStack stringItem = null;
        if (LegacyTickerDataCompat.contains(data, keyItem)) {
            String itemString = LegacyTickerDataCompat.getString(data, keyItem);
            stringItem = ItemStackUtil.stringToItemStack(itemString);
        }

        boolean match = true;

        int[] orderedDustItemSlots = new int[MatrixReactorMenu.ORDERED_DUST_INPUT_SLOT.length];
        int[] unorderedDustItemSlots = new int[MatrixReactorMenu.UNORDERED_DUST_INPUT_SLOT.length];

        int amount = itemStack.getAmount();
        int orderedDustItemSlotsP = 0;
        int orderedDustItemCount = 0;
        int unorderedDustItemSlotsP = 0;
        int unorderedDustItemCount = 0;
        for (int slot : MatrixReactorMenu.ORDERED_DUST_INPUT_SLOT) {
            if (FinalTechItems.ORDERED_DUST.verifyItem(blockMenu.getItemInSlot(slot))) {
                orderedDustItemSlots[orderedDustItemSlotsP++] = slot;
                orderedDustItemCount += blockMenu.getItemInSlot(slot).getAmount();
                if (orderedDustItemCount > amount) {
                    break;
                }
            }
        }
        if (orderedDustItemCount >= amount) {
            for (int slot : MatrixReactorMenu.UNORDERED_DUST_INPUT_SLOT) {
                if (FinalTechItems.UNORDERED_DUST.verifyItem(blockMenu.getItemInSlot(slot))) {
                    unorderedDustItemSlots[unorderedDustItemSlotsP++] = slot;
                    unorderedDustItemCount += blockMenu.getItemInSlot(slot).getAmount();
                    if (unorderedDustItemCount > amount) {
                        break;
                    }
                }
            }
        }
        if (orderedDustItemCount < amount || unorderedDustItemCount < amount) {
            match = false;
        }

        if (!match) {
            int count = LegacyTickerDataCompat.contains(data, keyCount) ? Integer.parseInt(LegacyTickerDataCompat.getString(data, keyCount)) : 0;
            count = count > 0 ? count - 1 : 0;
            LegacyTickerDataCompat.setValue(data, keyCount, String.valueOf(count));
        } else {
            orderedDustItemCount = amount;
            for (int slot : orderedDustItemSlots) {
                ItemStack dustItemStack = blockMenu.getItemInSlot(slot);
                int n = Math.min(dustItemStack.getAmount(), orderedDustItemCount);
                dustItemStack.setAmount(dustItemStack.getAmount() - n);
                orderedDustItemCount -= n;
                if (orderedDustItemCount == 0) {
                    break;
                }
            }

            unorderedDustItemCount = amount;
            for (int slot : unorderedDustItemSlots) {
                ItemStack dustItemStack = blockMenu.getItemInSlot(slot);
                int n = Math.min(dustItemStack.getAmount(), unorderedDustItemCount);
                dustItemStack.setAmount(dustItemStack.getAmount() - n);
                unorderedDustItemCount -= n;
                if (unorderedDustItemCount == 0) {
                    break;
                }
            }

            if (ItemStackUtil.isItemNull(stringItem) || !ItemStackUtil.isItemSimilar(itemStack, stringItem) || itemStack.getAmount() != stringItem.getAmount()) {
                LegacyTickerDataCompat.setValue(data, keyItem, ItemStackUtil.itemStackToString(itemStack));

                int count;
                if (FinalTechItems.ITEM_PHONY.verifyItem(blockMenu.getItemInSlot(MatrixReactorMenu.ITEM_PHONY_INPUT_SLOT[0]))) {
                    ItemStack itemPhony = blockMenu.getItemInSlot(MatrixReactorMenu.ITEM_PHONY_INPUT_SLOT[0]);
                    itemPhony.setAmount(itemPhony.getAmount() - 1);
                    count = 1;
                } else {
                    count = FinalTechChanged.getRandom().nextBoolean() ? 1 : 0;
                }

                LegacyTickerDataCompat.setValue(data, keyCount, String.valueOf(count));
            } else {
                int count = LegacyTickerDataCompat.contains(data, keyCount) ? Integer.parseInt(LegacyTickerDataCompat.getString(data, keyCount)) : 0;
                if (FinalTechItems.ITEM_PHONY.verifyItem(blockMenu.getItemInSlot(MatrixReactorMenu.ITEM_PHONY_INPUT_SLOT[0])) && blockMenu.getItemInSlot(MatrixReactorMenu.ITEM_PHONY_INPUT_SLOT[0]).getAmount() >= amount + count && amount + count <= ConstantTableUtil.ITEM_MAX_STACK) {
                    ItemStack itemPhony = blockMenu.getItemInSlot(MatrixReactorMenu.ITEM_PHONY_INPUT_SLOT[0]);
                    itemPhony.setAmount(itemPhony.getAmount() - count - amount);
                    count++;
                } else {
                    count = FinalTechChanged.getRandom().nextBoolean() ? count - 1 : count + 1;
                }

                if (count + itemStack.getAmount() >= this.difficulty) {
                    ItemStack existedItem = blockMenu.getItemInSlot(this.getOutputSlot()[0]);
                    if (ItemStackUtil.isItemNull(existedItem)) {
                        ItemStack outputItem = ItemStackUtil.cloneItem(itemStack);
                        outputItem.setAmount(1);
                        blockMenu.replaceExistingItem(this.getOutputSlot()[0], outputItem);
                        LegacyTickerDataCompat.setValue(data, keyItem, null);
                        LegacyTickerDataCompat.setValue(data, keyCount, "0");
                        if (blockMenu.hasViewer()) {
                            this.updateMenu(blockMenu, MatrixReactorMenu.STATUS_SLOT, this,
                                    "0",
                                    String.valueOf(difficulty));
                        }
                        return;
                    } else if (existedItem.getAmount() < existedItem.getMaxStackSize() && ItemStackUtil.isItemSimilar(existedItem, itemStack)) {
                        existedItem.setAmount(existedItem.getAmount() + 1);
                        LegacyTickerDataCompat.setValue(data, keyItem, null);
                        LegacyTickerDataCompat.setValue(data, keyCount, "0");
                        if (blockMenu.hasViewer()) {
                            this.updateMenu(blockMenu, MatrixReactorMenu.STATUS_SLOT, this,
                                    "0",
                                    String.valueOf(difficulty));
                        }
                        return;
                    }
                    count = count < this.difficulty ? count + 1 : this.difficulty;
                }

                count = Math.max(count, 0);
                LegacyTickerDataCompat.setValue(data, keyCount, String.valueOf(count));
            }
        }

        if (blockMenu.hasViewer()) {
            this.updateMenu(blockMenu, MatrixReactorMenu.STATUS_SLOT, this,
                    LegacyTickerDataCompat.getString(data, keyCount) == null ? "0" : LegacyTickerDataCompat.getString(data, keyCount),
                    String.valueOf(difficulty));
        }
    }

    @Override
    protected boolean isSynchronized() {
        return true;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTechChanged.getLanguageManager(), this,
                String.valueOf(this.difficulty),
                String.format("%.2f", Slimefun.getTickerTask().getTickRate() / 20.0));
    }

    private boolean allowedItem(@Nonnull ItemStack item) {
        if (Tag.SHULKER_BOXES.isTagged(item.getType()) || Material.BUNDLE.equals(item.getType())) {
            return false;
        }
        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            if (persistentDataContainer.getKeys().size() > 0) {
                for (NamespacedKey namespacedKey : persistentDataContainer.getKeys()) {
                    if (!"slimefun".equals(namespacedKey.getNamespace())) {
                        return false;
                    }
                }
            }
        }
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
        return slimefunItem == null || !notAllowedIdList.contains(slimefunItem.getId());
    }
}
