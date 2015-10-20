package se.kth.hopsworks.rest;

import com.google.zxing.WriterException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.primefaces.model.StreamedContent;
import se.kth.hopsworks.controller.ResponseMessages;
import se.kth.hopsworks.controller.UserStatusValidator;
import se.kth.hopsworks.controller.UsersController;
import se.kth.hopsworks.user.model.Users;
import se.kth.hopsworks.users.UserDTO;
import se.kth.hopsworks.users.UserFacade;

@Path("/auth")
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class AuthService {

  @EJB
  private UserFacade userBean;
  @EJB
  private UsersController userController;
  @EJB
  private UserStatusValidator statusValidator;
  @EJB
  private NoCacheResponse noCacheResponse;

  
  // To distinguish Yubikey users
  private final String YUBIKEY_USER_MARKER = "YUBIKEY_USER_MARKER";

  // For disabled OTP auth mode
  private final String YUBIKEY_OTP_PADDING
          = "EaS5ksRVErn2jiOmSQy5LM2X7LgWAZWfWYKQoPavbrhN";
  
  // For padding when password field is empty
  private final String MOBILE_OTP_PADDING = "123456";
  
  @GET
  @Path("session")
  @RolesAllowed({"SYS_ADMIN", "BBC_USER"})
  @Produces(MediaType.APPLICATION_JSON)
  public Response session(@Context SecurityContext sc,
          @Context HttpServletRequest req) throws AppException {
    JsonResponse json = new JsonResponse();
    req.getServletContext().log("SESSIONID: " + req.getSession().getId());
    try {
      json.setStatus("SUCCESS");
      json.setData(sc.getUserPrincipal().getName());
    } catch (Exception e) {
      throw new AppException(Response.Status.UNAUTHORIZED.getStatusCode(),
              ResponseMessages.AUTHENTICATION_FAILURE);
    }
    return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(
            json).build();
  }

  @POST
  @Path("login")
  @Produces(MediaType.APPLICATION_JSON)
  public Response login(@FormParam("email") String email,
          @FormParam("password") String password,  @FormParam("otp") String otp, @Context SecurityContext sc,
          @Context HttpServletRequest req, @Context HttpHeaders httpHeaders)
          throws AppException {

    req.getServletContext().log("email: " + email);
    req.getServletContext().log("SESSIONID@login: " + req.getSession().getId());
    req.getServletContext().log("SecurityContext: " + sc.getUserPrincipal());
    req.getServletContext().log("SecurityContext in role: " + sc.isUserInRole(
            "BBC_USER"));
    JsonResponse json = new JsonResponse();
    Users user = userBean.findByEmail(email);

    req.getServletContext().log("USER: " + user);
    req.getServletContext().log("1 step: " + email);
    
    // Add padding if custom realm is disabled
    if (otp == null || otp.isEmpty()) {
      otp = MOBILE_OTP_PADDING;
    }
    
    if(otp.length() == 6){
      password = password + otp;
    } else if(otp.length() == 44){
      password = password + otp + YUBIKEY_USER_MARKER;
    }
    
      
    
    //only login if not already logged in...
    if (sc.getUserPrincipal() == null) {
      if (user != null && statusValidator.checkStatus(user.getStatus())) {
        try {

          req.getServletContext().log("going to login. User status: " + user.
                  getStatus());
          req.login(email, password);
          req.getServletContext().log("3 step: " + email);
          userController.resetFalseLogin(user);
          userController.registerLoginInfo(user, "Successful login", req);
          //if the logedin user has no supported role logout
          if (!sc.isUserInRole("BBC_USER") && !sc.isUserInRole("SYS_ADMIN")) {
            req.logout();
            throw new AppException(Response.Status.UNAUTHORIZED.getStatusCode(),
                    "No valid role found for this user");
          }

        } catch (ServletException e) {
          userController.registerFalseLogin(user);
          userController.registerLoginInfo(user, "False login", req);
          throw new AppException(Response.Status.UNAUTHORIZED.getStatusCode(),
                  ResponseMessages.AUTHENTICATION_FAILURE);
        }
      } else { // if user == null
        throw new AppException(Response.Status.UNAUTHORIZED.getStatusCode(),
                ResponseMessages.AUTHENTICATION_FAILURE);
      }
    } else {
      req.getServletContext().log("Skip logged because already logged in: "
              + email);
    }

    //read the user data from db and return to caller
    json.setStatus("SUCCESS");
    json.setSessionID(req.getSession().getId());

    return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(
            json).build();
  }

  @GET
  @Path("logout")
  @Produces(MediaType.APPLICATION_JSON)
  public Response logout(@Context HttpServletRequest req) throws AppException {
    req.getServletContext().log("Logging out...");
    JsonResponse json = new JsonResponse();

    try {
      req.logout();
      json.setStatus("SUCCESS");
      req.getSession().invalidate();
    } catch (ServletException e) {
      throw new AppException(Response.Status.INTERNAL_SERVER_ERROR.
              getStatusCode(),
              "Logout failed on backend");
    }
    return Response.ok().entity(json).build();
  }

  @POST
  @Path("register")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(UserDTO newUser, @Context HttpServletRequest req)
          throws AppException {
    StreamedContent qrCode = null;
    req.getServletContext().log("Registering..." + newUser.getEmail() + ", "
            + newUser.getFirstName());

    JsonResponse json = new JsonResponse();

    try {
      qrCode = userController.registerUser(newUser);
      
    } catch (IOException | WriterException ex) {
      Logger.getLogger(AuthService.class.getName()).log(Level.SEVERE, null, ex);
    }
    req.getServletContext().log("successfully registered new user: '" + newUser.
            getEmail() + "'");
    
    Response.ResponseBuilder response = Response.ok((Object)qrCode);
    response.header("Content-disposition", "inline;");

    return response.build();
  }

  @POST
  @Path("recoverPassword")
  @Produces(MediaType.APPLICATION_JSON)
  public Response recoverPassword(@FormParam("email") String email,
          @FormParam("securityQuestion") String securityQuestion,
          @FormParam("securityAnswer") String securityAnswer,
          @Context SecurityContext sc,
          @Context HttpServletRequest req) throws AppException {
    JsonResponse json = new JsonResponse();

    userController.recoverPassword(email, securityQuestion, securityAnswer, req);
    json.setStatus("OK");
    json.setSuccessMessage(ResponseMessages.PASSWORD_RESET_SUCCESSFUL);

    return noCacheResponse.getNoCacheResponseBuilder(Response.Status.OK).entity(
            json).build();
  }

}
