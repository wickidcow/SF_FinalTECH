package io.taraxacum.finaltech.util;

import io.taraxacum.finaltech.FinalTechChanged;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;

import javax.annotation.Nonnull;
import io.taraxacum.libs.slimefun.compat.LegacyTickerDataCompat;

public class PerformanceLimitUtil {
    public static String KEY = "tps-charge";

    public static boolean charge(@Nonnull Object data) {
        int charge = LegacyTickerDataCompat.contains(data, KEY)
                ? Integer.parseInt(LegacyTickerDataCompat.getString(data, KEY))
                : 0;
        charge += FinalTechChanged.getTps();
        if (charge >= 20) {
            if (charge >= 40) {
                charge -= 20;
            }
            LegacyTickerDataCompat.setValue(data, KEY, String.valueOf(charge - 20));
            return true;
        } else {
            LegacyTickerDataCompat.setValue(data, KEY, String.valueOf(charge));
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean charge(@Nonnull Config config) {
        return charge((Object) config);
    }
}
