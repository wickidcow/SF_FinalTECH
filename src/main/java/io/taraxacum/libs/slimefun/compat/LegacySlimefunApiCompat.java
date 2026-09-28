package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Compatibility boundary for Slimefun APIs that changed after FinalTECH's
 * supported RC-37 floor.
 *
 * <p>Current Slimefun Legacy methods are discovered first and invoked without
 * exposing their newer parameter types in FinalTECH's always-loaded class
 * signatures. If a modern method is unavailable at runtime, the historical
 * RC-37 method is discovered by name instead. This keeps current servers off
 * deprecated APIs without introducing linkage failures on RC-37.</p>
 */
public final class LegacySlimefunApiCompat {

    private static final Object NO_METHOD = new Object();

    private LegacySlimefunApiCompat() {
    }

    public static int getResearchLevelCost(@Nonnull Research research) {
        Object value = tryInvoke(research, "getLevelCost");
        if (value == NO_METHOD) {
            value = invokeRequired(research, "getCost");
        }
        return ((Number) value).intValue();
    }

    public static void setResearchLevelCost(@Nonnull Research research, int cost) {
        if (tryInvoke(research, "setLevelCost", cost) == NO_METHOD) {
            invokeRequired(research, "setCost", cost);
        }
    }

    public static void addCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        if (tryInvoke(component, "addCharge", location, (long) charge) == NO_METHOD) {
            invokeRequired(component, "addCharge", location, charge);
        }
    }

    public static int getCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location) {
        Object value = tryInvoke(component, "getChargeLong", location);
        if (value == NO_METHOD) {
            value = invokeRequired(component, "getCharge", location);
        }
        return clampInt(((Number) value).longValue());
    }

    public static int getCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            @Nonnull Object legacyData) {
        Object modernData = LegacyBlockDataCompat.getModernDataContainer(location);
        if (modernData != null) {
            Object value = tryInvoke(component, "getChargeLong", location, modernData);
            if (value != NO_METHOD) {
                return clampInt(((Number) value).longValue());
            }
            value = tryInvoke(component, "getCharge", location, modernData);
            if (value != NO_METHOD) {
                return ((Number) value).intValue();
            }
        }

        return ((Number) invokeRequired(component, "getCharge", location, legacyData)).intValue();
    }

    public static int getGeneratedOutput(
            @Nonnull EnergyNetProvider provider,
            @Nonnull Location location,
            @Nonnull Object legacyData) {
        Object modernData = LegacyBlockDataCompat.getModernDataContainer(location);
        if (modernData != null) {
            Object value = tryInvoke(provider, "getGeneratedOutput", location, modernData);
            if (value != NO_METHOD) {
                return ((Number) value).intValue();
            }
            value = tryInvoke(provider, "getGeneratedOutputLong", location, modernData);
            if (value != NO_METHOD) {
                return clampInt(((Number) value).longValue());
            }
        }

        return ((Number) invokeRequired(provider, "getGeneratedOutput", location, legacyData)).intValue();
    }

    public static boolean willExplode(
            @Nonnull EnergyNetProvider provider,
            @Nonnull Location location,
            @Nonnull Object legacyData) {
        Object modernData = LegacyBlockDataCompat.getModernDataContainer(location);
        if (modernData != null) {
            Object value = tryInvoke(provider, "willExplode", location, modernData);
            if (value != NO_METHOD) {
                return Boolean.TRUE.equals(value);
            }
        }

        return Boolean.TRUE.equals(invokeRequired(provider, "willExplode", location, legacyData));
    }

    public static void setCharge(
            @Nonnull EnergyNetComponent component,
            @Nonnull Location location,
            int charge) {
        if (tryInvoke(component, "setCharge", location, (long) charge) == NO_METHOD) {
            invokeRequired(component, "setCharge", location, charge);
        }
    }

    private static int clampInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    @Nonnull
    private static Object invokeRequired(
            @Nonnull Object target,
            @Nonnull String methodName,
            @Nonnull Object... arguments) {
        Object value = tryInvoke(target, methodName, arguments);
        if (value == NO_METHOD) {
            throw new IllegalStateException(
                    "No compatible Slimefun method found: "
                            + target.getClass().getName()
                            + '#'
                            + methodName);
        }
        return value;
    }

    @Nonnull
    private static Object tryInvoke(
            @Nonnull Object target,
            @Nonnull String methodName,
            @Nonnull Object... arguments) {
        Method method = findCompatibleMethod(target.getClass(), methodName, arguments);
        if (method == null) {
            return NO_METHOD;
        }

        try {
            Object value = method.invoke(target, arguments);
            return method.getReturnType() == void.class ? Boolean.TRUE : value;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Could not access Slimefun compatibility method: " + method,
                    exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Slimefun compatibility method failed: " + method, cause);
        }
    }

    @Nullable
    private static Method findCompatibleMethod(
            @Nonnull Class<?> type,
            @Nonnull String methodName,
            @Nonnull Object[] arguments) {
        Method best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName)
                    || method.getParameterCount() != arguments.length) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            int score = method.isAnnotationPresent(Deprecated.class) ? 1000 : 0;
            boolean compatible = true;

            for (int i = 0; i < parameterTypes.length; i++) {
                int parameterScore = compatibilityScore(parameterTypes[i], arguments[i]);
                if (parameterScore < 0) {
                    compatible = false;
                    break;
                }
                score += parameterScore;
            }

            if (compatible && score < bestScore) {
                best = method;
                bestScore = score;
            }
        }

        return best;
    }

    private static int compatibilityScore(@Nonnull Class<?> parameterType, @Nullable Object argument) {
        if (argument == null) {
            return parameterType.isPrimitive() ? -1 : 10;
        }

        Class<?> argumentType = argument.getClass();
        if (parameterType.isPrimitive()) {
            Class<?> wrapper = wrapperType(parameterType);
            return wrapper == argumentType ? 0 : -1;
        }

        if (parameterType == argumentType) {
            return 0;
        }
        if (parameterType.isAssignableFrom(argumentType)) {
            return 1;
        }
        return -1;
    }

    @Nonnull
    private static Class<?> wrapperType(@Nonnull Class<?> primitive) {
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return primitive;
    }
}
