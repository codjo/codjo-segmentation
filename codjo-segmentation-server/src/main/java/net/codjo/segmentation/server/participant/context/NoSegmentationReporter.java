package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.participant.context.SegmentationReport.Task;
/**
 *
 */
final class NoSegmentationReporter implements SegmentationReporter {
    private static final Task NO_TASK = new Task() {
        public Task createTask(String name) {
            return NO_TASK;
        }


        public void reportError() {
        }


        public void close() {
        }
    };

    private static final SegmentationReport NO_REPORT = new SegmentationReport() {
        public Task createTask(String name) {
            return NO_TASK;
        }


        public void close() {
        }
    };


    NoSegmentationReporter() {
    }


    public final SegmentationReport create() {
        return NO_REPORT;
    }
}
