package io.taraxacum.libs.plugin.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Compatibility boundary for Paper's deprecated in-place ItemStack type mutation.
 *
 * <p>Several FinalTECH helper APIs receive a caller-owned ItemStack and are expected
 * to mutate that same object before continuing with shared icon/lore handling.
 * Replacing the stack instance would change that contract, so the historical
 * mutation is intentionally isolated here until those helper APIs can be redesigned.</p>
 */
public final class LegacyItemStackCompat {

    private LegacyItemStackCompat() {
    }

    @SuppressWarnings("deprecation")
    public static void setType(@Nonnull ItemStack item, @Nonnull Material material) {
        item.setType(material);
    }
}
