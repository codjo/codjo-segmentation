package net.codjo.segmentation.batch.plugin;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story;
import net.codjo.plugin.batch.BatchCore;
import net.codjo.plugin.batch.BatchException;
import net.codjo.plugin.common.CommandLineArguments;
import static net.codjo.segmentation.batch.plugin.SegmentationBatchPlugin.MINUTE;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import static net.codjo.workflow.common.util.WorkflowSystem.workFlowSystem;
import junit.framework.TestCase;
/**
 *
 */
public class SegmentationBatchPluginTest extends TestCase {

    private SegmentationBatchPlugin plugin;
    private Story story = new Story();
    private UserId userId = UserId.createId("polo", "secretPassword");


    public void test_getType() throws Exception {
        assertEquals(SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE, plugin.getType());
    }


    public void test_defaultTimeoutConfiguration() throws Exception {
        assertEquals(MINUTE * 120, plugin.getConfiguration().getWorkflowConfiguration().getDefaultTimeout());
    }


    public void test_doNothing() throws Exception {
        plugin.initContainer(null);
        plugin.stop();
    }


    public void test_execute() throws Exception {
        story.record().mock(workFlowSystem())
              .simulateJob("job<segmentation>(-endPeriod=200709, -startPeriod=200708, segmentations=1, 2)");

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                plugin.start(story.getAgentContainerFixture().getContainer());
                plugin.execute(userId, createArguments("1, 2", "200708", "200709"));
            }
        });

        story.execute();
    }


    public void test_execute_customizer() throws Exception {
        plugin.getConfiguration().setCustomizer(new SegmentationRequestCustomizer() {
            public void customize(SegmentationJobRequest request) throws BatchException {
                request.setSegmentationIds("overrided");
            }
        });

        story.record().mock(workFlowSystem())
              .simulateJob(
                    "job<segmentation>(-endPeriod=200709, -startPeriod=200708, segmentations=overrided)");

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                plugin.start(story.getAgentContainerFixture().getContainer());
                plugin.execute(userId, createArguments("old", "200708", "200709"));
            }
        });

        story.execute();
    }


    private CommandLineArguments createArguments(String segmentationIds,
                                                 String startPeriod,
                                                 String endPeriod) {
        return new CommandLineArguments(
              new String[]{"-" + SegmentationJobRequest.SEGMENTATION_IDS, segmentationIds,
                           "-" + BatchCore.BATCH_ARGUMENT,
                           "-startPeriod  " + startPeriod + " -endPeriod " + endPeriod});
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
        plugin = new SegmentationBatchPlugin();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }
}
