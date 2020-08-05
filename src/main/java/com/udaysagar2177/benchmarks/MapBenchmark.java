package com.udaysagar2177.benchmarks;

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

import com.koloboke.collect.map.IntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import com.udaysagar2177.maps.IntHolder;
import com.udaysagar2177.maps.IntIntEntrySeDeserializer;
import com.udaysagar2177.maps.OffHeapMap;
import com.udaysagar2177.maps.OffHeapMapImpl;
import com.udaysagar2177.maps.memory.DirectMemoryResource;
import com.udaysagar2177.maps.memory.MmapMemoryResource;
import com.udaysagar2177.maps.utils.RandomUtils;


import net.openhft.chronicle.core.values.IntValue;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.values.Values;

/**
 * Benchmarks for {@link OffHeapMap} impls in comparison to Koloboke & Chronicle maps.
 *
 * Benchmark                              Mode  Cnt         Score         Error  Units
 * MapBenchmark.testGetOnChronicleMap    thrpt   10   7658662.823 ±  391085.881  ops/s
 * MapBenchmark.testGetOnKolobokeMap     thrpt   10  19729611.919 ± 1112444.077  ops/s
 * MapBenchmark.testGetOnMmapOffHeapMap  thrpt   10  14932054.535 ± 1148252.476  ops/s
 * MapBenchmark.testGetOnOffHeapMap      thrpt   10  17817330.026 ± 1974688.265  ops/s
 * MapBenchmark.testPutOnChronicleMap    thrpt   10         1.633 ±       0.022  ops/s
 * MapBenchmark.testPutOnKolobokeMap     thrpt   10        19.164 ±       1.837  ops/s
 * MapBenchmark.testPutOnMmapOffHeapMap  thrpt   10        14.356 ±       2.228  ops/s
 * MapBenchmark.testPutOnOffHeapMap      thrpt   10        13.197 ±       0.826  ops/s
 *
 * @author uday
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
@Warmup(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
public class MapBenchmark {

    private int SIZE = 1_000_000;

    private IntIntMap intIntMap;
    private ChronicleMap<IntValue, IntValue> chronicleMap;
    private OffHeapMap<IntHolder, IntHolder> offHeapMap;
    private OffHeapMap<IntHolder, IntHolder> mmapOffHeapMap;

    private IntValue key = Values.newHeapInstance(IntValue.class);
    private IntValue value = Values.newHeapInstance(IntValue.class);
    private IntHolder keyHolder = new IntHolder();
    private IntHolder valueHolder = new IntHolder();

    @Setup
    public void setup() {
        intIntMap = HashIntIntMaps.newMutableMap(SIZE);
        chronicleMap = ChronicleMap
                .of(IntValue.class, IntValue.class)
                .name("a")
                .entries(SIZE)
                .putReturnsNull(true).create();
        offHeapMap = new OffHeapMapImpl<>(SIZE, 0.66f, new IntIntEntrySeDeserializer(),
                DirectMemoryResource::new, IntHolder::new, IntHolder::new);
        mmapOffHeapMap = new OffHeapMapImpl<>(SIZE, 0.66f, new IntIntEntrySeDeserializer(),
                (capacityInBytes) -> new MmapMemoryResource("/tmp", capacityInBytes),
                IntHolder::new, IntHolder::new);
    }

    @TearDown
    public void tearDown() throws Exception {
        mmapOffHeapMap.close();
        offHeapMap.close();
        chronicleMap.close();
    }

    @Benchmark
    public int testGetOnKolobokeMap() {
        return intIntMap.get(RandomUtils.randInt(Integer.MAX_VALUE));
    }

    @Benchmark
    public IntValue testGetOnChronicleMap() {
        key.setValue(RandomUtils.randInt(Integer.MAX_VALUE));
        return chronicleMap.getUsing(key, value);
    }

    @Benchmark
    public IntHolder testGetOnOffHeapMap() {
        return benchmarkGetOnOffHeapMap(offHeapMap);
    }

    @Benchmark
    public IntHolder testGetOnMmapOffHeapMap() {
        return benchmarkGetOnOffHeapMap(mmapOffHeapMap);
    }

    private IntHolder benchmarkGetOnOffHeapMap(OffHeapMap<IntHolder, IntHolder> offHeapMap) {
        keyHolder.setInt(1 + RandomUtils.randInt(100000000));
        return offHeapMap.get(keyHolder, valueHolder);
    }

    @Benchmark
    public int testPutOnKolobokeMap() {
        intIntMap.clear();
        int size = SIZE;
        for (int i = 0; i < size; i++) {
            int random = RandomUtils.randInt(Integer.MAX_VALUE);
            intIntMap.put(random, random);
        }
        return intIntMap.get(RandomUtils.randInt(Integer.MAX_VALUE));
    }

    @Benchmark
    public IntValue testPutOnChronicleMap() {
        chronicleMap.clear();
        for (int i = 0; i < SIZE; i++) {
            int random = RandomUtils.randInt(Integer.MAX_VALUE);
            key.setValue(random);
            value.setValue(random);
            chronicleMap.put(key, value);
        }
        key.setValue(RandomUtils.randInt(Integer.MAX_VALUE));
        return chronicleMap.getUsing(key, value);
    }

    @Benchmark
    public IntHolder testPutOnOffHeapMap() {
        return benchmarkPutOnOffHeapMap(offHeapMap);
    }

    @Benchmark
    public IntHolder testPutOnMmapOffHeapMap() {
        return benchmarkPutOnOffHeapMap(mmapOffHeapMap);
    }

    private IntHolder benchmarkPutOnOffHeapMap(OffHeapMap<IntHolder, IntHolder> offHeapMap) {
        offHeapMap.clear();
        int size = SIZE;
        for (int i = 0; i < size; i++) {
            int random = RandomUtils.randInt(100000000);
            keyHolder.setInt(random);
            valueHolder.setInt(random);
            offHeapMap.put(keyHolder, valueHolder, null);
        }
        keyHolder.setInt(RandomUtils.randInt(100000000));
        return offHeapMap.get(keyHolder, valueHolder);
    }

    public static void main(String[] args) {
        MapBenchmark mapBenchmark = new MapBenchmark();
        mapBenchmark.setup();
        mapBenchmark.testPutOnOffHeapMap();
    }

}
