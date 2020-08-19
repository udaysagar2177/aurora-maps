package io.github.udaysagar2177.maps.sedeserializers;

import io.github.udaysagar2177.maps.OffHeapMap;

/**
 * Interface that allows serialization and deserialization of key-value data onto memory.
 *
 * @param <K> type of Key to be stored in the {@link OffHeapMap}
 * @param <V> type of Value to be stored in the {@link OffHeapMap}
 *
 * @author uday
 */
public interface EntrySeDeserializer<K, V> {

    /**
     * Checks whether the given key is the free key.
     *
     * Free key is a key represented by data in memory that isn't occupied by any entry.
     *
     * @param key the key to check
     * @return true if the given key is free key, false otherwise.
     */
    boolean isFreeKey(K key);

    /**
     * Returns the no. of bytes used per key-value entry.
     */
    int numBytesPerEntry();

    /**
     * Reads the key data from the memory into given key flyweight.
     *
     * @param entryAddress memory address to read the key data from
     * @param usingKey flyweight to read the key data into
     */
    void readKey(long entryAddress, K usingKey);

    /**
     * Reads the value data from the memory into given key flyweight.
     *
     * @param entryAddress memory address to read the value data from
     * @param usingValue flyweight to read the value data into
     */
    void readValue(long entryAddress, V usingValue);

    /**
     * Writes the given key and value data into memory at the given memory address.
     *
     * @param entryAddress memory address to write the key-value data at
     * @param key key to be written
     * @param value value to be written
     */
    void write(long entryAddress, K key, V value);

    /**
     * Hash value of the given key.
     *
     * @param key key to compute the hash for
     * @return hash value of the given key.
     */
    int hash(K key);

    /**
     * Returns true if the entry at the given address is empty.
     *
     * @param entryAddress memory address to read the data from
     * @return true if the entry at the given address is empty.
     */
    boolean isEmpty(long entryAddress);

    /**
     * Clears the entry data at the given address.
     *
     * @param entryAddress memory address to check for empty entry
     */
    void clear(long entryAddress);

    /**
     * Checks whether the key at the given address is equal to the given key.
     *
     * @param entryAddress memory address to read the key data from
     * @param key key to compare
     * @return true if the key at the given address is equal to given key.
     */
    boolean equalsKey(long entryAddress, K key);

    /**
     * Copies the entry data from one address to another address.
     *
     * @param fromAddress address to copy the entry data from
     * @param toAddress address to copy the entry data to
     */
    void copy(long fromAddress, long toAddress);
}
