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
Benchmark                              Mode  Cnt         Score         Error  Units
MapBenchmark.testGetOnChronicleMap    thrpt   10   7658662.823 ±  391085.881  ops/s
MapBenchmark.testGetOnKolobokeMap     thrpt   10  19729611.919 ± 1112444.077  ops/s
MapBenchmark.testGetOnMmapOffHeapMap  thrpt   10  14932054.535 ± 1148252.476  ops/s
MapBenchmark.testGetOnOffHeapMap      thrpt   10  17817330.026 ± 1974688.265  ops/s

```

##### Putting 1 million integer key-values into map
```
Benchmark                              Mode  Cnt         Score         Error  Units
MapBenchmark.testPutOnChronicleMap    thrpt   10         1.633 ±       0.022  ops/s
MapBenchmark.testPutOnKolobokeMap     thrpt   10        19.164 ±       1.837  ops/s
MapBenchmark.testPutOnMmapOffHeapMap  thrpt   10        14.356 ±       2.228  ops/s
MapBenchmark.testPutOnOffHeapMap      thrpt   10        13.197 ±       0.826  ops/s
```

TODO:
1. Investigate and Improve performance of offheap-maps compared to koloboke maps
2. Add append-only map, i.e., a map that doesn't support removal and key-value overrides for
specific single writer multiple reader use cases
3. Move chronicle map dependency to benchmarks folder

Thanks to https://github.com/mikvor/hashmapTest & https://github.com/OpenHFT/Chronicle-Map for
inspiring this work.