package io.github.udaysagar2177.maps.sedeserializers;

import java.util.Arrays;


import net.openhft.chronicle.core.OS;
import sun.misc.Unsafe;

/**
 * {@link EntrySeDeserializer} implementation that handles serialization of byte[] key-values.
 *
 * @author uday
 */
public class ByteArrByteArrEntrySeDeserializer
        implements EntrySeDeserializer<ByteArrHolder, ByteArrHolder> {

    private static final int KEY_OFFSET = 0;
    private final int keyLength;
    private final int valueLength;
    private final int valueOffset;
    private final int entryLength;

    public ByteArrByteArrEntrySeDeserializer(int keyLength, int valueLength) {
        this.keyLength = keyLength;
        this.valueLength = valueLength;
        this.valueOffset = KEY_OFFSET + keyLength;
        this.entryLength = keyLength + valueLength;
    }

    @Override
    public boolean isFreeKey(ByteArrHolder key) {
        checkByteArrLength(key.getBytes(), keyLength);
        byte[] bytes = key.getBytes();
        int i = 0;
        for (;i < keyLength - 7; i += 8) {
            if (OS.memory().readLong(bytes, i + Unsafe.ARRAY_BYTE_BASE_OFFSET) != 0) {
                return false;
            }
        }
        for (;i < keyLength; i++) {
            if (OS.memory().readByte(bytes, i + Unsafe.ARRAY_BYTE_BASE_OFFSET) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int numBytesPerEntry() {
        return entryLength;
    }

    @Override
    public void readKey(long entryAddress, ByteArrHolder usingKey) {
        checkByteArrLength(usingKey.getBytes(), keyLength);
        OS.memory().copyMemory(entryAddress + KEY_OFFSET, usingKey.getBytes(),
                Unsafe.ARRAY_BYTE_BASE_OFFSET, keyLength);
    }

    @Override
    public void readValue(long entryAddress, ByteArrHolder usingValue) {
        checkByteArrLength(usingValue.getBytes(), valueLength);
        OS.memory().copyMemory(entryAddress + valueOffset, usingValue.getBytes(),
                Unsafe.ARRAY_BYTE_BASE_OFFSET, valueLength);
    }

    @Override
    public void write(long entryAddress, ByteArrHolder key, ByteArrHolder value) {
        checkByteArrLength(key.getBytes(), keyLength);
        checkByteArrLength(value.getBytes(), valueLength);
        OS.memory().copyMemory(key.getBytes(), 0, entryAddress + KEY_OFFSET, keyLength);
        OS.memory().copyMemory(value.getBytes(), 0, entryAddress + valueOffset, valueLength);
    }

    @Override
    public int hash(ByteArrHolder key) {
        checkByteArrLength(key.getBytes(), keyLength);
        byte[] bytes = key.getBytes();
        long hash = 0;
        int i = 0;
        for (;i < bytes.length - 7; i += 8) {
            hash = 31 * hash + OS.memory().readLong(bytes, i + Unsafe.ARRAY_BYTE_BASE_OFFSET);
        }
        for (;i < bytes.length; i++) {
            hash = 31 * hash + OS.memory().readByte(bytes, i + Unsafe.ARRAY_BYTE_BASE_OFFSET);
        }
        return (int) hash;
    }

    @Override
    public boolean isEmpty(long entryAddress) {
        int i = 0;
        for (;i < keyLength - 7; i += 8) {
            if (OS.memory().readLong(entryAddress + i) != 0) {
                return false;
            }
        }
        for (;i < keyLength; i++) {
            if (OS.memory().readByte(entryAddress + i) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear(final long entryAddress) {
        OS.memory().setMemory(entryAddress, entryLength, (byte) 0);
    }

    @Override
    public boolean equalsKey(long entryAddress, ByteArrHolder key) {
        checkByteArrLength(key.getBytes(), keyLength);
        // TODO: optimize if possible and check for endian type
        byte[] bytes = key.getBytes();
        for (int i = 0; i < keyLength; i++) {
            if (OS.memory().readByte(entryAddress++) != bytes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void copy(long fromAddress, long toAddress) {
        OS.memory().copyMemory(fromAddress, toAddress, entryLength);
    }

    private static void checkByteArrLength(byte[] bytes, int keyLength) {
        if (bytes.length != keyLength) {
            throw new IllegalStateException(
                    String.format("Given bytes %s length %s is not equal to expected length %s",
                            Arrays.toString(bytes), bytes.length, keyLength));
        }
    }
}
