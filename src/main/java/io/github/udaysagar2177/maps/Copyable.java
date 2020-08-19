package io.github.udaysagar2177.maps;

/**
 * Defines a contract to be able to copy data from one object to another.
 *
 * @author uday
 */
public interface Copyable<K> {

    /**
     * Copy the data from the given parameter to this.
     *
     * @param from object to copy the data from.
     */
    void copyFrom(K from);
}
