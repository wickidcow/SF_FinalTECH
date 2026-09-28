package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

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

    public static boolean hasBlockData(@Nonnull Location location) {
        if (MODERN != null) {
            return MODERN.hasBlockData(location);
        }
        return LegacyAccess.hasBlockData(location);
    }

    public static boolean hasMenu(@Nonnull Location location) {
        if (MODERN != null) {
            return MODERN.hasMenu(location);
        }
        return LegacyAccess.hasMenu(location);
    }

    /**
     * Returns the current Slimefun block-data container without exposing its
     * modern storage type in FinalTECH's always-loaded API surface.
     *
     * <p>RC-37 has no equivalent container type, so this returns {@code null}
     * there and callers must use their historical fallback.</p>
     */
    @Nullable
    public static Object getModernDataContainer(@Nonnull Location location) {
        if (MODERN == null) {
            return null;
        }
        return MODERN.getDataContainer(location);
    }

    /**
     * Exposes the historical per-world BlockStorage inventory map used only by
     * FinalTECH's RC-37 data-loss recovery path.
     *
     * <p>Current Slimefun Legacy no longer owns this registry and returns
     * {@code null} from {@code BlockStorage.getStorage(world)}, so modern
     * servers naturally skip this legacy-only recovery scan.</p>
     */
    @Nullable
    public static Map<Location, BlockMenu> getLegacyWorldInventories(@Nonnull World world) {
        Object storage = LegacyAccess.getStorage(world);
        return storage == null ? null : LegacyAccess.getInventories(storage);
    }

    /**
     * Creates/recreates the Slimefun identity record at this location.
     *
     * <p>The special Slimefun id is deliberately not exposed through
     * {@link #setValue(Location, String, String)} because it is identity, not
     * ordinary persisted key/value data.</p>
     */
    public static void setSlimefunId(@Nonnull Location location, @Nonnull String slimefunId) {
        if (MODERN != null) {
            MODERN.createBlock(location, slimefunId);
            return;
        }
        LegacyAccess.setSlimefunId(location, slimefunId);
    }

    /**
     * Removes the complete Slimefun block-data record at this location.
     *
     * <p>This preserves the historical {@code BlockStorage.clearBlockInfo}
     * semantics while using the current block-data controller when available.</p>
     */
    public static void removeBlock(@Nonnull Location location) {
        if (MODERN != null) {
            MODERN.removeBlock(location);
            return;
        }
        LegacyAccess.removeBlock(location);
    }

    /**
     * Invokes the explicit storage flush hooks used by older Slimefun releases
     * when those hooks exist. Current Slimefun Legacy persists through its
     * storage controller and therefore requires no explicit flush.
     */
    public static void flushLegacyStorage() {
        try {
            Class<?> blockStorageClass = Class.forName("me.mrCookieSlime.Slimefun.api.BlockStorage");

            try {
                blockStorageClass.getMethod("saveChunks").invoke(null);
            } catch (NoSuchMethodException ignored) {
                // Modern storage controller: no explicit global flush hook.
            }

            for (World world : Bukkit.getWorlds()) {
                Object storage = blockStorageClass.getMethod("getStorage", World.class).invoke(null, world);
                if (storage == null) {
                    continue;
                }

                try {
                    storage.getClass().getMethod("save").invoke(storage);
                } catch (NoSuchMethodException ignored) {
                    // Modern storage controller: persistence is managed centrally.
                }
            }

            try {
                blockStorageClass.getMethod("saveChunks").invoke(null);
            } catch (NoSuchMethodException ignored) {
                // Modern storage controller: no explicit global flush hook.
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            throw new IllegalStateException("Could not invoke legacy storage save hooks", exception);
        }
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
            Method createBlock = controllerType.getMethod("createBlock", Location.class, String.class);
            Method removeBlock = controllerType.getMethod("removeBlock", Location.class);

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
                    getBlockMenu,
                    createBlock,
                    removeBlock);
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
            Method getBlockMenu,
            Method createBlock,
            Method removeBlock) {

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

        private boolean hasBlockData(@Nonnull Location location) {
            try {
                return getLoadedData(location) != null;
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block-data existence API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("check block data existence", exception);
            }
        }

        private boolean hasMenu(@Nonnull Location location) {
            Object menu = getMenu(location);
            return menu != NO_RECORD_OBJECT && menu != null;
        }

        @Nullable
        private Object getDataContainer(@Nonnull Location location) {
            try {
                return getLoadedData(location);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block-data container API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("read data container", exception);
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

        private void createBlock(@Nonnull Location location, @Nonnull String slimefunId) {
            try {
                Object databaseManager = getDatabaseManager.invoke(null);
                Object controller = getBlockDataController.invoke(databaseManager);
                createBlock.invoke(controller, location, slimefunId);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block identity API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("create block", exception);
            }
        }

        private void removeBlock(@Nonnull Location location) {
            try {
                Object databaseManager = getDatabaseManager.invoke(null);
                Object controller = getBlockDataController.invoke(databaseManager);
                removeBlock.invoke(controller, location);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access Slimefun Legacy block-data removal API", exception);
            } catch (InvocationTargetException exception) {
                throw unwrap("remove block", exception);
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
     *
     * <p>Historical BlockStorage calls are resolved reflectively so current
     * builds do not compile against the deprecated facade, while RC-37 keeps
     * the same method names, arguments, and persisted values.</p>
     */
    private static final class LegacyAccess {

        private static final Class<?> BLOCK_STORAGE = loadBlockStorage();
        private static final Method GET_VALUE =
                method("getLocationInfo", Location.class, String.class);
        private static final Method SET_VALUE =
                method("addBlockInfo", Location.class, String.class, String.class);
        private static final Method GET_MENU =
                method("getInventory", Location.class);
        private static final Method HAS_BLOCK_DATA =
                method("hasBlockInfo", Location.class);
        private static final Method HAS_MENU =
                method("hasInventory", Block.class);
        private static final Method SET_ID =
                method("addBlockInfo", Location.class, String.class, String.class, boolean.class);
        private static final Method REMOVE_BLOCK =
                method("clearBlockInfo", Location.class);
        private static final Method GET_STORAGE =
                method("getStorage", World.class);

        private LegacyAccess() {
        }

        private static Class<?> loadBlockStorage() {
            try {
                return Class.forName("me.mrCookieSlime.Slimefun.api.BlockStorage");
            } catch (ClassNotFoundException | LinkageError exception) {
                throw new IllegalStateException("Could not load RC-37 BlockStorage compatibility API", exception);
            }
        }

        private static Method method(@Nonnull String name, @Nonnull Class<?>... parameterTypes) {
            try {
                return BLOCK_STORAGE.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException exception) {
                throw new IllegalStateException(
                        "RC-37 BlockStorage compatibility method is unavailable: " + name,
                        exception);
            }
        }

        @Nullable
        private static Object invoke(@Nonnull Method method, @Nonnull Object... arguments) {
            try {
                return method.invoke(null, arguments);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Could not access RC-37 BlockStorage compatibility API", exception);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw new IllegalStateException("RC-37 BlockStorage compatibility call failed", cause);
            }
        }

        @Nullable
        private static String getValue(@Nonnull Location location, @Nonnull String key) {
            return (String) invoke(GET_VALUE, location, key);
        }

        @Nullable
        private static String getSlimefunId(@Nonnull Location location) {
            return getValue(location, "id");
        }

        private static void setValue(
                @Nonnull Location location,
                @Nonnull String key,
                @Nullable String value) {
            invoke(SET_VALUE, location, key, value);
        }

        @Nullable
        private static BlockMenu getMenu(@Nonnull Location location) {
            return (BlockMenu) invoke(GET_MENU, location);
        }

        private static boolean hasBlockData(@Nonnull Location location) {
            return Boolean.TRUE.equals(invoke(HAS_BLOCK_DATA, location));
        }

        private static boolean hasMenu(@Nonnull Location location) {
            return Boolean.TRUE.equals(invoke(HAS_MENU, location.getBlock()));
        }

        private static void setSlimefunId(@Nonnull Location location, @Nonnull String slimefunId) {
            invoke(SET_ID, location, "id", slimefunId, true);
        }

        private static void removeBlock(@Nonnull Location location) {
            invoke(REMOVE_BLOCK, location);
        }

        @Nullable
        private static Object getStorage(@Nonnull World world) {
            return invoke(GET_STORAGE, world);
        }

        @SuppressWarnings("unchecked")
        private static Map<Location, BlockMenu> getInventories(@Nonnull Object storage) {
            try {
                Field inventories = BLOCK_STORAGE.getDeclaredField("inventories");
                inventories.setAccessible(true);
                return (Map<Location, BlockMenu>) inventories.get(storage);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not access legacy BlockStorage inventory registry", exception);
            }
        }
    }

}
