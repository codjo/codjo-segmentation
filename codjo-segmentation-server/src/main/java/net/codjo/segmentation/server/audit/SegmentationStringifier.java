package net.codjo.segmentation.server.audit;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.server.plugin.StringifierImpl;
/**
 *
 */
public class SegmentationStringifier extends StringifierImpl {

    public SegmentationStringifier() {
        super(SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE);
    }


    public String toString(JobRequest jobRequest) {
        String segmentationIds = new SegmentationJobRequest(jobRequest).getSegmentationIds();
        final String[] splits;

        StringBuilder stringBuilder = new StringBuilder("Axes : ");
        if (segmentationIds == null) {
            splits = new String[0];
        }
        else {
            splits = segmentationIds.replaceAll(" ", "").split(",");
            stringBuilder.append(splits[0]);
        }

        int nbIds = splits.length;
        for (int i = 1; i < Math.min(nbIds, 3); i++) {
            stringBuilder.append(", ").append(splits[i]);
        }
        if (nbIds > 3) {
            stringBuilder.append(", ... (").append(nbIds - 3).append(" de plus)");
        }
        return stringBuilder.toString();
    }
}
