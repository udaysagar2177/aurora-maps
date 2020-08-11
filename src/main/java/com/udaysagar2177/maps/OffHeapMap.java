package com.udaysagar2177.maps;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.udaysagar2177.maps.sedeserializers.EntrySeDeserializer;

/**
 * Interface for {@link Map} like implementations that store key-values off-heap and provide high
 * performance access while avoiding GC pressure. Due to necessity of flyweights in accessing
 * off-heap data and very specialized use cases for this kind of map, this interface supports just a
 * few popular map functionalities.
 *
 * This map doesn't support null keys or values. The implementations can choose to allow concurrent
 * reads or writes.
 *
 * @param <K>
 *         a flyweight object that holds data that can be easily read/written using
 *         {@link EntrySeDeserializer}.
 * @param <V>
 *         a flyweight object that holds data that can be easily read/written using
 *         {@link EntrySeDeserializer}.
 * @author uday
 */
public interface OffHeapMap<K, V> extends AutoCloseable {

    /**
     * Associates the specified value with the specified key in this map. If the map previously
     * contained a mapping for the key, the old value is replaced by the specified value. The old
     * value is collected in the given previousValueHolder param when it is not {@code null}.
     *
     * @param key
     *         key with which the specified value is to be associated
     * @param value
     *         value to be associated with the specified key
     * @param previousValueHolder
     *         a variable to hold previous value associated with key before this update. If
     *         {@code null} is supplied, the old value is not collected
     * @return the previous value associated with key or {@code null} if there was no mapping for
     * the given key.
     */
    V put(@NotNull K key, @NotNull V value, V previousValueHolder);

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains
     * no mapping for the key.
     *
     * @param key
     *         the key whose associated value is to be returned
     * @param usingValue
     *         a variable to hold the data read from off-heap location.
     * @return the value to which the specified key is mapped, or {@code null} if this map contains
     * no mapping for the key
     */
    V get(@NotNull K key, @NotNull V usingValue);

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key
     *         key whose mapping is to be removed from the map
     * @param usingValue
     *         a variable to hold previous value associated with key before this update. If
     *         {@code null} is supplied, the old value is not collected
     * @return the previous value associated with key if value flyweight is provided. Returns
     *         {@code null} if value flyweight is null or if there was no mapping for key.
     * @throws UnsupportedOperationException
     *         if remove operation is not supported by this map
     */
    V remove(@NotNull K key, V usingValue);

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    int size();

    /**
     * Removes all of the mappings from this map (optional operation). The map will be empty after
     * this call returns.
     *
     * @throws UnsupportedOperationException
     *         if clear operation is not supported by this map
     */
    void clear();

    /**
     * Closes the map thereby releasing any resources.
     */
    @Override
    void close() throws Exception;
}
