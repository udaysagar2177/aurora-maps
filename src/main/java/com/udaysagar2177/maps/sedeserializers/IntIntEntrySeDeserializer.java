package com.udaysagar2177.maps.sedeserializers;

import com.udaysagar2177.maps.utils.MapUtils;


import net.openhft.chronicle.core.OS;

/**
 * {@link EntrySeDeserializer} implementation that handles serialization of integer key-values.
 *
 * @author uday
 */
public class IntIntEntrySeDeserializer implements EntrySeDeserializer<IntHolder, IntHolder> {

    private static final int FREE_KEY = 0;
    private static final int KEY_OFFSET = 0;
    private static final int VALUE_OFFSET = Integer.BYTES;
    private static final int ENTRY_LENGTH = Integer.BYTES * 2;

    @Override
    public boolean isFreeKey(IntHolder key) {
        return key.getInt() == FREE_KEY;
    }

    @Override
    public int numBytesPerEntry() {
        return ENTRY_LENGTH;
    }

    @Override
    public void readKey(long entryAddress, IntHolder usingKey) {
        usingKey.setInt(OS.memory().readInt(entryAddress + KEY_OFFSET));
    }

    @Override
    public void readValue(long entryAddress, IntHolder usingValue) {
        usingValue.setInt(OS.memory().readInt(entryAddress + VALUE_OFFSET));
    }

    @Override
    public void write(long entryAddress, IntHolder key, IntHolder value) {
        OS.memory().writeInt(entryAddress + VALUE_OFFSET, value.getInt());
        OS.memory().writeInt(entryAddress + KEY_OFFSET, key.getInt());
    }

    @Override
    public int hash(IntHolder key) {
        return MapUtils.phiMix(key.getInt());
    }

    @Override
    public boolean isEmpty(long entryAddress) {
        return OS.memory().readInt(entryAddress + KEY_OFFSET) == FREE_KEY;
    }

    @Override
    public void clear(long entryAddress) {
        OS.memory().writeInt(entryAddress + KEY_OFFSET, 0);
        OS.memory().writeInt(entryAddress + VALUE_OFFSET, 0);
    }

    @Override
    public boolean equalsKey(long entryAddress, IntHolder key) {
        return OS.memory().readInt(entryAddress + KEY_OFFSET) == key.getInt();
    }

    @Override
    public void copy(long fromAddress, long toAddress) {
        OS.memory().copyMemory(fromAddress, toAddress, ENTRY_LENGTH);
    }
}
