package se.kth.bbc.study.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import org.primefaces.model.DualListModel;
import se.kth.bbc.lims.MessagesController;
import se.kth.bbc.study.StudyMB;
import se.kth.bbc.study.metadata.CollectionTypeStudyDesignEnum;

/**
 *
 * @author stig
 */
@ManagedBean
@ViewScoped
public class SamplesController {

  private static final Logger logger = Logger.getLogger(SamplesController.class.
          getName());

  @ManagedProperty(value = "#{studyManagedBean}")
  private StudyMB study;

  @EJB
  private SamplecollectionFacade samplecollectionFacade;

  @EJB
  private SampleFacade sampleFacade;

  private boolean collectionSelected = false;
  private boolean sampleSelected = false;

  private Samplecollection selectedCollection;
  private Sample selectedSample;

  public List<Samplecollection> getSamplecollection() {
    return samplecollectionFacade.findByStudyname(study.getStudyName());
  }

  public StudyMB getStudy() {
    return study;
  }

  public void setStudy(StudyMB study) {
    this.study = study;
  }

  public boolean isCollectionSelected() {
    return collectionSelected;
  }

  public void setCollectionSelected(boolean collectionSelected) {
    this.collectionSelected = collectionSelected;
  }

  public boolean isSampleSelected() {
    return sampleSelected;
  }

  public void setSampleSelected(boolean sampleSelected) {
    this.sampleSelected = sampleSelected;
  }

  public Samplecollection getSelectedCollection() {
    return selectedCollection;
  }

  public void setSelectedCollection(Samplecollection selectedCollection) {
    this.selectedCollection = selectedCollection;
  }

  public Sample getSelectedSample() {
    return selectedSample;
  }

  public void setSelectedSample(Sample selectedSample) {
    this.selectedSample = selectedSample;
  }

  public void selectCollection(String id) {
    Samplecollection coll = getCollection(id);
    this.collectionSelected = (coll != null);
    this.selectedCollection = coll;
  }

  public void selectSample(String sampleId) {
    if (selectedCollection == null) {
      //should never happen
      return;
    }
    String collectionId = selectedCollection.getId();
    Samplecollection coll = getCollection(collectionId);
    for (Sample s : coll.getSampleCollection()) {
      if (s.getId().equals(sampleId)) {
        this.selectedSample = s;
        this.sampleSelected = true;
        return;
      }
    }
    this.selectedSample = null;
    this.sampleSelected = false;
  }

  private Samplecollection getCollection(String id) {
    for (Samplecollection coll : getSamplecollection()) {
      if (coll.getId().equals(id)) {
        return coll;
      }
    }
    return null;
  }

  public DualListModel<CollectionTypeStudyDesignEnum> getCollectionTypeDualList() {
    if (selectedCollection == null) {
      return new DualListModel<>();
    }
    List<CollectionTypeStudyDesignEnum> target = selectedCollection.
            getCollectionTypeList();
    List<CollectionTypeStudyDesignEnum> source = new ArrayList<>();
    for (CollectionTypeStudyDesignEnum item : CollectionTypeStudyDesignEnum.
            values()) {
      if (!target.contains(item)) {
        source.add(item);
      }
    }
    return new DualListModel<>(source, target);
  }

  public void setCollectionTypeDualList(
          DualListModel<CollectionTypeStudyDesignEnum> duallist) {
    this.selectedCollection.setCollectionTypeList(duallist.getTarget());
  }

  public DualListModel<MaterialTypeEnum> getMaterialTypeDualList() {
    if (selectedSample == null) {
      return new DualListModel<>();
    }
    List<MaterialTypeEnum> target = selectedSample.getMaterialTypeList();
    List<MaterialTypeEnum> source = new ArrayList<>();
    for (MaterialTypeEnum item : MaterialTypeEnum.
            values()) {
      if (!target.contains(item)) {
        source.add(item);
      }
    }
    return new DualListModel<>(source, target);
  }

  public void setMaterialTypeDualList(DualListModel<MaterialTypeEnum> duallist) {
    this.selectedSample.setMaterialTypeList(duallist.getTarget());
  }

  public void updateSampleCollection() {
    try {
      samplecollectionFacade.update(selectedCollection);
      MessagesController.addInfoMessage(MessagesController.SUCCESS,
              "Samplecollection data updated.", "updateSuccess");
    } catch (EJBException e) {
      logger.log(Level.SEVERE, "Failed to update samplecollection metadata", e);
      MessagesController.addErrorMessage(MessagesController.ERROR,
              "Failed to update data.", "updateFail");
    }
  }

  public void updateSample() {
    try {
      sampleFacade.update(selectedSample);
      MessagesController.addInfoMessage(MessagesController.SUCCESS,
              "Sample data updated.", "updateSuccess");
    } catch (EJBException e) {
      logger.log(Level.SEVERE, "Failed to update sample metadata", e);
      MessagesController.addErrorMessage(MessagesController.ERROR,
              "Failed to update data.", "updateFail");
    }
  }

}
