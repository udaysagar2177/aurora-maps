package io.github.udaysagar2177.maps.sedeserializers;

import java.util.Objects;

import io.github.udaysagar2177.maps.Copyable;

/**
 * Alternative to auto-boxing that converts a primitive long into an object.
 *
 * @author uday
 */
public class LongHolder implements Copyable<LongHolder> {

    private long longValue;

    public long getLong() {
        return longValue;
    }

    public LongHolder setLong(long longValue) {
        this.longValue = longValue;
        return this;
    }

    @Override
    public String toString() {
        return Long.toString(longValue);
    }

    @Override
    public void copyFrom(LongHolder from) {
        this.longValue = from.getLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LongHolder longHolder = (LongHolder) o;
        return longValue == longHolder.longValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longValue);
    }
}
