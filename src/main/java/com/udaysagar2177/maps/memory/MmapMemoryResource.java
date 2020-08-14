package com.udaysagar2177.maps.memory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

import com.udaysagar2177.maps.utils.Preconditions;


import sun.nio.ch.DirectBuffer;

/**
 * An {@link MemoryResource} implementation that allocates memory by memory-mapping a file.
 *
 * The file used for memory-mapping is deleted and then re-created before use. All new files used
 * by this resource have an increasing file index in the file name to avoid conflicts between two
 * or more map objects using this resource type.
 *
 * @author uday
 */
public class MmapMemoryResource implements MemoryResource {

    private static final AtomicLong nextFileIndex = new AtomicLong(0);

    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel fileChannel;
    private final DirectBuffer byteBuffer;
    private final long address;
    private final int capacityInBytes;

    public MmapMemoryResource(String dataFolderPath, int capacityInBytes) {
        this(dataFolderPath, "offHeapMap", capacityInBytes);
    }

    public MmapMemoryResource(String dataFolderPath, String filePrefix, int capacityInBytes) {
        this.capacityInBytes = capacityInBytes;
        this.file = new File(String.format("%s/%s_%d.dat", dataFolderPath, filePrefix,
                nextFileIndex.getAndIncrement()));
        try {
            if (file.exists()) {
                Preconditions.checkState(file.delete(),
                        String.format("Unable to delete file %s before init",
                                file.getAbsolutePath()));
            }
            Preconditions.checkState(file.createNewFile(),
                    String.format("Unable to create file %s", file.getAbsolutePath()));
            this.raf = new RandomAccessFile(file, "rw");
            this.fileChannel = raf.getChannel();
            this.byteBuffer = (DirectBuffer) fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, capacityInBytes);
            this.address = byteBuffer.address();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void close() throws Exception {
        byteBuffer.cleaner().clean();
        fileChannel.close();
        raf.close();
        if (!file.delete()) {
            throw new IllegalStateException(String.format("Unable to delete file %s", file.getAbsolutePath()));
        }
    }
}
