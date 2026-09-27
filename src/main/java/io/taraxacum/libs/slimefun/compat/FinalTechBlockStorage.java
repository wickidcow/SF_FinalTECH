package io.taraxacum.libs.slimefun.compat;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
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
 * Storage compatibility boundary for FinalTECH.
 *
 * <p>Current Slimefun Legacy stores block data through BlockDataController, while
 * FinalTECH also keeps an RC-37 compatibility floor where only BlockStorage exists.
 * This facade preserves FinalTECH's historical storage semantics without exposing
 * deprecated storage APIs throughout machine code.</p>
 */
public final class FinalTechBlockStorage {

    private static final Accessor ACCESSOR = createAccessor();

    private FinalTechBlockStorage() {
    }

    @Nullable
    public static String getLocationInfo(@Nonnull Location location, @Nonnull String key) {
        return ACCESSOR.getLocationInfo(location, key);
    }

    public static void addBlockInfo(@Nonnull Location location, @Nonnull String key, @Nullable String value) {
        ACCESSOR.addBlockInfo(location, key, value, false);
    }

    public static void addBlockInfo(
            @Nonnull Location location,
            @Nonnull String key,
            @Nullable String value,
            boolean updateTicker) {
        ACCESSOR.addBlockInfo(location, key, value, updateTicker);
    }

    public static void addBlockInfo(@Nonnull Block block, @Nonnull String key, @Nullable String value) {
        addBlockInfo(block.getLocation(), key, value);
    }

    public static void addBlockInfo(
            @Nonnull Block block,
            @Nonnull String key,
            @Nullable String value,
            boolean updateTicker) {
        addBlockInfo(block.getLocation(), key, value, updateTicker);
    }

    @Nullable
    public static BlockMenu getInventory(@Nonnull Block block) {
        return ACCESSOR.getInventory(block.getLocation());
    }

    @Nullable
    public static BlockMenu getInventory(@Nonnull Location location) {
        return ACCESSOR.getInventory(location);
    }

    public static boolean hasInventory(@Nonnull Block block) {
        return ACCESSOR.hasInventory(block.getLocation());
    }

    public static boolean hasInventory(@Nonnull Location location) {
        return ACCESSOR.hasInventory(location);
    }

    public static boolean hasBlockInfo(@Nonnull Block block) {
        return ACCESSOR.hasBlockInfo(block.getLocation());
    }

    public static boolean hasBlockInfo(@Nonnull Location location) {
        return ACCESSOR.hasBlockInfo(location);
    }

    public static void clearBlockInfo(@Nonnull Block block) {
        ACCESSOR.clearBlockInfo(block.getLocation(), true);
    }

    public static void clearBlockInfo(@Nonnull Location location) {
        ACCESSOR.clearBlockInfo(location, true);
    }

    public static void clearBlockInfo(@Nonnull Block block, boolean destroy) {
        ACCESSOR.clearBlockInfo(block.getLocation(), destroy);
    }

    public static void clearBlockInfo(@Nonnull Location location, boolean destroy) {
        ACCESSOR.clearBlockInfo(location, destroy);
    }

    public static void deleteLocationInfoUnsafely(@Nonnull Location location, boolean destroy) {
        ACCESSOR.deleteLocationInfoUnsafely(location, destroy);
    }

    /**
     * Returns the RC-37 in-memory inventory map used by FinalTECH's historical
     * data-loss repair pass. Current Slimefun Legacy deliberately returns null,
     * matching its BlockStorage#getStorage compatibility behavior.
     */
    @Nullable
    public static Map<Location, BlockMenu> getLegacyStoredInventories(@Nonnull World world) {
        return ACCESSOR.getLegacyStoredInventories(world);
    }

