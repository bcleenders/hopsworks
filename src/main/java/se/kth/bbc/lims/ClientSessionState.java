package se.kth.bbc.lims;

import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import se.kth.bbc.security.ua.UserManager;
import se.kth.bbc.security.ua.model.User;
import se.kth.bbc.study.StudyFacade;
import se.kth.bbc.study.Study;

/**
 *
 * @author stig
 */
@ManagedBean
@SessionScoped
public class ClientSessionState implements Serializable {

  @EJB
  private StudyFacade studyFacade;
  
  @EJB
  private UserManager userFacade;

  private Study activeStudy;
  
  private User user;

  public void setActiveStudy(Study study) {
    this.activeStudy = study;
  }

  public Study getActiveStudy() {
    return activeStudy;
  }

  public String getActiveStudyname() {
    if (activeStudy != null) {
      return activeStudy.getName();
    } else {
      return null;
    }
  }

  public void setActiveStudyByUserAndName(User user, String studyname) {
    activeStudy = studyFacade.findByNameAndOwner(studyname, user);
  }

  private HttpServletRequest getRequest() {
    return (HttpServletRequest) FacesContext.getCurrentInstance().
            getExternalContext().getRequest();
  }

  /**
   * Get the username of the user currently logged in.
   * <p>
   * @return Email address of the user currently logged in. (Serves as
   * username.)
   */
  public String getLoggedInUsername() {
    return getRequest().getUserPrincipal().getName();
  }
  
  public User getLoggedInUser(){
    if(user == null){
      String email = getRequest().getUserPrincipal().getName();
      user = userFacade.findByEmail(email);
    }
    return user;
  }

}
