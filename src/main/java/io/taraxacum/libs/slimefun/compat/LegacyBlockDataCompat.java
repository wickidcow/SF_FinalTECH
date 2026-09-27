package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Compatibility boundary for FinalTECH's persisted Slimefun block-data access.
 *
 * <p>Current Slimefun Legacy uses BlockDataController. FinalTECH still supports
 * the upstream RC-37 API floor, which predates that controller. Reads and writes
 * therefore prefer the modern controller when a block-data record exists and
 * deliberately fall back to the historical BlockStorage facade otherwise.
 * Persisted key names and string values are never translated or rewritten.</p>
 */
public final class LegacyBlockDataCompat {

    private static final ModernAccess MODERN = createModernAccess();

    private LegacyBlockDataCompat() {
    }

    @Nullable
    public static String getValue(@Nonnull Location location, @Nonnull String key) {
        if (MODERN != null) {
            String value = MODERN.getValue(location, key);
            if (value != ModernAccess.NO_RECORD) {
                return value;
            }
        }
        return LegacyAccess.getValue(location, key);
    }

    @Nullable
    public static String getSlimefunId(@Nonnull Location location) {
        if (MODERN != null) {
            String id = MODERN.getSlimefunId(location);
            if (id != ModernAccess.NO_RECORD) {
                return id;
            }
        }
        return LegacyAccess.getSlimefunId(location);
    }

    public static void setValue(
            @Nonnull Location location,
            @Nonnull String key,
            @Nullable String value) {
        if (MODERN != null && MODERN.setValue(location, key, value)) {
            return;
        }
        LegacyAccess.setValue(location, key, value);
    }

    @Nullable
    public static BlockMenu getMenu(@Nonnull Location location) {
        if (MODERN != null) {
            Object menu = MODERN.getMenu(location);
            if (menu != ModernAccess.NO_RECORD_OBJECT) {
                return (BlockMenu) menu;
            }
        }
        return LegacyAccess.getMenu(location);
    }

    @Nullable
    private static ModernAccess createModernAccess() {
        try {
            Method getDatabaseManager = Slimefun.class.getMethod("getDatabaseManager");
            Method getBlockDataController =
                    getDatabaseManager.getReturnType().getMethod("getBlockDataController");
            Class<?> controllerType = getBlockDataController.getReturnType();
            Method getBlockData = controllerType.getMethod("getBlockData", Location.class);
            Class<?> blockDataType = getBlockData.getReturnType();
            Method isDataLoaded = blockDataType.getMethod("isDataLoaded");
            Method loadBlockData = controllerType.getMethod("loadBlockData", blockDataType);
            Method getData = blockDataType.getMethod("getData", String.class);
            Method getSfId = blockDataType.getMethod("getSfId");
            Method setData = blockDataType.getMethod("setData", String.class, String.class);
            Method removeData = blockDataType.getMethod("removeData", String.class);
            Method getBlockMenu = blockDataType.getMethod("getBlockMenu");

            return new ModernAccess(
                    getDatabaseManager,
                    getBlockDataController,
                    getBlockData,
                    isDataLoaded,
                    loadBlockData,
                    getData,
                    getSfId,
                    setData,
                    removeData,
                    getBlockMenu);
        } catch (NoSuchMethodException | LinkageError ignored) {
            return null;
        }
    }

    private record ModernAccess(
            Method getDatabaseManager,
            Method getBlockDataController,
            Method getBlockData,
            Method isDataLoaded,
            Method loadBlockData,
            Method getData,
            Method getSfId,
            Method setData,
            Method removeData,
            Method getBlockMenu) {

        private static final String NO_RECORD = new String("FINALTECH_NO_BLOCK_DATA_RECORD");
        private static final Object NO_RECORD_OBJECT = new Object();

        private Object getLoadedData(@Nonnull Location location)
                throws IllegalAccessException, InvocationTargetException {
            Object databaseManager = getDatabaseManager.invoke(null);
            Object controller = getBlockDataController.invoke(databaseManager);
            Object blockData = getBlockData.invoke(controller, location);
            if (blockData != null && !Boolean.TRUE.equals(isDataLoaded.invoke(blockData))) {
                loadBlockData.invoke(controller, blockData);
            }
            return blockData;
        }

        @Nullable
        private String getValue(@Nonnull Location location, @Nonnull String key) {
            try {
                Object blockData = getLoadedData(location);
                if (blockData == null) {
                    return NO_RECORD;
                }
                return (String) getData.invoke(blockData, key);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block-data API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("read", exception);
            }
        }

        @Nullable
        private String getSlimefunId(@Nonnull Location location) {
            try {
                Object blockData = getLoadedData(location);
                if (blockData == null) {
                    return NO_RECORD;
                }
                return (String) getSfId.invoke(blockData);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block identity API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("read identity", exception);
            }
        }

        @Nullable
        private Object getMenu(@Nonnull Location location) {
            try {
                Object blockData = getLoadedData(location);
                if (blockData == null) {
                    return NO_RECORD_OBJECT;
                }
                return getBlockMenu.invoke(blockData);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block-menu API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("read menu", exception);
            }
        }

        private boolean setValue(
                @Nonnull Location location,
                @Nonnull String key,
                @Nullable String value) {
            try {
                Object blockData = getLoadedData(location);
                if (blockData == null) {
                    return false;
                }
                if (value == null) {
                    removeData.invoke(blockData, key);
                } else {
                    setData.invoke(blockData, key, value);
                }
                return true;
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block-data API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("write", exception);
            }
        }

        private static RuntimeException unwrap(String operation, InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                return runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            return new IllegalStateException("Slimefun Legacy block-data " + operation + " failed", cause);
        }
    }

    /**
     * Intentional RC-37 and no-modern-record compatibility boundary.
     */
    @SuppressWarnings("deprecation")
    private static final class LegacyAccess {

        private LegacyAccess() {
        }

        @Nullable
        private static String getValue(@Nonnull Location location, @Nonnull String key) {
            return me.mrCookieSlime.Slimefun.api.BlockStorage.getLocationInfo(location, key);
        }

        @Nullable
        private static String getSlimefunId(@Nonnull Location location) {
            return me.mrCookieSlime.Slimefun.api.BlockStorage.getLocationInfo(location, "id");
        }

        private static void setValue(
                @Nonnull Location location,
                @Nonnull String key,
                @Nullable String value) {
            me.mrCookieSlime.Slimefun.api.BlockStorage.addBlockInfo(location, key, value);
        }

        @Nullable
        private static BlockMenu getMenu(@Nonnull Location location) {
            return me.mrCookieSlime.Slimefun.api.BlockStorage.getInventory(location);
        }
    }
}
