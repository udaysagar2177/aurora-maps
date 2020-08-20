package io.github.udaysagar2177.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;


import io.github.udaysagar2177.maps.memory.MemoryResource;
import io.github.udaysagar2177.maps.sedeserializers.EntrySeDeserializer;
import io.github.udaysagar2177.maps.utils.MapUtils;

/**
 * A non-threadsafe implementation of {@link OffHeapMap} that scales up as necessary to honor the
 * suggested load factor. To resolve key collisions, Linear probing scheme is used because it is
 * fast, simple and allows CPU to exploit spatial locality on off-heap data.
 *
 * The data structure used to store key-values is a list of off-heap {@link MemoryResource}
 * allocated w.r.t to {@link OffHeapMapImpl#MAX_MEMORY_RESOURCE_CAPACITY} and
 * {@link OffHeapMapImpl#numBytesPerEntry}. This list acts as an abstraction to contiguous
 * Hash Table. Positions in the Hash Table are translated to an index in the list and then an
 * internal index inside each {@link MemoryResource}. During rehashing operations,
 * new {@link MemoryResource}s are allocated to support bigger Hash Table size and
 * old {@link MemoryResource}s are released immediately.
 *
 * It is a required condition that provided {@link MemoryResource} and {@link EntrySeDeserializer}
 * should be compatible in determining {@link EntrySeDeserializer#isEmpty(long)} and {@link
 * EntrySeDeserializer#isFreeKey(Object)}. Otherwise, this map behavior will be wrong.
 *
 * For better performance, allocate this map with expected no. of entries or sufficiently large no.
 * of entries to minimize rehash operations. But the trade-off is that more off-heap memory will be
 * committed upfront.
 *
 * @param <K>
 *         a flyweight object that holds data that can be easily read/written using
 *         {@link EntrySeDeserializer}.
 * @param <V>
 *         a flyweight object that holds data that can be easily read/written using
 *         {@link EntrySeDeserializer}.
 * @author uday
 */