    @Nonnull
    private static Accessor createAccessor() {
        try {
            Method getDatabaseManager = Slimefun.class.getMethod("getDatabaseManager");
            Method getBlockDataController =
                    getDatabaseManager.getReturnType().getMethod("getBlockDataController");
            Class<?> controllerType = getBlockDataController.getReturnType();
            Method getBlockData = controllerType.getMethod("getBlockData", Location.class);
            Class<?> blockDataType = getBlockData.getReturnType();
            Method isDataLoaded = blockDataType.getMethod("isDataLoaded");
            Method loadBlockData = controllerType.getMethod("loadBlockData", blockDataType);
            Method getSfId = blockDataType.getMethod("getSfId");
            Method getData = blockDataType.getMethod("getData", String.class);
            Method setData = blockDataType.getMethod("setData", String.class, String.class);
            Method removeData = blockDataType.getMethod("removeData", String.class);
            Method getBlockMenu = blockDataType.getMethod("getBlockMenu");
            Method createBlock = controllerType.getMethod("createBlock", Location.class, String.class);
            Method removeBlock = controllerType.getMethod("removeBlock", Location.class);

            return new ModernAccessor(
                    getDatabaseManager, getBlockDataController, getBlockData, isDataLoaded,
                    loadBlockData, getSfId, getData, setData, removeData, getBlockMenu,
                    createBlock, removeBlock);
        } catch (NoSuchMethodException | LinkageError ignored) {
            return new LegacyAccessor();
        }
    }

    private interface Accessor {
        @Nullable String getLocationInfo(Location location, String key);
        void addBlockInfo(Location location, String key, @Nullable String value, boolean updateTicker);
        @Nullable BlockMenu getInventory(Location location);
        boolean hasInventory(Location location);
        boolean hasBlockInfo(Location location);
        void clearBlockInfo(Location location, boolean destroy);
        void deleteLocationInfoUnsafely(Location location, boolean destroy);
        @Nullable Map<Location, BlockMenu> getLegacyStoredInventories(World world);
    }

    private static final class ModernAccessor implements Accessor {

        private final Method getDatabaseManager;
        private final Method getBlockDataController;
        private final Method getBlockData;
        private final Method isDataLoaded;
        private final Method loadBlockData;
        private final Method getSfId;
        private final Method getData;
        private final Method setData;
        private final Method removeData;
        private final Method getBlockMenu;
        private final Method createBlock;
        private final Method removeBlock;

        private ModernAccessor(
                Method getDatabaseManager,
                Method getBlockDataController,
                Method getBlockData,
                Method isDataLoaded,
                Method loadBlockData,
                Method getSfId,
                Method getData,
                Method setData,
                Method removeData,
                Method getBlockMenu,
                Method createBlock,
                Method removeBlock) {
            this.getDatabaseManager = getDatabaseManager;
            this.getBlockDataController = getBlockDataController;
            this.getBlockData = getBlockData;
            this.isDataLoaded = isDataLoaded;
            this.loadBlockData = loadBlockData;
            this.getSfId = getSfId;
            this.getData = getData;
            this.setData = setData;
            this.removeData = removeData;
            this.getBlockMenu = getBlockMenu;
            this.createBlock = createBlock;
            this.removeBlock = removeBlock;
        }

        private Object controller() {
            try {
                Object databaseManager = getDatabaseManager.invoke(null);
                return getBlockDataController.invoke(databaseManager);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw unwrap("Could not access Slimefun Legacy block-data controller", exception);
            }
        }

        @Nullable
        private Object loadedBlockData(Location location) {
            try {
                Object controller = controller();
                Object blockData = getBlockData.invoke(controller, location);
                if (blockData != null && !Boolean.TRUE.equals(isDataLoaded.invoke(blockData))) {
                    loadBlockData.invoke(controller, blockData);
                }
                return blockData;
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw unwrap("Could not load Slimefun Legacy block data", exception);
            }
        }

        @Override
        public String getLocationInfo(Location location, String key) {
            Object blockData = loadedBlockData(location);
            if (blockData == null) {
                return null;
            }
            try {
                return "id".equals(key)
                        ? (String) getSfId.invoke(blockData)
                        : (String) getData.invoke(blockData, key);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw unwrap("Could not read Slimefun Legacy block data", exception);
            }
        }

