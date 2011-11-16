package net.codjo.segmentation.server.plugin;
import net.codjo.agent.Behaviour;
import net.codjo.agent.behaviour.SequentialBehaviour;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.aspect.AspectManager;
import net.codjo.segmentation.server.participant.context.ContextManagerMock;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.workflow.server.api.JobAgent.MODE;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SegmentationJobAgentTest {
    private SegmentationJobAgent segmentationJobAgent;
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
    public void test_getJobProtocolParticipant() throws Exception {
        segmentationJobAgent = new SegmentationJobAgent(new ContextManagerMock(),
                                                        new AspectManager(),
                                                        new JdbcServiceUtil());
        assertTrue(segmentationJobAgent.getJobProtocolParticipant() instanceof SegmentationAspectParticipant);
    }


    @Test
    public void test_start() throws Exception {
        segmentationJobAgent = new SegmentationJobAgent(new ContextManagerMock(),
                                                        new AspectManager(),
                                                        new JdbcServiceUtil(), MODE.DELEGATE);

        fixture.startNewAgent("segmentation-job-agent", segmentationJobAgent);

        Behaviour behaviour = segmentationJobAgent.getJobProtocolParticipant().getExecuteJobBehaviour();
        Assert.assertTrue(behaviour instanceof SequentialBehaviour);
    }
}
