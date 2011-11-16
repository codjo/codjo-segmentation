package net.codjo.segmentation.batch.plugin;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import static net.codjo.plugin.batch.BatchCore.BATCH_ARGUMENT;
import static net.codjo.plugin.batch.BatchCore.BATCH_DATE;
import static net.codjo.plugin.batch.BatchCore.BATCH_INITIATOR;
import net.codjo.plugin.batch.BatchException;
import net.codjo.plugin.batch.BatchPlugin;
import net.codjo.plugin.common.CommandLineArguments;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.workflow.common.schedule.ScheduleLauncher;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
/**
 *
 */
public class SegmentationBatchPlugin implements BatchPlugin {
    private static final Logger LOG = Logger.getLogger(SegmentationBatchPlugin.class);
    private AgentContainer container;
    private SegmentationBatchConfiguration configuration = new SegmentationBatchConfigurationImpl();
    static final int MINUTE = 1000 * 60;


    public String getType() {
        return SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE;
    }


    public void initContainer(ContainerConfiguration containerConfiguration) throws Exception {

    }


    public void start(AgentContainer agentContainer) throws Exception {
        container = agentContainer;
    }


    public void stop() throws Exception {

    }


    public void execute(UserId userId, CommandLineArguments arguments)
          throws ContainerFailureException, BatchException {

        SegmentationJobRequest request = new SegmentationJobRequest();
        initArguments(request, arguments);

        LOG.info("Arguments de segmentation - " + request.toRequest().getArguments().encode());

        ScheduleLauncher launcher = new ScheduleLauncher(userId, arguments.getArgument(BATCH_INITIATOR));
        launcher.setWorkflowConfiguration(configuration.getWorkflowConfiguration());
        launcher.executeWorkflow(container, customizeRequest(request).toRequest());
    }


    public SegmentationBatchConfiguration getConfiguration() {
        return configuration;
    }


    private SegmentationJobRequest customizeRequest(SegmentationJobRequest request) throws BatchException {
        SegmentationRequestCustomizer customizer = getConfiguration().getCustomizer();
        if (customizer != null) {
            customizer.customize(request);
        }
        return request;
    }


    private void initArguments(SegmentationJobRequest request, CommandLineArguments arguments) {
        Iterator allArguments = arguments.getAllArguments();
        while (allArguments.hasNext()) {
            String argument = (String)allArguments.next();
            if (SegmentationJobRequest.SEGMENTATION_IDS.equals(argument)) {
                request.setSegmentationIds(arguments.getArgument(argument));
            }
            else if (BATCH_INITIATOR.equals(argument)) {
                request.setInitiatorLogin(arguments.getArgument(argument));
            }
            else if (BATCH_DATE.equals(argument)) {
                request.setDate(arguments.getDateArgument(BATCH_DATE));
            }
            else if (BATCH_ARGUMENT.equals(argument)) {
                String otherArguments = arguments.getArgument(argument);
                if (otherArguments != null) {

                    StringTokenizer tokenizer = new StringTokenizer(otherArguments, " ");
                    while (tokenizer.hasMoreTokens()) {
                        String parameterName = tokenizer.nextToken();
                        String parameterValue;
                        try {
                            parameterValue = tokenizer.nextToken();
                            request.putParameter(parameterName, parameterValue);
                        }
                        catch (NoSuchElementException exception) {
                            request.putParameter(parameterName, "");
                        }
                    }
                }
            }
        }
    }


    private static class SegmentationBatchConfigurationImpl implements SegmentationBatchConfiguration {
        private SegmentationRequestCustomizer customizer;
        private WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration();


        SegmentationBatchConfigurationImpl() {
            workflowConfiguration.setDefaultTimeout(MINUTE * 120);
        }


        public SegmentationRequestCustomizer getCustomizer() {
            return customizer;
        }


        public WorkflowConfiguration getWorkflowConfiguration() {
            return workflowConfiguration;
        }


        public void setCustomizer(SegmentationRequestCustomizer customizer) {
            this.customizer = customizer;
        }
    }
}
