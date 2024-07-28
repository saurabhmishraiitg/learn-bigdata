# HBase

- [HBase](#hbase)
  - [About](#about)
  - [Hbase Architecture](#hbase-architecture)
    - [Memstore](#memstore)
    - [Some performance Paramters](#some-performance-paramters)
    - [Performance Improvements](#performance-improvements)
    - [Compression](#compression)
    - [WAL](#wal)
    - [Hbase Utilities](#hbase-utilities)
    - [Splitting](#splitting)
    - [HDFS vs Hbase](#hdfs-vs-hbase)
  - [Interview Questions](#interview-questions)

## About

- HBase is C (Consistency), P (Partition Tolerant) as per the CAP Theorem
- The hbase shell can be a made enormously powerful with ruby scripts. An example of the same
  - <http://www.srikanthps.com/2011/07/power-of-ruby-and-hbase-shell.html>
- Access hbase shell in non-interactive mode
  - echo 'list' | hbase shell -n
  - <http://www.cloudera.com/documentation/enterprise/5-4-x/topics/cdh_ig_hbase_shell.html>
  - Or run the scripts having hbase commands as such
    - hbase shell <PATH_TO_SCRIPT>
  - Time.at(time) provide more human-readable information about the date when the cell was added to the table
    - Time.at(1344763701019/1000)
  - Create an .irbrc file for yourself in your home directory. Add customizations. A useful one is command history so commands are save across
   require 'irb/ext/save-history'
   IRB.conf[:SAVE_HISTORY] = 100
   IRB.conf[:HISTORY_FILE] = "#{ENV['HOME']}/.irb-save-history"

- To filter on an column value in Hbase table
  - TBA

- Add a coprocessor to Hbase table
  - alter 'ims:smishra-spark-ing10-0', METHOD => 'table_att','coprocessor'=>'|org.apache.hadoop.hbase.coprocessor.AggregateImplementation||'
- To get the size of a row in hbase table use the following script
  - TBA

## Hbase Architecture

- What's a Hfile
  - <http://blog.cloudera.com/blog/2012/06/hbase-io-hfile-input-output/>
- Hbase Write path
  - <http://blog.cloudera.com/blog/2012/06/hbase-write-path/>
- Visualize Hbase compactions
  - <http://www.ngdata.com/visualizing-hbase-flushes-and-compactions/>
- Write to Hbase performance
  - <http://hbase.apache.org/0.94/book/perf.writing.html>

### Memstore

- The main reason for using Memstore is the need to store data on DFS ordered by row key. As HDFS is designed for sequential reads/writes, with no file modifications allowed, HBase cannot efficiently write data to disk as it is being received: the written data will not be sorted (when the input is not sorted) which means not optimized for future retrieval. To solve this problem HBase buffers last received data in memory (in Memstore), "sorts" it before flushing, and then writes to HDFS using fast sequential writes. Note that in reality HFile is not just a simple list of sorted rows, it is much more than that.
- Apart from solving the "non-ordered" problem, Memstore also has other benefits, e.g.:
  - It acts as a in-memory cache which keeps recently added data. This is useful in numerous cases when last written data is accessed more frequently than older data
  - There are certain optimizations that can be done to rows/cells when they are stored in memory before writing to persistent store. E.g. when it is configured to store one version of a cell for certain CF and Memstore contains multiple updates for that cell, only most recent one can be kept and older ones can be omitted (and never written to HFile).

Important thing to note is that every Memstore flush creates one HFile per CF.

- Memstores of all column families are flushed together (this might change). This means creating N HFiles per flush, one for each CF. Thus, uneven data amount in CF will cause too many HFiles to be created: when Memstore of one CF reaches threshold all Memstores of other CFs are flushed too. As stated above too frequent flush operations and too many HFiles may affect cluster performance.
  - Hint: in many cases having one CF is the best schema design.

### Some performance Paramters

- `hbase.hstore.compactionThreshold` (affects after how many store files is compaction triggered)
- `hbase.hregion.memstore.flush.size` (Memstore will be flushed to disk if size of the memstore
  exceeds this number of bytes. Value is checked by a thread that runs every hbase.server.thread.wakefrequency)
- `hbase.regionserver.handler.count` (Number of RPC Server instances spun up on RegionServers)
- `hbase.client.write.buffer` (Write buffer size in bytes. A larger buffer requires more memory on both the client and the server because the server instantiates the passed write buffer to process it but reduces the number of remote procedure calls )

### Performance Improvements

- Observation : We can see lesser flushes and larger hfile sizes, if increase the memstore flush limit. But increasing the memstore flush limit means we risk running into regionserver heapsize lower/upper limit which will trigger flush for all the regions in the region server and if the upper limit is breached then even the updates will be blocked until the flush completes. Hence if you are having high # of regions per region server then increasing the memstore flush limit is risky. Try to reduce your # of region foot print before considering increase in this property

  e.g.
  Consider the following example. (<https://sematext.com/blog/2012/07/16/hbase-memstore-what-you-should-know/>)

- You have max heap in RS set to 10GB.
- hbase.hregion.memstore.flush.size is set to 300Mb
- hbase.regionserver.global.memstore.lowerLimit is set to 0.35
- hbase.regionserver.global.memstore.upperLimit is set to 0.4

  - You start with 10 Regions. In this case memstores will be flushed mainly because of the hbase.hregion.memstore.flush.size threshold, as *normally* (flushes are fast, etc.) you will never reach second threashold (max size occupied by 10 regions in this case is 3GB which is 30% of heap).
  - Number of regions grows to 100. In this case even 20 Regions with more or less "full" memstore (up to 300Mb, so 6GB total, which is 60% of heap) will cause RS to reach second threshold. More importantly, in this case it will reach hbase.regionserver.global.memstore.upperLimit, which will cause updates blocking.
  With that said, when you configure hbase.hregion.memstore.flush.size think about how many regions there will be. And try to keep regions count per RS more or less constant. Otherwise, adjust this param as number of regions per RS grow.

- HBase data is organized similarly to a sorted map, with the sorted key space partitioned into different shards or regions. An HBase client updates a table by invoking put or delete commands. When a client requests a change, that request is routed to a region server right away by default. However, programmatically, a client can cache the changes in the client side, and flush these changes to region servers in a batch, by turning the autoflush off. If autoflush is turned off, the changes are cached until flush-commits is invoked, or the buffer is full depending on the buffer size set programmatically or configured with parameter "hbase.client.write.buffer".

- Each row key belongs to a specific region which is served by a region server. So based on the put or delete's key, an HBase client can locate a proper region server. At first, it locates the address of the region server hosting the -ROOT- region from the ZooKeeper quorum.  From the root region server, the client finds out the location of the region server hosting the -META- region.  From the meta region server, then we finally locate the actual region server which serves the requested region.  This is a three-step process, so the region location is cached to avoid this expensive series of operations. If the cached location is invalid (for example, we get some unknown region exception), it's time to re-locate the region and update the cache.

### Compression

- Create a hbase table with compression enabled. This help save data on the disk and also in IOs
- create 'mytable', {NAME=>'mycolumnfamily:', COMPRESSION=>'SNAPPY'} (or LZO, GZ, NONE)

### WAL

- Although writing data to the memstore is efficient, it also introduces an element of risk: Information stored in memstore is stored in volatile memory, so if the system fails, all memstore information is lost. To help mitigate this risk, HBase saves updates in a write-ahead-log (WAL) before writing the information to memstore. In this way, if a region server fails, information that was stored in that server's memstore can be recovered from its WAL. The data in a WAL file is organized differently from HFile. WAL files contain a list of edits, with one edit representing a single put or delete. The edit includes information about the change and the region to which the change applies. Edits are written chronologically, so, for persistence, additions are appended to the end of the WAL file that is stored on disk. Because WAL files are ordered chronologically, there is never a need to write to a random place within the file.
- A region server serves many regions, but does not have a WAL file for each region. Instead, one active WAL file is shared among all regions served by the region server. Because WAL files are rolled periodically, one region server may have many WAL files. Note that there is only one active WAL per region server at a given time.

### Hbase Utilities

- <http://hbase.apache.org/0.94/book/ops_mgt.html>
- To get the list of utilities available in your hbase version use the following commands exactly. Even I don't understand whats the HADOOP_CLASSPATH doing, but without it it didn't work
  - HADOOP_CLASSPATH=`${HBASE_HOME}/bin/hbase classpath` ${HADOOP_HOME}/bin/hadoop jar ${}HBASE_HOME}/hbase-server.jar
- To read a Hfile use the utility Hfile tool
  - hbase org.apache.hadoop.hbase.io.hfile.Hfile
- Details about hbck tool
  - <http://hbase.apache.org/0.94/book/hbck.in.depth.html>
- Copy table utility allows copying tables within or inter cluster. Allows for defining what column families to copy, what window of changes to copy (incremental copy) etc.
  - bin/hbase org.apache.hadoop.hbase.mapreduce.CopyTable [--starttime=X] [--endtime=Y] [--new.name=NEW] [--peer.adr=ADR] tablename
  - <http://blog.cloudera.com/blog/2012/06/online-hbase-backups-with-copytable-2/>
- Import tsv - utility to import delimited data to Hbase or create hfiles for bulk loading
  - hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.columns=a,b,c <tablename> <hdfs-inputdir>
  - hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.columns=a,b,c -Dimporttsv.bulk.output=hdfs://storefile-outputdir <tablename> <hdfs-data-inputdir>
- Completebulkload utility will load pre-created hfiles in HTables
- WALPlayer is a utility to replay WAL files into Hbase.
- The WAL can be replayed for a set of tables or all tables, and a timerange can be provided (in milliseconds). The WAL is filtered to this set of tables. The output can optionally be mapped to another set of tables. WALPlayer can also generate HFiles for later bulk importing, in that case only a single table and no mapping can be specified.
- RowCounter is a mapreduce job to count all the rows of a table. This is a good utility to use as a sanity check to ensure that HBase can read all the blocks of a table if there are any concerns of metadata inconsistency. It will run the mapreduce all in a single process but it will run faster if you have a MapReduce cluster in place for it to exploit.
- HBase ships another diagnostic mapreduce job called CellCounter. Like RowCounter, it gathers more fine-grained statistics about your table. The statistics gathered by RowCounter are more fine-grained and include:
  - Total number of rows in the table.
  - Total number of CFs across all rows.
  - Total qualifiers across all rows.
  - Total occurrence of each CF.
  - Total occurrence of each qualifier.
  - Total number of versions of each qualifier.

The program allows you to limit the scope of the run. Provide a row regex or prefix to limit the rows to analyze. Use hbase.mapreduce.scan.column.family to specify scanning a single column family.

- Sentry - an hbase monitoring utility
  - <https://github.com/sentric/hannibal>

### Splitting

 <http://hortonworks.com/blog/apache-hbase-region-splitting-and-merging/>

- Region splitting to allow for even data load and avoiding hot spotting of regionservers
  - One issue with pre-splitting is calculating the split points for the table. You can use the RegionSplitter utility. RegionSplitter creates the split points, by using a pluggable SplitAlgorithm. HexStringSplit and UniformSplit are two predefined algorithms. The former can be used if the row keys have a prefix for hexadecimal strings (like if you are using hashes as prefixes). The latter divides up the key space evenly assuming they are random byte arrays
  - hbase org.apache.hadoop.hbase.util.RegionSplitter test_table HexStringSplit -c 10 -f f1
  - where -c 10, specifies the requested number of regions as 10, and -f specifies the column families you want in the table, separated by ":". The tool will create a table named "test_table" with 10 regions
- Else can create tables with your choice of split points
  - create 'test_table', 'f1', SPLITS=> ['a', 'b', 'c']
  - echo -e  "a\nb\nc" >/tmp/splits
  - hbase(main):015:0> create 'test_table', 'f1', SPLITSFILE=>'/tmp/splits'

- Split policies
  - ConstantSizeRegionSplitPolicy The first one is the default and only split policy for HBase versions before 0.94. It splits the regions when the total data size for one of the stores (corresponding to a column-family) in the region gets bigger than configured "hbase.hregion.max.filesize", which has a default value of 10GB. This split policy is ideal in cases, where you are have done pre-splitting, and are interested in getting lower number of regions per region server.
  - IncreasingToUpperBoundRegionSplitPolicyThe default split policy for HBase 0.94 and trunk is IncreasingToUpperBoundRegionSplitPolicy, which does more aggressive splitting based on the number of regions hosted in the same region server. The split policy uses the max store file size based on Min (R^2 * "hbase.hregion.memstore.flush.size", "hbase.hregion.max.filesize"), where R is the number of regions of the same table hosted on the same regionserver

### HDFS vs Hbase

HDFS is a distributed file system and has the following properties:

- It is optimized for streaming access of large files. You would typically store files that are in the 100s of MB upwards on HDFS and access them through MapReduce to process them in batch mode.
- HDFS files are write once files. You can append to files in some of the recent versions but that is not a feature that is very commonly used. Consider HDFS files as write-once and read-many files. There is no concept of random writes.
- HDFS doesn't do random reads very well.

HBase on the other hand is a database that stores it's data in a distributed filesystem. The filesystem of choice typically is HDFS owing to the tight integration between HBase and HDFS. Having said that, it doesn't mean that HBase can't work on any other filesystem. It's just not proven in production and at scale to work with anything except HDFS.
HBase provides you with the following:

- Low latency access to small amounts of data from within a large data set. You can access single rows quickly from a billion row table.
- Flexible data model to work with and data is indexed by the row key.
- Fast scans across tables.
- Scale in terms of writes as well as total volume of data.

It uses HDFS as storage - which takes care of backup\redundency\etc but its an "online store" - meaning you can query it for specific row\rows etc and get an immediate value.

## Interview Questions

- **What is CAP Theorem?**
  - <https://dzone.com/articles/better-explaining-cap-theorem>
  - It states that any distributed system can choose to guarantee any two of the 3 tenets of Consistency, Availability, Partition Tolerance.
    - Consistency: every read would get you the most recent write
    - Availability: every node (if not failed) always executes queries
    - Partition-tolerance: even if the connections between nodes are down, the other two (A & C) promises, are kept.
  - Hbase chooses Consistency and Partition Tolerance
  - Cassandra chooses Availability and Partition Tolerance
- **What are bloom filters?**
- **Explain the Write Path, Read Path**
- **Major Compaction vs Minor Compaction**
