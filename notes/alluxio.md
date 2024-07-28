# Alluxio

Alluxio (formerly known as Tachyon) is a virtual distributed storage system. It bridges the gap between computation frameworks and storage systems, enabling computation applications to connect to numerous storage systems through a common interface. Read more about Alluxio Overview.

The Alluxio project originated from a research project called Tachyon at AMPLab, UC Berkeley, which was the data layer of the Berkeley Data Analytics Stack (BDAS).

- [Alluxio](#alluxio)
  - [Alluxio on Homebrew](#alluxio-on-homebrew)
  - [Alluxio On Docker](#alluxio-on-docker)
  - [Alluxio FS Commands](#alluxio-fs-commands)
  - [Connecting to Alluxio with Spark](#connecting-to-alluxio-with-spark)
  - [Concepts](#concepts)

## Alluxio on Homebrew

- Install
  - `brew install alluxio`
    - Installation directory
      - `/usr/local/Cellar/alluxio/2.4.1`
    - Conf directory
      - `/usr/local/Cellar/alluxio/2.4.1/libexec/conf`
- Ensure to configure ssh keys for your localhost, to allow user to ssh to localhost without password prompt
  - i.e. add the `.ssh/id_rsa.pub` key to the `.ssh/authorized_keys` file
- Check you local installation is looking good
  - `alluxio validateEnv local`
  - `alluxio runTests`
- Configuration Changes
  - rename the file `alluxio-site.properties.template` to `alluxio-site.properties`
  - Uncomment the following 2 lines in the file and update appropriately
    - `alluxio.master.hostname=localhost`
    - `alluxio.master.mount.table.root.ufs=/Users/sxxx/Desktop/tmp/alluxio`
- Format the Alluxio Filesystem
  - `alluxio format`
- Start the alluxio master and worker node (1)
  - `alluxio-start.sh local SudoMount`
  - To stop the cluster
    - `alluxio-stop.sh local`
  - Logs can be found in location
    - `/usr/local/Cellar/alluxio/2.4.1/libexec/logs/master.log`
- Check for filesystem availability
  - `alluxio fs ls /`
- Browse the UI at
  - `http://localhost:19999/overview`

## Alluxio On Docker

> Was able to get the setup working only for dedicated network. `host` network did not work as the refresh of GUI was not working properly.
> Also, accessing the alluxion filesystem from Spark was not successful when trying to read file, as it was unable to identify worker hostname i.e. `alluxio-worker`

- Start Alluxio master

```bash
docker network create alluxio_network

docker run -d  --rm \
    -p 19999:19999 \
    -p 19998:19998 \
    --net=alluxio_network \
    --name=alluxio-master \
    -v /Users/sxxx/Desktop/tmp/alluxio:/opt/alluxio/underFSStorage \
    -e ALLUXIO_JAVA_OPTS=" \
       -Dalluxio.master.hostname=alluxio-master" \
    alluxio/alluxio-enterprise master
```

- Start Alluxio worker

```bash

docker run -d --rm --net=alluxio_network \
    -p 29999:29999 \
    -p 30000:30000 \
    --name=alluxio-worker \
    --shm-size=1G \
    -v /Users/sxxx/Desktop/tmp/alluxio:/opt/alluxio/underFSStorage \
    -e ALLUXIO_JAVA_OPTS=" \
       -Dalluxio.worker.ramdisk.size=1G \
       -Dalluxio.master.hostname=alluxio-master \
       -Dalluxio.worker.hostname=alluxio-worker \
       -Dalluxio.worker.memory.size=1G" \
    alluxio/alluxio-enterprise worker
```

- Mount a GCS filesystem
  - The gcs.version=2 is available currently only in alluxio-enterprise version of docker image 2.4.1

```bash
alluxio fs mount --option alluxio.underfs.gcs.version=2 --option fs.gcs.credential.path=/opt/alluxio-enterprise-2.4.1-2.0/underFSStorage/svc-gec-etl-2020-12-14.json /gcs gs://7a6d4d3560ff58c28cef0096a7dbb16086aab77e53cab84d622cbd9794c90d
```

## Alluxio FS Commands

- List files
  - `alluxio fs ls /`
- Copy files
  - `alluxio fs copyFromLocal LICENSE /LICENSE`
- Persist files
  - `alluxio fs persist /LICENSE`
- Mount a GCS filesystem
  - This is not available in verion `2.4.1` of alluxio for homebrew
  - `alluxio fs mount --option alluxio.underfs.gcs.version=2 --option fs.gcs.credential.path=/Users/sxxx/Desktop/tmp/gcloud/svc-dev-2021-02-07.json /gcs gs://7a6d4d3560ff58c28cef0096a7dbb16086aab77e53cab84d622cbd9794c90d`
  - An alternative legacy approach is as follows. This requires creating a pair of accessKeyId and accessKeySecret for the GCS bucket
    - `alluxio fs mount --option fs.gcs.accessKeyId=XXX --option fs.gcs.secretAccessKey=XXX /gcs gs://7a6d4d3560ff58c28cef0096a7dbb16086aab77e53cab84d622cbd9794c90d/`

## Connecting to Alluxio with Spark

- Maven Dependency

```xml
<dependency>
  <groupId>org.alluxio</groupId>
  <artifactId>alluxio-shaded-client</artifactId>
  <version>2.4.0</version>
</dependency>
```

- Creating a spark connector

```scala
def createSparkSession: SparkSession = {
val sparkConf = new SparkConf()
    .setMaster("local[*]")
    .set("spark.driver.bindAddress", "127.0.0.1")
    .set("fs.alluxio.impl", "alluxio.hadoop.FileSystem")

val spark = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

val df: DataFrame = spark.read.csv("alluxio://localhost:19998/gcs/sect-shop/landing/BARS_DAILY.txt")
```

## Concepts

- Alluxio Supports 4 write types with jobs such as Spark

| Write Type    | Description                                                                  | Write Speed                                                                                | Fault Tolerance                                       |
| ------------- | ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ | ----------------------------------------------------- |
| MUST_CACHE    | Writes directly to Alluxio memory.                                           | Very fast.                                                                                 | Data loss if a worker crashes.                        |
| THROUGH       | Writes directly to under storage.                                            | Limited to under storage throughput.                                                       | Dependent upon under storage.                         |
| CACHE_THROUGH | Writes to Alluxio and under storage synchronously.                           | Data in memory and persisted to under storage synchronously.                               | Dependent upon under storage.                         |
| ASYNC_THROUGH | Writes to Alluxio first and then asynchronously writes to the under storage. | Nearly as fast as MUST_CACHE and data persisted to under storage without user interaction. | Possible to lose data if only one replica is written. |
