package net.codjo.segmentation.server.participant.context;
import java.util.Collection;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.family.XmlPreferenceLoader;
/**
 *
 */
public class ContextManager extends AbstractContext<String, SessionContext> {
    private final XmlPreferenceLoader xmlPreferenceLoader;

    /**
     * The {@link SegmentationReporter} to use for reporting progress of segmentation and computing its statistics. Uses
     * {@link SegmentationReporter#NONE} to disable these features.
     */
    private final SegmentationReporter reporter = /*SegmentationReporter.NONE;*/ new DetailedSegmentationReporter();


    public ContextManager(XmlPreferenceLoader xmlPreferenceLoader) {
        this.xmlPreferenceLoader = xmlPreferenceLoader;
    }


    public XmlFamilyPreference getFamilyPreference(String familyId) {
        synchronized (lock) {
            return xmlPreferenceLoader.getFamilyPreference(familyId);
        }
    }


    public SessionContext getSessionContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return get(todo.getContent().getRequestJobId());
        }
    }


    public FamilyContext getFamilyContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return getSessionContext(todo).getFamilyContext(todo);
        }
    }


    public SegmentationContext getSegmentationContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return getFamilyContext(todo).getSegmentationContext(todo);
        }
    }


    public Collection<SessionContext> getSessions() {
        synchronized (lock) {
            return getContexts().values();
        }
    }


    public SegmentationReport createReport() {
        return reporter.create();
    }
}
