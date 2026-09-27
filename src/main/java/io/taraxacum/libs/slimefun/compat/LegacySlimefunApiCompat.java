package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import org.bukkit.Location;

import javax.annotation.Nonnull;

/**
 * Compatibility boundary for Slimefun APIs that remain part of FinalTECH's RC-37 floor.
 *
 * <p>Current Slimefun Legacy marks these historical entry points deprecated, but
 * upstream RC-37 does not expose their newer replacements. FinalTECH keeps this
 * narrow adapter so normal addon code is warning-clean without raising the
 * supported Slimefun API floor or changing machine behavior.</p>
 */
@SuppressWarnings("deprecation")
public final class LegacySlimefunApiCompat {

    private LegacySlimefunApiCompat() {
    }

    public static int getResearchLevelCost(@Nonnull Research research) {
        return research.getCost();
    }

    public static void addCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        component.addCharge(location, charge);
    }

    public static int getCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location) {
        return component.getCharge(location);
    }

    public static void setCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        component.setCharge(location, charge);
    }
}
