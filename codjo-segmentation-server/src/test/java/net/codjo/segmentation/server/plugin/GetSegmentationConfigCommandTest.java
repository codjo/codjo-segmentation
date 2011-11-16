package net.codjo.segmentation.server.plugin;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerCommandTestCase;
/**
 *
 */
public class GetSegmentationConfigCommandTest extends HandlerCommandTestCase {
    @Override
    protected HandlerCommand createHandlerCommand() {
        return new GetSegmentationConfigCommand();
    }


    @Override
    protected String getHandlerId() {
        return "getSegmentationConfig";
    }
}
