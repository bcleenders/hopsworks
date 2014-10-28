package se.kth.bbc.fileoperations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import se.kth.bbc.study.fb.Inode;

/**
 * Managed bean for accessing operations on the file system. Downloading,
 * uploading, creating files and directories. Methods do not care about the
 * specific implementation of the file system (i.e. separation between DB and FS
 * is not made here).
 *
 * @author stig
 */
@ManagedBean(name = "fileOperationsMB") //Not sure it makes sense to have it as an MB. Perhaps functionality should be split between an MB and EJB
@RequestScoped
public class FileOperationsManagedBean implements Serializable {

    @EJB
    private FileOperations fileOps;

    private String newFolderName;    
    private static final Logger logger = Logger.getLogger(FileOperationsManagedBean.class.getName());

    /**
     * Download the file at the specified inode.
     *
     * @param inode The file to download.
     * @return StreamedContent of the file to be downloaded.
     */
    public StreamedContent downloadFile(Inode inode) {

        StreamedContent sc = null;
        try {
            InputStream is = fileOps.getInputStream(inode);
            String extension = getExtension(inode.getPath());
            String filename = inode.getPath().substring(inode.getPath().lastIndexOf(File.separator) + 1);

            sc = new DefaultStreamedContent(is, extension, filename);
            logger.log(Level.INFO, "File was downloaded from HDFS path: {0}", inode.getStudyPath());
        } catch (IOException ex) {
            Logger.getLogger(FileOperationsManagedBean.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: add error message
        }
        return sc;
    }
    
    /**
     * Create a new folder with the name newFolderName (class property) at the
     * specified path. The path must NOT contain the new folder name. Set this
     * using the <i>newFolderName</i> property.
     *
     * @param path Location at which to create the new folder, not including the
     * name of the new folder.
     */
    public void mkDir(String path) {
        String location;
        if (path.endsWith(File.separator)) {
            location = path + newFolderName;
        } else {
            location = path + File.separator + newFolderName;
        }
        try {
            boolean success = fileOps.mkDir(location);
            if(success){
                newFolderName = null;
            }else{
                //TODO: add error message.
            }
        } catch (IOException ex) {
            Logger.getLogger(FileOperationsManagedBean.class.getName()).log(Level.SEVERE, null, ex);
            //TODO: add error message
        }
    }

    public void setNewFolderName(String s) {
        newFolderName = s;
    }

    public String getNewFolderName() {
        return newFolderName;
    }

    private static String getExtension(String path) {
        String filename = path.substring(path.lastIndexOf(File.separator));
        if (filename.length() == 0) // path was a folder TODO: check if this is right!!
        {
            return null;
        } else if (filename.lastIndexOf('.') < 0) //file does not have extension
        {
            return "";
        } else {
            return filename.substring(filename.lastIndexOf('.') + 1);
        }
    }
}
