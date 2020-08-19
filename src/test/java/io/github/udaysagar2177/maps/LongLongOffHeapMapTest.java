package io.github.udaysagar2177.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;


import io.github.udaysagar2177.maps.memory.DirectMemoryResource;
import io.github.udaysagar2177.maps.sedeserializers.LongHolder;
import io.github.udaysagar2177.maps.sedeserializers.LongLongEntrySeDeserializer;

public class LongLongOffHeapMapTest {

    private static final Random RANDOM = new Random();

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMapCreation() {
        new OffHeapMapImpl<>(0, 0.66f, new LongLongEntrySeDeserializer(), DirectMemoryResource::new,
                LongHolder::new, LongHolder::new);
    }

    @Test
    public void testSingleElementMap() {
        OffHeapMapImpl<LongHolder, LongHolder> map = new OffHeapMapImpl<>(1, 0.66f,
                new LongLongEntrySeDeserializer(), DirectMemoryResource::new,
                LongHolder::new, LongHolder::new);
        LongHolder key = new LongHolder();
        LongHolder value = new LongHolder();
        LongHolder valueFlyweight = new LongHolder();
        // test put and get
        assertNull(map.put(key.setLong(-1), value.setLong(2), valueFlyweight));
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
            assertNull(map.put(key.setLong(i), value.setLong(i * 2), valueFlyweight));
            assertEquals(value, map.get(key, valueFlyweight));
        }
    }

    @Test
    public void testFreeKeyValue() throws Exception {
        int numElements = 10;
        OffHeapMapImpl<LongHolder, LongHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new LongLongEntrySeDeserializer(), DirectMemoryResource::new,
                LongHolder::new, LongHolder::new);
        LongHolder key = new LongHolder();
        LongHolder putValue = new LongHolder();
        LongHolder getValue = new LongHolder();
        try {
            key.setLong(0);
            map.put(key, putValue.setLong(1), null);
            assertEquals(1, map.get(key, getValue).getLong());
            map.put(key, putValue.setLong(11), null);
            assertEquals(11, map.get(key, getValue).getLong());
            assertEquals(11, map.remove(key, getValue).getLong());
            assertNull(map.remove(key, getValue));
            map.put(key, putValue.setLong(111), null);
            assertEquals(111, map.get(key, getValue).getLong());
            assertEquals(111, map.remove(key, getValue).getLong());
            assertNull(map.remove(key, getValue));
        } finally {
            map.close();
        }
    }

    @Test
    public void testPutGetsWithRandomData() throws Exception {
        int numElements = 100000;
        OffHeapMapImpl<LongHolder, LongHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new LongLongEntrySeDeserializer(), DirectMemoryResource::new,
                LongHolder::new, LongHolder::new);
        LongHolder key = new LongHolder();
        LongHolder removeKey = new LongHolder();
        LongHolder value = new LongHolder();
        LongHolder valueFlyweight = new LongHolder();
        Map<Long, Long> hashMap = new HashMap<>();
        try {
            for (int k = 0; k < 5; k++) {
                map.clear();
                hashMap.clear();
                for (int i = 0; i < numElements; i++) {
                    long randLong = RANDOM.nextLong();
                    key.setLong(randLong);
                    value.setLong(randLong * 2);
                    if (RANDOM.nextInt(10) < 2) {
                        Long removedValue = hashMap.remove(removeKey.getLong());
                        LongHolder removedValueHolder = map.remove(removeKey, valueFlyweight);
                        if (removedValue != null) {
                            assertEquals((long) removedValue, removedValueHolder.getLong());
                        }
                        removeKey.copyFrom(key); // copy next key for deletion next time
                    }
                    Long previousValue = hashMap.put(key.getLong(), value.getLong());
                    LongHolder previousValueHolder = map.put(key, value, valueFlyweight);
                    if (previousValue != null) {
                        assertEquals((long) previousValue, previousValueHolder.getLong());
                    }
                }
            }
        } finally {
            map.close();
        }
    }
}