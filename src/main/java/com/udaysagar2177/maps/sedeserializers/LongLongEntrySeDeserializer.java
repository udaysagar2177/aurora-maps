package com.udaysagar2177.maps.sedeserializers;

import com.udaysagar2177.maps.utils.MapUtils;


import net.openhft.chronicle.core.OS;

/**
 * {@link EntrySeDeserializer} implementation that handles serialization of long key-values.
 *
 * @author uday
 */
public class LongLongEntrySeDeserializer implements EntrySeDeserializer<LongHolder, LongHolder> {

    private static final long FREE_KEY = 0;
    private static final int KEY_OFFSET = 0;
    private static final int VALUE_OFFSET = Long.BYTES;
    private static final int ENTRY_LENGTH = Long.BYTES * 2;

    @Override
    public boolean isFreeKey(LongHolder key) {
        return key.getLong() == FREE_KEY;
    }

    @Override
    public int numBytesPerEntry() {
        return ENTRY_LENGTH;
    }

    @Override
    public void readKey(long entryAddress, LongHolder usingKey) {
        usingKey.setLong(OS.memory().readLong(entryAddress + KEY_OFFSET));
    }

    @Override
    public void readValue(long entryAddress, LongHolder usingValue) {
        usingValue.setLong(OS.memory().readLong(entryAddress + VALUE_OFFSET));
    }

    @Override
    public void write(long entryAddress, LongHolder key, LongHolder value) {
        OS.memory().writeLong(entryAddress + VALUE_OFFSET, value.getLong());
        OS.memory().writeLong(entryAddress + KEY_OFFSET, key.getLong());
    }

    @Override
    public int hash(LongHolder key) {
        return MapUtils.phiMix(key.getLong());
    }

    @Override
    public boolean isEmpty(long entryAddress) {
        return OS.memory().readLong(entryAddress + KEY_OFFSET) == FREE_KEY;
    }

    @Override
    public void clear(long entryAddress) {
        OS.memory().writeLong(entryAddress + KEY_OFFSET, 0);
        OS.memory().writeLong(entryAddress + VALUE_OFFSET, 0);
    }

    @Override
    public boolean equalsKey(long entryAddress, LongHolder key) {
        return OS.memory().readLong(entryAddress + KEY_OFFSET) == key.getLong();
    }

    @Override
    public void copy(long fromAddress, long toAddress) {
        OS.memory().copyMemory(fromAddress, toAddress, ENTRY_LENGTH);
    }
}
