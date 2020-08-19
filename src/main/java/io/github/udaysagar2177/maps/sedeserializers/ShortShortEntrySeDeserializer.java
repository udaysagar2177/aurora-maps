package io.github.udaysagar2177.maps.sedeserializers;

import io.github.udaysagar2177.maps.utils.MapUtils;


import net.openhft.chronicle.core.OS;

/**
 * {@link EntrySeDeserializer} implementation that handles serialization of short key-values.
 *
 * @author uday
 */
public class ShortShortEntrySeDeserializer
        implements EntrySeDeserializer<ShortHolder, ShortHolder> {

    private static final short FREE_KEY = 0;
    private static final int KEY_OFFSET = 0;
    private static final int VALUE_OFFSET = Short.BYTES;
    private static final int ENTRY_LENGTH = Short.BYTES * 2;

    @Override
    public boolean isFreeKey(ShortHolder key) {
        return key.getShort() == FREE_KEY;
    }

    @Override
    public int numBytesPerEntry() {
        return ENTRY_LENGTH;
    }

    @Override
    public void readKey(long entryAddress, ShortHolder usingKey) {
        usingKey.setShort(OS.memory().readShort(entryAddress + KEY_OFFSET));
    }

    @Override
    public void readValue(long entryAddress, ShortHolder usingValue) {
        usingValue.setShort(OS.memory().readShort(entryAddress + VALUE_OFFSET));
    }

    @Override
    public void write(long entryAddress, ShortHolder key, ShortHolder value) {
        OS.memory().writeShort(entryAddress + VALUE_OFFSET, value.getShort());
        OS.memory().writeShort(entryAddress + KEY_OFFSET, key.getShort());
    }

    @Override
    public int hash(ShortHolder key) {
        return MapUtils.phiMix(key.getShort());
    }

    @Override
    public boolean isEmpty(long entryAddress) {
        return OS.memory().readShort(entryAddress + KEY_OFFSET) == FREE_KEY;
    }

    @Override
    public void clear(long entryAddress) {
        OS.memory().writeShort(entryAddress + KEY_OFFSET, (short) 0);
        OS.memory().writeShort(entryAddress + VALUE_OFFSET, (short) 0);
    }

    @Override
    public boolean equalsKey(long entryAddress, ShortHolder key) {
        return OS.memory().readShort(entryAddress + KEY_OFFSET) == key.getShort();
    }

    @Override
    public void copy(long fromAddress, long toAddress) {
        OS.memory().copyMemory(fromAddress, toAddress, ENTRY_LENGTH);
    }
}
