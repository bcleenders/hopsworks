package se.kth.bbc.project.fb;

import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simplified version of the Inode entity to allow for easier access through web
 * interface.
 * <p>
 * @author stig
 */
@XmlRootElement
public class InodeView {

  private String name;
  private boolean dir;
  private boolean parent;
  private String path;
  private Date modification;
  private int id;
  private int template;

  public InodeView() {
  }

  public InodeView(Inode i, String path) {
    this.name = i.getName();
    this.dir = i.isDir();
    this.id = i.getId();
    this.template = i.getTemplate();
    this.parent = false;
    this.path = path;
    this.modification = new Date(i.getModified().getTime());
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

  public void setParent(boolean parent) {
    this.parent = parent;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setModification(Date modification) {
    this.modification = modification;
  }

  public String getName() {
    return name;
  }

  public boolean isDir() {
    return dir;
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

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getTemplate() {
    return this.template;
  }

  public void setTemplate(int template) {
    this.template = template;
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
