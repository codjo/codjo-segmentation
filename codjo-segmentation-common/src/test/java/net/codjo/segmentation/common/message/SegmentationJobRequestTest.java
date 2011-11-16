package net.codjo.segmentation.common.message;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.JobRequestWrapper;
import net.codjo.workflow.common.message.JobRequestWrapperTestCase;
/**
 *
 */
public class SegmentationJobRequestTest extends JobRequestWrapperTestCase {

    public void test_constructor() throws Exception {
        SegmentationJobRequest request = new SegmentationJobRequest();
        assertEquals(null, request.getSegmentationIds());
        request.setSegmentationIds("1");
        assertEquals("1", request.getSegmentationIds());
    }


    public void test_put() throws Exception {
        SegmentationJobRequest request = new SegmentationJobRequest();
        request.putParameter("key", "value");
        assertEquals("value", request.getParameter("key"));
    }


    @Override
    protected String getJobRequestType() {
        return SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE;
    }


    @Override
    protected JobRequestWrapper createWrapper(JobRequest jobRequest) {
        return new SegmentationJobRequest(jobRequest);
    }
}
