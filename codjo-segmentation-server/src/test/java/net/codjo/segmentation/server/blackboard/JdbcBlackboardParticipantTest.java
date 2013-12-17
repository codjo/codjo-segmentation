package net.codjo.segmentation.server.blackboard;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.codjo.segmentation.server.blackboard.JdbcBlackboardParticipantTest.Exceptions.RootCause;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.sql.server.JdbcServiceUtilMock;
import net.codjo.test.common.ListAppender.LogEntryMatcher;
import net.codjo.test.common.LogString;
import net.codjo.test.common.LoggerRule;
import net.codjo.util.time.MockTimeSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static net.codjo.segmentation.server.blackboard.JdbcBlackboardParticipantTest.Exceptions.exceptions;
import static net.codjo.segmentation.server.blackboard.JdbcBlackboardParticipantTest.ExceptionsInTwoTimeWindows.twoExceptions;
import static net.codjo.test.common.ListAppender.Count.count;
import static org.apache.commons.lang.exception.ExceptionUtils.getRootCause;
/**
 *
 */
@RunWith(Theories.class)
public class JdbcBlackboardParticipantTest {
    private static final long TIME_WINDOW_VALUE = 60;
    private static final TimeUnit TIME_WINDOW_UNIT = TimeUnit.SECONDS;

    private static final String ERROR1 = generateUniqueErrorMessage();
    private static final String ERROR2 = generateUniqueErrorMessage();
    private static final SQLException ERROR1_LEVEL1;
    private static final SQLException ERROR1_LEVEL2;
    private static final SQLException ERROR1_LEVEL3;
    private static final SQLException ERROR2_LEVEL2;


    static {
        ERROR1_LEVEL1 = exception(ERROR1);
        ERROR1_LEVEL2 = exception(generateUniqueErrorMessage(), ERROR1);
        ERROR1_LEVEL3 = exception(generateUniqueErrorMessage(), generateUniqueErrorMessage(), ERROR1);

        ERROR2_LEVEL2 = exception(generateUniqueErrorMessage(), ERROR2);
    }


    @DataPoint
    public static final Exceptions NO_ERROR = exceptions();
    @DataPoint
    public static final Exceptions ERRORS_1xE1L1 = exceptions(ERROR1_LEVEL1);
    @DataPoint
    public static final Exceptions ERRORS_1xE1L2 = exceptions(ERROR1_LEVEL2);
    @DataPoint
    public static final Exceptions ERRORS_1xE1L3 = exceptions(ERROR1_LEVEL3);

    @DataPoint
    public static final Exceptions ERRORS_2xE1L1 = exceptions(ERROR1_LEVEL1, ERROR1_LEVEL1);
    @DataPoint
    public static final Exceptions ERRORS_2xE1L2 = exceptions(ERROR1_LEVEL2, ERROR1_LEVEL2);
    @DataPoint
    public static final Exceptions ERRORS_2xE1L3 = exceptions(ERROR1_LEVEL3, ERROR1_LEVEL3);

    @DataPoint
    public static final Exceptions ERRORS_1xE1L1_2xE2L2 = exceptions(ERROR2_LEVEL2, ERROR1_LEVEL1, ERROR2_LEVEL2);
    @DataPoint
    public static final Exceptions ERRORS_2xE1L3_1xE2L2 = exceptions(ERROR1_LEVEL3, ERROR2_LEVEL2, ERROR1_LEVEL3);

    @DataPoint
    public static final ExceptionsInTwoTimeWindows ERRORS_2TW = twoExceptions(ERROR1_LEVEL1, ERROR1_LEVEL1);

    @DataPoint
    public static final long TIME0 = 0;
    @DataPoint
    public static final long TIME1 = TIME_WINDOW_VALUE / 2;

    private LogString log = new LogString();

    @Rule
    public LoggerRule rule = new LoggerRule();


    @Test
    public void test_handleTodo() throws Exception {
        JdbcServiceUtilMock mock = new JdbcServiceUtilMock(log);

        JdbcBlackboardParticipant participant =
              new JdbcBlackboardParticipant(mock,
                                            JdbcBlackboardParticipant.TransactionType.AUTO_COMMIT,
                                            new Level("my-level")) {
                  @Override
                  protected void handleTodo(Todo todo, Level fromLevel, Connection connection) {
                      log.call("handleTodo", fromLevel.getName(), todo.getId(),
                               (connection == null ? "Null connection" : "good connection"));
                  }


                  @Override
                  protected ErrorLogLimiter getErrorLogLimiter(Todo todo) {
                      return ErrorLogLimiter.NONE;
                  }
              };

        //noinspection unchecked
        participant.handleTodo(new Todo(1), new Level("level"));

        log.assertContent("getConnectionPool(null, message:null), handleTodo(level, 1, good connection)");
    }


    @Theory
    public void testSameSQLExceptionReportedOnlyOnce_sameTimeWindow(Exceptions exceptions, long startTime)
          throws Exception {
        testSameSQLExceptionReportedOnlyOnce(exceptions, true, startTime, true);
    }


