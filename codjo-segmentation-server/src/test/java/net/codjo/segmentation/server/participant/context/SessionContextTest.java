package net.codjo.segmentation.server.participant.context;
import java.util.concurrent.TimeUnit;
import net.codjo.segmentation.server.blackboard.DefaultErrorLogLimiter;
import net.codjo.segmentation.server.blackboard.ErrorLogLimiter;
import net.codjo.segmentation.server.blackboard.message.Todo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
/**
 *
 */
@RunWith(Theories.class)
public class SessionContextTest {
    @DataPoint
    public static long TIME_WINDOW_MINUS_2 = -2L;
    @DataPoint
    public static long TIME_WINDOW_MINUS_1 = -1L;
    @DataPoint
    public static long TIME_WINDOW_ZERO = 0L;
    @DataPoint
    public static long TIME_WINDOW_1 = 1L;
    @DataPoint
    public static long TIME_WINDOW_2 = 2L;

    private final SessionContext sessionContext = new SessionContext(null, 0, TimeUnit.SECONDS);


    @Test
    public void test_getFamilyContext() throws Exception {
        FamilyContext context = new FamilyContext(null, null);

        sessionContext.put("family-id", context);

        Assert.assertSame(context, sessionContext.get("family-id"));
        Assert.assertSame(context, sessionContext.getFamilyContext(toTodo("family-id")));

        sessionContext.remove("family-id");

        Assert.assertNull(sessionContext.get("family-id"));
    }


    @Theory
    public void test_getErrorLogLimiter(long timeWindowValue) {
        SessionContext sessionContext = new SessionContext(null, timeWindowValue, TimeUnit.SECONDS);
        Class<? extends ErrorLogLimiter> expectedClass = (timeWindowValue <= 0) ?
                                                         ErrorLogLimiter.NONE.getClass() :
                                                         DefaultErrorLogLimiter.class;

        Assert.assertNotNull(sessionContext.getErrorLogLimiter());
        Assert.assertEquals(expectedClass, sessionContext.getErrorLogLimiter().getClass());
    }


    private Todo<TodoContent> toTodo(String familyId) {
        return new Todo<TodoContent>(new TodoContent(null, familyId));
    }
}
