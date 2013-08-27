package net.codjo.segmentation.server.participant.context;
import java.util.Collection;
import java.util.Set;
import net.codjo.segmentation.server.blackboard.message.Todo;
/**
 *
 */
public class SessionContext extends AbstractContext<String, FamilyContext> {
    private final SegmentationReport report;


    public SessionContext(SegmentationReport report) {
        this.report = (report == null) ? SegmentationReporter.NONE.create() : report;
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
}
