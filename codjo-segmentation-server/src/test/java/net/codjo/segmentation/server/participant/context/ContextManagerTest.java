package net.codjo.segmentation.server.participant.context;
import junit.framework.TestCase;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.family.XmlPreferenceLoader;
/**
 *
 */
public class ContextManagerTest extends TestCase {
    private final ContextManager contextManager =
          new ContextManager(new XmlPreferenceLoaderMock());


    public void test_getFamilyPreference() throws Exception {
        XmlFamilyPreference preference = contextManager.getFamilyPreference("family1");
        assertNotNull(preference);
        assertEquals("AP_ROOT", preference.getRootTable());
    }


    public void test_sessionContext() throws Exception {
        SessionContext context = new SessionContext(null);

        contextManager.put("jobRequestId", context);

        SessionContext actual = contextManager.get("jobRequestId");

        assertSame(context, actual);

        contextManager.remove("jobRequestId");

        assertNull(contextManager.get("jobRequestId"));
    }


    public void test_getSegmentationContext() throws Exception {
        SegmentationContext segmentationContext = new SegmentationContext(12, null, null, null);

        FamilyContext familyContext = new FamilyContext(null, null);
        familyContext.putSegmentationContext(segmentationContext);

        SessionContext sessionContext = new SessionContext(null);
        sessionContext.put("family-id", familyContext);
        contextManager.put("jobId", sessionContext);

        Todo<TodoContent> todo = new Todo<TodoContent>(new TodoContent("jobId", "family-id", 12));
        assertSame(segmentationContext, contextManager.getSegmentationContext(todo));
    }


    private class XmlPreferenceLoaderMock extends XmlPreferenceLoader {

        @Override
        public XmlFamilyPreference getFamilyPreference(String familyId) {
            return new XmlFamilyPreference(familyId, "AP_ROOT", "AP_DEST");
        }
    }
}
