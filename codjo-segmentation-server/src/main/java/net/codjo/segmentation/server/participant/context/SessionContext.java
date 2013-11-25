package net.codjo.segmentation.server.participant.context;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.codjo.segmentation.server.blackboard.DefaultErrorLogLimiter;
import net.codjo.segmentation.server.blackboard.ErrorLogLimiter;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.util.time.SystemTimeSource;
import org.apache.log4j.Logger;
/**
 *
 */
public class SessionContext extends AbstractContext<String, FamilyContext> {
    private static final Logger LOGGER = Logger.getLogger(SessionContext.class);

    private final SegmentationReport report;
    private final ErrorLogLimiter errorLogLimiter;


    public SessionContext(SegmentationReport report, long timeWindowValue, TimeUnit timeWindowUnit) {
        this.report = (report == null) ? SegmentationReporter.NONE.create() : report;
        if (timeWindowValue <= 0) {
            errorLogLimiter = ErrorLogLimiter.NONE;
        }
        else {
            errorLogLimiter = new DefaultErrorLogLimiter(SystemTimeSource.defaultIfNull(null),
                                                         timeWindowValue,
                                                         timeWindowUnit);
        }

        LOGGER.info("errorLogLimiter=" + errorLogLimiter);
    }


    public SegmentationReport getReport() {
        return report;
    }


    public FamilyContext getFamilyContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return get(todo.getContent().getFamilyId());
        }
    }


    public Set<String> getFamilyIds() {
        synchronized (lock) {
            return getContexts().keySet();
        }
    }


    public Collection<FamilyContext> getFamilyContexts() {
        synchronized (lock) {
            return getContexts().values();
        }
    }


    public final ErrorLogLimiter getErrorLogLimiter() {
        return errorLogLimiter;
    }
}
