package net.codjo.segmentation.server.participant.context;
import net.codjo.util.time.DaySegmenter;
import net.codjo.util.time.SegmentedStatistics;
import net.codjo.util.time.SegmentedStatistics.Segmenter;
import net.codjo.util.time.Statistics;
import net.codjo.util.time.TimeSource;
import org.joda.time.Days;
/**
 * A specialized implementation of {@link SimpleSegmentationReporter} that adds detailed statistics for each period of
 * the day, when the segmentation is running.
 *
 * @see #getStatisticsSegmenter() to know how a day is divided.
 */
public class DetailedSegmentationReporter extends SimpleSegmentationReporter {
    private static final int DEFAULT_NB_SEGMENTS = Days.ONE.toStandardMinutes().dividedBy(15).getMinutes();

    /**
     * This is the default segmenter that splits a day into segments of 15 minutes.
     */
    private static final DaySegmenter DEFAULT_SEGMENTER = new DaySegmenter("HH:mm", DEFAULT_NB_SEGMENTS);


    public DetailedSegmentationReporter() {
        super();
    }


    public DetailedSegmentationReporter(TimeSource timeSource) {
        super(timeSource);
    }


    /**
     * Gets the {@link Segmenter} used to divide time into segments.
     */
    public Segmenter getStatisticsSegmenter() {
        return DEFAULT_SEGMENTER;
    }


    @Override
    protected SegmentationReport create(TimeSource timeSource) {
        return new DetailedSegmentationReport(timeSource);
    }


    private class DetailedSegmentationReport extends SimpleReport {
        private DetailedSegmentationReport(TimeSource timeSource) {
            super(timeSource);
        }


        @Override
        protected Statistics createStatistics() {
            return new SegmentedStatistics(getStatisticsSegmenter());
        }


        @Override
        protected void addTime(Statistics statistics, long begin, long timeToAdd) {
            ((SegmentedStatistics)statistics).addTime(begin, timeToAdd);
        }


        @Override
        protected void appendDetailedStats(StringBuilder s, String taskName, Statistics statistics) {
            SegmentedStatistics segmentedStatistics = (SegmentedStatistics)statistics;
            s.append(LINE_SEPARATOR).append("\tDetails per period :");
            Segmenter segmenter = getStatisticsSegmenter();
            for (int segment : segmentedStatistics.getSegments()) {
                s.append(LINE_SEPARATOR).append("\t\t").append(segmenter.getSegmentName(segment)).append(' ');
                appendStats(s, null, segmentedStatistics.getSegmentStatistics(segment), null, false);
            }
        }
    }
}
