package com.udaysagar2177.maps.utils;

/**
 * Utility functions around map operations.
 *
 * @author uday
 */
public class MapUtils {

    private MapUtils() { /* do nothing. */ }

    /**
     * Returns hash value of given integer.
     */
    public static int phiMix(int x) {
        int h = x * 0x9E3779B9; // phiMix(x) taken from FastUtil;
        return h ^ (h >> 16);
    }

    /**
     * Computes the least power of two larger than or equal to <code>Math.ceil( expected / f )</code>.
     *
     * @param expectedSize
     *         the expected number of entries in a hash table.
     * @param loadFactor
     *         the load factor.
     * @return the table size to hold expected size of entries.
     * @throws IllegalArgumentException
     *         if the necessary size is larger than 2<sup>30</sup>.
     */
    public static int tableSizeFor(int expectedSize, float loadFactor) {
        long desiredCapacity = (long) Math.ceil(expectedSize / loadFactor);
        if (desiredCapacity > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String
                    .format("Storage gets too large with expected size %s, load factor %s",
                            expectedSize, loadFactor));
        }
        // find next closest power of 2.
        if (desiredCapacity <= 2) {
            return 2;
        }
        desiredCapacity--;
        desiredCapacity |= desiredCapacity >> 1;
        desiredCapacity |= desiredCapacity >> 2;
        desiredCapacity |= desiredCapacity >> 4;
        desiredCapacity |= desiredCapacity >> 8;
        desiredCapacity |= desiredCapacity >> 16;
        return (int) ((desiredCapacity | desiredCapacity >> 32) + 1);
    }
}
