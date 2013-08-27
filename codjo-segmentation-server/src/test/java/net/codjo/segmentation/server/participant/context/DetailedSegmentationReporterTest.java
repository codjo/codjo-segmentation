package net.codjo.segmentation.server.participant.context;
import java.util.HashMap;
import java.util.Map;
import net.codjo.segmentation.server.participant.context.SegmentationReport.Task;
import net.codjo.util.time.SegmentedStatistics;
import net.codjo.util.time.SegmentedStatistics.Segmenter;
import net.codjo.util.time.SimpleSegmenter;
import net.codjo.util.time.TimeSource;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
/**
 *
 */
public class DetailedSegmentationReporterTest extends AbstractSegmentationReporterTest<DetailedSegmentationReporter> {
    private static final Logger LOG = Logger.getLogger(DetailedSegmentationReporterTest.class);

    private final Map<String, SegmentedStatistics> statsPerTask = new HashMap<String, SegmentedStatistics>();
    private final Map<String, Long> runningTasks = new HashMap<String, Long>();


    public DetailedSegmentationReporterTest() {
        super(true);
    }


    @Override
    DetailedSegmentationReporter createReporter(TimeSource timeSource) {
        return new DetailedSegmentationReporter(timeSource);
    }


    @Test
    public void testGetStatisticsSegmenter() {
        DetailedSegmentationReporter reporter = createReporter(timeSource);

        assertNotNull("statisticsSegmenter != null", reporter.getStatisticsSegmenter());
    }


    @Override
    protected long getTimeIncrementForTaskIteration() {
        // go to next segment for each new run of the main task
        long result = 0L;
        if (getSegmenter() instanceof SimpleSegmenter) {
            result = ((SimpleSegmenter)getSegmenter()).getSegmentSize();
        }
        return result;
    }


    @Override
    protected void taskCreated(String subTaskName, Task subTask) {
        long begin = timeSource.getTime();
        runningTasks.put(subTaskName, begin);
    }


    @Override
    protected void taskClosed(String subTaskName, Task subTask) {
        long begin = runningTasks.remove(subTaskName);
        long end = timeSource.getTime();

        SegmentedStatistics stats = statsPerTask.get(subTaskName);
        if (stats == null) {
            stats = new SegmentedStatistics(getSegmenter());
            statsPerTask.put(subTaskName, stats);
        }
        stats.addTime(begin, end - begin);
    }


    @Override
    protected void appendDetailedStats(StringBuilder buffer, String taskName) {
        buffer.append("\tDetails per period :").append(LINE_SEPARATOR);
        SegmentedStatistics stats = statsPerTask.get(taskName);
        for (int segment : stats.getSegments()) {
            buffer.append("\t\t").append(getSegmenter().getSegmentName(segment));
            buffer.append(" : ");
            appendStats(buffer, taskName, stats.getSegmentStatistics(segment), null);
        }
    }


    private Segmenter getSegmenter() {
        return reporter.getStatisticsSegmenter();
    }
}
