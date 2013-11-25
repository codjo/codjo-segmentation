package net.codjo.segmentation.server.blackboard;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.codjo.util.time.TimeSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.log4j.Logger;
/**
 *
 */
final public class DefaultErrorLogLimiter implements ErrorLogLimiter {
    private final Map<String, MutableLong> rootCauses = new HashMap<String, MutableLong>();
    private final TimeSource timeSource;

    /**
     * Size of the time window in milliseconds.
     */
    private final long timeWindow;

    private long skippedLogs;


    public DefaultErrorLogLimiter(TimeSource timeSource, long timeWindowValue, TimeUnit timeWindowUnit) {
        this.timeSource = timeSource;
        this.timeWindow = timeWindowUnit.toMillis(timeWindowValue);
    }

//        public void clearLogErrorCounters() {
//            synchronized (rootCauses) {
//                rootCauses.clear();
//            }
//        }


    public boolean logError(SQLException e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause == null) {
            rootCause = e;
        }

        String key = rootCause.getClass().getName() + '#' + rootCause.getMessage();

        boolean shouldLog = false;
        synchronized (rootCauses) {
            MutableLong firstOccurence = rootCauses.get(key);
            if (firstOccurence == null) {
                // this error never happened before (open a new time window) => log it
                firstOccurence = new MutableLong(timeSource.getTime());
                rootCauses.put(key, firstOccurence);
                shouldLog = true;
            }
            else {
                if (isOldRecord(firstOccurence)) {
                    // this occurence open a new time window => log it
                    firstOccurence.setValue(timeSource.getTime());
                    shouldLog = true;
                }
                // else : this occurence is in the current time window => don't log it
            }

            if (!shouldLog) {
                skippedLogs++;
            }
        }

        return shouldLog;
    }


    public void close() {
        Logger.getLogger(getClass()).warn(skippedLogs + " error logs were skipped to avoid duplicates");
    }


    private boolean isOldRecord(MutableLong firstOccurrence) {
        long time = timeSource.getTime();
        return (firstOccurrence.longValue() + timeWindow) <= time;
    }
}