        @Override
        public void addBlockInfo(Location location, String key, String value, boolean updateTicker) {
            try {
                Object controller = controller();
                if ("id".equals(key)) {
                    if (value != null) {
                        createBlock.invoke(controller, location, value);
                    }
                    return;
                }

                Object blockData = loadedBlockData(location);
                if (blockData == null) {
                    return;
                }

                if (value == null) {
                    removeData.invoke(blockData, key);
                } else {
                    setData.invoke(blockData, key, value);
                }
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw unwrap("Could not update Slimefun Legacy block data", exception);
            }
        }

        @Override
        public BlockMenu getInventory(Location location) {
            Object blockData = loadedBlockData(location);
            if (blockData == null) {
                return null;
            }
            try {
                return (BlockMenu) getBlockMenu.invoke(blockData);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw unwrap("Could not access Slimefun Legacy block inventory", exception);
            }
        }

        @Override
        public boolean hasInventory(Location location) {
            return getInventory(location) != null;
        }

        @Override
        public boolean hasBlockInfo(Location location) {
            return loadedBlockData(location) != null;
        }

        @Override
        public void clearBlockInfo(Location location, boolean destroy) {
            remove(location);
        }

        @Override
        public void deleteLocationInfoUnsafely(Location location, boolean destroy) {
            remove(location);
        }

        private void remove(Location location) {
            try {
                removeBlock.invoke(controller(), location);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw unwrap("Could not remove Slimefun Legacy block data", exception);
            }
        }

        @Override
        public Map<Location, BlockMenu> getLegacyStoredInventories(World world) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class LegacyAccessor implements Accessor {

        @Override
        public String getLocationInfo(Location location, String key) {
            return me.mrCookieSlime.Slimefun.api.BlockStorage.getLocationInfo(location, key);
        }

        @Override
        public void addBlockInfo(Location location, String key, String value, boolean updateTicker) {
            me.mrCookieSlime.Slimefun.api.BlockStorage.addBlockInfo(location, key, value, updateTicker);
        }

        @Override
        public BlockMenu getInventory(Location location) {
            return me.mrCookieSlime.Slimefun.api.BlockStorage.getInventory(location);
        }

        @Override
        public boolean hasInventory(Location location) {
            me.mrCookieSlime.Slimefun.api.BlockStorage storage =
                    me.mrCookieSlime.Slimefun.api.BlockStorage.getStorage(location.getWorld());
            return storage != null && storage.hasInventory(location);
        }

        @Override
        public boolean hasBlockInfo(Location location) {
            return me.mrCookieSlime.Slimefun.api.BlockStorage.hasBlockInfo(location);
        }

        @Override
        public void clearBlockInfo(Location location, boolean destroy) {
            me.mrCookieSlime.Slimefun.api.BlockStorage.clearBlockInfo(location, destroy);
        }

        @Override
        public void deleteLocationInfoUnsafely(Location location, boolean destroy) {
            me.mrCookieSlime.Slimefun.api.BlockStorage.deleteLocationInfoUnsafely(location, destroy);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<Location, BlockMenu> getLegacyStoredInventories(World world) {
            me.mrCookieSlime.Slimefun.api.BlockStorage storage =
                    me.mrCookieSlime.Slimefun.api.BlockStorage.getStorage(world);
            if (storage == null) {
                return null;
            }

            try {
                Field field = me.mrCookieSlime.Slimefun.api.BlockStorage.class.getDeclaredField("inventories");
                field.setAccessible(true);
                return (Map<Location, BlockMenu>) field.get(storage);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not inspect RC-37 stored inventories", exception);
            }
        }
    }

    private static IllegalStateException unwrap(String message, ReflectiveOperationException exception) {
        Throwable cause = exception instanceof InvocationTargetException invocation
                ? invocation.getCause()
                : exception;
        if (cause instanceof RuntimeException runtimeException) {
            return new IllegalStateException(message, runtimeException);
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new IllegalStateException(message, cause);
    }
}
