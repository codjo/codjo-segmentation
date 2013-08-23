package net.codjo.segmentation.server.participant.context;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import net.codjo.segmentation.server.participant.context.SegmentationReport.Task;
import net.codjo.util.time.SimpleStatistics;
import net.codjo.util.time.Statistics;
import net.codjo.util.time.SystemTimeSource;
import net.codjo.util.time.TimeSource;
import org.apache.commons.lang.mutable.MutableInt;
/**
 * A simple implementation of {@link SegmentationReporter} that computes statistics per task.
 */
public class SimpleSegmentationReporter implements SegmentationReporter {
    private static final String REPORT_NAME = "Report";
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final TimeSource timeSource;


    public SimpleSegmentationReporter() {
        this(null);
    }


    public SimpleSegmentationReporter(TimeSource timeSource) {
        this.timeSource = SystemTimeSource.defaultIfNull(timeSource);
    }


    /**
     * {@inheritDoc}
     */
    public final SegmentationReport create() {
        return create(timeSource);
    }


    protected SegmentationReport create(TimeSource timeSource) {
        return new SimpleReport(timeSource);
    }


    private class BaseObject {
        private final TimeSource timeSource;
        private final String name;
        private final long begin;

        protected SimpleReport report;
        private boolean closed = false;

        private boolean error = false;


        public BaseObject(TimeSource timeSource, String name) {
            this.timeSource = timeSource;
            this.name = name;
            this.begin = timeSource.getTime();
        }


        protected final void setReport(SimpleReport report) {
            this.report = report;
        }


        public final void close() {
            if (!closed) {
                long duration = timeSource.getTime() - begin;
                closed = true;

                report.reportTaskTime(getName(), begin, duration, error);

                doAfterClose(duration);
            }
        }


        public final void reportError() {
            error = true;
        }


        protected void doAfterClose(long duration) {
        }


        public final String getName() {
            return name;
        }
    }

    protected class DefaultTask extends BaseObject implements Task {
        private DefaultTask(TimeSource timeSource, DefaultTask parent, String name, SimpleReport report) {
            super(timeSource, ((parent == null) ? "" : parent.getName() + '.') + name);
            setReport(report);
        }


        public final DefaultTask createTask(String name) {
            DefaultTask child = new DefaultTask(timeSource, this, name, report);
            return child;
        }


        @Override
        protected void doAfterClose(long duration) {
        }
    }


    static double roundTo3Digits(double value) {
        return (double)Math.round(value * 1000d) / 1000d;
    }


    protected class SimpleReport extends BaseObject implements SegmentationReport {
        private final Map<String, Statistics> stats = new HashMap<String, Statistics>();
        private final Map<String, MutableInt> errorCounters = new HashMap<String, MutableInt>();


        protected SimpleReport(TimeSource timeSource) {
            super(timeSource, REPORT_NAME);
            setReport(this);
        }


        public final Task createTask(String name) {
            return new DefaultTask(timeSource, null, name, this);
        }


        @Override
        protected final void doAfterClose(long duration) {
            StringBuilder s = new StringBuilder().append("Statistics for segmentation :").append(LINE_SEPARATOR);

            TreeSet<String> sortedTasks = new TreeSet<String>(stats.keySet());

            for (String task : sortedTasks) {
                Statistics statistics = stats.get(task);

                if (REPORT_NAME.equals(task)) {
                    s.append(REPORT_NAME).append(": totalTime=").append(statistics.getTotalTime()).append(" ms");
                }
                else {
                    appendStats(s, task, statistics, errorCounters.get(task), true);
                    appendDetailedStats(s, task, statistics);
                }

                s.append(LINE_SEPARATOR);
            }
            LOGGER.info(s.toString());
        }


        protected void appendDetailedStats(StringBuilder s, String task, Statistics statistics) {
        }


        protected Statistics createStatistics() {
            return new SimpleStatistics();
        }


        protected void addTime(Statistics statistics, long begin, long timeToAdd) {
            ((SimpleStatistics)statistics).addTime(timeToAdd);
        }


        protected final void appendStats(StringBuilder s,
                                         String task,
                                         Statistics stats,
                                         MutableInt nbErrors,
                                         boolean reportErrors) {
            if (task != null) {
                s.append(task);
            }
            s.append(": ");

            if ((stats == null) || stats.isEmpty()) {
                s.append("no statistics");
            }
            else {
                s.append("count=").append(stats.getCount());
                s.append(", min=").append(stats.getMinTime()).append(" ms");
                s.append(", max=").append(stats.getMaxTime()).append(" ms");
                s.append(", totalTime=").append(stats.getTotalTime()).append(" ms");
                s.append(", meanTime=").append(roundTo3Digits(stats.getMeanTime())).append(" ms");
                s.append(", meanFrequency=")
                      .append(roundTo3Digits(stats.getMeanFrequency()))
                      .append(" operation(s)/second");
            }

            if (reportErrors) {
                s.append("; ");
                if (nbErrors == null) {
                    s.append("no error");
                }
                else {
                    s.append(nbErrors.intValue()).append(" error");
                    s.append((nbErrors.intValue() > 1) ? "s" : "");
                }
            }
        }


        public final void reportTaskTime(String name, long begin, long duration, boolean error) {
            synchronized (stats) {
                Statistics s = stats.get(name);
                if (s == null) {
                    s = createStatistics();
                    stats.put(name, s);
                }
                addTime(s, begin, duration);

                if (error) {
                    MutableInt errorCounter = errorCounters.get(name);
                    if (errorCounter == null) {
                        errorCounter = new MutableInt();
                        errorCounters.put(name, errorCounter);
                    }
                    errorCounter.increment();
                }
            }
        }
    }
}
