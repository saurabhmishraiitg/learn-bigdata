# HDFS File Size, File Count Management

Aim is to run the HDFS lean and clean.

- [HDFS File Size, File Count Management](#hdfs-file-size-file-count-management)
  - [Objectives](#objectives)
  - [Scenarios](#scenarios)
  - [Strategies](#strategies)
  - [Solution Salient Features](#solution-salient-features)

## Objectives

- Less Number of files
  - This is desirable, when you have lot of small files
- Large File Sizes
  - This is desirable when you have
- Less Number of Files and Large File Sizes
- Compressed Files

## Scenarios

- Date Partition / Market Partition

    ```txt
    load_date=2019-05-24/cntry_cd=US
                    ..../cntry_cd=MX
                    ..../...
                    ..../cntry_cd=K1
    load_date=2019-05-25/cntry_cd=US
                    ..../cntry_cd=MX
                    ..../...
                    ..../cntry_cd=K1
    ...
    ...
    ...
    load_date=2020-05-24/cntry_cd=US
                    ..../cntry_cd=MX
                    ..../...
                    ..../cntry_cd=K1
    ```

- Market Partition / Date Partition
- Date Partition (lot of files)

  ```txt
  load_date=2019-05-24/<file-1> (large file)
                ....../<file-2> (medium file)
                ....../<file-3> (medium file)
                ...
                ....../<file-n> (small file)
  ```

- External Tables (with Source Data)
- Managed Tables
  - Getting re-written daily
  - Getting added to daily

## Strategies

- Yearly data archiving. Ensure to check-in unarchive strategy as well

## Solution Salient Features

- Able to investigate a given location/table and provide information about type of Issue - small file, large # of files, hierarchial partition etc.
- Provide un-archiving strategy as well to revert the data for usage without much intervention
- Validation tools, to ensure data archiving was successful
