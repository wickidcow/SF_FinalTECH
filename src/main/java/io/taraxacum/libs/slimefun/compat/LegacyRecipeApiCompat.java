package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.implementation.items.tools.GoldPan;
import io.github.thebusybiscuit.slimefun4.implementation.items.tools.NetherGoldPan;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Compatibility helpers for recipe APIs where FinalTECH historically exposed one
 * representative ingredient rather than every accepted alternative.
 *
 * <p>The modern APIs are used wherever they can reproduce that behavior exactly.
 * A cached reflective fallback preserves Bukkit's representative-item contract for
 * choice implementations not available on the release compile baseline, without
 * linking FinalTECH bytecode directly to the deprecated RecipeChoice method.</p>
 */
public final class LegacyRecipeApiCompat {

    private static final Method REPRESENTATIVE_ITEM_METHOD = findRepresentativeItemMethod();

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

        return getReflectiveRepresentative(choice);
    }

    @Nullable
    private static ItemStack getReflectiveRepresentative(@Nonnull RecipeChoice choice) {
        Method method = REPRESENTATIVE_ITEM_METHOD;
        if (method == null) {
            return null;
        }

        try {
            Object representative = method.invoke(choice);
            return representative instanceof ItemStack itemStack ? itemStack.clone() : null;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not access RecipeChoice representative item", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("RecipeChoice representative lookup failed", cause);
        }
    }

    @Nullable
    private static Method findRepresentativeItemMethod() {
        try {
            return RecipeChoice.class.getMethod("getItemStack");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
