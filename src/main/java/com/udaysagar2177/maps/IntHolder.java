package com.udaysagar2177.maps;

/**
 * Alternative to auto-boxing that converts a primitive int into an object.
 *
 * @author uday
 */
public class IntHolder implements Copyable<IntHolder> {

    private int intValue;

    public int getInt() {
        return intValue;
    }

    public IntHolder setInt(int intValue) {
        this.intValue = intValue;
        return this;
    }

    @Override
    public String toString() {
        return Integer.toString(intValue);
    }

    @Override
    public void copyFrom(IntHolder from) {
        this.intValue = from.getInt();
    }
}
