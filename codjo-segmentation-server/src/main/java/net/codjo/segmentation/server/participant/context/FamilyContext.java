package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 *
 */
public class FamilyContext extends AbstractContext<Integer, SegmentationContext> {
    private XmlFamilyPreference familyPreference;
    private Map<String, String> parameters;


    public FamilyContext(XmlFamilyPreference familyPreference, Map<String, String> parameters) {
        this.familyPreference = familyPreference;
        this.parameters = parameters;
    }


    public SegmentationContext getSegmentationContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return get(todo.getContent().getSegmentationId());
        }
    }


    public SegmentationContext getSegmentationContext(int segmentationId) {
        synchronized (lock) {
            return get(segmentationId);
        }
    }


    public void putSegmentationContext(SegmentationContext context) {
        synchronized (lock) {
            put(context.getSegmentationId(), context);
        }
    }


    public List<SegmentationContext> getSegmentationContexts() {
        synchronized (lock) {
            List<SegmentationContext> segmentationContexts = new ArrayList<SegmentationContext>();
            for (SegmentationContext context : getContexts().values()) {
                segmentationContexts.add(context);
            }
            return segmentationContexts;
        }
    }


    public XmlFamilyPreference getFamilyPreference() {
        synchronized (lock) {
            return familyPreference;
        }
    }


    public Map<String, String> getParameters() {
        synchronized (lock) {
            return parameters;
        }
    }
}
