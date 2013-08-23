package net.codjo.segmentation.server.participant.context;
import net.codjo.util.time.TimeSource;
/**
 *
 */
public class SimpleSegmentationReporterTest extends AbstractSegmentationReporterTest {
    public SimpleSegmentationReporterTest() {
        super(true);
    }


    @Override
    SegmentationReporter createReporter(TimeSource timeSource) {
        return new SimpleSegmentationReporter(timeSource);
    }
}
