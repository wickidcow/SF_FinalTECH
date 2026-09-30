package io.taraxacum.libs.slimefun.compat;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.TestBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.taraxacum.finaltech.util.BlockTickerUtil;
import io.taraxacum.finaltech.util.ConstantTableUtil;
import io.taraxacum.libs.slimefun.dto.BlockStorageHelper;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ModernTickerDataTest {
    @Test
    void reactorResetRemovesItemThenPersistsZeroCount() {
        TestBlockData data = new TestBlockData();
        data.setData("item", "serialized-item");
        data.setData("count", "37");
        data.setData("owner", "unchanged");
        data.updates().clear();

        // This sequence previously threw from BlockDataConfigWrapper after removing item.
        LegacyTickerDataCompat.setValue(data, "item", null);
        LegacyTickerDataCompat.setValue(data, "count", "0");

        assertNull(data.getData("item"));
        assertEquals("0", data.getData("count"));
        assertEquals("unchanged", data.getData("owner"));
        assertEquals("FINALTECH_TEST", data.getSfId());
        assertEquals(List.of("item", "count"), data.updates());
    }

    @Test
    void resettingAnAlreadyAbsentItemStillResetsCount() {
        TestBlockData data = new TestBlockData();
        LegacyTickerDataCompat.setValue(data, "item", null);
        LegacyTickerDataCompat.setValue(data, "count", "0");
        assertFalse(LegacyTickerDataCompat.contains(data, "item"));
        assertEquals("0", data.getData("count"));
        assertEquals(List.of("count"), data.updates());
    }

    @Test
    void emptyAndSerializedStringsArePreservedVerbatim() {
        TestBlockData data = new TestBlockData();
        LegacyTickerDataCompat.setValue(data, "item", "AA==\nbytes:0123");
        LegacyTickerDataCompat.setValue(data, "position", "");
        assertEquals("AA==\nbytes:0123", LegacyTickerDataCompat.getString(data, "item"));
        assertTrue(LegacyTickerDataCompat.contains(data, "position"));
        assertEquals("", LegacyTickerDataCompat.getString(data, "position"));
        assertNull(LegacyTickerDataCompat.getString(data, "missing"));
    }

    @Test
    void helperDefaultsAndNullRemovalUseCanonicalData() {
        TestBlockData data = new TestBlockData();
        BlockStorageHelper helper = new BlockStorageHelper("test", List.of("0", "1")) {
            @Override
            public String getKey() {
                return "mms";
            }
        };
        assertEquals("0", helper.getOrDefaultValue(data));
        helper.setOrClearValue(data, "1");
        assertEquals("1", data.getData("mms"));
        helper.setOrClearValue(data, null);
        assertNull(data.getData("mms"));
        assertEquals("0", helper.getOrDefaultValue(data));
        assertEquals(List.of("mms", "mms"), data.updates());
    }

    @Test
    void sleepCountdownAndRemovalRetainTheirExistingSemantics() {
        TestBlockData data = new TestBlockData();
        BlockTickerUtil.setSleep(data, "2.5");
        BlockTickerUtil.subSleep(data);
        assertEquals("1.5", data.getData(ConstantTableUtil.CONFIG_SLEEP));
        BlockTickerUtil.subSleep(data);
        BlockTickerUtil.subSleep(data);
        assertEquals("0", data.getData(ConstantTableUtil.CONFIG_SLEEP));
        assertTrue(BlockTickerUtil.hasSleep(data));
        BlockTickerUtil.setSleep(data, null);
        assertFalse(BlockTickerUtil.hasSleep(data));
    }

    @Test
    void intervalWrapperForwardsTheSameRecordAndUniqueTick() {
        TestBlockData data = new TestBlockData();
        AtomicInteger ticks = new AtomicInteger();
        AtomicInteger uniqueTicks = new AtomicInteger();
        BlockTicker delegate = new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return true;
            }

            @Override
            public void tick(Block block, SlimefunItem item, SlimefunBlockData received) {
                assertSame(data, received);
                ticks.incrementAndGet();
                received.setData("seen", "yes");
            }

            @Override
            public void uniqueTick() {
                uniqueTicks.incrementAndGet();
            }
        };
        BlockTicker wrapper = BlockTickerUtil.getIndependentIntervalBlockTicker(delegate, 4);
        assertTrue(wrapper.isSynchronized());
        // Exercise the same storage-neutral dispatch used by current Slimefun.
        wrapper.tick(null, null, (ASlimefunDataContainer) data);
        wrapper.tick(null, null, (ASlimefunDataContainer) data);
        wrapper.uniqueTick();
        assertEquals(2, ticks.get());
        assertEquals(1, uniqueTicks.get());
        assertEquals("yes", data.getData("seen"));
        assertEquals("3", data.getData(ConstantTableUtil.CONFIG_SLEEP));
    }

    @Test
    @SuppressWarnings("deprecation")
    void modernManualDispatchStillReachesOtherAddonsLegacyTicker() {
        TestBlockData data = new TestBlockData();
        data.setData("count", "12");
        AtomicInteger ticks = new AtomicInteger();
        BlockTicker legacyAddon = new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return false;
            }

            @Override
            public void tick(Block block, SlimefunItem item, Config received) {
                assertEquals("12", received.getString("count"));
                received.setValue("count", "13");
                ticks.incrementAndGet();
            }
        };
        BlockTicker wrapper = BlockTickerUtil.getIndependentIntervalBlockTicker(legacyAddon, 2);
        wrapper.tick(null, null, data);
        assertEquals(1, ticks.get());
        assertEquals("13", data.getData("count"));
        assertFalse(wrapper.isSynchronized());
    }

    @Test
    void energyAdapterUsesModernContainerOverloads() {
        TestBlockData data = new TestBlockData();
        EnergyNetProvider provider = new EnergyNetProvider() {
            @Override
            public String getId() {
                return "FINALTECH_TEST";
            }

            @Override
            public int getCapacity() {
                return 200;
            }

            @Override
            public int getGeneratedOutput(Location location, ASlimefunDataContainer received) {
                assertSame(data, received);
                return 123;
            }

            @Override
            public boolean willExplode(Location location, ASlimefunDataContainer received) {
                assertSame(data, received);
                return true;
            }
        };
        assertEquals(123, LegacySlimefunApiCompat.getGeneratedOutput(provider, null, data));
        assertTrue(LegacySlimefunApiCompat.willExplode(provider, null, data));
    }
}