public class OffHeapMapImpl<K extends Copyable<K>, V extends Copyable<V>>
        implements OffHeapMap<K, V> {

    private static final int MAX_MEMORY_RESOURCE_CAPACITY = Integer.MAX_VALUE;

    private final float loadFactor;
    private final EntrySeDeserializer<K, V> entrySeDeserializer;
    private final Supplier<K> keyFactory;
    private final Supplier<V> valueFactory;
    private final int numBytesPerEntry;
    private final Function<Integer, MemoryResource> memoryResourceFactory;
    private final List<MemoryResource> memoryResources;
    private final int maxNumEntriesPerMemoryResource;
    private final K freeKey;
    private final V freeValue;

    private boolean hasFreeKey = false;
    private int threshold;
    private int hashTableSize;
    private int modulo;
    private int size = 0;

    public OffHeapMapImpl(int expectedElements,
                          float loadFactor,
                          EntrySeDeserializer<K, V> entrySeDeserializer,
                          Function<Integer, MemoryResource> memoryResourceFactory,
                          Supplier<K> keyFactory,
                          Supplier<V> valueFactory) {
        checkArguments(expectedElements, loadFactor, entrySeDeserializer.numBytesPerEntry());
        this.loadFactor = loadFactor;
        this.keyFactory = keyFactory;
        this.valueFactory = valueFactory;
        this.entrySeDeserializer = entrySeDeserializer;
        this.memoryResourceFactory = memoryResourceFactory;
        this.numBytesPerEntry = entrySeDeserializer.numBytesPerEntry();
        this.maxNumEntriesPerMemoryResource = MAX_MEMORY_RESOURCE_CAPACITY / numBytesPerEntry;
        this.freeKey = keyFactory.get();
        this.freeValue = valueFactory.get();

        this.hashTableSize = MapUtils.tableSizeFor(expectedElements, loadFactor);
        this.threshold = Math.max(1, (int) (hashTableSize * loadFactor));
        this.modulo = hashTableSize - 1;

        this.memoryResources = allocateMemory(hashTableSize, numBytesPerEntry);
    }

    @Override
    public V put(K key, V value, V previousValueHolder) {
        if (entrySeDeserializer.isFreeKey(key)) {
            if (hasFreeKey) {
                if (previousValueHolder != null) {
                    previousValueHolder.copyFrom(freeValue);
                }
                freeValue.copyFrom(value);
                hasFreeKey = true;
                return previousValueHolder;
            }
            freeKey.copyFrom(key);
            freeValue.copyFrom(value);
            hasFreeKey = true;
            size++;
            return null;
        }
        int position = getPosition(entrySeDeserializer.hash(key));
        do {
            long address = getAddress(position); // TODO: simplify address calc
            if (entrySeDeserializer.equalsKey(address, key)) {
                if (previousValueHolder != null) {
                    entrySeDeserializer.readValue(address, previousValueHolder);
                }
                entrySeDeserializer.write(address, key, value);
                return previousValueHolder;
            }
            if (entrySeDeserializer.isEmpty(address)) {
                entrySeDeserializer.write(address, key, value);
                size++;
                if (size > threshold) {
                    rehash();
                }
                return null;
            }
            position = (position + 1) & modulo;
        } while (true);
    }

    @Override
    public V get(K key, V usingValue) {
        if (entrySeDeserializer.isFreeKey(key)) {
            return hasFreeKey ? freeValue : null;
        }
        int position = getPosition(entrySeDeserializer.hash(key));
        do {
            long address = getAddress(position);
            if (entrySeDeserializer.equalsKey(address, key)) {
                entrySeDeserializer.readValue(address, usingValue);
                return usingValue;
            }
            if (entrySeDeserializer.isEmpty(address)) {
                return null;
            }
            position = (position + 1) & modulo;
        } while (true);
    }

    @Override
    public V remove(K key, V usingValue) {
        if (entrySeDeserializer.isFreeKey(key)) {
            if (hasFreeKey) {
                size--;
                hasFreeKey = false;
                if (usingValue != null) {
                    usingValue.copyFrom(freeValue);
                    return usingValue;
                }
            }
            return null;
        }
        int position = getPosition(entrySeDeserializer.hash(key));
        do {
            long address = getAddress(position);
            if (entrySeDeserializer.equalsKey(address, key)) {
                if (usingValue != null) {
                    entrySeDeserializer.readValue(address, usingValue);
                }
                shiftKeys(position);
                size--;
                return usingValue;
            }
            if (entrySeDeserializer.isEmpty(address)) {
                return null;
            }
            position = (position + 1) & modulo;
        } while (true);
    }

    private void shiftKeys(int currentPosition) {
        int freeSlot;
        long freeSlotAddress;
        int currentKeySlot;
        long currentAddress;
        K key = keyFactory.get();
        do {
            freeSlot = currentPosition;
            freeSlotAddress = getAddress(freeSlot);
            currentPosition = (currentPosition + 1) & modulo;
            currentAddress = getAddress(currentPosition);
            while (true) {
                if (entrySeDeserializer.isEmpty(currentAddress)) {
                    entrySeDeserializer.clear(freeSlotAddress);
                    return;
                }
                entrySeDeserializer.readKey(currentAddress, key);
                currentKeySlot = getPosition(entrySeDeserializer.hash(key));
                if (freeSlot <= currentPosition) {
                    if (freeSlot >= currentKeySlot || currentKeySlot > currentPosition) {
                        break;
                    }
                } else {
                    if (currentPosition < currentKeySlot && currentKeySlot <= freeSlot) {
                        break;
                    }
                }
                currentPosition = (currentPosition + 1) & modulo;
                currentAddress = getAddress(currentPosition);
            }
            entrySeDeserializer.copy(currentAddress, freeSlotAddress);
        } while (true);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        for (int i = 0; i < memoryResources.size(); i++) {
            MemoryResource memoryResource = memoryResources.get(i);
            for (int j = 0; j < memoryResource.capacityInBytes(); j += numBytesPerEntry) {
                entrySeDeserializer.clear(memoryResource.getAddress() + j);
            }
        }
        size = 0;
    }

    @Override
    public void close() throws Exception {
        for (MemoryResource memoryResource : memoryResources) {
            memoryResource.close();
        }
    }

    private int getPosition(int hash) {
        return hash & modulo;
    }

    /**
     * Returns the address of this position in the hash table.
     */
    private long getAddress(int position) {
        int resourceIndex = position / maxNumEntriesPerMemoryResource;
        int internalPosition = position % maxNumEntriesPerMemoryResource;
        return memoryResources.get(resourceIndex).getAddress()
                + (internalPosition * numBytesPerEntry);
    }

    private void rehash() {
        List<MemoryResource> oldResources = new ArrayList<>(memoryResources);
        try {
            int expectedElements = threshold * 2;
            this.hashTableSize = MapUtils.tableSizeFor(expectedElements, loadFactor);
            this.threshold = Math.max(1, (int) (hashTableSize * loadFactor));
            this.modulo = hashTableSize - 1;

            List<MemoryResource> newResources = allocateMemory(hashTableSize, numBytesPerEntry);
            memoryResources.clear();
            memoryResources.addAll(newResources);
            newResources.clear();

            size = 0;
            K key = keyFactory.get();
            V value = valueFactory.get();
            V prevValue = valueFactory.get();
            for (int i = 0; i < oldResources.size(); i++) {
                MemoryResource memoryResource = oldResources.get(i);
                long address = memoryResource.getAddress();
                long endAddress = memoryResource.getAddress() + memoryResource.capacityInBytes();
                while (address < endAddress) {
                    if (!entrySeDeserializer.isEmpty(address)) {
                        entrySeDeserializer.readKey(address, key);
                        entrySeDeserializer.readValue(address, value);
                        if (put(key, value, prevValue) != null) {
                            throw new IllegalStateException(String
                                    .format("Key %s already has value %s during rehash",
                                            key, prevValue));
                        }
                    }
                    address += numBytesPerEntry;
                }
            }
        } finally {
            for (MemoryResource oldResource : oldResources) {
                try {
                    oldResource.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private List<MemoryResource> allocateMemory(int numElements, int numBytesPerEntry) {
        List<MemoryResource> newResources = new ArrayList<>();
        long requiredMemory = ((long) numElements) * numBytesPerEntry;
        while (requiredMemory > 0) {
            int capacity = (int) Math.min(requiredMemory, MAX_MEMORY_RESOURCE_CAPACITY);
            newResources.add(memoryResourceFactory.apply(capacity));
            requiredMemory -= capacity;
        }
        return newResources;
    }

    private static void checkArguments(int numEntries, float loadFactor, int numBytesPerEntry) {
        if (loadFactor <= 0 || loadFactor >= 1) {
            throw new IllegalArgumentException("loadFactor must be between 0 and 1");
        }
        if (numEntries <= 0) {
            throw new IllegalArgumentException("numEntries must be positive!");
        }
        if (numBytesPerEntry >= MAX_MEMORY_RESOURCE_CAPACITY) {
            throw new IllegalArgumentException(String
                    .format("Given entry length is greater than max allowed entry length %s",
                            MAX_MEMORY_RESOURCE_CAPACITY));
        }
    }
}