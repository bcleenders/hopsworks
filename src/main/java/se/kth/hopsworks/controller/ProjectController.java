/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.hopsworks.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.core.Response;
import se.kth.bbc.activity.Activity;
import se.kth.bbc.activity.ActivityFacade;
import se.kth.bbc.fileoperations.FileOperations;
import se.kth.bbc.lims.Constants;
import se.kth.bbc.security.ua.UserManager;
import se.kth.bbc.security.ua.model.User;
import se.kth.bbc.study.Study;
import se.kth.bbc.study.StudyFacade;
import se.kth.bbc.study.StudyRoleTypes;
import se.kth.bbc.study.StudyTeam;
import se.kth.bbc.study.StudyTeamFacade;
import se.kth.bbc.study.StudyTeamPK;
import se.kth.bbc.study.fb.Inode;
import se.kth.bbc.study.fb.InodeFacade;
import se.kth.bbc.study.services.StudyServiceEnum;
import se.kth.bbc.study.services.StudyServiceFacade;
import se.kth.hopsworks.rest.AppException;

/**
 * @author André<amore@kth.se>
 * @author Ermias<ermiasg@kth.se>
 */
@Stateless
public class ProjectController {

  private final static Logger logger = Logger.getLogger(ProjectController.class.
          getName());
  @EJB
  private StudyFacade studyFacade;
  @EJB
  private StudyTeamFacade studyTeamFacade;
  @EJB
  private UserManager userBean;
  @EJB
  private ProjectNameValidator projectNameValidator;
  @EJB
  private ActivityFacade activityFacade;
  @EJB
  private FileOperations fileOps;
  @EJB
  private StudyServiceFacade studyServicesFacade;
  @EJB
  private InodeFacade inodes;

