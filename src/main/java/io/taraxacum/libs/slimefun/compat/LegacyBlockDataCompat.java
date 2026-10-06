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

    enum IdentityDecision {
        CREATE,
        ALREADY_TARGET,
        CONFLICT
    }

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

    static IdentityDecision identityDecision(
            @Nullable SlimefunBlockData existing,
            @Nonnull String slimefunId) {
        if (existing == null) {
            return IdentityDecision.CREATE;
        }
        return slimefunId.equals(existing.getSfId())
                ? IdentityDecision.ALREADY_TARGET
                : IdentityDecision.CONFLICT;
    }

    /**
     * Creates the requested identity only when no block-data record currently exists.
     *
     * <p>This is the safe boundary for the historical menu repair path. It never replaces
     * an existing record, including a record whose Slimefun ID is unresolved by this addon.</p>
     *
     * @return true only when this call created a new record
     */
    public static boolean createSlimefunIdIfAbsent(
            @Nonnull Location location,
            @Nonnull String slimefunId) {
        if (identityDecision(getLoadedData(location), slimefunId) != IdentityDecision.CREATE) {
            return false;
        }

        try {
            controller().createBlock(location, slimefunId);
            return true;
        } catch (IllegalStateException race) {
            // A record may have appeared between the preflight and createBlock's own cache check.
            // Preserve whichever record won that race instead of replacing it.
            if (getLoadedData(location) != null) {
                return false;
            }
            throw race;
        }
    }

    /**
     * Creates a Slimefun identity at an empty location.
     *
     * <p>Calling this for the same existing identity is idempotent. A different existing identity
     * is never overwritten: intentional identity transitions must remove the old block-data record
     * first, as FinalTECH's transformation machines already do.</p>
     */
    public static void setSlimefunId(@Nonnull Location location, @Nonnull String slimefunId) {
        IdentityDecision decision = identityDecision(getLoadedData(location), slimefunId);
        if (decision == IdentityDecision.ALREADY_TARGET) {
            return;
        }
        if (decision == IdentityDecision.CONFLICT) {
            throw new IllegalStateException(
                    "Refusing to replace existing Slimefun block identity at " + location);
        }
        controller().createBlock(location, slimefunId);
    }

    /**
     * Removes the complete Slimefun block-data record at this location.
     */
    public static void removeBlock(@Nonnull Location location) {
        controller().removeBlock(location);
    }
}
