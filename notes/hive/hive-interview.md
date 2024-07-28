# Hive Interview

## Hands On

- **Question** Write a SQL query to delete all duplicate email entries in a table named Person, keeping only unique emails based on its smallest Id.

    | Id | Email            |
    +----+------------------+
    | 1  | john@example.com |
    | 2  | bob@example.com  |
    | 3  | john@example.com |
    +----+------------------+

  - Id is the primary key column for this table.
  - For example, after running your query, the above Person table should have the following rows:

    | id | email            |
    +----+------------------+
    | 1  | john@example.com |
    | 2  | bob@example.com  |
    +----+------------------+

    **Solution-1** : Select min(id), email from table_name group by email

- **Question** Wordcount+, provide the nth most occuring word and also the files it's part of.

    ****Sample Data**** \
    Hadoop is the Elephant King!
    A yellow and elegant thing. \
    He never forgets
    Useful data, or lets \
    An extraneous element cling!
    A wonderful king is Hadoop. \
    The elephant plays well with Sqoop.
    But what helps him to thrive \
    Are Impala, and Hive,
    And HDFS in the group.

- **Question** Calculate # of sessions based upon following dataset. Assuming a single session is defined as consecutive activities by a user with difference < 3m b/w each.

    | SiteId | Userid | Time | Date | Activity Type |
    |---| --- | --- | --- | --- |
    | irctc | usr1 | 10:01 | 10-Mar | Click        |
    | irctc | usr1 | 10:03 | 10-Mar | Scroll       |
    | irctc | usr1 | 10:15 | 10-Mar | Scroll       |
    | irctc | usr2 | 10:01 | 10-Mar | Acknowledge  |
    | irctc | usr2 | 10:20 | 10-Mar | Click        |
    | irctc | usr3 | 10:15 | 10-Mar | Click        |
    | flpkrt | usr1 | 10:01 | 10-Mar | Click       |
    | flpkrt | usr1 | 10:33 | 10-Mar | Click       |
    | flpkrt | usr1 | 10:50 | 10-Mar | Acknowledge |
    | flpkrt | usr1 | 10:51 | 10-Mar | Acknowledge |
    | flpkrt | usr1 | 11:05 | 10-Mar | Acknowledge |
    | flpkrt | usr1 | 11:19 | 10-Mar | Acknowledge |
    | flpkrt | usr2 | 10:01 | 10-Mar | Scroll      |
    | flpkrt | usr2 | 10:10 | 10-Mar | Click       |
    | flpkrt | usr3 | 10:55 | 10-Mar | Scroll      |

    ****Answer****

    | SiteId | Date | Session Count |
    | irctc | 10-Mar | 5 |
    | flpkrt | 10-Mar | 8 |

- **Question** Implement Wordcount in HIVE?
  - **TBA**

- **Question** Find out EMP_NAMES having salary more than their manager

    EMPLOYEE TABLE

    EMP_ID | EMP_NAME | MANAGER_EMP_ID | SALARY
    1      | RAVI     | 3              | 2000
    2      | HARRY | 1              | 1000
    3      | VIJAY    | 7             | 1500
    4      | SAM      | 11            |   500
    11      | RAHUL | 12           | 1500

    > hint : use same table
    >

- **Question** We have 2 complex columns in a table (e.g. Array). They are of the same size/length. We need to write a query to join the elements of both the columns based upon position. The size of array is variable for different rows.
  - e.g.
    - COL1 : ["a","b","c"]
    - COL2 : ["i","j","k"]
    - Output (3 rows)
     a:i
     b:j
     c:k

  - **Solution**
    - Use `posexplode`, which provide a positional value after exploding a column. Can explode both columns and use where condition to join the same positional records
    - Create a UDF to take both columns as input and internally iterate and generate an array, which can then be explode to give multiple rows

## Theoritical

- **Question** What are columnar data format? In what use-cases are they leveraged.
  - **Answer**
  - Size
  - Select
  - Indexing

