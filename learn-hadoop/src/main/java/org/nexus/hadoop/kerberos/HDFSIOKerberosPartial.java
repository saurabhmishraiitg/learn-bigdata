package org.nexus.hadoop.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * In this class we are demonstrating how without explicitly providing keytab
 * details and kerberos mode, we can still connect to HDFS, if the necessary
 * details are available in the environment i.e. kinit has been done.
 *
 * @author sxxx
 */
public class HDFSIOKerberosPartial {

    public static void main(String[] args) throws IOException {

        Configuration conf = new Configuration();
//The following property is enough for a non-kerberized setup
//		conf.set("fs.defaultFS", "localhost:9000");

//need following set of properties to access a kerberized cluster
        conf.set("fs.defaultFS", "hdfs://dev16ha");
        conf.set("dfs.nameservices", "dev16ha");
        conf.set("dfs.ha.namenodes.dev16ha", "nn1,nn2");
        conf.set("dfs.namenode.rpc-address.dev16ha.nn1", "xxx.xx-xx.com:8020");
        conf.set("dfs.namenode.rpc-address.dev16ha.nn2", "xxx.xx-xx.com:8020");
        conf.set("dfs.client.failover.proxy.provider.dev16ha",
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");

        try (FileSystem fs = FileSystem.get(conf);) {
            FileStatus[] fileStatuses = fs.listStatus(new Path("/user/xxx/dropoff"));
            for (FileStatus fileStatus : fileStatuses) {
                System.out.println(fileStatus.getPath().getName());
            }
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
