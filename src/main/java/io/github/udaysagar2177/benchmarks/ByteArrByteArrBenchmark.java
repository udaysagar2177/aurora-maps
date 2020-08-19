package io.github.udaysagar2177.benchmarks;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import com.koloboke.collect.map.ObjObjMap;
import com.koloboke.collect.map.hash.HashObjObjMaps;


import io.github.udaysagar2177.maps.OffHeapMap;
import io.github.udaysagar2177.maps.OffHeapMapImpl;
import io.github.udaysagar2177.maps.memory.DirectMemoryResource;
import io.github.udaysagar2177.maps.memory.MmapMemoryResource;
import io.github.udaysagar2177.maps.sedeserializers.ByteArrByteArrEntrySeDeserializer;
import io.github.udaysagar2177.maps.sedeserializers.ByteArrHolder;
import net.openhft.chronicle.map.ChronicleMap;

/**
 * Benchamrks for {@link OffHeapMap} impls that uses byte[] as key and values, in comparison to
 * Koloboke & Chronicle maps.
 *
 * @author uday
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
@Warmup(iterations = 10, time = 10000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 10000, timeUnit = TimeUnit.MILLISECONDS)
public class ByteArrByteArrBenchmark {

    private static final Random RANDOM = new Random();

    private int SIZE = 1_000_000;
    private int KEY_LENGTH = 24;
    private int VALUE_LENGTH = 42;

    private ObjObjMap<byte[], byte[]> kolobokeMap;
    private ChronicleMap<byte[], byte[]> chronicleMap;
    private OffHeapMap<ByteArrHolder, ByteArrHolder> offHeapMap;
    private OffHeapMap<ByteArrHolder, ByteArrHolder> mmapOffHeapMap;

    private byte[] key = new byte[KEY_LENGTH];
    private byte[] value = new byte[VALUE_LENGTH];
    private ByteArrHolder keyHolder = new ByteArrHolder(KEY_LENGTH);
    private ByteArrHolder valueHolder = new ByteArrHolder(VALUE_LENGTH);

    @Setup
    public void setup() {
        kolobokeMap = HashObjObjMaps.newMutableMap(SIZE);
        chronicleMap = ChronicleMap
                .of(byte[].class, byte[].class)
                .averageKeySize(KEY_LENGTH)
                .averageValueSize(VALUE_LENGTH)
                .name("a")
                .entries(SIZE)
                .putReturnsNull(true).create();
        offHeapMap = new OffHeapMapImpl<>(SIZE, 0.66f,
                new ByteArrByteArrEntrySeDeserializer(KEY_LENGTH, VALUE_LENGTH),
                DirectMemoryResource::new, () -> new ByteArrHolder(KEY_LENGTH),
                () -> new ByteArrHolder(VALUE_LENGTH));
        mmapOffHeapMap = new OffHeapMapImpl<>(SIZE, 0.66f,
                new ByteArrByteArrEntrySeDeserializer(KEY_LENGTH, VALUE_LENGTH),
                (capacityInBytes) -> new MmapMemoryResource("/tmp", capacityInBytes),
                () -> new ByteArrHolder(KEY_LENGTH),
                () -> new ByteArrHolder(VALUE_LENGTH));
    }

    @TearDown
    public void tearDown() throws Exception {
        mmapOffHeapMap.close();
        offHeapMap.close();
        chronicleMap.close();
    }

    @Benchmark
    public byte[] testGetOnKolobokeMap() {
        return kolobokeMap.get(getBytes(keyHolder.getBytes(), KEY_LENGTH));
    }

    @Benchmark
    public byte[] testGetOnChronicleMap() {
        getBytes(key, KEY_LENGTH);
        return chronicleMap.getUsing(key, value);
    }

    @Benchmark
    public ByteArrHolder testGetOnOffHeapMap() {
        return benchmarkGetOnOffHeapMap(offHeapMap);
    }

    @Benchmark
    public ByteArrHolder testGetOnMmapOffHeapMap() {
        return benchmarkGetOnOffHeapMap(mmapOffHeapMap);
    }

    private ByteArrHolder benchmarkGetOnOffHeapMap(
            OffHeapMap<ByteArrHolder, ByteArrHolder> offHeapMap) {
        getBytes(keyHolder.getBytes(), KEY_LENGTH);
        return offHeapMap.get(keyHolder, valueHolder);
    }

    @Benchmark
    public byte[] testPutOnKolobokeMap() {
        kolobokeMap.clear();
        int size = SIZE;
        for (int i = 0; i < size; i++) {
            getBytes(null, KEY_LENGTH);
            getBytes(null, VALUE_LENGTH);
            kolobokeMap.put(keyHolder.getBytes(), valueHolder.getBytes());
        }
        return kolobokeMap.get(getBytes(keyHolder.getBytes(), KEY_LENGTH));
    }

    @Benchmark
    public byte[] testPutOnChronicleMap() {
        chronicleMap.clear();
        for (int i = 0; i < SIZE; i++) {
            getBytes(keyHolder.getBytes(), KEY_LENGTH);
            getBytes(valueHolder.getBytes(), VALUE_LENGTH);
            chronicleMap.put(key, value);
        }
        getBytes(keyHolder.getBytes(), KEY_LENGTH);
        return chronicleMap.getUsing(key, value);
    }

    @Benchmark
    public ByteArrHolder testPutOnOffHeapMap() {
        return benchmarkPutOnOffHeapMap(offHeapMap);
    }

    @Benchmark
    public ByteArrHolder testPutOnMmapOffHeapMap() {
        return benchmarkPutOnOffHeapMap(mmapOffHeapMap);
    }

    private ByteArrHolder benchmarkPutOnOffHeapMap(
            OffHeapMap<ByteArrHolder, ByteArrHolder> offHeapMap) {
        offHeapMap.clear();
        int size = SIZE;
        for (int i = 0; i < size; i++) {
            getBytes(keyHolder.getBytes(), KEY_LENGTH);
            getBytes(valueHolder.getBytes(), VALUE_LENGTH);
            offHeapMap.put(keyHolder, valueHolder, null);
        }
        getBytes(keyHolder.getBytes(), KEY_LENGTH);
        return offHeapMap.get(keyHolder, valueHolder);
    }

    private static byte[] getBytes(byte[] data, int length) {
        if (data == null) {
            data = new byte[length];
        }
        for (int i = 0; i < length; i++) {
            data[i] = (byte) RANDOM.nextInt(128);
        }
        return data;
    }

    public static void main(String[] args) {
        ByteArrByteArrBenchmark mapBenchmark = new ByteArrByteArrBenchmark();
        mapBenchmark.setup();
        mapBenchmark.testPutOnOffHeapMap();
    }

}