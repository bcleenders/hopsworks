package se.kth.hopsworks.controller;

import de.huberlin.wbi.cuneiform.core.semanticmodel.HasFailedException;
import de.huberlin.wbi.cuneiform.core.semanticmodel.TopLevelContext;
import de.huberlin.wbi.cuneiform.core.staticreduction.StaticNodeVisitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import se.kth.bbc.fileoperations.FileOperations;
import se.kth.bbc.jobs.cuneiform.model.InputParameter;
import se.kth.bbc.jobs.cuneiform.model.OutputParameter;
import se.kth.bbc.jobs.cuneiform.model.WorkflowDTO;
import se.kth.bbc.lims.Constants;
import se.kth.bbc.lims.Utils;

/**
 * Interaction point between frontend and backend. Upload, inspect Cuneiform workflows.
 * @author stig
 */
@Stateless
public class CuneiformController {

  @EJB
  private FileOperations fops;

  /**
   * Inspect the workflow at the given path under the projectname. The path
   * should be project-relative, e.g. /DataSetX/WorkflowY. The absolute path is
   * set up based on the given project name. This method returns a WorkflowDTO
   * with the contents and input and output parameter.
   * <p>
   * @param projectName The name of the project in which this workflow resides.
   * @param path The project-relative path to the workflow file.
   * @return WorkflowDTO with (a.o.) the workflow parameters.
   * @throws java.io.IOException on failure of reading the workflow.
   * @throws de.huberlin.wbi.cuneiform.core.semanticmodel.HasFailedException On inspection failure.
   */
  public WorkflowDTO inspectWorkflow(String projectName, String path) throws
          IOException, HasFailedException {
    // Strip the relative path of leading slashes and construct the full path.
    while (path.startsWith("/")) {
      path = path.substring(1);
    }
    String fullPath = "hdfs:///" + Constants.DIR_ROOT + "/" + projectName + "/"
            + path;
    // Get the workflow name.
    String wfName = Utils.getFileName(path);

    // Get the contents
    String txt = fops.cat(fullPath);
    // Inspect the workflow and get the parameter lists
    TopLevelContext tlc = StaticNodeVisitor.createTlc(txt);
    List<String> freenames = StaticNodeVisitor.getFreeVarNameList(tlc);
    List<String> outnames = StaticNodeVisitor.getTargetVarNameList(tlc);    
    // Construct and fill the parameter lists
    ArrayList<InputParameter> inputs = new ArrayList<>(freenames.size());
    ArrayList<OutputParameter> outputs = new ArrayList<>(outnames.size());
    for(String par : freenames){
      inputs.add(new InputParameter(par));
    }
    for(String par: outnames){
      outputs.add(new OutputParameter(par));
    }
    
    //Create the workflowDTO
    WorkflowDTO wf = new WorkflowDTO(wfName, txt, inputs, outputs);
    return wf;
  }

}
