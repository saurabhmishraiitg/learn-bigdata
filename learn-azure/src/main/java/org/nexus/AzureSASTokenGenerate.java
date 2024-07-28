package org.nexus;

//import com.azure.core.util.Context;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;

//import com.google.api.services.storage.Storage;
import com.google.api.gax.paging.Page;
import com.google.cloud.hadoop.fs.gcs.*;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.hadoop.gcsio.GoogleCloudStorageOptions;
import com.google.cloud.spark.bigquery.repackaged.com.google.cloud.bigquery.BigQuery;
import com.google.cloud.spark.bigquery.repackaged.com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.spark.sql.SparkSession;

public class AzureSASTokenGenerate {

  public static String connectStr = "DefaultEndpointsProtocol=https;AccountName=xxx;AccountKey=xxx//xxx/"
    + "xxx+xxx+xxxx==;EndpointSuffix=core.windows.net";
  public static String containerName = "xxx-prod";

  public static BlobServiceClient getClient() {
    // Create a BlobServiceClient object using a connection string
    return new BlobServiceClientBuilder().connectionString(connectStr).buildClient();
  }

  public static BlobContainerClient getContainerClient(BlobServiceClient blobServiceClient, String _containerName) {
    // Create the container and return a container client object
    return blobServiceClient.getBlobContainerClient(_containerName);
  }

  public static Long getBlobCount(BlobContainerClient blobContainerClient) {
    ListBlobsOptions options = new ListBlobsOptions().setMaxResultsPerPage(500).setDetails(new BlobListDetails().setRetrieveDeletedBlobs(false).setRetrieveSnapshots(true));
    Duration duration = Duration.ofMinutes(5);

    return blobContainerClient.listBlobs(options, duration).stream().count();
  }

  public static void listBlobs(BlobContainerClient blobContainerClient) {
    // List the blob(s) in the container.
    ListBlobsOptions options = new ListBlobsOptions().setMaxResultsPerPage(500).setDetails(new BlobListDetails().setRetrieveDeletedBlobs(false).setRetrieveSnapshots(true));
    Duration duration = Duration.ofMinutes(5);

    for (BlobItem blobItem : blobContainerClient.listBlobs(options, duration)) {
      System.out.println("\t" + blobItem.getName());
    }
  }

  public static void readBQTable() {

  }

  public static void getSparkSession() {
    SparkSession spark = SparkSession
      .builder()
      .appName("Java Spark SQL basic example")
      .master("local[*]")
//      .config("spark.some.config.option", "some-value")
      .getOrCreate();
  }

  public static void getGCSStorageClient() throws IOException {
    String jsonKeyFile = "~/.config/gcloud/application_default_credentials.json";
    String projectId = "xx-ww-xx-xx-dev";
    Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonKeyFile));

    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credentials).build().getService();
    Page<Bucket> buckets = storage.list();

    for (Bucket bucket : buckets.iterateAll()) {
      System.out.println(bucket.getName());
    }
  }

  public static String getSASToken(BlobClient blobClient, String contentType) {
    OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1000);
    BlobSasPermission sasPermission = new BlobSasPermission().setReadPermission(true);
    BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission)
      .setStartTime(OffsetDateTime.now().minusMinutes(500)).setProtocol(SasProtocol.HTTPS_ONLY)
      .setContentType(contentType);
    return blobClient.generateSas(sasSignatureValues);
  }

  public static void readFromGCS() {

  }

  public static void getBigQueryClient() {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
  }

  public static void main(String[] args) throws IOException {
    String blobName = "xxxx";

//    BlobClient blobClient = getContainerClient(getClient(), containerName).getBlobClient(blobName);

    getGCSStorageClient();



    // Reference URL : https://xxx.blob.core.windows.net/xx-prod/xxx
  }
}
