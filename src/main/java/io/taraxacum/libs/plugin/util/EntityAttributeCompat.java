package io.taraxacum.libs.plugin.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;

/**
 * Modern attribute access with an isolated compatibility fallback.
 */
public final class EntityAttributeCompat {

    private EntityAttributeCompat() {
    }

    public static double getMaxHealth(@Nonnull LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        return maxHealth == null ? LegacyAccess.getMaxHealth(entity) : maxHealth.getValue();
    }

    @SuppressWarnings("deprecation")
    private static final class LegacyAccess {

        private LegacyAccess() {
        }

        private static double getMaxHealth(@Nonnull LivingEntity entity) {
            return entity.getMaxHealth();
        }
    }
}
