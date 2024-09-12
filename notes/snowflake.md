# Snowflake

## Queries

- How does it differ from BigQuery
  - BigQuery you are paying with un-compressed data
  - Can work across multiple cloud providers. May not be applicable with
    Walmart use-case. We are not copying the data anytime, but just using the
    compute.
  - Ease of use. able to do everything with SQL, Java, Python, Scala etc.
  - User permissions etc. can be managed even through SQL, need not go to the
    CLI
  - Data is 16MB micro partitions, with all metadata pre-calculated
- Which use-cases it should be preferred with
- How to get started with it to get a flavor of it, for me or my team to play
  with it
- Any metrics around performance/pricing metrics around standard usecases and
  their comparison with comparisons
  - Data is cached
  - Charge by compressed/uncompressed
- How does security work across different cloud providers. The data we work
  with is highly sensitive/HIPAA and hence need to be certified with all these
  measures
- How does concurrency and compute resources is managed
- SRCR, SSP etc. are all in place
- Strengths
- Works across multiple cloud partners - GCP, Azure, AWS
  - Software is spun up on the cloud provider of your choice
- Paying for compressed data size
- No need to worry about compress, governance etc. around the data
- Takes care of managing the concurrency. Scale horizontally
- Gives Spark cluster internally. Supports natively in python, scala, java,
  ANSI SQL
- Works with multiple data types - JSON, PARQUET