  /**
   * Creates a new project(study), the related DIR, the different services in
   * the project, and the master of the project.
   *
   * @param newStudyName the name of the new project(study)
   * @param email
   * @return
   * @throws AppException if the project name already exists.
   * @throws IOException if the DIR associated with the project could not be
   * created. For whatever reason.
   */
  @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
  //this needs to be an atomic operation (all or nothing) REQUIRES_NEW 
  //will make sure a new transaction is created even if this method is
  //called from within a transaction.
  public Study createStudy(String newStudyName, String email) throws
          AppException, IOException {
    User user = userBean.getUserByEmail(email);
    //if there is no project by the same name for this user and project name is valid
    if (projectNameValidator.isValidName(newStudyName) && !studyFacade.
            studyExistsForOwner(newStudyName, user)) {
      //Create a new study object
      Date now = new Date();
      Study study = new Study(newStudyName, user, now);
      //create folder structure
      //mkStudyDIR(study.getName());
      logger.log(Level.FINE, "{0} - study directory created successfully.",
              study.getName());

      //Persist study object
      studyFacade.persistStudy(study);
      studyFacade.flushEm();//flushing it to get study id
      //Add the activity information     
      logActivity(ActivityFacade.NEW_STUDY,
              ActivityFacade.FLAG_STUDY, user, study);
      //update role information in study
      addStudyMaster(study.getId(), user.getEmail());
      logger.log(Level.FINE, "{0} - study created successfully.", study.
              getName());
      return study;
    } else {
      logger.log(Level.SEVERE, "Study with name {0} already exists!",
              newStudyName);
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.PROJECT_NAME_EXIST);
    }

  }

  /**
   * Returns a Study
   * <p>
   * @param id the identifier for a Study
   * @return Study
   * @throws se.kth.hopsworks.rest.AppException if the study could not be found.
   */
  public Study findStudyById(Integer id) throws AppException {

    Study study = studyFacade.find(id);
    if (study != null) {
      return study;
    } else {
      throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
              ResponseMessages.PROJECT_NOT_FOUND);
    }
  }

  public boolean addServices(Study study, List<StudyServiceEnum> services,
          String userEmail) {
    boolean addedService = false;
    //Add the desired services
    for (StudyServiceEnum se : services) {
      if (!studyServicesFacade.findEnabledServicesForStudy(study).contains(se)) {
        studyServicesFacade.addServiceForStudy(study, se);
        addedService = true;
      }
    }

    if (addedService) {
      User user = userBean.getUserByEmail(userEmail);
      logActivity(ActivityFacade.ADDED_SERVICES, ActivityFacade.FLAG_STUDY,
              user, study);
    }
    return addedService;
  }

  public void changeName(Study study, String newStudyName, String userEmail)
          throws AppException {
    User user = userBean.getUserByEmail(userEmail);

    boolean nameExists = studyFacade.studyExistsForOwner(newStudyName, user);

    if (projectNameValidator.isValidName(newStudyName) && nameExists) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.PROJECT_NAME_EXIST);
    }
    study.setName(newStudyName);
    studyFacade.mergeStudy(study);

    logActivity(ActivityFacade.STUDY_NAME_CHANGED, ActivityFacade.FLAG_STUDY,
            user, study);
  }

  //Set the study owner as study master in StudyTeam table
  private void addStudyMaster(Integer study_id, String userName) {
    StudyTeamPK stp = new StudyTeamPK(study_id, userName);
    StudyTeam st = new StudyTeam(stp);
    st.setTeamRole(StudyRoleTypes.MASTER.getTeam());
    st.setTimestamp(new Date());
    studyTeamFacade.persistStudyTeam(st);
  }

  //create study on HDFS
  private void mkStudyDIR(String studyName) throws IOException {

    String rootDir = Constants.DIR_ROOT;
    String studyPath = File.separator + rootDir + File.separator + studyName;
    String resultsPath = studyPath + File.separator
            + Constants.DIR_RESULTS;
    String cuneiformPath = studyPath + File.separator
            + Constants.DIR_CUNEIFORM;
    String samplesPath = studyPath + File.separator
            + Constants.DIR_SAMPLES;

    fileOps.mkDir(studyPath);
    fileOps.mkDir(resultsPath);
    fileOps.mkDir(cuneiformPath);
    fileOps.mkDir(samplesPath);
  }

  /**
   * Remove a study and optionally all associated files.
   *
   * @param studyID to be removed
   * @param email
   * @param deleteFilesOnRemove if the associated files should be deleted
   * @return true if the study and the associated files are removed
   * successfully, and false if the associated files could not be removed.
   * @throws IOException if the hole operation failed. i.e the study is not
   * removed.
   * @throws AppException if the project could not be found.
   */
  public boolean removeByName(Integer studyID, String email,
          boolean deleteFilesOnRemove) throws IOException, AppException {
    boolean success = !deleteFilesOnRemove;
    User user = userBean.getUserByEmail(email);
    Study study = studyFacade.find(studyID);
    if (study == null) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.PROJECT_NOT_FOUND);
    }
    studyFacade.remove(study);

    logActivity(ActivityFacade.REMOVED_STUDY,
            ActivityFacade.FLAG_STUDY, user, study);

    if (deleteFilesOnRemove) {
      String path = File.separator + Constants.DIR_ROOT + File.separator
              + study.getName();
      success = fileOps.rmRecursive(path);
    }
    logger.log(Level.FINE, "{0} - study removed.", study.getName());

    return success;
  }

  /**
   * Adds new team members to a project(study) - bulk persist
   * if team role not specified or not in (Master or Researcher)defaults to
   * researcher
   * <p>
   * @param study
   * @param email
   * @param studyTeams
   * @return a list of user names that could not be added to the project team
   * list.
   */
  @TransactionAttribute(TransactionAttributeType.NEVER)
  public List<String> addMembers(Study study, String email,
          List<StudyTeam> studyTeams) {
    List<String> failedList = new ArrayList<>();
    User user = userBean.getUserByEmail(email);
    User newMember;
    for (StudyTeam studyTeam : studyTeams) {
      try {
        if (!studyTeam.getStudyTeamPK().getTeamMember().equals(user.getEmail())) {

          //if the role is not properly set set it to the defualt resercher.
          if (studyTeam.getTeamRole() == null || (!studyTeam.getTeamRole().
                  equals(StudyRoleTypes.RESEARCHER.getTeam()) && !studyTeam.
                  getTeamRole().equals(StudyRoleTypes.MASTER.getTeam()))) {
            studyTeam.setTeamRole(StudyRoleTypes.RESEARCHER.getTeam());
          }

          studyTeam.setTimestamp(new Date());
          newMember = userBean.getUserByEmail(studyTeam.getStudyTeamPK().getTeamMember());
          if (newMember!= null && !studyTeamFacade.isUserMemberOfStudy(study, newMember)) {
            //this makes sure that the member is added to the study sent as the 
            //first param b/c the securty check was made on the parameter sent as path.
            studyTeam.getStudyTeamPK().setStudyId(study.getId());
            studyTeamFacade.persistStudyTeam(studyTeam);
            logger.log(Level.FINE, "{0} - member added to study : {1}.",
                    new Object[]{newMember.getEmail(),
                      study.getName()});

            logActivity(ActivityFacade.NEW_MEMBER + studyTeam.
                    getStudyTeamPK().getTeamMember(),
                    ActivityFacade.FLAG_STUDY, user, study);
          } else if (newMember == null){
            failedList.add(studyTeam.getStudyTeamPK().getTeamMember() + " was not found in the system.");
          } else {
            failedList.add(newMember.getEmail() + " is alrady a member in this project.");
          }
        }
      } catch (EJBException ejb) {
        failedList.add(studyTeam.getStudyTeamPK().getTeamMember() + "could not be added. Try again later.");
        logger.log(Level.SEVERE, "Adding  team member {0} to members failed",
                studyTeam.getStudyTeamPK().getTeamMember());
      }
    }
    return failedList;
  }

  private boolean isStudyPresentInHdfs(String studyname) {
    Inode root = inodes.getStudyRoot(studyname);
    if (root == null) {
      logger.log(Level.INFO, "Study folder not found in HDFS for study {0} .",
              studyname);
      return false;
    }
    return true;
  }

  /**
   * Project info as data transfer object that can be sent to the user.
   * <p>
   * @param studyID of the project
   * @return project DTO that contains team members and services
   * @throws se.kth.hopsworks.rest.AppException
   */
  public ProjectDTO getStudyByID(Integer studyID) throws AppException {
    Study study = studyFacade.find(studyID);
    if (study == null) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.PROJECT_NOT_FOUND);
    }
    List<StudyTeam> studyTeam = studyTeamFacade.findMembersByStudy(study);
    List<StudyServiceEnum> studyServices = studyServicesFacade.
            findEnabledServicesForStudy(study);
    List<String> services = new ArrayList<>();
    for (StudyServiceEnum s : studyServices) {
      services.add(s.toString());
    }
    return new ProjectDTO(study, services, studyTeam);
  }

  /**
   * Deletes a member from a project
   *
   * @param study
   * @param email
   * @param toRemoveEmail
   * @throws AppException
   */
  public void deleteMemberFromTeam(Study study, String email,
          String toRemoveEmail) throws AppException {
    User userToBeRemoved = userBean.getUserByEmail(toRemoveEmail);
    if (userToBeRemoved == null) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.USER_DOES_NOT_EXIST);
      //user not found
    }
    StudyTeam studyTeam = studyTeamFacade.findStudyTeam(study, userToBeRemoved);
    if (studyTeam == null) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.TEAM_MEMBER_NOT_FOUND);
    }
    studyTeamFacade.removeStudyTeam(study, userToBeRemoved);
    User user = userBean.getUserByEmail(email);
    logActivity(ActivityFacade.REMOVED_MEMBER + toRemoveEmail,
            ActivityFacade.FLAG_STUDY, user, study);
  }

  /**
   * Updates the role of a member
   * <p>
   * @param study
   * @param owner that is performing the update
   * @param toUpdateEmail
   * @param newRole
   * @throws AppException
   */
  public void updateMemberRole(Study study, String owner,
          String toUpdateEmail, String newRole) throws AppException {
    User projOwner = userBean.getUserByEmail(owner);
    User user = userBean.getUserByEmail(toUpdateEmail);
    if (user == null) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.USER_DOES_NOT_EXIST);
      //user not found
    }
    StudyTeam studyTeam = studyTeamFacade.findStudyTeam(study, user);
    if (studyTeam == null) {
      throw new AppException(Response.Status.BAD_REQUEST.getStatusCode(),
              ResponseMessages.TEAM_MEMBER_NOT_FOUND);
      //member not found
    }
    if (!studyTeam.getTeamRole().equals(newRole)) {
      studyTeam.setTeamRole(newRole);
      studyTeam.setTimestamp(new Date());
      studyTeamFacade.update(studyTeam);
      logActivity(ActivityFacade.CHANGE_ROLE + toUpdateEmail,
              ActivityFacade.FLAG_STUDY, projOwner, study);
    }

  }

  /**
   * Retrieves all the study teams that a user have a role
   * <p>
   * @param email of the user
   * @return a list of study team
   */
  public List<StudyTeam> findStudyByUser(String email) {
    User user = userBean.getUserByEmail(email);
    return studyTeamFacade.findByMember(user);
  }

  /**
   * Retrieves all the study teams for a study
   * <p>
   * @param studyID
   * @return a list of study team
   */
  public List<StudyTeam> findStudyTeamById(Integer studyID) {
    Study study = studyFacade.find(studyID);
    return studyTeamFacade.findMembersByStudy(study);
  }

  /**
   * Logs activity
   * <p>
   * @param activityPerformed the description of the operation performed
   * @param flag on what the operation was performed(FLAG_STUDY, FLAG_USER)
   * @param performedBy the user that performed the operation
   * @param performedOn the project the operation was performed on.
   */
  public void logActivity(String activityPerformed, String flag,
          User performedBy, Study performedOn) {
    Date now = new Date();
    Activity activity = new Activity();
    activity.setActivity(activityPerformed);
    activity.setFlag(flag);
    activity.setStudy(performedOn);
    activity.setTimestamp(now);
    activity.setUser(performedBy);

    activityFacade.persistActivity(activity);
  }
}
