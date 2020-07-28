package com.udaysagar2177.maps;

import static org.junit.Assert.*;

import org.junit.Test;

import com.udaysagar2177.maps.memory.DirectMemoryResource;
import com.udaysagar2177.maps.utils.RandomUtils;

public class OffHeapMapTest {

    @Test
    public void testOffHeapMap() throws Exception {
        int numElements = 10000;
        OffHeapMapImpl<IntHolder, IntHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new IntIntEntrySeDeserializer(), DirectMemoryResource::new,
                IntHolder::new, IntHolder::new);
        IntHolder key = new IntHolder();
        IntHolder value = new IntHolder();
        try {
            for (int k = 0; k < 10; k++) {
                map.clear();
                int[] keys = new int[numElements];
                for (int i = 0; i < numElements; i++) {
                    int randInt = 1 + RandomUtils.randInt(100000000);
                    keys[i] = randInt;
                    map.put(key.setInt(randInt), value.setInt(randInt), null);
                }
                for (int i = 0; i < numElements; i++) {
                    assertEquals(keys[i], map.get(key.setInt(keys[i]), value).getInt());
                }
            }
        } finally {
            map.close();
        }
    }

    @Test
    public void testFreeKeyValue() throws Exception {
        int numElements = 10;
        OffHeapMapImpl<IntHolder, IntHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new IntIntEntrySeDeserializer(), DirectMemoryResource::new,
                IntHolder::new, IntHolder::new);
        IntHolder key = new IntHolder();
        IntHolder putValue = new IntHolder();
        IntHolder getValue = new IntHolder();
        try {
            map.put(key.setInt(0), putValue.setInt(1), null);
            assertEquals(1, map.get(key, getValue).getInt());
            map.put(key.setInt(0), putValue.setInt(11), null);
            assertEquals(11, map.get(key, getValue).getInt());
        } finally {
            map.close();
        }
    }
}