    @Theory
    public void testSameSQLExceptionReportedOnlyOnce_twoTimeWindows(ExceptionsInTwoTimeWindows exceptions,
                                                                    long startTime) throws Exception {
        testSameSQLExceptionReportedOnlyOnce(exceptions, false, startTime, true);
    }


    @Theory
    public void testSameSQLExceptionReportedManyTimes_noErrorLogLimiter(Exceptions exceptions) throws Exception {
        testSameSQLExceptionReportedOnlyOnce(exceptions, false, 0, false);
    }


    private void testSameSQLExceptionReportedOnlyOnce(Exceptions exceptions,
                                                      boolean sameTimePeriod,
                                                      long startTime,
                                                      boolean enableLimiter)
          throws Exception {
        final DefaultErrorLogLimiter limiter;
        final MockTimeSource timeSource;
        if (enableLimiter) {
            timeSource = new MockTimeSource();
            timeSource.inc(startTime);
            limiter = new DefaultErrorLogLimiter(timeSource, TIME_WINDOW_VALUE, TIME_WINDOW_UNIT);
        }
        else {
            timeSource = null;
            limiter = null;
        }

        for (SQLException exception : exceptions.exceptions()) {
            JdbcServiceUtilMock mock = new JdbcServiceUtilMock(log);
            mock.mockGetConnectionPoolFailure(exception);
            Level level = new Level("my-level");
            JdbcBlackboardParticipant participant =
                  new JdbcBlackboardParticipant(mock,
                                                JdbcBlackboardParticipant.TransactionType.AUTO_COMMIT, level) {
                      @Override
                      protected void handleTodo(Todo todo, Level fromLevel, Connection connection) {
                      }


                      @Override
                      protected ErrorLogLimiter getErrorLogLimiter(Todo todo) {
                          return limiter;
                      }
                  };

            try {
                //noinspection unchecked
                participant.handleTodo(new Todo(1), level);
            }
            catch (NullPointerException npe) {
                // it's expected
            }

            if (!sameTimePeriod && (timeSource != null)) {
                // advance mocked time to the next window
                timeSource.inc(TIME_WINDOW_UNIT.toMillis(TIME_WINDOW_VALUE));
            }
        }

        for (Iterator<RootCause> itEntry = exceptions.rootCauses(); itEntry.hasNext(); ) {
            RootCause expectedResult = itEntry.next();
            SQLException expectedRootCause = expectedResult.rootCause;
            final int expectedCount;
            if (enableLimiter) {
                if (sameTimePeriod) {
                    expectedCount = 1;
                }
                else {
                    // we assume here that there is only one common root cause
                    // for all exceptions returned by exceptions()
                    expectedCount = exceptions.exceptions().length;
                }
            }
            else {
                expectedCount = expectedResult.count;
            }
            rule.getAppender().assertHasLog(LogEntryMatcher.exception(expectedRootCause.getClass(),
                                                                      expectedRootCause.getMessage()),
                                            count(expectedCount));
        }
    }


    static class Exceptions {
        private final SQLException[] exceptions;


        public static Exceptions exceptions(SQLException... exceptions) {
            return new Exceptions(exceptions);
        }


        private Exceptions(SQLException... exceptions) {
            this.exceptions = exceptions;
        }


        public Iterator<RootCause> rootCauses() {
            Map<String, RootCause> rootCauses = new HashMap<String, RootCause>();
            for (Throwable t : exceptions) {
                SQLException rootCause = (SQLException)getRootCause(t);
                if (rootCause == null) {
                    rootCause = (SQLException)t;
                }
                String key = rootCause.getClass().getName() + '#' + rootCause.getMessage();
                RootCause r = rootCauses.get(key);
                if (r == null) {
                    r = new RootCause(rootCause);
                    rootCauses.put(key, r);
                }
                r.count++;
            }
            return rootCauses.values().iterator();
        }


        public static class RootCause {
            private final SQLException rootCause;
            private int count;


            public RootCause(SQLException rootCause) {
                this.rootCause = rootCause;
            }
        }


        public SQLException[] exceptions() {
            return exceptions;
        }
    }

    static class ExceptionsInTwoTimeWindows extends Exceptions {
        public static ExceptionsInTwoTimeWindows twoExceptions(SQLException exception1, SQLException exception2) {
            return new ExceptionsInTwoTimeWindows(exception1, exception2);
        }


        private ExceptionsInTwoTimeWindows(SQLException exception1, SQLException exception2) {
            super(exception1, exception2);
        }
    }

    private static int errorNumber = 1;


    private static String generateUniqueErrorMessage() {
        String result = "Error" + errorNumber;
        errorNumber++;
        return result;
    }


    public static SQLException exception(String... messages) {
        SQLException result = null;

        for (int i = messages.length - 1; i >= 0; i--) {
            SQLException e = new SQLException(messages[i]);
            if (result != null) {
                e.initCause(result);
            }
            result = e;
        }

        return result;
    }
}
