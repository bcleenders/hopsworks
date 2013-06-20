package se.kth.kthfsdashboard.role;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import se.kth.kthfsdashboard.host.Host;
import se.kth.kthfsdashboard.host.HostEJB;
import se.kth.kthfsdashboard.struct.InstanceFullInfo;
import se.kth.kthfsdashboard.util.Formatter;

/**
 *
 * @author Hamidreza Afzali <afzali@kth.se>
 */
@ManagedBean
@RequestScoped
public class RoleController {

   @EJB
   private HostEJB hostEJB;
   @EJB
   private RoleEJB roleEjb;
   @ManagedProperty("#{param.hostname}")
   private String hostname;
   @ManagedProperty("#{param.role}")
   private String role;
   @ManagedProperty("#{param.service}")
   private String service;
   @ManagedProperty("#{param.cluster}")
   private String cluster;
   private List<InstanceFullInfo> instanceInfoList;
   private String health;
   private static final Logger logger = Logger.getLogger(RoleController.class.getName());

   public RoleController() {
   }

   @PostConstruct
   public void init() {
      logger.info("init RoleController");
      instanceInfoList = new ArrayList<InstanceFullInfo>();
      try {
         Role r = roleEjb.find(hostname, cluster, service, role);
         Host h = hostEJB.findHostByName(hostname);
         String ip = h.getPublicIp();
         InstanceFullInfo info = new InstanceFullInfo(r.getCluster(), 
                 r.getService(), r.getRole(), r.getHostname(), ip, 
                 r.getWebPort(), r.getStatus(), r.getHealth().toString());
         info.setPid(r.getPid());
         info.setUptime(Formatter.time(r.getUptime() * 1000));
         instanceInfoList.add(info);
         health = r.getHealth().toString();
      } catch (Exception ex) {
         logger.warning("init: ".concat(ex.getMessage()));
      }
   }

   public String getHealth() {
      return health;
   }

   public String getRole() {
      return role;
   }

   public void setRole(String role) {
      this.role = role;
   }

   public String getService() {
      return service;
   }

   public void setService(String service) {
      this.service = service;
   }

   public String getHostname() {
      return hostname;
   }

   public void setHostname(String hostname) {
      this.hostname = hostname;
   }

   public void setCluster(String cluster) {
      this.cluster = cluster;
   }

   public String getCluster() {
      return cluster;
   }

   public List<InstanceFullInfo> getInstanceFullInfo() {
      return instanceInfoList;
   }
}
