package io.taraxacum.libs.slimefun.dto;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Final_ROOT
 * @since 2.4
 */
/**
 * Location and identity view over the canonical Slimefun Legacy block-data record.
 */
public class LocationInfo {
    private Location location;
    private SlimefunBlockData config;
    private String id;
    private SlimefunItem slimefunItem;

    private LocationInfo(@Nonnull Location location, @Nonnull SlimefunBlockData config, @Nonnull String id, @Nonnull SlimefunItem slimefunItem) {
        this.location = location;
        this.config = config;
        this.id = id;
        this.slimefunItem = slimefunItem;
    }

    @Nullable
    public static LocationInfo get(@Nonnull Location location) {
        SlimefunBlockData config = LegacyTickerDataCompat.getData(location);
        String id = LegacyBlockDataCompat.getSlimefunId(location);
        if (config != null && id != null) {
            SlimefunItem slimefunItem = SlimefunItem.getById(id);
            if (slimefunItem != null) {
                return new LocationInfo(location, config, id, slimefunItem);
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

    public SlimefunBlockData getData() {
        return config;
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
        SlimefunBlockData config = LegacyTickerDataCompat.getData(location);
        String id = LegacyBlockDataCompat.getSlimefunId(location);
        if (config == null || id == null) {
            return false;
        }
        SlimefunItem slimefunItem = SlimefunItem.getById(id);
        if (slimefunItem == null) {
            return false;
        }

        this.location = location;
        this.config = config;
        this.id = id;
        this.slimefunItem = slimefunItem;

        return true;
    }

    public boolean newInstance() {
        return this.newInstance(this.location);
    }
}
