package se.kth.hopsworks.util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Asynchronous;

public class LocalhostServices {

  public static String createUserAccount(String username, String projectName, List<String> sshKeys) throws IOException {

    String user = getUsernameInProject(username, projectName);
    String home = Settings.HOPS_USERS_HOMEDIR + user;
    if (new File(home).exists()) {
      throw new IOException("Home directory already exists: " + home);
    }
    StringBuilder publicKeysAsString = new StringBuilder();
    for (String key : sshKeys) {
      publicKeysAsString.append(key).append(System.lineSeparator());
    }

    // Need to execute /srv/mkuser.sh as 'root' using sudo. Same goes for /usr/sbin/deluser
    // Solution is to add them to /etc/sudoers.d/glassfish file. Chef cookbook does this for us.
    List<String> commands = new ArrayList<>();
    commands.add("/bin/bash");
    commands.add("-c");
    // Need to enclose public keys in quotes here.
    commands.add("sudo /srv/mkuser.sh " + user + " \"" + publicKeysAsString.toString() + "\"");

    SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
    String stdout = "", stderr = "";
    try {
      int result = commandExecutor.executeCommand();
      // get the stdout and stderr from the command that was run
      stdout = commandExecutor.getStandardOutputFromCommand();
      stderr = commandExecutor.getStandardErrorFromCommand();
      if (result != 0) {
        throw new IOException("Could not create user: " + home + " - " + stderr);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new IOException("Interrupted. Could not create user: " + home + " - " + stderr);
    }

    return stdout;
  }

  public static String deleteUserAccount(String username, String projectName) throws IOException {
    // Run using a bash script the following with sudo '/usr/sbin/deluser johnny'

    String user = getUsernameInProject(username, projectName);
    String home = Settings.HOPS_USERS_HOMEDIR + user;

    if (new File(home).exists() == false) {
      throw new IOException("Home directory does not exist: " + home);
    }
    List<String> commands = new ArrayList<String>();
    commands.add("/bin/bash");
    commands.add("-c");
    commands.add("sudo /usr/sbin/deluser " + user);

    SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
    String stdout = "", stderr = "";
    try {
      int result = commandExecutor.executeCommand();
      // get the stdout and stderr from the command that was run
      stdout = commandExecutor.getStandardOutputFromCommand();
      stderr = commandExecutor.getStandardErrorFromCommand();
      if (result != 0) {
        throw new IOException("Could not delete user " + home + " - " + stderr);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new IOException("Interrupted. Could not delete user: " + home + " - " + stderr);
    }
    return stdout;
  }
  
  public static String createUserCertificates(int projectId, int userId) throws IOException {
    
    String sslCertFile = Settings.CA_CERT_DIR + projectId + "__" + userId + ".cert.pem";
    String sslKeyFile = Settings.CA_KEY_DIR + projectId + "__" + userId + ".key.pem";

    if (new File(sslCertFile).exists() || new File(sslKeyFile).exists()) {
      throw new IOException("Certs exist already: " + sslCertFile + " & " + sslKeyFile);
    }
    
    // Need to execute CreatingUserCerts.sh as 'root' using sudo. 
    // Solution is to add them to /etc/sudoers.d/glassfish file. Chef cookbook does this for us.
    // TODO: Hopswork-chef needs to put script in glassfish directory!
    List<String> commands = new ArrayList<>();
    commands.add("/bin/bash");
    commands.add("-c");   
    commands.add("/srv/glassfish/domain1/config/ca/intermediate" + "/" + Settings.SSL_CREATE_CERT_SCRIPTNAME + " " + projectId + "__" + userId);

    SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
    String stdout = "", stderr = "";
    try {
      int result = commandExecutor.executeCommand();
      // get the stdout and stderr from the command that was run
      stdout = commandExecutor.getStandardOutputFromCommand();
      stderr = commandExecutor.getStandardErrorFromCommand();
      if (result != 0) {
        throw new IOException(stderr);
      }
    } catch (InterruptedException e) {
      throw new IOException("Interrupted. Could not generate the certificates: " + stderr);
    }
    return stdout;
   }
   
  public static String getUsernameInProject(String username, String projectName) {

    if (username.contains("@")) {
      throw new IllegalArgumentException("Email sent in - should be username");
    }

    return username + Settings.HOPS_USERNAME_SEPARATOR + projectName;
  }

  public static String getUsernameFromEmail(String email) {
    String username = email.substring(0, email.lastIndexOf("@"));
    if (username.contains(".")) {
      username.replace(".", "_");
    }
    if (username.contains("__")) {
      username.replace("__", "_");
    }
    if (username.length() > Settings.MAX_USERNME_LEN) {
      username = username.substring(0,Settings.MAX_USERNME_LEN-1);
    }
    return username;
  }
}
