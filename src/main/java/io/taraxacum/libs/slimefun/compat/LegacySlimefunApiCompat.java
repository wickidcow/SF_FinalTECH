package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer;
import org.bukkit.Location;

import javax.annotation.Nonnull;

/**
 * Adapter preserving FinalTECH's existing research and integer energy behavior.
 * Data-aware operations use the modern Slimefun Legacy container overloads.
 */
@SuppressWarnings("deprecation")
public final class LegacySlimefunApiCompat {

    private LegacySlimefunApiCompat() {
    }

    /**
     * Runs block/world work on the scheduler that owns this location.
     * This maps to the global server thread on Paper/Purpur and to the owning region on Folia.
     */
    public static void runAt(@Nonnull Location location, @Nonnull Runnable runnable) {
        Slimefun.runSyncAt(location, runnable);
    }

    /**
     * Runs delayed block/world work on the scheduler that owns this location.
     */
    public static void runAt(
            @Nonnull Location location,
            @Nonnull Runnable runnable,
            long delay) {
        Slimefun.runSyncAt(location, runnable, delay);
    }

    public static int getResearchLevelCost(@Nonnull Research research) {
        return research.getCost();
    }

    public static void setResearchLevelCost(@Nonnull Research research, int cost) {
        research.setCost(cost);
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

    public static int getCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            @Nonnull ASlimefunDataContainer data) {
        return component.getCharge(location, data);
    }

    public static int getGeneratedOutput(
            @Nonnull EnergyNetProvider provider,
            @Nonnull Location location,
            @Nonnull ASlimefunDataContainer data) {
        return provider.getGeneratedOutput(location, data);
    }

    public static boolean willExplode(
            @Nonnull EnergyNetProvider provider,
            @Nonnull Location location,
            @Nonnull ASlimefunDataContainer data) {
        return provider.willExplode(location, data);
    }

    public static void setCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        component.setCharge(location, charge);
    }
}
