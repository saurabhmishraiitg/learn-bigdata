# Apache Hudi

- [Apache Hudi](#apache-hudi)
  - [Optimization Notes](#optimization-notes)

## Optimization Notes

- Modified property Hudi parallelism
  - `option("hoodie.upsert.shuffle.parallelism", "1000")`
- Replaced Index type `simple`  with `Bloom`
- Added index property
  - `.option("hoodie.bloom.index.parallelism", "1000")`
- On the Spark side
  - persist to disk instead of memory
  - increased the number partitions on the read side from Kafka, thereby exploding 6 input partitions to 'x' tasks on spark
