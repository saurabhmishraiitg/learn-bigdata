# HIVE Syntax Guide

## Command List

- Run the command in debug mode and generate log file
  - `hive -v -f check.hql -hiveconf hive.root.logger=DEBUG -hiveconf hive.log.file=debug.log -hiveconf hive.log.dir=/tmp/hivedebug`
  - You can use similar properties while opening the HIVE shell prompt
  - Also file under path can be tailed to get the running INFO logs when you are working with HIVE CLI
    - `/tmp/<user>/hive.log`
- Load Data from a path
  - Load data from a HDFS path \
    `LOAD DATA INPATH '/directory-path/file.csv' INTO TABLE <mytable>;`
  - Load data from a LFS path \
    `LOAD DATA LOCAL INPATH '/directory-path/file.csv' INTO TABLE <mytable>;`
  - With both the above file gets deleted from the original location
- Create a table on `DELIMITED` data

    ```sql
    CREATE [EXTERNAL] TABLE test01(
        col1 STRING,
        ...
        col2 STRING)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
    STORED AS TEXTFILE
    LOCATION '<LOCATION>';
    ```

    **or**

    ```sql
    CREATE TABLE test01(
        col1 STRING,
        ...
        col2 STRING
    )
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
    WITH SERDEPROPERTIES (
    "separatorChar" = ",",
    "quoteChar"     = "'",
    "escapeChar"    = "\\"
    )
    STORED AS TEXTFILE
    LOCATION <LOCATION>;
    ```

- Create a `PARTITIONED` table

  ```sql
  CREATE [EXTERNAL] TABLE test01(
          col1 STRING,
          ...
          col2 STRING)
  PARTITIONED BY (load_date STRING)
  ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
  STORED AS TEXTFILE
  LOCATION '<LOCATION>';
  ```

- `ARCHIVE` a partition

  ```sql
  set hive.archive.enabled=true;
  set hive.archive.har.parentdir.settable=true;
  set har.partfile.size=1099511627776;

  -- Archive
  ALTER TABLE table_name ARCHIVE PARTITION (partition_col = partition_col_value, partition_col = partiton_col_value, ...);

  -- Un-Archive
  ALTER TABLE srcpart UNARCHIVE PARTITION(ds='2008-04-08', hr='12')
  ```

  - `hive.archive.enabled` controls whether archiving operations are enabled.
  - `hive.archive.har.parentdir.settable` informs Hive whether the parent directory can be set while creating the archive. In recent versions of Hadoop the -p option can specify the root directory of the archive
  - `har.partfile.size` controls the size of the files that make up the archive. The archive will contain `size_of_partition/har.partfile.size` files, rounded up. Higher values mean fewer files, but will result in longer archiving times due to the reduced number of mappers.
  - Trade-off of this approach is that queries may be slower due to the additional overhead in reading from the HAR.
  - Archived partitions cannot be overwritten with `INSERT OVERWRITE`. The partition must be unarchived first.

- Create a `VIEW`

  ```sql
  CREATE VIEW [IF NOT EXISTS] view_name
      [(column_name [COMMENT 'column_comment'][, ...])]
      [COMMENT 'view_comment']
    AS select_statement
  ```

- Parse `JSON` column
  - 2 approaches are available
    - [xPathUDF](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+XPathUDF)
      - Not Explored Yet
    - [get_json_object](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+UDF#LanguageManualUDF-get_json_object)
      - e.g.

```sql
-- Sample Column
-- [{"stepName":"Submit","stepOrder":1,"lastUpdatedBy":"23d76badd0ddacc39f88ca38965dd9","lastUpdatedTs":1533588033656},{"stepName":"Review (Home Office)","stepOrder":2,"lastUpdatedBy":"33de74a5cdddabd2d7bbd96aab4cc54752fbce","lastUpdatedTs":1533908564154},{"stepName":"Resolution (Home Office)","stepOrder":3,"lastUpdatedBy":"33de6fece9c6aac5","lastUpdatedTs":1535657776571}]

select GET_JSON_OBJECT(stepmetadata, '$[*].stepOrder'), GET_JSON_OBJECT(stepmetadata, '$[*].lastUpdatedTs') from table_name limit 1;

-- Output
-- [1,2,3]       [1533588033656,1533908564154,1535657776571]
```

- Set the staging directory for your HIVE queries
  - `set hive.exec.stagingdir = /tmp/test123/`
- HIVE Variable Substitution
  - Use `hiveconf` at the invocation
    - `hive --hiveconf ims=$ENV_HOME -v -f hiveScript.HIVE`
    - Usage : `use ${hiveconf:ims};`
  - Using `hivevar` in setter
    - `set hivevar:tablename=mytable;`
    - Usage : `hive> select * from ${tablename}`
    - Usage : `hive> select * from ${hivevar:tablename}`
  - Using `hivevar` at invocation
    - `hive --hivevar dbname=rawdb -v -f sample.hql`
    - Usage : `use ${dbname};`
- `CREATE` a database
  - `create database tll_test_bucket location 'gs://114e63e68c7e4c838233009aaac69a2190dc0acbb982864cb9c0b38b659303/'`
- `REFLECT` UDF in HIVE
  - [reference](https://cwiki.apache.org/confluence/display/Hive/ReflectUDF)

```sql
add jar /u/users/svcggapp/code/reach/target/reach-spark.jar;
add jar hdfs:///user/svcggdat//reach-spark.jar;
select recordId, createdby, reflect("com.wxxxlabs.gdo.gg.reach.utils.Decrypt", "decryptString", createdby) from table1;
```
