package io.taraxacum.common.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericUtilityBoundaryTest {

    @Test
    void firstNotNullPreservesParameterizedValueIdentityAndOrder() {
        List<String> first = new ArrayList<>(List.of("first"));
        List<String> second = new ArrayList<>(List.of("second"));
        List<String> result = JavaUtil.getFirstNotNull(null, first, second);
        assertSame(first, result);
        result.add("retained");
        assertEquals(List.of("first", "retained"), first);
        assertEquals(List.of("second"), second);
    }

    @Test
    void firstNotNullRetainsEmptyAndAllNullResults() {
        assertNull(JavaUtil.<List<String>>getFirstNotNull());
        assertNull(JavaUtil.getFirstNotNull(new Object[]{null, null}));
    }

    @Test
    void matchOnceSupportsParameterizedValuesWithoutChangingTheArray() {
        List<String> first = List.of("first");
        List<String> second = List.of("second");
        List<?>[] targets = {first, second};
        List<?>[] before = targets.clone();
        assertTrue(JavaUtil.matchOnce(new ArrayList<>(second), targets));
        assertFalse(JavaUtil.matchOnce(List.of("missing"), targets));
        assertFalse(JavaUtil.<List<String>>matchOnce(first));
        assertArrayEquals(before, targets);
    }

    @Test
    void matchOnceRetainsItsExistingNullTargetFailure() {
        assertThrows(NullPointerException.class,
                () -> JavaUtil.matchOnce("value", new String[]{null}));
    }

    @Test
    void reflectionRetainsTheOriginalParameterizedFieldValue() throws IllegalAccessException {
        Parent holder = new Parent();
        List<String> result = ReflectionUtil.getProperty(holder, Parent.class, "values");
        assertSame(holder.values, result);
        result.add("second");
        assertEquals(List.of("first", "second"), holder.values);
    }

    @Test
    void reflectionStillFindsInheritedFieldsAndReturnsNullForMissingFields() throws IllegalAccessException {
        Child holder = new Child();
        List<String> result = ReflectionUtil.getProperty(holder, Child.class, "values");
        assertSame(((Parent) holder).values, result);
        Object missing = ReflectionUtil.getProperty(holder, Child.class, "missing");
        assertNull(missing);
    }

    @Test
    void reflectionRetainsCallerSideTypeMismatchFailure() {
        Parent holder = new Parent();
        assertThrows(ClassCastException.class, () -> {
            String mismatch = ReflectionUtil.getProperty(holder, Parent.class, "values");
            fail("Expected a caller-side type check, received " + mismatch);
        });
    }

    private static class Parent {
        private final List<String> values = new ArrayList<>(List.of("first"));
    }

    private static final class Child extends Parent {
    }
}
