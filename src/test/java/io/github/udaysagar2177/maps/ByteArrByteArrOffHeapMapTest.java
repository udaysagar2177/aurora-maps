package io.github.udaysagar2177.maps;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;


import io.github.udaysagar2177.maps.memory.DirectMemoryResource;
import io.github.udaysagar2177.maps.sedeserializers.ByteArrByteArrEntrySeDeserializer;
import io.github.udaysagar2177.maps.sedeserializers.ByteArrHolder;

public class ByteArrByteArrOffHeapMapTest {

    private static final Random RANDOM = new Random();

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMapCreation() {
        new OffHeapMapImpl<>(0, 0.66f, new ByteArrByteArrEntrySeDeserializer(2, 2),
                DirectMemoryResource::new, () -> new ByteArrHolder(1), () -> new ByteArrHolder(1));
    }

    @Test
    public void testSingleElementMap() {
        int numElements = 10;
        int keyLength = 5;
        int valueLength = 10;
        OffHeapMapImpl<ByteArrHolder, ByteArrHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new ByteArrByteArrEntrySeDeserializer(keyLength, valueLength),
                DirectMemoryResource::new, () -> new ByteArrHolder(keyLength),
                () -> new ByteArrHolder(valueLength));
        ByteArrHolder key = new ByteArrHolder(keyLength);
        ByteArrHolder putValue = new ByteArrHolder(valueLength);
        ByteArrHolder getValue = new ByteArrHolder(valueLength);

        // test put and get
        key.setBytes("abcde".getBytes(Charset.defaultCharset()));
        putValue.setBytes("abcdefghij".getBytes(Charset.defaultCharset()));
        assertNull(map.put(key, putValue, getValue));
        assertEquals(putValue, map.get(key, getValue));

        // test remove
        assertEquals(putValue, map.remove(key, getValue));
        assertEquals(0, map.size());

        // test additional puts
        for (int i = 0; i < numElements; i++) {
            key.setBytes(getBytes(keyLength));
            putValue.setBytes(getBytes(valueLength));
            assertNull(map.put(key, putValue, getValue));
            assertEquals(putValue, map.get(key, getValue));
        }

        // test clear
        map.clear();
        assertEquals(0, map.size());
    }

    @Test
    public void testFreeKeyValue() throws Exception {
        int numElements = 10;
        int keyLength = 5;
        int valueLength = 10;
        OffHeapMapImpl<ByteArrHolder, ByteArrHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new ByteArrByteArrEntrySeDeserializer(keyLength, valueLength),
                DirectMemoryResource::new, () -> new ByteArrHolder(keyLength),
                () -> new ByteArrHolder(valueLength));
        ByteArrHolder key = new ByteArrHolder(keyLength);
        ByteArrHolder putValue = new ByteArrHolder(valueLength);
        ByteArrHolder getValue = new ByteArrHolder(valueLength);
        for (int i = 0; i < keyLength; i++) {
            key.getBytes()[i] = 0;
        }
        try {
            putValue.setBytes("abcdefghij".getBytes(Charset.defaultCharset()));
            assertNull(map.put(key, putValue, getValue));
            assertArrayEquals(putValue.getBytes(), map.get(key, getValue).getBytes());

            putValue.setBytes("xycdefghij".getBytes(Charset.defaultCharset()));
            map.put(key, putValue, getValue);
            assertArrayEquals(putValue.getBytes(), map.get(key, getValue).getBytes());
            assertArrayEquals(putValue.getBytes(), map.remove(key, getValue).getBytes());
            assertNull(map.remove(key, getValue));

            putValue.setBytes("zwcdefghij".getBytes(Charset.defaultCharset()));
            assertNull(map.put(key, putValue, getValue));
            assertArrayEquals(putValue.getBytes(), map.get(key, getValue).getBytes());
            assertArrayEquals(putValue.getBytes(), map.remove(key, getValue).getBytes());
            assertNull(map.remove(key, getValue));
        } finally {
            map.close();
        }
    }

    @Test
    public void testPutGetsWithRandomData() throws Exception {
        int numElements = 100000;
        int keyLength = 24;
        int valueLength = 42;
        OffHeapMapImpl<ByteArrHolder, ByteArrHolder> map = new OffHeapMapImpl<>(numElements, 0.66f,
                new ByteArrByteArrEntrySeDeserializer(keyLength, valueLength),
                DirectMemoryResource::new, () -> new ByteArrHolder(keyLength),
                () -> new ByteArrHolder(valueLength));
        ByteArrHolder key = new ByteArrHolder(keyLength);
        ByteArrHolder putValue = new ByteArrHolder(valueLength);
        ByteArrHolder getValue = new ByteArrHolder(valueLength);
        ByteArrHolder removeKey = new ByteArrHolder(keyLength);
        Map<ByteArrHolder, ByteArrHolder> hashMap = new HashMap<>();

        try {
            for (int k = 0; k < 5; k++) {
                map.clear();
                hashMap.clear();
                for (int i = 0; i < numElements; i++) {
                    key.setBytes(getBytes(keyLength));
                    putValue.setBytes(getBytes(valueLength));
                    if (RANDOM.nextInt(10) < 2) {
                        ByteArrHolder removedValue = hashMap.remove(removeKey);
                        ByteArrHolder removedValueHolder = map.remove(removeKey, getValue);
                        if (removedValue != null) {
                            assertEquals(removedValue, removedValueHolder);
                        }
                        removeKey.copyFrom(key); // copy next key for deletion next time
                    }
                    ByteArrHolder hashMapKey = new ByteArrHolder(keyLength);
                    hashMapKey.copyFrom(key);
                    ByteArrHolder hashMapValue = new ByteArrHolder(valueLength);
                    hashMapValue.copyFrom(putValue);
                    ByteArrHolder previousValue = hashMap.put(hashMapKey, hashMapValue);
                    ByteArrHolder previousValueHolder = map.put(key, putValue, getValue);
                    if (previousValue != null) {
                        assertEquals(previousValue, previousValueHolder);
                    }
                }
            }
        } finally {
            map.close();
        }
    }

    private static byte[] getBytes(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) RANDOM.nextInt(128);
        }
        return data;
    }
}