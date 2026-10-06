package io.taraxacum.libs.slimefun.compat;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.TestBlockData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentityTransitionPolicyTest {

    @Test
    void emptyLocationAllowsIdentityCreation() {
        assertEquals(
                LegacyBlockDataCompat.IdentityDecision.CREATE,
                LegacyBlockDataCompat.identityDecision(null, "FINALTECH_TEST"));
    }

    @Test
    void sameIdentityIsIdempotent() {
        TestBlockData data = new TestBlockData();
        data.setData("owner", "preserve");

        assertEquals(
                LegacyBlockDataCompat.IdentityDecision.ALREADY_TARGET,
                LegacyBlockDataCompat.identityDecision(data, "FINALTECH_TEST"));
        assertEquals("preserve", data.getData("owner"));
    }

    @Test
    void differentIdentityIsAConflictAndDoesNotTouchStoredData() {
        TestBlockData data = new TestBlockData();
        data.setData("owner", "preserve");
        data.setData("count", "37");
        data.updates().clear();

        assertEquals(
                LegacyBlockDataCompat.IdentityDecision.CONFLICT,
                LegacyBlockDataCompat.identityDecision(data, "OTHER_MACHINE"));
        assertEquals("FINALTECH_TEST", data.getSfId());
        assertEquals("preserve", data.getData("owner"));
        assertEquals("37", data.getData("count"));
        assertEquals(0, data.updates().size());
    }
}
