package io.taraxacum.libs.plugin.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;

/**
 * Modern attribute access for living-entity maximum health.
 */
public final class EntityAttributeCompat {

    private EntityAttributeCompat() {
    }

    public static double getMaxHealth(@Nonnull LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) {
            throw new IllegalStateException(
                    "Living entity " + entity.getType() + " does not expose Attribute.MAX_HEALTH"
            );
        }
        return maxHealth.getValue();
    }
}
