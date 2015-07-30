package se.kth.bbc.project.fb;

import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;
import se.kth.bbc.lims.Constants;
import se.kth.hopsworks.dataset.Dataset;

/**
 * Simplified version of the Inode entity to allow for easier access through web
 * interface.
 * <p>
 * @author stig
 */
@XmlRootElement
public final class InodeView {

  private String name;
  private boolean dir;
  private boolean parent;
  private String path;
  private long size;
  private boolean shared;
  private String owningProjectName;
  private Date modification;
  private Date accessTime;
  private int id;
  private int template;
  private String description;
  private boolean status;

  public InodeView() {
  }
  
  public InodeView(Inode i, String path) {
    this.name = i.getInodePK().getName();
    this.dir = i.isDir();
    this.id = i.getId();
    this.size = i.getSize();
    //put the template id in the REST response
    this.template = i.getTemplate();
    this.parent = false;
    this.path = path;
    this.modification = new Date(i.getModificationTime().longValue());
    this.accessTime = new Date(i.getAccessTime().longValue());
  }
  
  public InodeView(Inode parent, Dataset ds, String path) {
    this.name = ds.getInode().getInodePK().getName();
    this.dir = ds.getInode().isDir();
    this.id = ds.getInode().getId();
    this.size = ds.getInode().getSize();
    this.template = ds.getInode().getTemplate();
    this.parent = false;
    this.path = path;
    this.modification = new Date(ds.getInode().getModificationTime().longValue());
    this.accessTime = new Date(ds.getInode().getAccessTime().longValue());
    this.shared = (!parent.inodePK.getName().equals(ds.getProjectId().getName()));
    if (this.shared){
      this.name = parent.inodePK.getName() + Constants.SHARED_FILE_SEPARATOR + this.name;
    }
    this.owningProjectName = parent.inodePK.getName();
    this.description = ds.getDescription();
    this.status = ds.getStatus();
  }
  
  private InodeView(String name, boolean dir, boolean parent, String path) {
    this.name = name;
    this.dir = dir;
    this.parent = parent;
    this.path = path;
    this.modification = null;
  }

  public static InodeView getParentInode(String path) {
    String name = "..";
    boolean dir = true;
    boolean parent = true;
    int lastSlash = path.lastIndexOf("/");
    if (lastSlash == path.length() - 1) {
      lastSlash = path.lastIndexOf("/", lastSlash - 1);
    }
    path = path.substring(0, lastSlash);
    return new InodeView(name, dir, parent, path);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDir(boolean dir) {
    this.dir = dir;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setTemplate(int template) {
    this.template = template;
  }

  public void setParent(boolean parent) {
    this.parent = parent;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setModification(Date modification) {
    this.modification = modification;
  }

  public void setAccessTime(Date accessTime) {
    this.accessTime = accessTime;
  }

  public String getName() {
    return name;
  }

  public boolean isDir() {
    return dir;
  }

  public int getId() {
    return this.id;
  }

  public int getTemplate() {
    return this.template;
  }

  public boolean isParent() {
    return parent;
  }

  public String getPath() {
    return path;
  }

  public Date getModification() {
    return modification;
  }

  public Date getAccessTime() {
    return accessTime;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public boolean isShared() {
    return shared;
  }

  public void setShared(boolean shared) {
    this.shared = shared;
  }

  public String getOwningProjectName() {
    return owningProjectName;
  }

  public void setOwningProjectName(String owningProjectName) {
    this.owningProjectName = owningProjectName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }
  
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 13 * hash + Objects.hashCode(this.name);
    hash = 13 * hash + Objects.hashCode(this.path);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InodeView other = (InodeView) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return Objects.equals(this.path, other.path);
  }

}
