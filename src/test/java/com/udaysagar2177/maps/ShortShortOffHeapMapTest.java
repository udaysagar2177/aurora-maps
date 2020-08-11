package com.udaysagar2177.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.udaysagar2177.maps.memory.DirectMemoryResource;
import com.udaysagar2177.maps.sedeserializers.ShortHolder;
import com.udaysagar2177.maps.sedeserializers.ShortShortEntrySeDeserializer;

public class ShortShortOffHeapMapTest {

    private static final Random RANDOM = new Random();

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMapCreation() {
        new OffHeapMapImpl<>(0, 0.66f, new ShortShortEntrySeDeserializer(), DirectMemoryResource::new,
                ShortHolder::new, ShortHolder::new);
    }

    @Test
    public void testSingleElementMap() {
        OffHeapMapImpl<ShortHolder, ShortHolder> map = new OffHeapMapImpl<>(1, 0.66f,
                new ShortShortEntrySeDeserializer(), DirectMemoryResource::new,
                ShortHolder::new, ShortHolder::new);
        ShortHolder key = new ShortHolder();
        ShortHolder value = new ShortHolder();
        ShortHolder valueFlyweight = new ShortHolder();
        // test put and get
        assertNull(map.put(key.setShort((short) -1), value.setShort((short) 2), valueFlyweight));
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
            assertNull(map.put(key.setShort((short) i), value.setShort((short) (i * 2)), valueFlyweight));
            assertEquals(value, map.get(key, valueFlyweight));
        }
    }

    @Test
    public void testFreeKeyValue() throws Exception {
        int numElements = 10;
        OffHeapMapImpl<ShortHolder, ShortHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new ShortShortEntrySeDeserializer(), DirectMemoryResource::new,
                ShortHolder::new, ShortHolder::new);
        ShortHolder key = new ShortHolder();
        ShortHolder putValue = new ShortHolder();
        ShortHolder getValue = new ShortHolder();
        try {
            key.setShort((short) 0);
            map.put(key, putValue.setShort((short) 1), null);
            assertEquals(1, map.get(key, getValue).getShort());
            map.put(key, putValue.setShort((short) 11), null);
            assertEquals(11, map.get(key, getValue).getShort());
            assertEquals(11, map.remove(key, getValue).getShort());
            assertNull(map.remove(key, getValue));
            map.put(key, putValue.setShort((short) 111), null);
            assertEquals(111, map.get(key, getValue).getShort());
            assertEquals(111, map.remove(key, getValue).getShort());
            assertNull(map.remove(key, getValue));
        } finally {
            map.close();
        }
    }

    @Test
    public void testPutGetsWithRandomData() throws Exception {
        int numElements = 100000;
        OffHeapMapImpl<ShortHolder, ShortHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new ShortShortEntrySeDeserializer(), DirectMemoryResource::new,
                ShortHolder::new, ShortHolder::new);
        ShortHolder key = new ShortHolder();
        ShortHolder removeKey = new ShortHolder();
        ShortHolder value = new ShortHolder();
        ShortHolder valueFlyweight = new ShortHolder();
        Map<Short, Short> hashMap = new HashMap<>();
        try {
            for (int k = 0; k < 5; k++) {
                map.clear();
                hashMap.clear();
                for (int i = 0; i < numElements; i++) {
                    short randShort = (short) RANDOM.nextInt(Short.MAX_VALUE);
                    key.setShort(randShort);
                    value.setShort((short) (randShort * 2));
                    if (RANDOM.nextInt(10) < 2) {
                        Short removedValue = hashMap.remove(removeKey.getShort());
                        ShortHolder removedValueHolder = map.remove(removeKey, valueFlyweight);
                        if (removedValue != null) {
                            assertEquals((short) removedValue, removedValueHolder.getShort());
                        }
                        removeKey.copyFrom(key); // copy next key for deletion next time
                    }
                    Short previousValue = hashMap.put(key.getShort(), value.getShort());
                    ShortHolder previousValueHolder = map.put(key, value, valueFlyweight);
                    if (previousValue != null) {
                        assertEquals((short) previousValue, previousValueHolder.getShort());
                    }
                }
            }
        } finally {
            map.close();
        }
    }
}