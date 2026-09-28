package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
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
public final class LegacySlimefunApiCompat {

    private LegacySlimefunApiCompat() {
    }

    public static int getResearchLevelCost(@Nonnull Research research) {
        return research.getLevelCost();
    }

    public static void setResearchLevelCost(@Nonnull Research research, int cost) {
        research.setLevelCost(cost);
    }

    public static void addCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        component.addCharge(location, (long) charge);
    }

    public static int getCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location) {
        long charge = component.getChargeLong(location);
        if (charge > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (charge < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) charge;
    }

    @SuppressWarnings("deprecation")
    public static int getCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            @Nonnull Object data) {
        return component.getCharge(location, (Config) data);
    }

    @SuppressWarnings("deprecation")
    public static int getGeneratedOutput(
            @Nonnull EnergyNetProvider provider,
            @Nonnull Location location,
            @Nonnull Object data) {
        return provider.getGeneratedOutput(location, (Config) data);
    }

    @SuppressWarnings("deprecation")
    public static boolean willExplode(
            @Nonnull EnergyNetProvider provider,
            @Nonnull Location location,
            @Nonnull Object data) {
        return provider.willExplode(location, (Config) data);
    }

    public static void setCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        component.setCharge(location, (long) charge);
    }
}
