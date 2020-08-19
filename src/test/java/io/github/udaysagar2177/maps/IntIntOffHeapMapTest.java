package io.github.udaysagar2177.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;


import io.github.udaysagar2177.maps.memory.DirectMemoryResource;
import io.github.udaysagar2177.maps.sedeserializers.IntHolder;
import io.github.udaysagar2177.maps.sedeserializers.IntIntEntrySeDeserializer;

public class IntIntOffHeapMapTest {

    private static final Random RANDOM = new Random();

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMapCreation() {
        new OffHeapMapImpl<>(0, 0.66f, new IntIntEntrySeDeserializer(), DirectMemoryResource::new,
                IntHolder::new, IntHolder::new);
    }

    @Test
    public void testSingleElementMap() {
        OffHeapMapImpl<IntHolder, IntHolder> map = new OffHeapMapImpl<>(1, 0.66f,
                new IntIntEntrySeDeserializer(), DirectMemoryResource::new,
                IntHolder::new, IntHolder::new);
        IntHolder key = new IntHolder();
        IntHolder value = new IntHolder();
        IntHolder valueFlyweight = new IntHolder();
        // test put and get
        assertNull(map.put(key.setInt(-1), value.setInt(2), valueFlyweight));
        assertEquals(value, map.get(key, valueFlyweight));
        assertEquals(1, map.size());
        // test remove
        assertEquals(value, map.remove(key, valueFlyweight));
        assertEquals(0, map.size());
        // test clear
        map.clear();
        assertEquals(0, map.size());
        // test additional puts
        int numElements = 10;
        for (int i = 0; i < numElements; i++) {
            assertNull(map.put(key.setInt(i), value.setInt(i * 2), valueFlyweight));
            assertEquals(value, map.get(key, valueFlyweight));
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
            key.setInt(0);
            map.put(key, putValue.setInt(1), null);
            assertEquals(1, map.get(key, getValue).getInt());
            map.put(key, putValue.setInt(11), null);
            assertEquals(11, map.get(key, getValue).getInt());
            assertEquals(11, map.remove(key, getValue).getInt());
            assertNull(map.remove(key, getValue));
            map.put(key, putValue.setInt(111), null);
            assertEquals(111, map.get(key, getValue).getInt());
            assertEquals(111, map.remove(key, getValue).getInt());
            assertNull(map.remove(key, getValue));
        } finally {
            map.close();
        }
    }

    @Test
    public void testPutGetsWithRandomData() throws Exception {
        int numElements = 100000;
        OffHeapMapImpl<IntHolder, IntHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new IntIntEntrySeDeserializer(), DirectMemoryResource::new,
                IntHolder::new, IntHolder::new);
        IntHolder key = new IntHolder();
        IntHolder removeKey = new IntHolder();
        IntHolder value = new IntHolder();
        IntHolder valueFlyweight = new IntHolder();
        Map<Integer, Integer> hashMap = new HashMap<>();
        try {
            for (int k = 0; k < 5; k++) {
                map.clear();
                hashMap.clear();
                for (int i = 0; i < numElements; i++) {
                    int randInt = RANDOM.nextInt(100000000);
                    key.setInt(randInt);
                    value.setInt(randInt * 2);
                    if (RANDOM.nextInt(10) < 2) {
                        Integer removedValue = hashMap.remove(removeKey.getInt());
                        IntHolder removedValueHolder = map.remove(removeKey, valueFlyweight);
                        if (removedValue != null) {
                            assertEquals((int) removedValue, removedValueHolder.getInt());
                        }
                        removeKey.copyFrom(key); // copy next key for deletion next time
                    }
                    Integer previousValue = hashMap.put(key.getInt(), value.getInt());
                    IntHolder previousValueHolder = map.put(key, value, valueFlyweight);
                    if (previousValue != null) {
                        assertEquals((int) previousValue, previousValueHolder.getInt());
                    }
                }
            }
        } finally {
            map.close();
        }
    }
}