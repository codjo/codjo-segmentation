package net.codjo.segmentation.common.message;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.JobRequestWrapper;
import java.util.Map;
/**
 * Représente une requete de segmentation.
 */
public class SegmentationJobRequest extends JobRequestWrapper {
    public static final String SEGMENTATION_REQUEST_TYPE = "segmentation";
    public static final String SEGMENTATION_IDS = "segmentations";


    public SegmentationJobRequest() {
        this(new JobRequest());
    }


    public SegmentationJobRequest(JobRequest request) {
        super(SEGMENTATION_REQUEST_TYPE, request);
    }


    public String getSegmentationIds() {
        return getArgument(SEGMENTATION_IDS);
    }


    /**
     * @param segmentationIds liste des axes a executer (e.x "1, 2")
     */
    public void setSegmentationIds(String segmentationIds) {
        setArgument(SEGMENTATION_IDS, segmentationIds);
    }


    public void putParameter(String key, String value) {
        setArgument(key, value);
    }

    public void putParameters(Map<String, String> mapFields) {
        for (Map.Entry<String, String> mapEntry : mapFields.entrySet()) {
            setArgument(mapEntry.getKey(), mapEntry.getValue());
        }
    }


    public String getParameter(String key) {
        return getArgument(key);
    }
}
