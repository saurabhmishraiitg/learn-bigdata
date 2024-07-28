package org.nexus;

import com.azure.storage.blob.BlobClient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReadFile {

  public static String BASE_URL = "https://xxx.blob.core.windows.net/xx-prod/";

  public static ArrayList<String> readCSVFile(String fileName) throws IOException {
    File file = new File(fileName);
    BufferedReader br = new BufferedReader(new FileReader(file));
    String st;

    return (ArrayList<String>) br.lines().collect(Collectors.toList());
  }

  public static String getContentType(String rawContentType) {
    String contentType;
    switch (rawContentType) {
      case "msg":
        contentType = "msg";
        //application/octet-stream
        break;
      case "application/vnd\\.openxmlformats-officedocument\\.presentationml\\.presentation":
        contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        break;
      case "application/vnd\\.ms-excel":
        contentType = "application/vnd.ms-excel";
        break;
      case "application/vnd\\.ms-excel\\.sheet\\.macroEnabled\\.12":
        contentType = "application/vnd.ms-excel.sheet.macroEnabled.12";
        break;
      case "application/vnd\\.ms-excel\\.sheet\\.binary\\.macroEnabled\\.12":
        contentType = "application/vnd.ms-excel.sheet.binary.macroEnabled.12";
        break;
      case "application/vnd\\.openxmlformats-officedocument\\.wordprocessingml\\.document":
        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        break;
      case "application/pdf":
        contentType = "application/pdf";
        break;
      case "image/png":
        contentType = "image/png";
        break;
      case "text/plain":
        contentType = "text/plain";
        break;
      case "image/jpeg":
        contentType = "image/jpeg";
        break;
      case "image/svg\\+xml":
        contentType = "image/svg\\+xml";
        break;
      case "application/vnd\\.openxmlformats-officedocument\\.spreadsheetml\\.sheet":
        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        break;
      default:
        contentType = "text/plain";
        System.out.println("Invalid contentType found" + rawContentType);
        break;
    }

    return contentType;
  }

  public class Attachment {
    String workflowId;
    String submissionId;
    String fileId;
    String fileName;
    String fileContentType;
    String sasURL;

    @Override
    public String toString() {
      return "Attachment{" +
        "workflowId='" + workflowId + '\'' +
        ", submissionId='" + submissionId + '\'' +
        ", fileId='" + fileId + '\'' +
        ", fileName='" + fileName + '\'' +
        ", fileContentType='" + fileContentType + '\'' +
        ", sasURL='" + sasURL + '\'' +
        '}';
    }

    public String toLine() {
      return "'" + workflowId + "';'" + submissionId + "';'" + fileId + "';'" + fileName.replace(';', '_').replace('\'','_') + "';'" + fileContentType + "';'" + sasURL + "'";
    }
  }

  public Attachment getColumns(String line, String delimiter) {
    String[] columns = line.split(delimiter);

    Attachment attachment = new Attachment();
    attachment.workflowId = columns[0];
    attachment.submissionId = columns[1];
    attachment.fileId = columns[2];
    attachment.fileName = columns[3];
    attachment.fileContentType = columns[4];

    return attachment;
  }

  public static String generateSASToken(String fileId, String contentType) {
    BlobClient blobClient = AzureSASTokenGenerate.getContainerClient(AzureSASTokenGenerate.getClient(), AzureSASTokenGenerate.containerName).getBlobClient(fileId);

    return AzureSASTokenGenerate.getSASToken(blobClient, contentType);
  }


  public static void writeToFile(String fileName, List<Attachment> attachmentList) throws IOException {
    FileWriter fileWriter = new FileWriter(fileName);
    for (Attachment attachment : attachmentList) {
      fileWriter.write(attachment.toLine() + System.lineSeparator());
    }
    fileWriter.close();
  }

  public static void main(String[] args) throws IOException {
    String inputFileName = "/Users/xxx/Downloads/bquxjob_209xxxxc77.csv";
    String outputFileName = "/Users/xxx/Downloads/xxx.csv";
    ArrayList<String> lines = readCSVFile(inputFileName);

    ReadFile rdFile = new ReadFile();
    List<Attachment> attachmentList = new ArrayList<Attachment>();

    lines.stream().map(line -> {
      Attachment attachment = rdFile.getColumns(line, ",");
      attachment.sasURL = BASE_URL + attachment.fileId + "?"
        + generateSASToken(attachment.fileId, getContentType(attachment.fileContentType));
      return attachment;
    }).forEach(attachmentList::add);
//      .forEach(System.out::println);

//    attachmentList.forEach(System.out::println);

    writeToFile(outputFileName, attachmentList);
  }
}
