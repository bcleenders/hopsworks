package se.kth.bbc.study.fb;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author stig
 */
@Entity
@Table(name = "inodes")
@XmlRootElement
@NamedQueries({
  @NamedQuery(name = "Inode.findAll",
          query
          = "SELECT i FROM Inode i"),
  @NamedQuery(name = "Inode.findById",
          query
          = "SELECT i FROM Inode i WHERE i.id = :id"),
  @NamedQuery(name = "Inode.findByParentId",
          query
          = "SELECT i FROM Inode i WHERE i.inodePK.parentId = :parentId"),
  @NamedQuery(name = "Inode.findByName",
          query
          = "SELECT i FROM Inode i WHERE i.inodePK.name = :name"),
  @NamedQuery(name = "Inode.findByModificationTime",
          query
          = "SELECT i FROM Inode i WHERE i.modificationTime = :modificationTime"),
  @NamedQuery(name = "Inode.findByPrimaryKey",
          query
          = "SELECT i FROM Inode i WHERE i.inodePK = :inodePk"),
  @NamedQuery(name = "Inode.findByAccessTime",
          query
          = "SELECT i FROM Inode i WHERE i.accessTime = :accessTime"),
  @NamedQuery(name = "Inode.findByClientName",
          query
          = "SELECT i FROM Inode i WHERE i.clientName = :clientName"),
  @NamedQuery(name = "Inode.findByClientMachine",
          query
          = "SELECT i FROM Inode i WHERE i.clientMachine = :clientMachine"),
  @NamedQuery(name = "Inode.findByClientNode",
          query
          = "SELECT i FROM Inode i WHERE i.clientNode = :clientNode"),
  @NamedQuery(name = "Inode.findByGenerationStamp",
          query
          = "SELECT i FROM Inode i WHERE i.generationStamp = :generationStamp"),
  @NamedQuery(name = "Inode.findByHeader",
          query
          = "SELECT i FROM Inode i WHERE i.header = :header"),
  @NamedQuery(name = "Inode.findBySymlink",
          query
          = "SELECT i FROM Inode i WHERE i.symlink = :symlink"),
  @NamedQuery(name = "Inode.findByDir",
          query
          = "SELECT i FROM Inode i WHERE i.dir = :dir"),
  @NamedQuery(name = "Inode.findByQuotaEnabled",
          query
          = "SELECT i FROM Inode i WHERE i.quotaEnabled = :quotaEnabled"),
  @NamedQuery(name = "Inode.findByUnderConstruction",
          query
          = "SELECT i FROM Inode i WHERE i.underConstruction = :underConstruction"),
  @NamedQuery(name = "Inode.findBySubtreeLocked",
          query
          = "SELECT i FROM Inode i WHERE i.subtreeLocked = :subtreeLocked"),
  @NamedQuery(name = "Inode.findBySubtreeLockOwner",
          query
          = "SELECT i FROM Inode i WHERE i.subtreeLockOwner = :subtreeLockOwner")})
public class Inode implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  protected InodePK inodePK;
  @Basic(optional = false)
  @NotNull
  @Column(name = "id")
  private int id;
  @Column(name = "modification_time")
  private BigInteger modificationTime;
  @Column(name = "access_time")
  private BigInteger accessTime;
  @Lob
  @Column(name = "permission")
  private byte[] permission;
  @Size(max = 100)
  @Column(name = "client_name")
  private String clientName;
  @Size(max = 100)
  @Column(name = "client_machine")
  private String clientMachine;
  @Size(max = 100)
  @Column(name = "client_node")
  private String clientNode;
  @Column(name = "generation_stamp")
  private Integer generationStamp;
  @Column(name = "header")
  private BigInteger header;
  @Size(max = 3000)
  @Column(name = "symlink")
  private String symlink;
  @Basic(optional = false)
  @NotNull
  @Column(name = "dir")
  private boolean dir;
  @Basic(optional = false)
  @NotNull
  @Column(name = "quota_enabled")
  private boolean quotaEnabled;
  @Basic(optional = false)
  @NotNull
  @Column(name = "under_construction")
  private boolean underConstruction;
  @Column(name = "subtree_locked")
  private Boolean subtreeLocked;
  @Column(name = "subtree_lock_owner")
  private BigInteger subtreeLockOwner;

  public Inode() {
  }

  public Inode(InodePK inodePK) {
    this.inodePK = inodePK;
  }

  public Inode(InodePK inodePK, int id, boolean dir, boolean quotaEnabled,
          boolean underConstruction) {
    this.inodePK = inodePK;
    this.id = id;
    this.dir = dir;
    this.quotaEnabled = quotaEnabled;
    this.underConstruction = underConstruction;
  }

  public Inode(int parentId, String name) {
    this.inodePK = new InodePK(parentId, name);
  }

  public InodePK getInodePK() {
    return inodePK;
  }

  public void setInodePK(InodePK inodePK) {
    this.inodePK = inodePK;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public BigInteger getModificationTime() {
    return modificationTime;
  }

  public void setModificationTime(BigInteger modificationTime) {
    this.modificationTime = modificationTime;
  }

  public BigInteger getAccessTime() {
    return accessTime;
  }

  public void setAccessTime(BigInteger accessTime) {
    this.accessTime = accessTime;
  }

  public byte[] getPermission() {
    return permission;
  }

  public void setPermission(byte[] permission) {
    this.permission = permission;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getClientMachine() {
    return clientMachine;
  }

  public void setClientMachine(String clientMachine) {
    this.clientMachine = clientMachine;
  }

  public String getClientNode() {
    return clientNode;
  }

  public void setClientNode(String clientNode) {
    this.clientNode = clientNode;
  }

  public Integer getGenerationStamp() {
    return generationStamp;
  }

  public void setGenerationStamp(Integer generationStamp) {
    this.generationStamp = generationStamp;
  }

  public BigInteger getHeader() {
    return header;
  }

  public void setHeader(BigInteger header) {
    this.header = header;
  }

  public String getSymlink() {
    return symlink;
  }

  public void setSymlink(String symlink) {
    this.symlink = symlink;
  }

  public boolean getDir() {
    return dir;
  }

  public void setDir(boolean dir) {
    this.dir = dir;
  }

  public boolean getQuotaEnabled() {
    return quotaEnabled;
  }

  public void setQuotaEnabled(boolean quotaEnabled) {
    this.quotaEnabled = quotaEnabled;
  }

  public boolean getUnderConstruction() {
    return underConstruction;
  }

  public void setUnderConstruction(boolean underConstruction) {
    this.underConstruction = underConstruction;
  }

  public Boolean getSubtreeLocked() {
    return subtreeLocked;
  }

  public void setSubtreeLocked(Boolean subtreeLocked) {
    this.subtreeLocked = subtreeLocked;
  }

  public BigInteger getSubtreeLockOwner() {
    return subtreeLockOwner;
  }

  public void setSubtreeLockOwner(BigInteger subtreeLockOwner) {
    this.subtreeLockOwner = subtreeLockOwner;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (inodePK != null ? inodePK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Inode)) {
      return false;
    }
    Inode other = (Inode) object;
    if ((this.inodePK == null && other.inodePK != null) ||
            (this.inodePK != null && !this.inodePK.equals(other.inodePK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "se.kth.bbc.study.fb.Inode[ inodePK=" + inodePK + " ]";
  }
  
}
