package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.family.XmlPreferenceLoader;
import java.util.Collection;
/**
 *
 */
public class ContextManager extends AbstractContext<String, SessionContext> {
    private final XmlPreferenceLoader xmlPreferenceLoader;


    public ContextManager(XmlPreferenceLoader xmlPreferenceLoader) {
        this.xmlPreferenceLoader = xmlPreferenceLoader;
    }


    public XmlFamilyPreference getFamilyPreference(String familyId) {
        synchronized (lock) {
            return xmlPreferenceLoader.getFamilyPreference(familyId);
        }
    }


    public FamilyContext getFamilyContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return get(todo.getContent().getRequestJobId()).getFamilyContext(todo);
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
}
