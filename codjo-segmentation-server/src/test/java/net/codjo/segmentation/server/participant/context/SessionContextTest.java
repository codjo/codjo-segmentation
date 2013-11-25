package net.codjo.segmentation.server.participant.context;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;
import net.codjo.segmentation.server.blackboard.message.Todo;
/**
 *
 */
public class SessionContextTest extends TestCase {
    private final SessionContext sessionContext = new SessionContext(null, 0, TimeUnit.SECONDS);


    public void test_getFamilyContext() throws Exception {
        FamilyContext context = new FamilyContext(null, null);

        sessionContext.put("family-id", context);

        assertSame(context, sessionContext.get("family-id"));
        assertSame(context, sessionContext.getFamilyContext(toTodo("family-id")));

        sessionContext.remove("family-id");

        assertNull(sessionContext.get("family-id"));
    }


    private Todo<TodoContent> toTodo(String familyId) {
        return new Todo<TodoContent>(new TodoContent(null, familyId));
    }
}
