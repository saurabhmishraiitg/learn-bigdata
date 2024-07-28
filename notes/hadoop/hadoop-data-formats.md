# Hadoop Data Formats

Hadoop supports numerous data formats, each of them having their own weakness and strenght. Following is a short comparison on strengths/weaknesses of 3 of these data formats

- AVRO
- ORC
- Parquet

|                      | AVRO                               | ORC                           | Parquet                        |
| -------------------- | ---------------------------------- | ----------------------------- | ------------------------------ |
| Type                 | Row Based                          | Columnar                      | Columnar                       |
| Strengths            | Schema Evolution                   | Compatibility with HIVE       | Efficient for Nested Datatypes |
| Query Pattern        | Write Intensive Jobs -> Row Format | Querying Limited # of Columns | Querying Limited # of Columns  |
| Compression          | Average                            | Best                          | Good                           |
| Platform Support     | Kafka                              | HIVE                          | Spark                          |
| ACID Trnx Support    | No                                 | Yes                           | No                             |
| Indexing             | NA                                 | Block Level                   | ???                            |
| LLAP Caching Support | No                                 | Yes                           | No                             |

- `Avro` is a row-based data format slash a data serialization system released by Hadoop working group in 2009. The data schema is stored as JSON (which means human-readable) in the header while the rest of the data is stored in binary format. One shining point of Avro is its robust support for schema evolution.
- `ORC` is a row columnar data format highly optimized for reading, writing, and processing data in Hive and it was created by Hortonworks in 2013 as part of the Stinger initiative to speed up Hive. ORC files are made of stripes of data where each stripe contains index, row data, and footer (where key statistics such as count, max, min, and sum of each column are conveniently cached).
- `Parquet` is a row columnar data format created by Cloudera and Twitter in 2013. Parquet files consist of row groups, header, and footer, and in each row group data in the same columns are stored together. Parquet is specialized in efficiently storing and processing nested data types.
