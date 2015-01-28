package se.kth.bbc.lims;

/**
 * Constants class to facilitate deployment on different servers.
 * TODO: move this to  configuration file to be read!
 *
 * @author stig
 */
public class Constants {
  
  //TODO: check entire project for correct closing of resources!

    //Local path to the hiway jar
    public static final String HIWAY_JAR_PATH = "/srv/hiway/hiway-core-0.2.0-SNAPSHOT.jar";
    
    //User under which yarn is run
    public static final String DEFAULT_YARN_USER = "glassfish";
    
    //Relative output path (within hdfs study folder) which to write cuneiform output to
    public static final String CUNEIFORM_DEFAULT_OUTPUT_PATH = "Cuneiform/Output/";
    
    //Default configuration locations
    public static final String DEFAULT_HADOOP_CONF_DIR = "/srv/hadoop/etc/hadoop/";
    public static final String DEFAULT_YARN_CONF_DIR = "/srv/hadoop/etc/hadoop/";
    
    //Default configuration file names
    public static final String DEFAULT_YARN_CONFFILE_NAME = "yarn-site.xml";
    public static final String DEFAULT_HADOOP_CONFFILE_NAME = "core-site.xml";
    public static final String DEFAULT_HDFS_CONFFILE_NAME = "hdfs-site.xml";
            
    //Environment variable keys
    public static final String ENV_KEY_YARN_CONF_DIR = "YARN_CONF_DIR";
    public static final String ENV_KEY_HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    
    //Spark constants
    public static final String SPARK_STAGING_DIR = ".sparkStaging";
    public static final String SPARK_LOCRSC_SPARK_JAR = "__spark__.jar";
    public static final String SPARK_LOCRSC_APP_JAR = "__app__.jar";
    public static final String SPARK_AM_MAIN = "org.apache.spark.deploy.yarn.ApplicationMaster";
    public static final String DEFAULT_SPARK_JAR_PATH = "/srv/spark/spark.jar";
    public static final String SPARK_DEFAULT_OUTPUT_PATH = "Spark/Output/";
   
}
