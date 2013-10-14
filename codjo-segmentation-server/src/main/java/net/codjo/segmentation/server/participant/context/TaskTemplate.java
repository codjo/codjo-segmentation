package net.codjo.segmentation.server.participant.context;
import java.sql.SQLException;
import java.util.concurrent.CancellationException;
import net.codjo.segmentation.server.participant.common.ComputeException;
import net.codjo.segmentation.server.participant.context.SegmentationReport.Task;
import org.apache.log4j.Logger;

/**
 * Template of a segmentation task (or subtask ...) that can be monitored by a {@link SegmentationReporter}.
 */
public abstract class TaskTemplate {
    private static final Logger LOG = Logger.getLogger(TaskTemplate.class);

    private final Task reportTask;


    public TaskTemplate(SegmentationReport report, String taskName) {
        this(report.createTask(taskName));
    }


    public TaskTemplate(TaskTemplate parent, String taskName) {
        this(parent.reportTask.createTask(taskName));
    }


    private TaskTemplate(Task reportTask) {
        this.reportTask = reportTask;
    }


    public final void runComputation() throws CancellationException, SQLException {
        try {
            doRun();
        }
        catch (ComputeException e) {
            handleComputeException(e);
        }
        catch (Exception e) {
            reportTask.reportError();
            handleException(e);
        }
        finally {
            reportTask.close();
        }
    }


    public final void run() throws CancellationException {
        try {
            doRun();
        }
        catch (Exception e) {
            reportTask.reportError();
            handleException(e);
        }
        finally {
            reportTask.close();
        }
    }


    abstract protected void doRun() throws Exception;


    /**
     * @return true if the exception must be rethrown.
     */
    protected void handleException(Exception exception) {
        LOG.fatal(exception.getMessage(), exception);
    }


    /**
     * @return true if the exception must be rethrown.
     */
    protected void handleComputeException(ComputeException exception) throws SQLException {
    }
}
