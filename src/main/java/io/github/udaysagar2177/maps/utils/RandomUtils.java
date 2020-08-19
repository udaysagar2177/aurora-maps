package io.github.udaysagar2177.maps.utils;

import java.util.Random;

/**
 * Random utility functions.
 *
 * TODO: remove or move this to a place common to tests/benchmarks.
 *
 * @author uday
 */
public class RandomUtils {

    private static final Random RANDOM = new Random();

    public static final int randInt(int max) {
        return Math.abs(RANDOM.nextInt(max));
    }
}
