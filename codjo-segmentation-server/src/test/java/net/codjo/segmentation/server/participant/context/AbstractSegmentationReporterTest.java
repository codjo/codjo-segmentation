package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.participant.context.SegmentationReport.Task;
import net.codjo.test.common.LoggerRule;
import net.codjo.util.time.MockTimeSource;
import net.codjo.util.time.SimpleStatistics;
import net.codjo.util.time.Statistics;
import net.codjo.util.time.TimeSource;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static net.codjo.segmentation.server.participant.context.SimpleSegmentationReporter.roundTo3Digits;
import static org.junit.Assert.assertEquals;
/**
 *
 */
@RunWith(Theories.class)
public abstract class AbstractSegmentationReporterTest<T extends SegmentationReporter> {
    private static final Logger LOG = Logger.getLogger(AbstractSegmentationReporterTest.class);

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @DataPoint
    public static final int[] NO_TASK = {};
    @DataPoint
    public static final int[] ONE_TASK = {1};
    @DataPoint
    public static final int[] TWO_TASKS = {1, 2};
    @DataPoint
    public static final int[] THREE_TASKS = {2, 3, 1};

    @Rule
    public final LoggerRule loggerRule = new LoggerRule(Level.INFO);

    protected final MockTimeSource timeSource = new MockTimeSource();

    private final boolean doReport;
    protected T reporter;


    protected AbstractSegmentationReporterTest(boolean doReport) {
        this.doReport = doReport;
    }


    abstract T createReporter(TimeSource timeSource);


    @Before
    public void setUp() throws Exception {
        reporter = createReporter(timeSource);
    }


    @Theory
    public void testReporter_withError(int[] taskTimeMultipliers) throws Exception {
        testReporter(true, taskTimeMultipliers);
    }


    @Theory
    public void testReporter_withoutError(int[] taskTimeMultipliers) throws Exception {
        testReporter(false, taskTimeMultipliers);
    }


    private void testReporter(boolean error, int[] taskTimeMultipliers) throws Exception {
        final int nbTasks = taskTimeMultipliers.length;
        String task = "myTask";

        String subTask1 = "mySubTask1";
        long[] subTask1Times = {-6, 1, 8, 2}; // negative values are used to simulate an error
        SimpleStatistics subTask1Stats = new SimpleStatistics();
        MutableInt subTask1ErrorCount = new MutableInt();

        String subTask2 = "mySubTask2";
        long[] subTask2Times = {-6, 2, 5, -8, 22, 19}; // negative values are used to simulate an error
        SimpleStatistics subTask2Stats = new SimpleStatistics();
        MutableInt subTask2ErrorCount = new MutableInt();

        SimpleStatistics taskStats = new SimpleStatistics();
        MutableInt taskErrorCount = new MutableInt();

        // simulate a task reporting its progress
        SegmentationReport report = reporter.create();
        long reportBegin = timeSource.getTime();
        for (int i = 0; i < nbTasks; i++) {
            final long timeIncrement = getTimeIncrementForTaskIteration();
            timeSource.inc(timeIncrement);
            long totalTime = 0L;

            int taskTimeMultiplier = taskTimeMultipliers[i];

            Task mainTask = report.createTask(task);
            taskCreated(task, mainTask);

            totalTime += runSubTasks(taskTimeMultiplier,
                                     error,
                                     mainTask,
                                     task,
                                     subTask1,
                                     subTask1ErrorCount,
                                     subTask1Stats,
                                     subTask1Times);
            totalTime += runSubTasks(taskTimeMultiplier,
                                     error,
                                     mainTask,
                                     task,
                                     subTask2,
                                     subTask2ErrorCount,
                                     subTask2Stats,
                                     subTask2Times);
            LOG.debug("task (run #" + i + "): totalTime = " + totalTime);

            taskStats.addTime(totalTime);

            if (error) {
                mainTask.reportError();
                taskErrorCount.increment();
            }
            mainTask.close();
            taskClosed(task, mainTask);
        }
        assertTrue("Nothing logged", "".equals(loggerRule.getAppender().toString()));
        report.close();
        long reportTotalTime = timeSource.getTime() - reportBegin;

        // assertions
        if (doReport) {
            StringBuilder expectedLogs
                  = new StringBuilder("INFO: Statistics for segmentation :").append(LINE_SEPARATOR);
            expectedLogs.append("Report: totalTime=").append(reportTotalTime).append(" ms").append(LINE_SEPARATOR);

            if (nbTasks > 0) {
                appendStats(expectedLogs, true, task, taskStats, taskErrorCount);
                appendStats(expectedLogs, true, getTaskFullName(task, subTask1), subTask1Stats, subTask1ErrorCount);
                appendStats(expectedLogs, true, getTaskFullName(task, subTask2), subTask2Stats, subTask2ErrorCount);
            }

            assertEquals("LogContent", expectedLogs.toString(), loggerRule.getAppender().toString());
        }
        else {
            assertTrue("Nothing logged", loggerRule.getAppender().isEmpty());
        }
    }


