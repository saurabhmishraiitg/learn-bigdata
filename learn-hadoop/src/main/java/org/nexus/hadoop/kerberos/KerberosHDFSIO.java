package org.nexus.hadoop.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexus.loader.JSONConfLoader;

import java.io.IOException;

/**
 * This is a POC example to verify access to hadoop kerberized cluster remotely.
 * For more in-depth reading
 * 1. https://henning.kropponline.de/2016/02/14/a-secure-hdfs-client-example/
 */
public class KerberosHDFSIO {

    private static final Logger LOGGER = LogManager.getLogger(KerberosHDFSIO.class);

    public static void main(String[] args) throws IOException {
        //The location of krb5.conf file needs to be provided in the VM arguments for the JVM
        //-Djava.security.krb5.conf=/Users/xxx/Desktop/utils/cluster/xx/krb5.conf
//        String username = System.getenv("USER");
        String userHome = System.getenv("HOME");

        // Get the details for principal, config file folder and keytab location from the user conf
        String d17Principal = new JSONConfLoader(userHome + "/nexus.conf", "org.nexus")
                .getConfig("learn-hadoop", "d17-principal");
        String d17HadoopConfDir = new JSONConfLoader(userHome + "/nexus.conf", "org.nexus")
                .getConfig("learn-hadoop", "d17-hadoop-conf-dir");
        String d17KeytabPath = new JSONConfLoader(userHome + "/nexus.conf", "org.nexus")
                .getConfig("learn-hadoop", "d17-keytab-path");

        //The following property is enough for a non-kerberized setup
        // conf.set("fs.defaultFS", "localhost:9000");

        //need following set of properties to access a kerberized cluster
        Configuration conf = new Configuration();
        conf.addResource(new Path("file://" + d17HadoopConfDir + "/hdfs-site.xml"));
        conf.addResource(new Path("file://" + d17HadoopConfDir + "/core-site.xml"));

        // Following is the set of properties which are actually needed for this to work.
        // conf.set("fs.defaultFS", "hdfs://dev16ha");
        // conf.set("dfs.nameservices", "dev16ha");
        // conf.set("dfs.ha.namenodes.dev16ha", "nn1,nn2");
        // conf.set("dfs.namenode.rpc-address.dev16ha.nn1", "xxx.xx-xx.com:8020");
        // conf.set("dfs.namenode.rpc-address.dev16ha.nn2", "xxx.xx-xxx.com:8020");
        // conf.set("dfs.client.failover.proxy.provider.dev16ha",
        //         "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        // conf.set("hadoop.security.authentication", "kerberos");

        //If you don't want to bother with HA namenodes and want to hardcode the namenode service to hit, the use the single liner below
        //		conf.set("fs.defaultFS", "hdfs://xxx.xx-xxx.com:8020");

        // Initialize the keytab
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab(d17Principal, d17KeytabPath);

        try (FileSystem fs = FileSystem.get(conf);) {
            FileStatus[] fileStatuses = fs.listStatus(new Path("/user/xxx"));
            for (FileStatus fileStatus : fileStatuses) {
                LOGGER.info(fileStatus.getPath().getName());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
