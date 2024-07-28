package org.nexus.hadoop.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author naruto-kun
 */
public class HDFSIO {

    public static void main(String[] args) {
        Configuration conf = new Configuration();
// conf.set("fs.default.name", "hdfs://dev16ha");
        List<FileStatus> fileStatuses = new ArrayList<FileStatus>();
        try (FileSystem fs = FileSystem.get(conf)) {
//			FileSystem fs = FileSystem.get(conf);
            Path folderPath = new Path("/user/xxx/");
            if (fs != null && fs.exists(folderPath)) {
                FileStatus[] dir = fs.listStatus(folderPath);
                for (FileStatus file : dir) {
                    String fileName = file.getPath().getName();
                    System.out.println(fileName);
                }
            } else {
                System.out.println("FileSystem Instance Returned as Null or No Cubes folder Exists inside IMS_Home!");
            }

            fs.copyFromLocalFile(new Path(args[0]), new Path("/user/xxx"));
//			fs.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        System.out.println("Number of Files are : " + fileStatuses.size());
    }
}
