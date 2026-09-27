package io.taraxacum.libs.plugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Bridges FinalTECH's legacy section-formatted String text contracts onto
 * Paper's Adventure ItemMeta APIs without changing the visible formatting.
 */
public final class LegacyTextCompat {

    public static final String DARK_GRAY = "§8";
    public static final String GREEN = "§a";
    public static final String YELLOW = "§e";
    public static final String GRAY = "§7";

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern LEGACY_FORMAT = Pattern.compile("(?i)§[0-9A-FK-ORX]");

    private LegacyTextCompat() {
    }

    @Nonnull
    public static Component fromLegacy(@Nonnull String value) {
        return LEGACY.deserialize(value);
    }

    @Nullable
    public static String getDisplayName(@Nonnull ItemMeta itemMeta) {
        Component name = itemMeta.displayName();
        return name == null ? null : LEGACY.serialize(name);
    }

    @Nullable
    public static List<String> getLore(@Nonnull ItemMeta itemMeta) {
        List<Component> lore = itemMeta.lore();
        if (lore == null) {
            return null;
        }

        List<String> legacyLore = new ArrayList<>(lore.size());
        for (Component line : lore) {
            legacyLore.add(LEGACY.serialize(line));
        }
        return legacyLore;
    }

    public static void setLore(@Nonnull ItemMeta itemMeta, @Nullable List<String> lore) {
        if (lore == null) {
            itemMeta.lore(null);
            return;
        }

        List<Component> componentLore = new ArrayList<>(lore.size());
        for (String line : lore) {
            componentLore.add(LEGACY.deserialize(line));
        }
        itemMeta.lore(componentLore);
    }

    @Nullable
    public static String stripColor(@Nullable String value) {
        return value == null ? null : LEGACY_FORMAT.matcher(value).replaceAll("");
    }
}
