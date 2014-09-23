/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.bbc.study;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author stig
 */
@Entity
public class StudyDetail implements Serializable{
    
    @Id
    private String studyName;
    private String email;
    private String creator;

    public StudyDetail(){
        
    }
    
    public StudyDetail(String studyName, String email, String creatorName) {
        this.studyName = studyName;
        this.email = email;
        this.creator = creatorName;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    
}
