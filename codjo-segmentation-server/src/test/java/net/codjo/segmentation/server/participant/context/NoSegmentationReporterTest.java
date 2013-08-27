package net.codjo.segmentation.server.participant.context;
import net.codjo.util.time.TimeSource;
/**
 *
 */
public class NoSegmentationReporterTest extends AbstractSegmentationReporterTest {
    public NoSegmentationReporterTest() {
        super(false);
    }


    @Override
    SegmentationReporter createReporter(TimeSource timeSource) {
        return SegmentationReporter.NONE;
    }
}
