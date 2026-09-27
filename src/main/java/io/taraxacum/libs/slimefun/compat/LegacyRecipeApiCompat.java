package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.implementation.items.tools.GoldPan;
import io.github.thebusybiscuit.slimefun4.implementation.items.tools.NetherGoldPan;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Compatibility helpers for recipe APIs where FinalTECH historically exposed one
 * representative ingredient rather than every accepted alternative.
 *
 * <p>The modern APIs are used wherever they can reproduce that behavior exactly.
 * A narrow deprecated RecipeChoice fallback remains only for future/unknown choice
 * implementations that do not expose a modern representative-item accessor.</p>
 */
public final class LegacyRecipeApiCompat {

    private LegacyRecipeApiCompat() {
    }

    /**
     * Preserves the historical GoldPan#getInputMaterial() representative:
     * GRAVEL for the normal pan and SOUL_SAND for the Nether pan.
     */
    @Nonnull
    public static Material getPrimaryGoldPanInput(@Nonnull GoldPan goldPan) {
        Set<Material> inputs = goldPan.getInputMaterials();

        if (goldPan instanceof NetherGoldPan && inputs.contains(Material.SOUL_SAND)) {
            return Material.SOUL_SAND;
        }
        if (inputs.contains(Material.GRAVEL)) {
            return Material.GRAVEL;
        }
        if (inputs.isEmpty()) {
            throw new IllegalStateException("GoldPan exposes no input materials: " + goldPan.getId());
        }
        return inputs.iterator().next();
    }

    /**
     * Returns the same single representative ItemStack shape used by Bukkit's
     * legacy ingredient-map/list APIs.
     */
    @Nullable
    public static ItemStack getRepresentativeIngredient(@Nullable RecipeChoice choice) {
        if (choice == null) {
            return null;
        }

        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            List<Material> choices = materialChoice.getChoices();
            return choices.isEmpty() ? null : new ItemStack(choices.get(0));
        }

        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            List<ItemStack> choices = exactChoice.getChoices();
            return choices.isEmpty() ? null : choices.get(0).clone();
        }

        return getLegacyRepresentative(choice);
    }

    /**
     * Paper's generic RecipeChoice API still has no non-deprecated method for
     * obtaining a representative stack from every possible choice type.
     */
    @Nullable
    @SuppressWarnings("deprecation")
    private static ItemStack getLegacyRepresentative(@Nonnull RecipeChoice choice) {
        ItemStack representative = choice.getItemStack();
        return representative == null ? null : representative.clone();
    }
}
