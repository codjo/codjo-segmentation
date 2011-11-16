package net.codjo.segmentation.server.blackboard.message;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 *
 */
public class BlackboardActionBuilderTest extends TestCase {
    private LogString log = new LogString();


    public void test_actionList() throws Exception {
        Write write1 = new Write(new Todo<String>("1"), new Level("a"));
        Write write2 = write1.then().write(new Todo<String>("2"), new Level("a"));

        assertTrue(write2.hasBlackBoardActionBuilder());
        assertSame(write1.then(), write2.then());

        write1.then().visit(new BlackboardActionVisitorMock(log));

        log.assertContent("visit(write(todo:1)), visit(write(todo:2))");
    }
}
