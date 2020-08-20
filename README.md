# aurora-maps


The goal of this repository is to create fastest off-heap maps in Java for non-concurrent use
cases.

Currently, Chronicle maps are the best choice to store map data off-heap but due to its
concurrency support, it delivers poor performance for non-concurrent use cases. Performance
target of this repository is to get close to efficient on-heap implementations like Koloboke
maps. To keep it simple, only fixed size key-values are supported. If you need support for
variable size key-values, extra effort must be taken to store the data of variable size at some
other location and use the indices to that data in these maps.

### Quick start:
Add the following dependency in your Maven `pom.xml`:
```
<dependency>
  <groupId>io.github.udaysagar2177</groupId>
  <artifactId>aurora-maps</artifactId>
  <version>0.0.1</version>
</dependency>
```
Then, you can initialize and use a new off-heap map as shown below.

#### Example:

```java
public class IntIntMapExample {
    /**
     * Simple example that shows how to build a int-int map on off-heap memory.
     */
    public void example() throws Exception {

        // map creation
        OffHeapMapImpl<IntHolder, IntHolder> map = new OffHeapMapImpl<>(1, 0.66f,
                new IntIntEntrySeDeserializer(), DirectMemoryResource::new,
                IntHolder::new, IntHolder::new);

        try {
            // flyweights to be used to put or get data
            IntHolder key = new IntHolder();
            IntHolder putValue = new IntHolder();
            IntHolder getValue = new IntHolder();

            // put
            map.put(key.setInt(12), putValue.setInt(20), getValue);
            // get
            map.get(key, getValue);
            // remove
            map.remove(key, getValue);
            // clear for re-use
            map.clear();
        } finally {
            // close map and release off-heap memory
            map.close();
        }
    }
}
```

 This repository also provides support for short-short, long-long, byte[]-byte[] key values through `ShortShortEntrySeDeserializer`, `LongLongEntrySeDeserializer` and 
 `ByteArrByteArrEntrySeDeserializer`. Additional data types support can be added by extending `EntrySeDeserializer` interface.

 ### Mmap data to disk

If you want to create the map with memory mapped to disk, you can simply replace `DirectMemoryResource::new` in the above example with 
`(capacityInBytes) -> new MmapMemoryResource("/tmp", "intIntMap", capacityInBytes)`. Here, "/tmp"
 is the folder name inside which the data will be mapped. "intIntMap" is the file prefix used to
 create new files when scaling up the map. Same folder/file name can be used for multiple map
 objects because MmapMemoryResource appends an increasing id to all new file names.

### Performance comparison:

#### primitive int-int map
##### Single get performance
```
Benchmark                                   Mode   Cnt         Score         Error  Units
IntIntMapBenchmark.testGetOnChronicleMap    thrpt  100   9749814.908 ±  226087.135  ops/s
IntIntMapBenchmark.testGetOnKolobokeMap     thrpt  100  24617367.499 ±  878137.939  ops/s
IntIntMapBenchmark.testGetOnMmapOffHeapMap  thrpt  100  20850211.560 ± 1047023.575  ops/s
IntIntMapBenchmark.testGetOnOffHeapMap      thrpt  100  19598000.585 ±  714347.341  ops/s
```

##### 1 million insertions:
```
Benchmark                                    Mode  Cnt         Score         Error  Units
IntIntMapBenchmark.testPutOnChronicleMap    thrpt  100         1.633 ±       0.035  ops/s
IntIntMapBenchmark.testPutOnKolobokeMap     thrpt  100        24.604 ±       0.895  ops/s
IntIntMapBenchmark.testPutOnMmapOffHeapMap  thrpt  100        15.996 ±       0.601  ops/s
IntIntMapBenchmark.testPutOnOffHeapMap      thrpt  100        16.791 ±       0.618  ops/s
```

#### byte[]-byte[] map

* key size: 24, value size: 42

##### Single get performance
```
Benchmark                                         Mode  Cnt        Score      Error  Units
ByteArrByteArrBenchmark.testGetOnChronicleMap    thrpt   60  2339801.966 ± 7619.689  ops/s
ByteArrByteArrBenchmark.testGetOnKolobokeMap     thrpt   60  3730351.924 ± 3920.856  ops/s
ByteArrByteArrBenchmark.testGetOnMmapOffHeapMap  thrpt   60  2499914.883 ± 5112.507  ops/s
ByteArrByteArrBenchmark.testGetOnOffHeapMap      thrpt   60  2496837.620 ± 3866.586  ops/s
```

##### 1 million insertions:
```
Benchmark                                         Mode  Cnt        Score      Error  Units
ByteArrByteArrBenchmark.testPutOnChronicleMap    thrpt   60        0.927 ±    0.005  ops/s
ByteArrByteArrBenchmark.testPutOnKolobokeMap     thrpt   60        1.326 ±    0.002  ops/s
ByteArrByteArrBenchmark.testPutOnMmapOffHeapMap  thrpt   60        0.856 ±    0.017  ops/s
ByteArrByteArrBenchmark.testPutOnOffHeapMap      thrpt   60        0.984 ±    0.001  ops/s
```

Integer key-value maps are 10x faster than ChronicleMap on puts and 2x on gets. But byte array key-value maps are performing same as 
ChronicleMap, that needs to be investigated and improved.

TODO:
1. More performance improvements
2. Add append-only map wrapper, i.e., a map that doesn't support removal and key-value overrides for
 single writer multiple readers use case
3. Move chronicle map dependency to benchmarks folder

Thanks to https://github.com/mikvor/hashmapTest & https://github.com/OpenHFT/Chronicle-Map for
inspiring this work.