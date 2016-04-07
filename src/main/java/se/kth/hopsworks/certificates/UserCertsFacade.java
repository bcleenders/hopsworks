package se.kth.hopsworks.certificates;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import se.kth.hopsworks.rest.AppException;
import se.kth.bbc.project.Project;
import se.kth.hopsworks.user.model.Users;

/**
 *
 * @author paul
 */

@Stateless
public class UserCertsFacade {
    
    @PersistenceContext(unitName = "kthfsPU")
    private EntityManager em;
    
      protected EntityManager getEntityManager() {
        return em;
    }
      
    public UserCertsFacade() throws AppException, Exception {}
    
    public List<UserCertsDTO> findAllCerts(){
        TypedQuery<UserCerts> query = em.createNamedQuery(
        "UserCerts.findAll", UserCerts.class);
        List<UserCerts> res = query.getResultList();
        List<UserCertsDTO> certs = new ArrayList<>();
        
        for(UserCerts uc : res){
            certs.add(new UserCertsDTO(uc.getUserKey(),uc.getUserCert()));
        }
        return certs;
    }
    
    public List<UserCertsDTO> findUserCertsByProjectId(Project project) {
        TypedQuery<UserCerts> query = em.createNamedQuery(
                "UserCerts.findByProjectId", UserCerts.class);
        query.setParameter("projectId", project.getId());
        List<UserCerts> res = query.getResultList();      
        List<UserCertsDTO> certs = new ArrayList<>();
        for(UserCerts uc : res) {
            certs.add(new UserCertsDTO(uc.getUserKey(), uc.getUserCert()));
        }
        return certs;        
    }
    
    public List<UserCertsDTO> findUserCertsByUid(Users user){
        TypedQuery<UserCerts> query = em.createNamedQuery(
            "UserCerts.findByUserId", UserCerts.class);
        query.setParameter("userId", user.getUid());
        List<UserCerts> res = query.getResultList();
        List<UserCertsDTO> certs = new ArrayList<>();
        for(UserCerts uc : res) {
            certs.add(new UserCertsDTO(uc.getUserKey(), uc.getUserCert()));
        }
        return certs;        
    }
    
    public UserCertsDTO findUserCert(Project project, Users user) throws AppException{
        TypedQuery<UserCerts> query = em.createNamedQuery (
        "UserCerts.findUserProjectCert", UserCerts.class);
        query.setParameter("projectId", project.getId());
        query.setParameter("userId", user.getUid());
        UserCerts res = query.getSingleResult();
        UserCertsDTO uc = new UserCertsDTO();
        
        if(res.getUserKey().length < 1 || res.getUserCert().length < 1) {
            throw new AppException(Response.Status.NOT_FOUND.getStatusCode(),
                    "No certificates found in this project for this particular user.");
        } else {
            uc = new UserCertsDTO(res.getUserKey(), res.getUserCert());
        }
        return uc;        
    }      
}