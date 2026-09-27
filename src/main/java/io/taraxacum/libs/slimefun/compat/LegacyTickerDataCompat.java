package io.taraxacum.libs.slimefun.compat;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Zero-allocation access boundary for the legacy ticker data object.
 *
 * <p>RC-37 passes Config directly. Current Slimefun Legacy supplies a
 * BlockDataConfigWrapper through its binary-compatibility bridge, which remains
 * a Config instance. These helpers therefore preserve the exact object and
 * mutation semantics without reflective calls or per-tick wrapper allocation.</p>
 */
@SuppressWarnings("deprecation")
public final class LegacyTickerDataCompat {

    private LegacyTickerDataCompat() {
    }

    private static Config config(@Nonnull Object data) {
        return (Config) data;
    }

    /**
     * Returns the legacy ticker data view for a block location.
     *
     * <p>On current Slimefun Legacy this is a BlockDataConfigWrapper over the
     * canonical block-data record. On RC-37 it preserves the historical Config
     * object. Keeping this call here intentionally isolates the deprecated
     * compatibility facade from gameplay classes.</p>
     */
    @Nonnull
    public static Config getConfig(@Nonnull Location location) {
        return me.mrCookieSlime.Slimefun.api.BlockStorage.getLocationInfo(location);
    }

    public static boolean contains(@Nonnull Object data, @Nonnull String key) {
        return config(data).contains(key);
    }

    @Nullable
    public static String getString(@Nonnull Object data, @Nonnull String key) {
        return config(data).getString(key);
    }

    public static void setValue(
            @Nonnull Object data,
            @Nonnull String key,
            @Nullable Object value) {
        config(data).setValue(key, value);
    }
}
