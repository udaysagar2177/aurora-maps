package com.udaysagar2177.maps.sedeserializers;

import java.util.Objects;

import com.udaysagar2177.maps.Copyable;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IntHolder intHolder = (IntHolder) o;
        return intValue == intHolder.intValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue);
    }
}
