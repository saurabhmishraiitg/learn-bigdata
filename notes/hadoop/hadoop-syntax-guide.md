# Hadoop Syntax Guide

## Command List

- Creating a `har` file
  
  ```bash
  hadoop archive -archiveName name -p <src> <dest>
  # e.g.
  hadoop archive -archiveName testhar2.har -p /user/source/folder/to/har /user/destination/folder
  ```

  - Archiving does not delete the source files
  - `HAR` does not compress the files, it is analogous to the Linux `tar` command.
  - `har` is considered as folder in HDFS. Hence, delete a `har` file will require `hadoop fs -rm -r` command

- Count of files in a directory

  - `hadoop fs -count -q -v <path_to_check>`

- Decrypt a JCEKS file to extract secret
  - Using spark-shell and HDFS APIs

  ```scala
  import org.apache.hadoop.conf.Configuration
  import org.apache.hadoop.security.alias.CredentialProviderFactory

  //val CREDENTIAL_PROVIDER_PATH = "hadoop.security.credential.provider.path"
  val conf = new Configuration()
  conf.set(CredentialProviderFactory.CREDENTIAL_PROVIDER_PATH, jceksHdfsFilePath)
  val pass = conf.getPassword(passwordAlias)
  String.valueOf(pass)
  ```
