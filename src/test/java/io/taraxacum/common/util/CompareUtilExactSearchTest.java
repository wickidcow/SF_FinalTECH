package io.taraxacum.common.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompareUtilExactSearchTest {

    @Test
    void exactSearchIncludesFirstLastSingletonAndEmptyBoundaries() {
        assertEquals(-1, CompareUtil.getIntIndex(new int[0], 0));
        assertEquals(-1, CompareUtil.getIndex(new Integer[0], 0, CompareUtil.INTEGER_SIMPLE_COMPARATOR));
        assertEquals(0, CompareUtil.getIntIndex(new int[]{42}, 42));
        assertEquals(0, CompareUtil.getIndex(new Integer[]{42}, 42, CompareUtil.INTEGER_SIMPLE_COMPARATOR));
        assertEquals(-1, CompareUtil.getIntIndex(new int[]{42}, 41));
        assertEquals(-1, CompareUtil.getIndex(new Integer[]{42}, 43, CompareUtil.INTEGER_SIMPLE_COMPARATOR));

        int[] values = {-8, -3, 0, 5, 9};
        Integer[] boxed = {-8, -3, 0, 5, 9};
        for (int index = 0; index < values.length; index++) {
            assertEquals(index, CompareUtil.getIntIndex(values, values[index]));
            assertEquals(index, CompareUtil.getIndex(boxed, boxed[index], CompareUtil.INTEGER_SIMPLE_COMPARATOR));
        }
        assertEquals(-1, CompareUtil.getIntIndex(values, 8));
        assertEquals(-1, CompareUtil.getIndex(boxed, 8, CompareUtil.INTEGER_SIMPLE_COMPARATOR));
    }

    @Test
    void signedExtremaDoNotOverflowComparison() {
        int[] values = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
        Integer[] boxed = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE};
        assertTrue(CompareUtil.INTEGER_SIMPLE_COMPARATOR.compare(Integer.MAX_VALUE, Integer.MIN_VALUE) > 0);
        assertTrue(CompareUtil.INTEGER_SIMPLE_COMPARATOR.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0);
        for (int index = 0; index < values.length; index++) {
            assertEquals(index, CompareUtil.getIntIndex(values, values[index]));
            assertEquals(index, CompareUtil.getIndex(boxed, boxed[index], CompareUtil.INTEGER_SIMPLE_COMPARATOR));
        }
    }

    @Test
    void exactSearchMatchesStandardSortedArrayMembership() {
        for (int length = 0; length < 130; length++) {
            int[] values = new int[length];
            Integer[] boxed = new Integer[length];
            for (int index = 0; index < length; index++) {
                values[index] = index * 3 - 190;
                boxed[index] = values[index];
            }
            for (int query = -200; query <= 205; query++) {
                boolean present = Arrays.binarySearch(values, query) >= 0;
                int primitive = CompareUtil.getIntIndex(values, query);
                int generic = CompareUtil.getIndex(boxed, query, CompareUtil.INTEGER_SIMPLE_COMPARATOR);
                assertEquals(present, primitive >= 0, "primitive length=" + length + " query=" + query);
                assertEquals(present, generic >= 0, "generic length=" + length + " query=" + query);
                if (present) {
                    assertEquals(query, values[primitive]);
                    assertEquals(query, boxed[generic]);
                }
            }
        }
    }
}
