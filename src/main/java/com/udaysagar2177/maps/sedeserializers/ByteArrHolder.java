package com.udaysagar2177.maps.sedeserializers;

import java.util.Arrays;

import com.udaysagar2177.maps.Copyable;

/**
 * Wrapper around byte[] that implements {@link Copyable<ByteArrHolder>}.
 *
 * @author uday
 */
public class ByteArrHolder implements Copyable<ByteArrHolder> {

    private byte[] bytes;
    private final int length;

    public ByteArrHolder(int length) {
        this.bytes = new byte[length];
        this.length = length;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public ByteArrHolder setBytes(byte[] bytes) {
        checkLength(bytes, length);
        System.arraycopy(bytes, 0, this.bytes, 0, length);
        return this;
    }

    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }

    @Override
    public void copyFrom(ByteArrHolder from) {
        checkLength(from.getBytes(), length);
        System.arraycopy(from.getBytes(), 0, this.bytes, 0, length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ByteArrHolder byteArrHolder = (ByteArrHolder) o;
        return Arrays.equals(bytes, byteArrHolder.getBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    private static void checkLength(byte[] bytes, int length) {
        if (bytes.length != length) {
            throw new IllegalStateException(String
                    .format("Bytes %s length %s is not equal to expected length %s",
                            Arrays.toString(bytes), bytes.length, length));
        }
    }
}

