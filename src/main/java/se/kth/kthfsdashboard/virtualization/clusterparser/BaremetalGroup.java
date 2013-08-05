/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kthfsdashboard.virtualization.clusterparser;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Alberto Lorente Leal <albll@kth.se>
 */
public class BaremetalGroup implements Serializable{
    private String securityGroup;
    private int number;
    private List<String> hosts;
    private List<String> roles;
    private List<String> authorizePorts;

    public BaremetalGroup() {
    }

    public String getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(String securityGroup) {
        this.securityGroup = securityGroup;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getAuthorizePorts() {
        return authorizePorts;
    }

    public void setAuthorizePorts(List<String> authorizePorts) {
        this.authorizePorts = authorizePorts;
    }

    @Override
    public String toString() {
        return "BaremetalGroup{" + "securityGroup=" + securityGroup + ", number=" 
                + number + ", hosts=" + hosts + ", roles=" + roles + 
                ", authorizePorts=" + authorizePorts + '}';
    }
    
    
    
}
