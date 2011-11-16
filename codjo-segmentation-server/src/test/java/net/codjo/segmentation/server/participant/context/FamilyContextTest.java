package net.codjo.segmentation.server.participant.context;
import junit.framework.TestCase;
/**
 *
 */
public class FamilyContextTest extends TestCase {

    public void test_segmentationContext() throws Exception {
        FamilyContext familyContext = new FamilyContext(null, null);
        SegmentationContext context = new SegmentationContext(25, null, null, null);
        familyContext.putSegmentationContext(context);
        assertSame(context, familyContext.get(25));
    }
}
