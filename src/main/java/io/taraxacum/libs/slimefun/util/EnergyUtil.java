package io.taraxacum.libs.slimefun.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.common.util.StringNumberUtil;
import io.taraxacum.finaltech.util.ConstantTableUtil;
import io.taraxacum.libs.slimefun.compat.LegacyBlockDataCompat;
import io.taraxacum.libs.slimefun.compat.LegacySlimefunApiCompat;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import java.util.Objects;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;

/**
 * Legacy Config overloads are retained for RC-37 compatibility; location-based
 * energy access uses the maintained compatibility adapter.
 */
@SuppressWarnings("deprecation")
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
    public static String getCharge(@Nonnull Object data) {
        return Objects.requireNonNull(JavaUtil.getFirstNotNull(
                LegacyTickerDataCompat.getString(data, ConstantTableUtil.CONFIG_CHARGE),
                StringNumberUtil.ZERO));
    }

    @Nonnull
    public static String getCharge(@Nonnull Config config) {
        return getCharge((Object) config);
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

    public static void setCharge(@Nonnull Object data, @Nonnull String energy) {
        LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_CHARGE, energy);
    }

    public static void setCharge(@Nonnull Object data, int energy) {
        LegacyTickerDataCompat.setValue(data, ConstantTableUtil.CONFIG_CHARGE, String.valueOf(energy));
    }

    public static void setCharge(@Nonnull Config config, @Nonnull String energy) {
        setCharge((Object) config, energy);
    }

    public static void setCharge(@Nonnull Config config, int energy) {
        setCharge((Object) config, energy);
    }


}
