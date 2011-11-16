package net.codjo.segmentation.server.participant.context;
/**
 *
 */
public class TodoContent {
    private final String requestJobId;
    private final String familyId;
    private final int segmentationId;
    private final int pageId;


    public TodoContent(String requestJobId, String familyId) {
        this(requestJobId, familyId, -1, -1);
    }


    public TodoContent(String requestJobId, String familyId, int segmentationId) {
        this(requestJobId, familyId, segmentationId, -1);
    }


    public TodoContent(String requestJobId, String familyId, int segmentationId, int pageId) {
        this.requestJobId = requestJobId;
        this.familyId = familyId;
        this.segmentationId = segmentationId;
        this.pageId = pageId;
    }


    public TodoContent(TodoContent todoContent, int segmentationId, int pageId) {
        this(todoContent.getRequestJobId(), todoContent.getFamilyId(), segmentationId, pageId);
    }


    public String getRequestJobId() {
        return requestJobId;
    }


    public String getFamilyId() {
        return familyId;
    }


    public int getSegmentationId() {
        return segmentationId;
    }


    public int getPageId() {
        return pageId;
    }


    @Override
    public String toString() {
        if (segmentationId == -1 && pageId == -1) {
            return "TodoContent{" + requestJobId + '/' + familyId + '}';
        }
        else if (pageId == -1) {
            return "TodoContent{" + requestJobId + '/' + familyId + '/' + segmentationId + '}';
        }
        return "TodoContent{" + requestJobId + '/' + familyId + '/' + segmentationId + '/' + pageId + '}';
    }
}
