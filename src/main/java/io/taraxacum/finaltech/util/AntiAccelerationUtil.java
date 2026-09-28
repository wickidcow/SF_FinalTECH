package io.taraxacum.finaltech.util;

import io.taraxacum.finaltech.FinalTechChanged;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;

import javax.annotation.Nonnull;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;

public class AntiAccelerationUtil {
    public static String KEY = "anti-acceleration";

    /**
     * This method will determine if the given machine is accelerated.
     *
     * @param config The storage info in the machine location
     * @return whether a machine can work
     */
    public static boolean isAccelerated(@Nonnull Object data) {
        String value = LegacyTickerDataCompat.getString(data, KEY);
        if (value != null && Integer.parseInt(value) == FinalTechChanged.getSlimefunTickCount()) {
            return true;
        }
        LegacyTickerDataCompat.setValue(data, KEY, String.valueOf(FinalTechChanged.getSlimefunTickCount()));
        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean isAccelerated(@Nonnull Config config) {
        return isAccelerated((Object) config);
    }
}
