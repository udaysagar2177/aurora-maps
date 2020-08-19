package io.github.udaysagar2177.maps.memory;

/**
 * Interface for implementations that represent a memory space that's available for reads/writes.
 *
 * @author uday
 */
public interface MemoryResource extends AutoCloseable {

    /**
     * Capacity of this memory resource.
     *
     * @return No. of bytes available for reads/writes.
     */
    int capacityInBytes();

    /**
     * Beginning address of this memory resource.
     *
     * @return beginning address of this memory resource that can be used to write/read data.
     */
    long getAddress();

    /**
     * Releases this memory resource.
     */
    @Override
    void close() throws Exception;
}
