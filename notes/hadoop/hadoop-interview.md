# Hadoop Interview

- **Question** Explain about YARN cluster architecture. Different components and their role.
  - NameNode
    - Executes file system namespace operations like opening, closing, and renaming files and directories. It also determines the mapping of blocks to DataNodes.
    - The NameNode is the arbitrator and repository for all HDFS metadata. The system is designed in such a way that user data never flows through the NameNode.
  - DataNodes
    - Are responsible for serving read and write requests from the file system's clients. The DataNodes also perform block creation, deletion, and replication upon instruction from the NameNode.
  - Secondary NameNode
  - Resource Manager
  - Application Manager
  - Node Manager

- **Question** What's meant by `Splittable` file format? Why is ZLIB a non-`splittable` file format?

- **Question** What is difference between HDFS and YARN
  - HDFS (Hadoop Distributed File System) is the storage unit of Hadoop. It is responsible for storing different kinds of data as blocks in a distributed environment. It follows master and slave topology.
    - NameNode: NameNode is the master node in the distributed environment and it maintains the metadata information for the blocks of data stored in HDFS like block location, replication factors etc.
    - DataNode: DataNodes are the slave nodes, which are responsible for storing data in the HDFS. NameNode manages all the DataNodes.
  - YARN (Yet Another Resource Negotiator) is the processing framework in Hadoop, which manages resources and provides an execution environment to the processes.
    - ResourceManager: It receives the processing requests, and then passes the parts of requests to corresponding NodeManagers accordingly, where the actual processing takes place. It allocates resources to applications based on the needs.
    - NodeManager: NodeManager is installed on every DataNode and it is responsible for the execution of the task on every single DataNode.

- **Question** What are various hadoop daemons running in a cluster
  - NameNode: It is the master node which is responsible for storing the metadata of all the files and directories. It has information about blocks, that make a file, and where those blocks are located in the cluster.
  - Datanode: It is the slave node that contains the actual data.
  - Secondary NameNode: It periodically merges the changes (edit log) with the FsImage (Filesystem Image), present in the NameNode. It stores the modified FsImage into persistent storage, which can be used in case of failure of NameNode.
  - ResourceManager: It is the central authority that manages resources and schedule applications running on top of YARN.
  - NodeManager: It runs on slave machines, and is responsible for launching the application's containers (where applications execute their part), monitoring their resource usage (CPU, memory, disk, network) and reporting these to the ResourceManager.
  - JobHistoryServer: It maintains information about MapReduce jobs after the Application Master terminates.

  - Assumption and Goals
    - Hardware Failure
      - Detection of faults and quick, automatic recovery from them
    - Streaming Data Access
      - POSIX requirements have been traded at few places to increase data throughput rates
    - Large Datasets
      - Should support large size files as well as large number of files
    - Simple Concurrency Model
      - Write once and read many access model. File once created, written and closed need not be changed except for appends and truncate. And appends are done at the end of file rather than at any arbitrary point
    - Moving computation is cheaper than moving data
      - Reduces network traffic and optimized performance especially in case of large datasets

Portability across heterogeneous hardware and software platforms

- **Question** What is the difference between Hadoop 1 vs Hadoop 2
  - In Hadoop 1.x, "NameNode" is the single point of failure. In Hadoop 2.x, we have Active and Passive "NameNodes". If the active "NameNode" fails, the passive "NameNode" takes charge. Because of this, high availability can be achieved in Hadoop 2.x.
  - Also, in Hadoop 2.x, YARN provides a central resource manager. With YARN, you can now run multiple applications in Hadoop, all sharing a common resource. MRV2 is a particular type of distributed application that runs the MapReduce framework on top of YARN. Other tools can also perform data processing via YARN, which was a problem in Hadoop 1.x.

- **Question** What is role of fsImage and check-pointing in HDFS
  - Checkpointing" is a process that takes an FsImage, edit log and compacts them into a new FsImage. Thus, instead of replaying an edit log, the NameNode can load the final in-memory state directly from the FsImage. This is a far more efficient operation and reduces NameNode startup time. Checkpointing is performed by Secondary NameNode.

- **Question** How does HDFS architecture provide fault tolerance
  - Data replication

- **Question** Why having large number of small files in HDFS degrades Namenode performance
  - HDFS is more suitable for large amounts of data sets in a single file as compared to small amount of data spread across multiple files. As you know, the NameNode stores the metadata information regarding the file system in the RAM. Therefore, the amount of memory produces a limit to the number of files in my HDFS file system. In other words, too many files will lead to the generation of too much metadata. And, storing these metadata in the RAM will become a challenge. As a thumb rule, metadata for a file, block or directory takes 150 bytes.
  - Even jobs will be running slow, since each job will try to start too many parallel tasks.

