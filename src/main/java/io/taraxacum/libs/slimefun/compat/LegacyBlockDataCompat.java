package io.taraxacum.libs.slimefun.compat;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * FinalTECH's storage boundary for current Slimefun Legacy block data.
 *
 * <p>The method surface intentionally remains stable for FinalTECH callers so
 * persisted keys, item IDs, menus, and machine behavior do not change while
 * the retired pre-controller storage fallback is removed.</p>
 */
public final class LegacyBlockDataCompat {

    private LegacyBlockDataCompat() {
    }

    @Nonnull
    private static BlockDataController controller() {
        return Slimefun.getDatabaseManager().getBlockDataController();
    }

    @Nullable
    public static SlimefunBlockData getLoadedData(@Nonnull Location location) {
        BlockDataController controller = controller();
        SlimefunBlockData blockData = controller.getBlockData(location);
        if (blockData != null && !blockData.isDataLoaded()) {
            controller.loadBlockData(blockData);
        }
        return blockData;
    }

    @Nullable
    public static String getValue(@Nonnull Location location, @Nonnull String key) {
        SlimefunBlockData blockData = getLoadedData(location);
        return blockData == null ? null : blockData.getData(key);
    }

    @Nullable
    public static String getSlimefunId(@Nonnull Location location) {
        SlimefunBlockData blockData = getLoadedData(location);
        return blockData == null ? null : blockData.getSfId();
    }

    public static void setValue(
            @Nonnull Location location,
            @Nonnull String key,
            @Nullable String value) {
        SlimefunBlockData blockData = getLoadedData(location);
        if (blockData == null) {
            return;
        }

        if (value == null) {
            blockData.removeData(key);
        } else {
            blockData.setData(key, value);
        }
    }

    @Nullable
    public static BlockMenu getMenu(@Nonnull Location location) {
        SlimefunBlockData blockData = getLoadedData(location);
        return blockData == null ? null : blockData.getBlockMenu();
    }

    public static boolean hasBlockData(@Nonnull Location location) {
        return getLoadedData(location) != null;
    }

    public static boolean hasMenu(@Nonnull Location location) {
        SlimefunBlockData blockData = getLoadedData(location);
        return blockData != null && blockData.getBlockMenu() != null;
    }

    /**
     * Creates or recreates the Slimefun identity record at this location.
     * Persisted FinalTECH keys remain unchanged.
     */
    public static void setSlimefunId(@Nonnull Location location, @Nonnull String slimefunId) {
        controller().createBlock(location, slimefunId);
    }

    /**
     * Removes the complete Slimefun block-data record at this location.
     */
    public static void removeBlock(@Nonnull Location location) {
        controller().removeBlock(location);
    }
}
