package net.codjo.segmentation.server.participant.context;
import org.apache.log4j.Logger;
/**
 *
 */
public interface SegmentationReporter {
    final Logger LOGGER = Logger.getLogger(SegmentationReporter.class);

    public static final SegmentationReporter NONE = new NoSegmentationReporter();


    public SegmentationReport create();
}