- **Shuffle and Sort**
  - MapReduce makes the guarantee that the input to every reducer is sorted by key. The process by which the system performs the sort and transfers the map outputs to the reducers as inputs is known as the shuffle
  - Ref: Hadoop Definitive Guide 4th Edition Pg. 197
  - Map Side
    A map job's output gets written to disk after passing through a sequence of steps or checks as follows
    - The key values output by map tasks are written to a circular-in memory buffer
    - The in-memory buffer has a pre-defined capacity, which once close to breaching triggers spilling of records to disk
    - Before these records get spilled to disk, they are partitioned and sorted
    - Partitioning could either be default hash-key based or custom Partitioner provided by developer
    - The partitioned records are then sorted based upon their keys
    - If a combiner has been defined, then the partitioned and sorted records are passed through the combiner stage as well
    - A combiner helps in reducing the magnitude of data to be passed on to the reducer by doing some pre-aggregation
    - Enabling compression on the map output helps in reducing the I/O between mappers and reducers
    - The output from map tasks are written on the local machine and the details sent to ApplicationMaster/JobTracker which will share the necessary information with Reducers to pull the data
    - The data may be spilled multiple times during the run of a single map task run, depending upon buffer getting filled. But finally before the map task ends, all the spilled files are merged, partitioned and sorted. And if need, combiner is re-run on the partitioned and sorted output

  - Reduce Side
    The other half of the MR job i.e. Reducer operation takes care of remaining shuffle operations as follows
    - The reducer gets details about the map output splits (sitting on the local machine of the Map task) from the ApplicationMaster/JobTracker
    - Each reduce task may serve only specific # of partitions. That would require it to fetch (key,value) pairs corresponding to those partitions from all the map task nodes.
    - The reducer tasks get created after atleast 5% of map tasks have completed. And instead of waiting for all the maps to complete, they start the process of copying the data from the completed map tasks immediately after starting as part of the copy phase of the task
    - The partitioned files getting copied from individual map-task nodes are merged on the reducer node again  to generate a single sorted input file for the reducer
    - If combiner has been specified then it's re-run at this stage to further reduce input to the reducer
    - The map outputs are not deleted until the job completes as communicated by the ApplicationMaster, this allows the original data to be available in case one (or few) of the reduce tasks failed and need to be restarted
    - Based upon OutputFormat provided the each reducer may spill its' output to arbitrary # of files based upon spill size or partitioned output based upon pre-defined keys if MultipleOutputFormat is specified

- **Secondary Sorting**
  - In MapReduce, the reduce function is called one time for each unique map function output key.  Each call to it includes a collection of all values that accompanied that key in map outputs.  The framework sorts the data in between the map and reduce phase, meaning that a comparator is used to determine the order that keys and their corresponding lists of values are fed into the reduce function. The order of the values within a reduce function call, however, is typically unspecified and can vary between runs.
  - Secondary sort is a technique that allows the MapReduce programmer to control the order that the values show up within a reduce function call.
  - We can achieve this using a composite key that contains both the information needed to sort by key and the information needed by value, and then decoupling the grouping of the intermediate data from the sorting of the intermediate data.  By sorting, we mean deciding the order that map output key/value pairs are presented to the reduce functions.  We want to sort both by the keys and the values.  By grouping, we mean deciding which sets of key/value are lumped together into a single call of the reduce function.  We want to group only on the keys so that we don't get a separate call to the reduce function for each unique value.
  - In Apache Hadoop, the grouping is done in two places - the partitioner, which routes map outputs to reduce tasks, and the grouping comparator, which groups data within a reduce task.  Both of these are pluggable per-job.  The sorting is pluggable by setting the output key comparator.
  - Map uses `CustomPartitioner` to send to respective reducer using only the Natural Key
  - Reducer groups the received records using `GroupingComparator` on the Natural Key to send to reduce() method
  - OutputKeyComparator sorts the reduce keys on Composite Key before passing their values to the reduce() method
    - `Partitioner`
    - `GroupingComparator`
    - `CompositeKey.compareTo()`

- **What's the difference between split size and block size?**
  - **Solution** : Split are logical partitioning of data while block is physical partition of data on the disk. While block size will split the data on defined boundaries inconsiderate of the fact if the data in each block is logically complete or not (i.e. complete record exists in a block and not split across 2 or more blocks). Splits are more considerate in this regards. If properly configured, they will seek across block boundary to ensure that the complete records gets read in a mapper rather than incomplete one
