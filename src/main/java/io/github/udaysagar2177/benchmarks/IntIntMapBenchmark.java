package io.github.udaysagar2177.benchmarks;

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


import io.github.udaysagar2177.maps.OffHeapMap;
import io.github.udaysagar2177.maps.OffHeapMapImpl;
import io.github.udaysagar2177.maps.memory.DirectMemoryResource;
import io.github.udaysagar2177.maps.memory.MmapMemoryResource;
import io.github.udaysagar2177.maps.sedeserializers.IntHolder;
import io.github.udaysagar2177.maps.sedeserializers.IntIntEntrySeDeserializer;
import io.github.udaysagar2177.maps.utils.RandomUtils;
import net.openhft.chronicle.core.values.IntValue;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.values.Values;

/**
 * Benchmarks for {@link OffHeapMap} impls in comparison to Koloboke & Chronicle maps.
 *
 * Benchmark                              Mode  Cnt         Score         Error  Units
 * IntIntMapBenchmark.testGetOnChronicleMap    thrpt  100   9749814.908 ±  226087.135  ops/s
 * IntIntMapBenchmark.testGetOnKolobokeMap     thrpt  100  24617367.499 ±  878137.939  ops/s
 * IntIntMapBenchmark.testGetOnMmapOffHeapMap  thrpt  100  20850211.560 ± 1047023.575  ops/s
 * IntIntMapBenchmark.testGetOnOffHeapMap      thrpt  100  19598000.585 ±  714347.341  ops/s
 * IntIntMapBenchmark.testPutOnChronicleMap    thrpt  100         1.633 ±       0.035  ops/s
 * IntIntMapBenchmark.testPutOnKolobokeMap     thrpt  100        24.604 ±       0.895  ops/s
 * IntIntMapBenchmark.testPutOnMmapOffHeapMap  thrpt  100        15.996 ±       0.601  ops/s
 * IntIntMapBenchmark.testPutOnOffHeapMap      thrpt  100        16.791 ±       0.618  ops/s
 *
 * @author uday
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Fork(1)
@Warmup(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
public class IntIntMapBenchmark {

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
        IntIntMapBenchmark mapBenchmark = new IntIntMapBenchmark();
        mapBenchmark.setup();
        mapBenchmark.testPutOnOffHeapMap();
    }

}
