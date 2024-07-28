# Sqoop

- [Sqoop](#sqoop)
  - [Syntax Guide](#syntax-guide)
    - [Sqoop Eval Examples](#sqoop-eval-examples)

## Syntax Guide

### Sqoop Eval Examples

- **Informix**

  - `sqoop eval -files sqlhosts --driver com.informix.jdbc.IfxDriver --connect 'jdbc:informix-sqli://ipaddress:23321/imdct6:INFORMIXSERVER=alias' --username user --password pwd -query "select count(1) from dbname:tblname"`
  - Using `sqlhosts` file \
    - `sqoop eval -files sqlhosts --driver com.informix.jdbc.IfxDriver --connect 'jdbc:informix-sqli:INFORMIXSERVER=importsp;DATABASE=dbname;SQLH_TYPE=FILE;SQLH_FILE=sqlhosts' --username user --password pwd -query "select col1, col2 from dbname:tblname where col1=11114776 limit 3"`

- **DB2**
  - `sqoop eval --connect jdbc:db2://XXX.wxx-xxx.com:3700/XX --username XXX --password XXX --query "list tables for schema XX"`
  - `sqoop eval --connect jdbc:db2://XX.wxx-xxx.com:446/XX --username remotcf --password XXX --query "list tables for schema XX"`
  - `sqoop eval --connect jdbc:db2://156.95.34.85:438/XX --username XX --password XXX --query "select *from XX.XXX limit 10"`
  - `sqoop eval --connect jdbc:db2://XX:438/XX --username XX --password XXX --query "select* from tabl1 limit 10"`

  ```bash
  CodeBrownWorkflow {
  "user" = "DB2BIUS"
  "url" = "jdbc:db2://156.95.34.85:438/GM2P"
  "driver" = "com.ibm.db2.jcc.DB2Driver"
  }
  ```

- **SQLServer**

  ```bash
  sqoop import -Dorg.apache.sqoop.splitter.allow_text_splitter=true -Dhadoop.security.credential.provider.path=jceks://hdfs/user/xxx/secure/credentials/sectshop/sqlsrvr.jceks  --connect "jdbc:sqlserver://wxx-xxx.com;database=xx;port=14481;encrypt=true;trustServerCertificate=true;MultiSubnetFailover=False" --username xx --password-alias ssl.server.keystore.password --query "SELECT * FROM xx.dbo.xxx WHERE  \$CONDITIONS " --split-by ID --num-mappers 1 --fields-terminated-by ',' --target-dir hdfs://Prod17HA/user/hive/warehouse/xxxx.db/xxx --delete-target-dir
  ```

- To execute query to remote DB
  - sqoop eval --connect "jdbc:oracle:thin:@wxx-xxx.com:1526/xx" --username "xxx" --password "pass" --query "SELECT COUNT(*) FROM xxx.BU_INCIDENT"
- Skip Providing password in query
  - Use '-P' flag instead of '--password'
  - Or use --password-file
    - sqoop list-databases --connect "jdbc:mysql://prod.wxxx.com:3306/reach" --username xxx --password-file file:///u/users/sxxx/rch.pwd
    - The passwordfile should not have any new line characters, so create it like this
      - echo -n "password" > /u/users/sxxx/rch.pwd
      - <https://stackoverflow.com/questions/29696370/sqoop-import-password-file-function-not-working-properly-in-sqoop-1-4-4>

- Run export from HDFS to DB
  - sqoop export --connect "${ORCL_CONN_STR_CERT}" --username "${ORCL_CONN_USR_CERT}" --password "${ORCL_CONN_PASS_CERT}" --table xxx.BU_INCIDENT_INJRY --export-dir "${HDFS_DIR}/bu_incident_injry_oracle_stg" --input-fields-terminated-by '\0001' --lines-terminated-by '\n' -m 1 --map-column-java INJRY_DESC=String --input-null-string "NULL" --input-null-non-string ""
  - If datatype is blob in the DB, then you need to define a column mapping param
  - --map-column-java INJRY_DESC=String
  - Connect string example
  - jdbc:oracle:thin:@xxx.wxx-xxx.com:1526/xxx
  - Incase the data needs to be over-written in the DB
    - sqoop export --connect "${ORCL_CONN_STR_CERT}" --username "${ORCL_CONN_USR_CERT}" --password "${ORCL_CONN_PASS_CERT}" --table xxx.MKT_INCIDENT_CATG_MTH_SUMM --export-dir "${HDFS_DIR}/market_incident_catg_mth_summ_oracle_stg" --input-fields-terminated-by '\0001' --lines-terminated-by '\n' -m 1 --update-mode allowinsert --update-key YR_NBR,MTH_NBR,GEO_REGION_CODE,BU_FMT_DESC,INCIDENT_CATG_DESC --input-null-string "NULL" --input-null-non-string ""
  - Staging table can be used as well to load the data first into and then move to the target table in a single transaction. This helps in preventing data corruption during export during to intermittent failure. This way the data in the target table has consistency irrespective of failure of the independent export process.
  - You can load some of the export/import/eval command params in a properties file passed as config param to the sqoop command
    - sqoop --options-file a.props --query "SELECT COUNT(*) FROM xxx.BU_INCIDENT"
      - //a.props
      - eval
      - --connect
      - jdbc:oracle:thin:@xxx.wxx-xxx.com:1526/xxx
      - --username
      - xxx
      - --password
      - pas$
    - The parameters provided must be in order in which you would have normally provided them on CLI. There should be new line between each of them
    - Every param that you pass on CLI can be added to this file instead. So that your final command if reduced to
      - sqoop --options-file a.props
      - //a.props
        - eval
        - --connect
        - jdbc:oracle:thin:@wxx-xxx.com:1526/xxx
        - --username
        - xxx
        - --password
        - pas$
        - --query
        - SELECT COUNT(*) FROM xxx.BU_INCIDENT
  - Same way you can list available database schemas and tables within a schema in the DB you are accessing using
    - sqoop-list-databases --connect jdbc:mysql://localhost:3306/ --username root --password hr
    - sqoop-list-tables --connect jdbc:mysql://localhost:3306/vaibhav --username root --password hr
  - The 'connection-param-file' attribute is used to pass a properties file which has export/import specific configuration properties
    - sqoop export -D sqoop.export.records.per.statement=100 --table <tablename> --export-dir <path> --input-fields-terminated-by '\t' --input-lines-terminated-by '\n' --connect 'jdbc:netezza://<host>/<db>' --driver org.netezza.Driver --username <username> --password <passwrd> --connection-param-file sqoop.properties --batch
    - //sqoop.properties
      - jdbc.transaction.isolation=TRANSACTION_READ_UNCOMMITTED
      - This property will allow you to avoid deadlocks during JDBC export transactions
  - Sqoop also allows us to import all the tables in a DB to HIVE using
    - sqoop import-all-tables
    - -num-mappers 1
    - -connect "jdbc:mysql://nn01.itversity.com:3306/retail_db"
    - -username=retail_dba
    - -password=pass$
    - -hive-import
    - -hive-overwrite
    - -create-hive-table
    - -compress
    - -compression-codec org.apache.hadoop.io.compress.SnappyCodec
    - -outdir java_files
  - Sqoop options regardless of where they are loaded from, they must follow the ordering such that generic options appear first, tool specific options next, finally followed by options that are intended to be passed to child programs
  - Managing passwords in Sqoop
    - Ref : <http://ingest.tips/2015/03/12/managing-passwords-sqoop/>
      - Password provided on the command line
      - Password read from the console during the interactive execution of a Sqoop job.
      - Password provided on a secure file system that only the user can access.
    - This blog also has details about how to create an encrypted password store file in addition to a plain text password file
    - To create a JKS using hadoop binary and use it with sqoop
      - hadoop credential create orcl.pass -provider jceks://hdfs/user/xxx/sxxx/test.jceks
        - Here the part after //hdfs, is the absolute HDFS location where the key is going to be placed
        - Running this command will prompt you to enter the password to be stored in the keystore
        - Confirmation prompt to re-enter the password will be recieved as well
        - The create option is used to define an alias using which you are going to load your password
        - You  can trigger this command pointing to the same provider file multiple times with different create aliases, to create a single keystore with multiple passwords
      - sqoop eval -Dhadoop.security.credential.provider.path=jceks://hdfs/user/xxx/sxxx/test.jceks --connect "jdbc:oracle:thin:@xxx.wxx-xxx.com:1526/xxx" --username "xxx" --password-alias orcl.pass --query "SELECT COUNT(*) FROM xxx.BU_INCIDENT"
    - To use an encrypted password file
      - sqoop import \
      - -Dorg.apache.sqoop.credentials.loader.class=org.apache.sqoop.util.password.CryptoFileLoader \
      - -Dorg.apache.sqoop.credentials.loader.crypto.passphrase=xxx \
      - --connect jdbc:mysql://example.com/sqoop \
      - --username sqoop \
      - --password-file file:///tmp/pass.enc \
      - --table tbl
    - How to generate this encrypted file can be found in the blog post above
  - Using an oracle wallet for storing your credentials requires you to have oracle 11g utilties installed on your local
    - mkstore
  - You can force the class files to be created in a particular directory by using the param
    - --outdir "/u/users/xxx/sxxx/sqoop/test"
- Create HIVE tables mirroring the DDL in DB
  - sqoop create-hive-table --connect jdbc:oracle:thin:@xxx.wxx-xxx.com:1526/xxx --username xxx --password XXX --table xxx.BU_INCIDENT --hive-table sxxx.b1_inc
  - You would need to provide DB table name with it's schema and also new hive table name with the schema where it needs to be created.
- Informix connector
  - List tables
    - sqoop list-tables --driver com.informix.jdbc.IfxDriver --connect 'jdbc:informix-sqli://xxx:23301/imdct6:INFORMIXSERVER=xxx;DATABASE=imdct6' --username wmimport --password wxxx
  - Import Data
    - sqoop import -Dmapred.job.queue.name=ggdataload -Dorg.apache.sqoop.splitter.allow_text_splitter=true -files sqlhosts --driver com.informix.jdbc.IfxDriver --connect 'jdbc:informix-sqli:INFORMIXSERVER=importsp;DATABASE=imdct6;SQLH_TYPE=FILE;SQLH_FILE=sqlhosts' --username svcggifx --password XXX --table rqst_cncl_rsn_txt --split-by distribution_id --relaxed-isolation -m 5 --target-dir /user/xxx/data/raw/xxx.db//temp/rqst_cncl_rsn_txt --fields-terminated-by '\001' --hive-drop-import-delims --lines-terminated-by '\n' --null-non-string '\\N' --null-string '\\N' --outdir /u/users/xxx/aorta/tmp
