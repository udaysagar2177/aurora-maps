package io.github.udaysagar2177.maps.sedeserializers;

import java.util.Objects;

import io.github.udaysagar2177.maps.Copyable;

/**
 * Alternative to auto-boxing that converts a primitive short into an object.
 *
 * @author uday
 */
public class ShortHolder implements Copyable<ShortHolder> {

    private short shortValue;

    public short getShort() {
        return shortValue;
    }

    public ShortHolder setShort(short shortValue) {
        this.shortValue = shortValue;
        return this;
    }

    @Override
    public String toString() {
        return Short.toString(shortValue);
    }

    @Override
    public void copyFrom(ShortHolder from) {
        this.shortValue = from.getShort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ShortHolder shortHolder = (ShortHolder) o;
        return shortValue == shortHolder.shortValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortValue);
    }
}
