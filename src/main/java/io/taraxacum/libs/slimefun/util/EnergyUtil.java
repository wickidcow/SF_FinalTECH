package io.taraxacum.libs.slimefun.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.common.util.StringNumberUtil;
import io.taraxacum.finaltech.util.ConstantTableUtil;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Energy access through the canonical ticker record or location adapter.
 */
public class EnergyUtil {
    @Nonnull
    public static String getCharge(@Nonnull Location location) {
        SlimefunItem it = SlimefunItem.getById(LegacyBlockDataCompat.getSlimefunId(location));
        if (it != null)
            if (it instanceof EnergyNetComponent)
                return String.valueOf(LegacySlimefunApiCompat.getCharge((EnergyNetComponent) it, location));
        return "0";
    }

    @Nonnull
    public static String getCharge(@Nonnull SlimefunBlockData config) {
        return Objects.requireNonNull(JavaUtil.getFirstNotNull(config.getData(ConstantTableUtil.CONFIG_CHARGE), StringNumberUtil.ZERO));
    }

    public static void setCharge(@Nonnull Location location, @Nonnull String energy) {
        SlimefunItem it = SlimefunItem.getById(LegacyBlockDataCompat.getSlimefunId(location));
        if (it != null)
            if (it instanceof EnergyNetComponent)
                LegacySlimefunApiCompat.setCharge((EnergyNetComponent) it, location, Integer.parseInt(energy));
    }

    public static void setCharge(@Nonnull Location location, int energy) {
        SlimefunItem it = SlimefunItem.getById(LegacyBlockDataCompat.getSlimefunId(location));
        if (it != null)
            if (it instanceof EnergyNetComponent)
                LegacySlimefunApiCompat.setCharge((EnergyNetComponent) it, location, energy);
    }

    public static void setCharge(@Nonnull SlimefunBlockData config, @Nonnull String energy) {
        config.setData(ConstantTableUtil.CONFIG_CHARGE, energy);
    }

    public static void setCharge(@Nonnull SlimefunBlockData config, int energy) {
        config.setData(ConstantTableUtil.CONFIG_CHARGE, String.valueOf(energy));
    }


}
