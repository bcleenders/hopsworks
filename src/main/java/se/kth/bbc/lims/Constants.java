package se.kth.bbc.lims;

/**
 * Constants class to facilitate deployment on different servers.
 * TODO: move this to  configuration file to be read!
 *
 * @author stig
 */
public class Constants {

    //public static final String server = "LOCAL";
    public static final String server = "SNURRAN";

    public static final String UPLOAD_DIR = server.equals("LOCAL") ? "/home/stig/tst" : "/tmp";
    public static final String NAMENODE_URI = server.equals("LOCAL") ? "hdfs://localhost:8020":"hdfs://snurran.sics.se:9999";
    
    public static final String LOCAL_APPMASTER_DIR = server.equals("LOCAL") ? "/home/stig/tst/appMaster" : "/tmp/appMaster";
    public static final String LOCAL_EXTRA_DIR = server.equals("LOCAL") ? "/home/stig/tst/extraFiles" : "/tmp/extraFiles";
    
    public static final String JOB_UPLOAD_DIR = server.equals("LOCAL") ? "/home/stig/tst/jobs" : "/tmp/jobs";

    public static final String FLINK_CONF_DIR = server.equals("LOCAL") ? "/home/stig/Downloads/flink-0.8-incubating-SNAPSHOT/conf/.yarn-properties" : "/home/glassfish/stig/flink-0.8-incubating-SNAPSHOT/conf/.yarn-properties";

}
