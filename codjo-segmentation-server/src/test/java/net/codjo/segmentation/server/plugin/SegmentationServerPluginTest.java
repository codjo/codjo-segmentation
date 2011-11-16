package net.codjo.segmentation.server.plugin;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.mad.server.plugin.MadServerPluginMock;
import net.codjo.plugin.server.NodeCore;
import net.codjo.test.common.LogString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class SegmentationServerPluginTest {
    private LogString log = new LogString();
    private AgentContainerFixture fixture = new AgentContainerFixture();


    @Before
    public void setUp() throws Exception {
        fixture.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        fixture.doTearDown();
    }


    @Test
    public void test_handlerCommand() throws Exception {
        new SegmentationServerPlugin(new MadServerPluginMock(log), new NodeCore());

        log.assertContent(
              "madServerPluginConfiguration.addHandlerCommand(GetSegmentationConfigCommand)");
    }


    @Test
    public void test_start() throws Exception {
        fixture = new AgentContainerFixture();
        new SegmentationServerPlugin(new MadServerPluginMock(log),
                                     new NodeCore()).start(fixture.getContainer());

        fixture.assertNumberOfAgentWithService(1, SegmentationServerPlugin.JOB_TYPE);
    }


    @Test
    public void test_setMaxSegmentationJobAgent() throws Exception {
        fixture = new AgentContainerFixture();
        SegmentationServerPlugin segmentationServerPlugin
              = new SegmentationServerPlugin(new MadServerPluginMock(log),
                                             new NodeCore());

        segmentationServerPlugin.getConfiguration().setMaxSegmentationJobAgents(3);
        segmentationServerPlugin.start(fixture.getContainer());

        fixture.assertNumberOfAgentWithService(3, SegmentationServerPlugin.JOB_TYPE);
    }
}
