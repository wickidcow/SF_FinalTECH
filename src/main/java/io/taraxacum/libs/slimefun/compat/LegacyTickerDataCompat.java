package io.taraxacum.libs.slimefun.compat;

import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Storage-neutral access boundary for ticker and persisted block data.
 *
 * <p>Current Slimefun Legacy uses its block-data container directly. RC-37
 * supplies the historical Config object instead. Both representations remain
 * opaque to normal FinalTECH code, and reflective method discovery is cached
 * once per runtime data class to keep ticker access inexpensive.</p>
 */
public final class LegacyTickerDataCompat {

    private static final ClassValue<DataAccess> ACCESS = new ClassValue<>() {
        @Override
        protected DataAccess computeValue(@Nonnull Class<?> type) {
            return DataAccess.create(type);
        }
    };

    private LegacyTickerDataCompat() {
    }

    /**
     * Returns the best available data object for a block location.
     */
    @Nonnull
    public static Object getData(@Nonnull Location location) {
        Object modern = LegacyBlockDataCompat.getModernDataContainer(location);
        return modern != null ? modern : LegacyBlockDataCompat.getLegacyDataView(location);
    }

    public static boolean contains(@Nonnull Object data, @Nonnull String key) {
        DataAccess access = ACCESS.get(data.getClass());
        if (access.getData() != null) {
            return invoke(access.getData(), data, key) != null;
        }
        return Boolean.TRUE.equals(invokeRequired(access.contains(), data, "contains", key));
    }

    @Nullable
    public static String getString(@Nonnull Object data, @Nonnull String key) {
        DataAccess access = ACCESS.get(data.getClass());
        if (access.getData() != null) {
            return (String) invoke(access.getData(), data, key);
        }
        return (String) invokeRequired(access.getString(), data, "getString", key);
    }

    public static void setValue(
            @Nonnull Object data,
            @Nonnull String key,
            @Nullable String value) {
        DataAccess access = ACCESS.get(data.getClass());
        if (access.setData() != null && access.removeData() != null) {
            if (value == null) {
                invoke(access.removeData(), data, key);
            } else {
                invoke(access.setData(), data, key, value);
            }
            return;
        }

        invokeRequired(access.setValue(), data, "setValue", key, value);
    }

    @Nonnull
    public static Set<String> getKeys(@Nonnull Object data) {
        DataAccess access = ACCESS.get(data.getClass());

        if (access.getAllData() != null) {
            Object allData = invoke(access.getAllData(), data);
            if (!(allData instanceof Map<?, ?> map)) {
                throw new IllegalStateException(
                        "Slimefun getAllData() returned an unexpected type: "
                                + typeName(allData));
            }

            Set<String> keys = new LinkedHashSet<>();
            for (Object key : map.keySet()) {
                if (!(key instanceof String stringKey)) {
                    throw new IllegalStateException(
                            "Slimefun block-data key is not a String: " + key);
                }
                keys.add(stringKey);
            }
            return keys;
        }

        Object legacyKeys = invokeRequired(access.getKeys(), data, "getKeys");
        if (!(legacyKeys instanceof Set<?> set)) {
            throw new IllegalStateException(
                    "RC-37 Config.getKeys() returned an unexpected type: "
                            + typeName(legacyKeys));
        }

        Set<String> keys = new LinkedHashSet<>();
        for (Object key : set) {
            if (!(key instanceof String stringKey)) {
                throw new IllegalStateException("RC-37 Config key is not a String: " + key);
            }
            keys.add(stringKey);
        }
        return keys;
    }

    @Nonnull
    private static String typeName(@Nullable Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    @Nullable
    private static Object invoke(
            @Nonnull Method method,
            @Nonnull Object target,
            @Nonnull Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Could not access ticker-data compatibility method: " + method,
                    exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Ticker-data compatibility method failed: " + method, cause);
        }
    }

    @Nullable
    private static Object invokeRequired(
            @Nullable Method method,
            @Nonnull Object target,
            @Nonnull String methodName,
            @Nonnull Object... arguments) {
        if (method == null) {
            throw new IllegalStateException(
                    "No compatible ticker-data method found: "
                            + target.getClass().getName()
                            + '#'
                            + methodName);
        }
        return invoke(method, target, arguments);
    }

    private record DataAccess(
            Method getData,
            Method setData,
            Method removeData,
            Method getAllData,
            Method contains,
            Method getString,
            Method setValue,
            Method getKeys) {

        @Nonnull
        private static DataAccess create(@Nonnull Class<?> type) {
            Method getData = method(type, "getData", String.class);
            Method setData = method(type, "setData", String.class, String.class);
            Method removeData = method(type, "removeData", String.class);
            Method getAllData = method(type, "getAllData");

            Method contains = method(type, "contains", String.class);
            Method getString = method(type, "getString", String.class);
            Method setValue = method(type, "setValue", String.class, Object.class);
            Method getKeys = method(type, "getKeys");

            boolean modern = getData != null && setData != null && removeData != null;
            boolean legacy = contains != null && getString != null && setValue != null;

            if (!modern && !legacy) {
                throw new IllegalStateException(
                        "Unsupported Slimefun ticker-data implementation: " + type.getName());
            }

            return new DataAccess(
                    getData,
                    setData,
                    removeData,
                    getAllData,
                    contains,
                    getString,
                    setValue,
                    getKeys);
        }

        @Nullable
        private static Method method(
                @Nonnull Class<?> type,
                @Nonnull String name,
                @Nonnull Class<?>... parameterTypes) {
            try {
                return type.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                return null;
            }
        }
    }
}
