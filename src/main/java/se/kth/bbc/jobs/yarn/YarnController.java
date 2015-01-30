package se.kth.bbc.jobs.yarn;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import se.kth.bbc.fileoperations.FileOperations;
import se.kth.bbc.jobs.JobController;
import se.kth.bbc.jobs.JobControllerEvent;
import se.kth.bbc.jobs.jobhistory.JobHistoryFacade;
import se.kth.bbc.lims.ClientSessionState;
import se.kth.bbc.lims.MessagesController;
import se.kth.bbc.lims.StagingManager;

/**
 *
 * @author stig
 */
@ManagedBean
@ViewScoped
public class YarnController extends JobController {

  private static final Logger logger = Logger.getLogger(YarnController.class.
          getName());

  private static final String KEY_APP_NAME = "appname";
  private static final String KEY_ARGS = "args";
  private static final String KEY_MAIN = "mainClassName";

  @ManagedProperty(value = "#{clientSessionState}")
  private transient ClientSessionState sessionState;

  @EJB
  private StagingManager stagingManager;

  @EJB
  private JobHistoryFacade history;

  @EJB
  private FileOperations fops;

  @PostConstruct
  public void init() {
    try {
      String path = stagingManager.getStagingPath() + File.separator
              + sessionState.getLoggedInUsername() + File.separator
              + sessionState.getActiveStudyname();
      super.setBasePath(path);
      super.setJobHistoryFacade(history);
      super.setFileOperations(fops);
    } catch (IOException c) {
      logger.log(Level.SEVERE,
              "Failed to initialize Yarn staging folder for uploading.", c);
      MessagesController.addErrorMessage(
              "Failed to initialize Yarn controller. Running Yarn jobs will not work.");
    }
  }

  public String getAppMasterJarPath() {
    return getMainFilePath();
  }

  public void setAppName(String name) {
    putVariable(KEY_APP_NAME, name);
  }

  public String getAppName() {
    return getVariable(KEY_APP_NAME);
  }

  public String getArgs() {
    return getVariable(KEY_ARGS);
  }

  public void setArgs(String args) {
    putVariable(KEY_ARGS, args);
  }

  public void setMainClassName(String mainClass) {
    putVariable(KEY_MAIN, mainClass);
  }

  public String getMainClassName() {
    return getVariable(KEY_MAIN);
  }

  @Override
  protected void afterUploadMainFile(FileUploadEvent event) {
    //Nothing to do
  }

  @Override
  protected void afterUploadExtraFile(FileUploadEvent event) {
    //Nothing to do
  }

  public void runJar() {
    //TODO: fix this
    Map<String, String> files = getFiles();
    String appMasterJar = files.remove(KEY_MAIN_FILE);
    YarnRunner.Builder builder = new YarnRunner.Builder(appMasterJar,
            "appMaster.jar");
    if (!files.isEmpty()) {
      //builder.addAllLocalResourcesPaths(files);
    }
    builder.amArgs(getVariable(KEY_ARGS)).amMainClass(
            getVariable(KEY_MAIN));
    YarnRunner runner;
    try {
      runner = builder.build();
    } catch (IllegalStateException e) {
      logger.log(Level.SEVERE, "Could not initialize YarnRunner.", e);
      MessagesController.addErrorMessage("Failed to initialize Yarn client");
      return;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Could not initialize YarnRunner.", e);
      MessagesController.addErrorMessage("Failed to initialize Yarn client.");
      return;
    }
    try {
      runner.startAppMaster();
    } catch (IOException | YarnException e) {
      logger.
              log(Level.SEVERE, "Error while initializing Application Master.",
                      e);
      MessagesController.addErrorMessage(
              "Failed to initialize Application Master.");
    }
  }

  public void setSessionState(ClientSessionState sessionState) {
    this.sessionState = sessionState;
  }
  
  @Override
  protected String getUserMessage(JobControllerEvent event, String extraInfo) {
    switch (event) {
      case MAIN_UPLOAD_FAILURE:
        return "Failed to upload AM jar " + extraInfo + ".";
      case MAIN_UPLOAD_SUCCESS:
        return "AM jar "+extraInfo+" successfully uploaded.";
      default:
        return super.getUserMessage(event, extraInfo);
    }
  }

  @Override
  protected String getLogMessage(JobControllerEvent event, String extraInfo) {
    switch (event) {
      case MAIN_UPLOAD_FAILURE:
        return "Failed to upload AM jar " + extraInfo + ".";
      default:
        return super.getLogMessage(event, extraInfo);
    }
  }

}
