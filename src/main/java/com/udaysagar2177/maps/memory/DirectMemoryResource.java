package com.udaysagar2177.maps.memory;

import net.openhft.chronicle.core.OS;

/**
 * An {@link MemoryResource} implementation that allocates memory off-heap but still on RAM.
 *
 * @author uday
 */
public class DirectMemoryResource implements MemoryResource {

    private final int capacityInBytes;
    private final long address;

    public DirectMemoryResource(int capacityInBytes) {
        this.capacityInBytes = capacityInBytes;
        this.address = OS.memory().allocate(capacityInBytes);

        OS.memory().setMemory(address, capacityInBytes, (byte) 0);
        OS.memory().storeFence();
    }

    @Override
    public int capacityInBytes() {
        return capacityInBytes;
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public void close() {
        OS.memory().freeMemory(address, capacityInBytes);
    }
}
