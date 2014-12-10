/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.bbc.security.ua;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import se.kth.bbc.security.ua.model.People;

/**
 *
 * @author Ali Gholami <gholami@pdc.kth.se>
 */
@ManagedBean
@SessionScoped
public class ResetPassword implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(UserRegistration.class.getName());

     
    private String username;
    private String passwd1;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
    private String passwd2;
    private String answer;

    private String question;

    private People people;

    @EJB
    private UserManager mgr;

    @EJB
    private EmailBean emailBean;

    private SelectSecurityQuestionMenue secMgr;

    public People getPeople() {
        return people;
    }

    public void setPeople(People people) {
        this.people = people;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswd1() {
        return passwd1;
    }

    public void setPasswd1(String passwd1) {
        this.passwd1 = passwd1;
    }

    public String getPasswd2() {
        return passwd2;
    }

    public void setPasswd2(String passwd2) {
        this.passwd2 = passwd2;
    }

    //TODO: This hsould be changed to a url and then enforcing the password for reset upon first login
    public String sendTmpPassword() {

        people = mgr.getUser(this.username);

        try {
            
            if (!SecurityUtils.converToSHA256(answer).equals(people.getSecurityAnswer())) {

                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wrong answer", null));

                // Lock the account if 5 tmies wrong answer  
                int val = people.getFalseLogin();
                mgr.increaseLockNum(people.getUid(), val + 1);
                if (val > 5) {
                    mgr.deactivateUser(people.getUid());
                    return ("welcome");
                }
                return "";
            }

            // generate a radndom password
            String random_password = SecurityUtils.getRandomString();

            String message = buildResetMessage(random_password);
            
            logger.info("Entering email");
      
            // sned the new password to the user email
            emailBean.sendEmail(people.getEmail(), "Password reset", message);

            // make the account pending until it will be reset by user upon first login
            mgr.updateStatus(people.getUid(), AccountStatusIF.ACCOUNT_PENDING);

            // reset the old password with a new one
            mgr.resetPassword(people.getUid(), SecurityUtils.converToSHA256(random_password));
  
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | MessagingException ex) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Technical Error to reset password!", "null"));
            Logger.getLogger(ResetPassword.class.getName()).log(Level.SEVERE, null, ex);
            return ("");
        }
      
        return ("password_sent");
    }

    /**
     * Build a URL and send to user to reset their passwords
     *
     * @param random_password
     * @return
     */
    private String buildResetMessage(String random_password) {

        // TODO: make a url
        String urlFormat = random_password;
        return urlFormat;
    }

    public String changePassword() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) ctx.getExternalContext().getRequest();

        if (req.getRemoteUser() == null) {
             return ("welcome");
        }
        
        people = mgr.getUser(req.getRemoteUser());

        if (people == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            session.invalidate();
            return ("welcome");
        }

        try {
            // reset the old password with a new one
            mgr.resetPassword(people.getUid(), SecurityUtils.converToSHA256(passwd1));

            // make the account active until it will be reset by user upon first login
            mgr.updateStatus(people.getUid(), AccountStatusIF.ACCOUNT_ACTIVE);

            // logout user
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            session.invalidate();
            return ("password_changed");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(ResetPassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ("reset");
    }

    /**
     * Get the user security question
     * @return 
     */
    public String findQuestion() {

        people = mgr.getUser(this.username);
        if (people == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "User not found!", "null"));
            return "";
        }
        
        String quest = people.getSecurityQuestion();
        secMgr = new SelectSecurityQuestionMenue();
        this.question = secMgr.getUserQuestion(quest);

        return ("reset_password");
    }
}
