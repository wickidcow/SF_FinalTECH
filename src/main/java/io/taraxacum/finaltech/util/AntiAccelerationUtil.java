package io.taraxacum.finaltech.util;

import io.taraxacum.finaltech.FinalTechChanged;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;

import javax.annotation.Nonnull;

public class AntiAccelerationUtil {
    public static String KEY = "anti-acceleration";

    /**
     * This method will determine if the given machine is accelerated.
     *
     * @param config The storage info in the machine location
     * @return whether a machine can work
     */
    public static boolean isAccelerated(@Nonnull SlimefunBlockData config) {
        String s = config.getData(KEY);
        if (s != null && Integer.parseInt(s) == FinalTechChanged.getSlimefunTickCount()) {
            return true;
        }
        config.setData(KEY, String.valueOf(FinalTechChanged.getSlimefunTickCount()));
        return false;
    }
}
