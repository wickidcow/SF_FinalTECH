package io.taraxacum.libs.slimefun.dto;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Slimefun location/data snapshot used by FinalTECH runtime helpers.
 *
 * <p>The active data object is intentionally opaque: current Slimefun Legacy
 * supplies its block-data container while RC-37 supplies Config. The historical
 * {@link #getConfig()} method remains only for source/binary compatibility.</p>
 *
 * @author Final_ROOT
 * @since 2.4
 */
public class LocationInfo {
    private Location location;
    private Object data;
    private String id;
    private SlimefunItem slimefunItem;

    private LocationInfo(
            @Nonnull Location location,
            @Nonnull Object data,
            @Nonnull String id,
            @Nonnull SlimefunItem slimefunItem) {
        this.location = location;
        this.data = data;
        this.id = id;
        this.slimefunItem = slimefunItem;
    }

    @Nullable
    public static LocationInfo get(@Nonnull Location location) {
        Object data = LegacyTickerDataCompat.getData(location);
        String id = LegacyBlockDataCompat.getSlimefunId(location);
        if (id != null) {
            SlimefunItem slimefunItem = SlimefunItem.getById(id);
            if (slimefunItem != null) {
                return new LocationInfo(location, data, id, slimefunItem);
            }
        }

        return null;
    }

    public void refresh() {

    }

    public void cloneLocation() {
        this.location = this.location.clone();
    }

    public Location getLocation() {
        return location;
    }

    public Object getData() {
        return data;
    }

    /**
     * Historical RC-37 accessor retained for source/binary compatibility.
     *
     * <p>Internal FinalTECH code must use {@link #getData()} instead.</p>
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Config getConfig() {
        return cast(LegacyBlockDataCompat.getLegacyDataView(location));
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(@Nonnull Object value) {
        return (T) value;
    }

    public String getId() {
        return id;
    }

    public SlimefunItem getSlimefunItem() {
        return slimefunItem;
    }

    /**
     * @return false if there is no location info
     */
    public boolean newInstance(@Nonnull Location location) {
        Object data = LegacyTickerDataCompat.getData(location);
        String id = LegacyBlockDataCompat.getSlimefunId(location);
        if (id == null) {
            return false;
        }
        SlimefunItem slimefunItem = SlimefunItem.getById(id);
        if (slimefunItem == null) {
            return false;
        }

        this.location = location;
        this.data = data;
        this.id = id;
        this.slimefunItem = slimefunItem;

        return true;
    }

    public boolean newInstance() {
        return this.newInstance(this.location);
    }
}
