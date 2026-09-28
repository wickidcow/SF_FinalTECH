package io.taraxacum.libs.plugin.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Cross-version ItemStack material compatibility.
 *
 * <p>Modern Paper requires replacing the ItemStack when changing material type.
 * Older RC-37-era APIs do not expose {@code ItemStack#withType(Material)}, so
 * this helper resolves the modern replacement method reflectively and falls
 * back to the historical mutator only on runtimes where the replacement API is
 * unavailable. FinalTECH's active menu paths use {@link #withType(ItemStack, Material)}
 * and reinsert the returned stack.</p>
 */
public final class LegacyItemStackCompat {

    private static final Method WITH_TYPE = findMethod("withType");
    private static final Method SET_TYPE = findMethod("setType");

    private LegacyItemStackCompat() {
    }

    /**
     * Returns a stack with the requested material while preserving amount/meta.
     * The returned object may be a new stack and must replace the caller's old
     * inventory/menu reference.
     */
    @Nonnull
    public static ItemStack withType(@Nonnull ItemStack item, @Nonnull Material material) {
        if (WITH_TYPE != null) {
            Object replacement = invoke(WITH_TYPE, item, material);
            if (replacement instanceof ItemStack itemStack) {
                return itemStack;
            }
            throw new IllegalStateException("ItemStack#withType returned a non-ItemStack result");
        }

        ItemStack replacement = item.clone();
        setType(replacement, material);
        return replacement;
    }

    /**
     * Historical same-object mutation retained only for source/binary
     * compatibility with helper APIs that cannot return a replacement stack.
     * Active FinalTECH inventory paths should use {@link #withType(ItemStack, Material)}.
     */
    public static void setType(@Nonnull ItemStack item, @Nonnull Material material) {
        if (SET_TYPE == null) {
            throw new IllegalStateException("This server exposes neither ItemStack#withType nor ItemStack#setType");
        }
        invoke(SET_TYPE, item, material);
    }

    @Nonnull
    private static Object invoke(@Nonnull Method method, @Nonnull ItemStack item, @Nonnull Material material) {
        try {
            return method.invoke(item, material);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not access ItemStack material compatibility API", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("ItemStack material compatibility call failed", cause);
        }
    }

    private static Method findMethod(@Nonnull String name) {
        try {
            return ItemStack.class.getMethod(name, Material.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
