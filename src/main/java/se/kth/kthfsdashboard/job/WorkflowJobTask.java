/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kthfsdashboard.job;

import de.huberlin.cuneiform.compiler.local.LocalDispatcher;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import se.kth.kthfsdashboard.wf.Workflow;

/**
 *
 * @author Alberto Lorente Leal <albll@kth.se>
 */
public class WorkflowJobTask implements Callable<Job> {

    private Workflow selectedWorkflow;

    public WorkflowJobTask(Workflow selectedWorkflow) {
        this.selectedWorkflow = selectedWorkflow;
    }

    @Override
    public Job call() {
        final File dir = new File(System.getProperty("user.dir") + "/build");
        Job pending = new Job();
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date dateStarted = new Date();
            FileUtils.deleteDirectory(dir);
            LocalDispatcher ld = new LocalDispatcher(
                    dir, // the working directory for all the intermediate data
                    null, // the location of the log-file to be created. By default: (dir)/log_(runid).csv
                    selectedWorkflow.getWorkflowName());    // the run ID. By default, a random UUID

            ld.addInputString(selectedWorkflow.getWorkflowMetadata());
            ld.run();

            Date dateFinished = new Date();

            pending = new Job(selectedWorkflow.getOwner(),
                    selectedWorkflow.getWorkflowName(),
                    dateStarted.toString(),
                    dateFinished.getTime() - dateStarted.getTime());


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return pending;
        }
    }
}
