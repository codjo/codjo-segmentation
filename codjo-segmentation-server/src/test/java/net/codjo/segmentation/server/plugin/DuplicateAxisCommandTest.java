package net.codjo.segmentation.server.plugin;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerCommandTestCase;
import java.util.HashMap;
import java.util.Map;

public class DuplicateAxisCommandTest extends HandlerCommandTestCase {

    @Override
    protected HandlerCommand createHandlerCommand() {
        return new DuplicateAxisCommand();
    }


    @Override
    protected String getHandlerId() {
        return "duplicateAxis";
    }


    public void test_execute() throws Exception {
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("classificationId", "1");
        assertExecuteQuery("DuplicateAxisCommandTest", arguments);
    }
}
