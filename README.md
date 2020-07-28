# offheap-maps


The goal of this repository is to create fastest off-heap maps in Java for non-concurrent use
cases.

Currently, Chronicle maps are the best choice to store map data off-heap but due to its
concurrency support, it delivers poor performance for non-concurrent use cases. Performance
target of this repository is to get close to efficient on-heap implementations like Koloboke
maps. To keep it simple, only fixed size key-values are supported. If you need support for
variable sized key-values, extra effort must be taken to store variable size data at some other
location and use the indices to that data in these maps.

#### Performance comparison:

##### Getting a single key from map
```
Benchmark                             Mode  Cnt   Score   Error  Units
MapBenchmark.testGetOnChronicleMap    avgt   10  63.794 ± 4.410  ns/op
MapBenchmark.testGetOnMmapOffHeapMap  avgt   10  20.397 ± 3.365  ns/op
MapBenchmark.testGetOnOffHeapMap      avgt   10  24.881 ± 2.918  ns/op
MapBenchmark.testGetOnKolobokeMap     avgt   10  13.296 ± 0.146  ns/op

```

##### Putting 100K int-int key-values into map
```
Benchmark                             Mode  Cnt   Score   Error  Units
MapBenchmark.testPutOnChronicleMap    avgt   10  77.326 ± 4.900  ms/op
MapBenchmark.testPutOnMmapOffHeapMap  avgt   10   6.755 ± 0.248  ms/op
MapBenchmark.testPutOnOffHeapMap      avgt   10   7.702 ± 0.441  ms/op
MapBenchmark.testPutOnKolobokeMap     avgt   10   5.648 ± 0.989  ms/op
```

TODO:
1. Add map#remove support
2. Investigate and Improve performance of offheap maps
3. Add append-only map, i.e., a map that doesn't support removal and key-value overrides for
specific single writer multiple reader use cases
4. Move chronicle map dependency to benchmarks folder

Thanks to https://github.com/mikvor/hashmapTest & https://github.com/OpenHFT/Chronicle-Map for
inspiring this work.