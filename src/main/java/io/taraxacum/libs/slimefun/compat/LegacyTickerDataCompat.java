package io.taraxacum.libs.slimefun.compat;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Zero-allocation access to the canonical Slimefun Legacy ticker data.
 *
 * <p>The historical helper name remains stable, but no deprecated Config
 * wrapper or storage facade is used. A null value removes the persisted
 * key and returns normally so machine reset sequences can complete.</p>
 */
public final class LegacyTickerDataCompat {

    private LegacyTickerDataCompat() {
    }

    @Nullable
    public static SlimefunBlockData getData(@Nonnull Location location) {
        return LegacyBlockDataCompat.getLoadedData(location);
    }

    public static boolean contains(@Nonnull ASlimefunDataContainer data, @Nonnull String key) {
        return data.getData(key) != null;
    }

    @Nullable
    public static String getString(@Nonnull ASlimefunDataContainer data, @Nonnull String key) {
        return data.getData(key);
    }

    public static void setValue(
            @Nonnull ASlimefunDataContainer data,
            @Nonnull String key,
            @Nullable String value) {
        if (value == null) {
            data.removeData(key);
        } else {
            data.setData(key, value);
        }
    }
}