- **Question** Explain about how the data is stored in any one columnar format.
- **Question** Explain about some optimization scenarios you have worked upon in HIVE

  - Have a problem statement where all these/some of these techniques are being used
  - <https://towardsdatascience.com/apache-hive-optimization-techniques-1-ce55331dbf5e>
  - <https://towardsdatascience.com/apache-hive-optimization-techniques-2-e60b6200eeca>?
  - Partitioning
  - Bucketing
  - TEZ engine
    - Skips the DFS write by the reducer and piping the output directly to subsequent mapper as input
    - Cascading series of reducer without intervening mapper steps
    - Re-use of containers for successive phase of processing
    - Optimal resource usage using pre-warmed containers
    - Cost based optimizations
    - Vectorized query processing
    - Determining reducer count
      - <https://community.cloudera.com/t5/Community-Articles/Hive-on-Tez-Performance-Tuning-Determining-Reducer-Counts/ta-p/245680>
  - Compression
    - Tradeoff between CPU cost of compression and decompression vs IO savings
  - ORC Format
    - Higher level of compression
    - Ability to skip scanning an entire range of rows within a block if irrelevant to the query using light weight indexes stored within file
    - Ability to skip decompression of rows within a block, if irrelevant to the query
    - Single file as output of each task
    - Supports multiple streams to read the file simultaneously
    - Keeps metadata stores in file using Protocol Buffers, used for serializing structured data
    - Push down predicates
      - Min/Max value for each column and row positions within each column
    - Bloom Filters
      - Bloom Filters is a probabilistic data structure that tells us whether an element is present in a set or not by using a minimal amount of memory. A catchy thing about bloom filters is that they will occasionally incorrectly answer that an element is present when it is not. Their saving grace, however, is that they will never tell you that an element is not present if it is.
      - Bloom Filters again helps in the push-down predicates for ORC File formats. If a Bloom filter is specified for a column, even if the min/max values in a row-group's index say that a given column value is within the range for the row group, the Bloom filter can answer specifically whether the value is actually present. If there is a significant probability that values in a where clause will not be present, this can save a lot of pointless data manipulation.
    - Vectorization
      - Vectorization optimizes the query execution by processing a block of 1024 rows at a time, inside which each row is saved as a vector. On vectors, simple arithmetic and comparison operations are done very quickly. Execution is speeded up because within a large block of data of the same type, the compiler can generate code to execute identical functions in a tight loop without going through the long code path that would be required for independent function calls. This SIMD-like behavior gives rise to a host of low-level efficiencies: fewer instructions executed, superior caching behavior, improved pipelining, more favorable TLB behavior, etc.
  - Join Optimizations
    - STREAMTABLE
    - Multi-way Join
    - Map Join
    - Bucket Map Join
    - SMB Join (Sort-Merge-Bucket)
    - Skew Join
  - Cost Based Optimizations (CBO)
    - Logical Optimization
      - Projection Pruning
      - Deducing Transitive Predicates
      - Predicate Pushdown
      - Merging of Select-Select, Filter-Filter into a single operator
      - Multi-way Join
      - Query Rewrite to accommodate Join skew on some column values
    - Physical Optimization
      - Partition Pruning
      - Scan pruning based on partitions and bucketing
      - Scan pruning if a query is based on sampling
      - Apply Group By on map side in some cases
      - Optimize Union so that union can be performed on map side only
      - Decide which table to stream last, based on user hint in a multiway join
      - Remove unnecessary reduce sink operators
      - For queries with limit clause, reduce the number of files that needs to be scanned for the table

- **Question** Implement SCD Type 2 storage in HIVE. Also, explain what are different types of SCD implementations.
- **Question** Difference b/w AVRO and ORC File format? Which one to use for which use-case?
- **Question** Explain about Predicate Pushdown optimization in HIVE? How to enable and when to use it?
- **Scenario** : Schema of the source data system changes without any prior notice. How to handle such scenarios. What requirements you can enforce or design to address such automatically.

- **Windowing Functions in HIVE**

  - <http://shzhangji.com/blog/2017/09/04/hive-window-and-analytical-functions/>
  - Window Partition
    - PARTITION clause divides result set into window partitions by one or more columns, and the rows within can be optionally sorted by one or more columns. If there's not PARTITION BY, the entire result set is treated as a single partition; if there's not ORDER BY, window frames cannot be defined, and all rows within the partition constitutes a single frame.
  - Window Frame
  - Window Function
  - Implementation Detail
    - <https://www.slideshare.net/Hadoop_Summit/analytical-queries-with-hive>
    - Window query consists of two steps: divide records into partitions, and evaluate window functions on each of them. The partitioning process is intuitive in map-reduce paradigm, since Hadoop will take care of the shuffling and sorting. However, ordinary UDAF can only return one row for each group, but in window query, there need to be a table in, table out contract. So the community introduced Partitioned Table Function (PTF) into Hive.
    - PTF, as the name suggests, works on partitions, and inputs / outputs a set of table rows. The following sequence diagram lists the major classes of PTF mechanism. PTFOperator reads data from sorted source and create input partitions; WindowTableFunction manages window frames, invokes window functions (UDAF), and writes the results to output partitions.