    protected long getTimeIncrementForTaskIteration() {
        return 15 * 60 * 1000;
    }


    private String getTaskFullName(String main, String task) {
        return main + '.' + task;
    }


    protected void appendStats(StringBuilder buffer, String taskName, Statistics stats, MutableInt taskErrorCount) {
        appendStats(buffer, false, taskName, stats, taskErrorCount);
    }


    private void appendStats(StringBuilder buffer,
                             boolean prefixWithTask,
                             String taskName,
                             Statistics stats,
                             MutableInt taskErrorCount) {
        if (prefixWithTask) {
            buffer.append(taskName).append(": ");
        }

        boolean appendDetails = false;
        if (stats.isEmpty()) {
            buffer.append("no statistics");
        }
        else {
            buffer.append("count=").append(stats.getCount());
            buffer.append(", min=").append(stats.getMinTime()).append(" ms");
            buffer.append(", max=").append(stats.getMaxTime()).append(" ms");
            buffer.append(", totalTime=").append(stats.getTotalTime()).append(" ms");
            buffer.append(", meanTime=").append(roundTo3Digits(stats.getMeanTime())).append(" ms");
            buffer.append(", meanFrequency=")
                  .append(roundTo3Digits(stats.getMeanFrequency()))
                  .append(" operation(s)/second");
            if (prefixWithTask) {
                appendDetails = true;
            }
        }

        if (taskErrorCount != null) {
            int nbErrors = taskErrorCount.intValue();
            if (nbErrors < 1) {
                buffer.append("; no error");
            }
            else {
                buffer.append("; ").append(nbErrors).append(" error").append((nbErrors > 1) ? "s" : "");
            }
        }
        buffer.append(LINE_SEPARATOR);

        if (appendDetails) {
            appendDetailedStats(buffer, taskName);
        }
    }


    private long runSubTasks(final int taskTimeMultiplier,
                             final boolean error,
                             final Task mainTask,
                             final String taskName,
                             final String subTaskName,
                             final MutableInt subTaskErrorCount,
                             final SimpleStatistics subTaskStats,
                             long... subTaskDurations) {
        long totalTime = 0L;

        for (long duration : subTaskDurations) {
            Task subTask = mainTask.createTask(subTaskName);
            taskCreated(getTaskFullName(taskName, subTaskName), subTask);

            duration *= taskTimeMultiplier;

            boolean simulateError = error && (duration < 0);
            duration = Math.abs(duration);

            subTaskStats.addTime(duration);

            totalTime += duration;

            timeSource.inc(duration); // simulate the subtask

            // a negative duration is used to simulate an error after |duration| ms
            if (simulateError) {
                subTaskErrorCount.increment();
                subTask.reportError();
            }

            subTask.close();
            taskClosed(getTaskFullName(taskName, subTaskName), subTask);
        }

        LOG.debug("runSubTasks '" + subTaskName + "': totalTime = " + totalTime);
        return totalTime;
    }


    protected void appendDetailedStats(StringBuilder buffer, String taskName) {
    }


    protected void taskClosed(String subTaskName, Task subTask) {
    }


    protected void taskCreated(String subTaskName, Task subTask) {
    }
}
