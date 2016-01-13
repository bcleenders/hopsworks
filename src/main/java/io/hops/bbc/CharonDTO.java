/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.hops.bbc;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CharonDTO implements Serializable {

  private String charonPath;
  private String hdfsPath;
  private String siteID;

  public CharonDTO(String charonPath, String hdfsPath) {
    this.charonPath = charonPath;
    this.hdfsPath = hdfsPath;
  }

  public CharonDTO(String siteID) {
    this.siteID = siteID;
  }

  public CharonDTO() {
  }

  public String getCharonPath() {
    return charonPath;
  }

  public String getHdfsPath() {
    return hdfsPath;
  }

  public String getSiteID() {
    return siteID;
  }

  public void setCharonPath(String charonPath) {
    this.charonPath = charonPath;
  }
  public void setSiteID(String siteID) {
    this.siteID = siteID;
  }

  public void setHdfsPath(String hdfsPath) {
    this.hdfsPath = hdfsPath;
  }

  @Override
  public String toString() {
    return "Path: " + charonPath + " ; type: " + hdfsPath;
  }
  
}
