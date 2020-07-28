package com.udaysagar2177.maps.utils;

/**
 * Simple utility functions to verify conditions and throw exceptions.
 *
 * If this class gets few more functions, simply import Guava library.
 *
 * @author uday
 */
public class Preconditions {

    public static void checkState(boolean expression, String errorMsg) {
        if (!expression) {
            throw new IllegalStateException(errorMsg);
        }
    }
}
