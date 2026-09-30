package io.taraxacum.finaltech.util;

import io.taraxacum.finaltech.FinalTechChanged;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;

import javax.annotation.Nonnull;

public class PerformanceLimitUtil {
    public static String KEY = "tps-charge";

    public static boolean charge(@Nonnull SlimefunBlockData config) {
        int charge = (config.getData(KEY) != null) ? Integer.parseInt(config.getData(KEY)) : 0;
        charge += FinalTechChanged.getTps();
        if (charge >= 20) {
            if (charge >= 40) {
                charge -= 20;
            }
            config.setData(KEY, String.valueOf(charge - 20));
            return true;
        } else {
            config.setData(KEY, String.valueOf(charge));
            return false;
        }
    }
}
