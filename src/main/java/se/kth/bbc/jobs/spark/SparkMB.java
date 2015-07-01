package se.kth.bbc.jobs.spark;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import se.kth.bbc.activity.ActivityFacade;
import se.kth.bbc.fileoperations.FileOperations;
import se.kth.bbc.jobs.FileSelectionController;
import se.kth.bbc.jobs.JobMB;
import se.kth.bbc.jobs.JobControllerEvent;
import se.kth.bbc.jobs.jobhistory.JobHistory;
import se.kth.bbc.jobs.jobhistory.JobHistoryFacade;
import se.kth.bbc.lims.ClientSessionState;
import se.kth.bbc.lims.MessagesController;
import se.kth.bbc.lims.StagingManager;
import se.kth.hopsworks.controller.SparkController;

/**
 * Written for Spark 1.2. If the internals of running Spark on Yarn change,
 * this class may break since it sets environment variables and writes
 * LocalResources that have specific names.
 * <p>
 * @author stig
 */
@ManagedBean
@ViewScoped
public final class SparkMB extends JobMB {

  private static final Logger logger = Logger.getLogger(SparkMB.class.getName());

  //Variables for new job
  private String jobName, mainClass, args, appJarName;

  @ManagedProperty(value = "#{clientSessionState}")
  private ClientSessionState sessionState;

  @ManagedProperty(value = "#{fileSelectionController}")
  private FileSelectionController fileSelectionController;

  @EJB
  private JobHistoryFacade history;

  @EJB
  private FileOperations fops;

  @EJB
  private StagingManager stagingManager;

  @EJB
  private ActivityFacade activityFacade;

  @EJB
  private SparkController controller;

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

  public String getArgs() {
    return args;
  }

  public void setArgs(String args) {
    this.args = args;
  }

  public String getAppJarName() {
    return appJarName;
  }

  public void setSessionState(ClientSessionState sessionState) {
    this.sessionState = sessionState;
  }

  @PostConstruct
  public void init() {
    try {
      String path = stagingManager.getStagingPath() + File.separator
              + sessionState.getLoggedInUsername() + File.separator
              + sessionState.getActiveProjectname();
      super.setBasePath(path);
      super.setJobHistoryFacade(history);
      super.setFileOperations(fops);
      super.setFileSelector(fileSelectionController);
      super.setActivityFacade(activityFacade);
    } catch (IOException c) {
      logger.log(Level.SEVERE,
              "Failed to initialize Spark staging folder for uploading.", c);
      MessagesController.addErrorMessage(
              "Failed to initialize Spark controller. Running spark jobs will not work.");
    }
  }

  @Override
  public void registerMainFile(String filename, Map<String, String> attributes) {
    appJarName = filename;
  }

  @Override
  public void registerExtraFile(String filename, Map<String, String> attributes) {
    //TODO: allow for file input in Spark
  }

  public void startJob() {
    try {
      SparkJobConfiguration config = new SparkJobConfiguration();
      config.setJarPath(getMainFilePath());
      config.setMainClass(mainClass);
      config.setArgs(args);
      //TODO: config.addExtraFiles(getExtraFiles());
      JobHistory jh = controller.startJob(config, sessionState.
              getLoggedInUsername(), sessionState.getActiveProject().getId());
      setSelectedJob(jh);
    } catch (IllegalStateException | IOException e) {
      MessagesController.addErrorMessage("Failed to start application master.",
              e.getLocalizedMessage());
    }
  }

  @Override
  protected String getUserMessage(JobControllerEvent event, String extraInfo) {
    switch (event) {
      case MAIN_UPLOAD_FAILURE:
        return "Failed to upload application jar " + extraInfo + ".";
      case MAIN_UPLOAD_SUCCESS:
        return "Workflow file " + extraInfo + " successfully uploaded.";
      case EXTRA_FILE_FAILURE:
        return "Failed to upload input file " + extraInfo + ".";
      case EXTRA_FILE_SUCCESS:
        return "Input file " + extraInfo + " successfully uploaded.";
      default:
        return super.getUserMessage(event, extraInfo);
    }
  }

  @Override
  protected String getLogMessage(JobControllerEvent event, String extraInfo) {
    switch (event) {
      case MAIN_UPLOAD_FAILURE:
        return "Failed to upload application jar " + extraInfo + ".";
      case EXTRA_FILE_FAILURE:
        return "Failed to upload input file " + extraInfo + ".";
      default:
        return super.getLogMessage(event, extraInfo);
    }
  }

  public void setFileSelectionController(FileSelectionController fs) {
    this.fileSelectionController = fs;
  }

}
