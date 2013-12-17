package net.codjo.segmentation.server.plugin;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerFailureException;
import net.codjo.aspect.AspectConfigException;
import net.codjo.aspect.AspectManager;
import net.codjo.mad.server.plugin.MadServerPlugin;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.server.AbstractServerPlugin;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.segmentation.server.audit.SegmentationStringifier;
import net.codjo.segmentation.server.paramExport.agent.ExportParticipantAgent;
import net.codjo.segmentation.server.paramImport.agent.ImportParticipantAgent;
import net.codjo.segmentation.server.participant.CalculatorParticipant;
import net.codjo.segmentation.server.participant.DeleteParticipant;
import net.codjo.segmentation.server.participant.JobRequestAnalyzerParticipant;
import net.codjo.segmentation.server.participant.PaginatorParticipant;
import net.codjo.segmentation.server.participant.SegmentationParticipant;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.preference.family.BadConfigurationException;
import net.codjo.segmentation.server.preference.family.XmlPreferenceLoader;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.workflow.server.api.JobAgent.MODE;
import net.codjo.workflow.server.plugin.WorkflowServerPlugin;
import net.codjo.xml.XmlException;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class SegmentationServerPlugin extends AbstractServerPlugin {
    private static final Logger LOG = Logger.getLogger(SegmentationServerPlugin.class);

    public static final String JOB_TYPE = SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE;
    private final SegmentationServerPluginConfigurationImpl configuration
          = new SegmentationServerPluginConfigurationImpl();
    private AgentContainer agentContainer;
    private ContextManager manager;
    private MadServerPlugin madServerPlugin;
    private ApplicationCore core;


    public SegmentationServerPlugin() {
    }


    public SegmentationServerPlugin(MadServerPlugin madServerPlugin, ApplicationCore core) {
        this(madServerPlugin, core, null);
    }


    public SegmentationServerPlugin(MadServerPlugin madServerPlugin,
                                    ApplicationCore core,
                                    WorkflowServerPlugin workflowServerPlugin) {
        this.madServerPlugin = madServerPlugin;
        this.core = core;

        madServerPlugin.getConfiguration().addHandlerCommand(GetSegmentationConfigCommand.class);
        new SegmentationStringifier().install(workflowServerPlugin);
    }


    public SegmentationServerPluginConfiguration getConfiguration() {
        return configuration;
    }


    @Override
    public void start(AgentContainer container) throws Exception {
        agentContainer = container;
        manager = new ContextManager(createXmlPreferenceLoader());

        core.addGlobalComponent(manager);
        madServerPlugin.getConfiguration().addHandlerSql(GetSegmentationResultHandler.class);
        madServerPlugin.getConfiguration().addHandlerCommand(ExpressionCompilerCommand.class);
        madServerPlugin.getConfiguration().addHandlerCommand(DuplicateAxisCommand.class);

        LOG.info("Starting segmentation plugin with configuration " + configuration);
        for (int i = 0; i < configuration.getMaxSegmentationJobAgents(); i++) {
            container.acceptNewAgent(String.format("segmentation-job-agent-%s", i),
                                     createSegmentationAgent(MODE.NOT_DELEGATE))
                  .start();
        }

        final long timeWindowValue = configuration.getTimeWindowValue();
        final TimeUnit timeWindowUnit = configuration.getTimeWindowUnit();
        startParticipant(configuration.getMaxAnalyzerAgents(), "segmentation-analyzer",
                         new ParticipantBuilder() {
                             public SegmentationParticipant createParticipant() {
                                 return new JobRequestAnalyzerParticipant(manager, timeWindowValue, timeWindowUnit);
                             }
                         });
        startParticipant(configuration.getMaxDeleteAgents(), "segmentation-delete",
                         new ParticipantBuilder() {
                             public SegmentationParticipant createParticipant() {
                                 return new DeleteParticipant(manager);
                             }
                         });
        startParticipant(configuration.getMaxPaginatorAgents(), "segmentation-paginator",
                         new ParticipantBuilder() {
                             public SegmentationParticipant createParticipant() {
                                 return new PaginatorParticipant(manager);
                             }
                         });
        startParticipant(configuration.getMaxCalculatorAgents(), "segmentation-calculator",
                         new ParticipantBuilder() {
                             public SegmentationParticipant createParticipant() {
                                 return new CalculatorParticipant(manager);
                             }
                         });

        container.acceptNewAgent("segmentation-import-agent", new ImportParticipantAgent()).start();
        container.acceptNewAgent("segmentation-export-agent", new ExportParticipantAgent()).start();
    }


    private SegmentationJobAgent createSegmentationAgent(MODE mode) throws AspectConfigException {
        return new SegmentationJobAgent(manager, newAspectManager(), new JdbcServiceUtil(), mode);
    }


    @Override
    public void stop() throws Exception {
        core.removeGlobalComponent(ContextManager.class);
    }


    private static AspectManager newAspectManager() throws AspectConfigException {
        AspectManager aspectManager = new AspectManager();
        aspectManager.load();
        return aspectManager;
    }


    private void startParticipant(int count, String name, ParticipantBuilder builder)
          throws ContainerFailureException {
        for (int i = 0; i < count; i++) {
            startParticipant(name + "-" + i, builder.createParticipant());
        }
    }


    private void startParticipant(String name, SegmentationParticipant participant)
          throws ContainerFailureException {
        agentContainer.acceptNewAgent(name, new ParticipantAgent(participant)).start();
    }


    private XmlPreferenceLoader createXmlPreferenceLoader()
          throws IOException, XmlException, BadConfigurationException {
        XmlPreferenceLoader loader = new XmlPreferenceLoader();
        InputStream inputStream = configuration.configurationUrl.openStream();
        try {
            loader.load(new InputSource(inputStream));
        }
        finally {
            inputStream.close();
        }
        return loader;
    }


    static class SegmentationServerPluginConfigurationImpl
          implements SegmentationServerPluginConfiguration {
        private URL configurationUrl = getClass().getResource("/META-INF/segmentation-configs.xml");
        private int maxAnalyzerAgents = 2;
        private int maxDeleteAgents = 3;
        private int maxPaginatorAgents = 3;
        private int maxCalculatorAgents = 12;
        private int maxSegmentationJobAgents = 1;

        private long timeWindowValue = 0;
        private TimeUnit timeWindowUnit = TimeUnit.SECONDS;


        public int getMaxAnalyzerAgents() {
            return maxAnalyzerAgents;
        }


        public void setMaxAnalyzerAgents(int maxAnalyzerAgents) {
            this.maxAnalyzerAgents = maxAnalyzerAgents;
        }


        public int getMaxDeleteAgents() {
            return maxDeleteAgents;
        }


        public void setMaxDeleteAgents(int maxDeleteAgents) {
            this.maxDeleteAgents = maxDeleteAgents;
        }


        public int getMaxPaginatorAgents() {
            return maxPaginatorAgents;
        }


        public void setMaxPaginatorAgents(int maxPaginatorAgents) {
            this.maxPaginatorAgents = maxPaginatorAgents;
        }


        public int getMaxCalculatorAgents() {
            return maxCalculatorAgents;
        }


        public void setMaxCalculatorAgents(int maxCalculatorAgents) {
            this.maxCalculatorAgents = maxCalculatorAgents;
        }


        public int getMaxSegmentationJobAgents() {
            return maxSegmentationJobAgents;
        }


        public void setMaxSegmentationJobAgents(int maxSegmentationJobAgents) {
            this.maxSegmentationJobAgents = maxSegmentationJobAgents;
        }


        public void setTimeWindowValue(long timeWindowValue) {
            this.timeWindowValue = timeWindowValue;
        }


        public void setTimeWindowUnit(TimeUnit timeWindowUnit) {
            this.timeWindowUnit = timeWindowUnit;
        }


        public void setConfigurationFileUrl(URL url) {
            configurationUrl = url;
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("SegmentationServerPluginConfigurationImpl");
            sb.append("{maxAnalyzerAgents=").append(maxAnalyzerAgents);
            sb.append(", maxDeleteAgents=").append(maxDeleteAgents);
            sb.append(", maxPaginatorAgents=").append(maxPaginatorAgents);
            sb.append(", maxCalculatorAgents=").append(maxCalculatorAgents);
            sb.append(", maxSegmentationJobAgents=").append(maxSegmentationJobAgents);
            sb.append(", timeWindow=").append(timeWindowValue).append(' ').append(timeWindowUnit);
            sb.append('}');
            return sb.toString();
        }


        public long getTimeWindowValue() {
            return timeWindowValue;
        }


        public TimeUnit getTimeWindowUnit() {
            return timeWindowUnit;
        }
    }

    private static class ParticipantAgent extends Agent {
        private final SegmentationParticipant segmentationParticipant;


        private ParticipantAgent(SegmentationParticipant segmentationParticipant) {
            this.segmentationParticipant = segmentationParticipant;
        }


        @Override
        protected void setup() {
            addBehaviour(segmentationParticipant.toBehaviour());
        }


        @Override
        protected void tearDown() {
        }
    }
    private interface ParticipantBuilder {
        SegmentationParticipant createParticipant();
    }
}
