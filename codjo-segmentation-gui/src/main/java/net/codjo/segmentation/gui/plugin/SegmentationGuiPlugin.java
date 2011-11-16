package net.codjo.segmentation.gui.plugin;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.i18n.common.Language;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.mad.gui.base.GuiConfiguration;
import net.codjo.mad.gui.framework.MutableGuiContext;
import net.codjo.mad.gui.i18n.AbstractInternationalizableGuiPlugin;
import net.codjo.mad.gui.plugin.MadGuiPlugin;
import net.codjo.mad.gui.request.Preference;
import static net.codjo.mad.gui.request.PreferenceFactory.addPreference;
import static net.codjo.mad.gui.request.PreferenceFactory.containsPreferenceId;
import static net.codjo.mad.gui.request.PreferenceFactory.getPreference;
import net.codjo.plugin.batch.BatchException;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.segmentation.gui.SegmentationGuiOperations;
import net.codjo.segmentation.gui.exportParam.ExportParametersAction;
import net.codjo.segmentation.gui.importParam.ImportParametersAction;
import net.codjo.segmentation.gui.results.SegmentationResultAction;
import net.codjo.segmentation.gui.settings.ClassificationSettingsAction;
import net.codjo.segmentation.gui.wizard.ClassificationWizardAction;
import net.codjo.workflow.common.schedule.ScheduleLauncher;
import net.codjo.workflow.common.subscribe.JobEventHandler;
import net.codjo.workflow.gui.plugin.WorkflowGuiPlugin;
import java.util.Map;
import javax.swing.ImageIcon;
import org.xml.sax.InputSource;

public class SegmentationGuiPlugin extends AbstractInternationalizableGuiPlugin {
    public static final String JOB_TYPE = SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE;
    private static final String SETTINGS_ACTION = "ClassificationSettingsAction";
    private static final String WIZARD_ACTION = "ClassificationWizardAction";
    private static final String RESULTS_ACTION = "ClassificationResultsAction";
    private static final String IMPORT_PARAMETERS_ACTION = "ImportParametersAction";
    private static final String EXPORT_PARAMETERS_ACTION = "ExportParametersAction";
    private static final String CLASSIFICATION_WINDOW_PREFERENCE_ID = "ClassificationWindow";
    private static final String CLASS_STRUCT_WINDOW_PREFERENCE_ID = "ClassificationStructureWindow";
    private static final String CLASS_WIZARD_WINDOW_PREFERENCE_ID = "ClassificationWizardWindow";
    private static final String PREFERENCE_FILE_NAME = "segmentationPreference.xml";
    public static final String SEGMENTATION_GUI_PLUGIN_OPERATIONS = "SegmentationGuiOperations";
    public static final String SEGMENTATION_AXIS_TREE_MAX_DEPTH = "AxisTreeMaximumDepth";
    private SegmentationGuiPluginConfiguration configuration = new SegmentationGuiPluginConfiguration();
    private SegmentationGuiOperationsImpl operations = new SegmentationGuiOperationsImpl();
    private final ApplicationCore applicationCore;
    private final MadGuiPlugin madGuiPlugin;
    private AgentContainer container;
    private static final String MISSING_ANOMALY_ERROR
          = "Vous devez fournir la préférence d'affichage des anomalies (segmentationGuiPlugin.getConfiguration().setClassificationAnomalyPreferenceId())";


    public SegmentationGuiPlugin(ApplicationCore pluginManager,
                                 MadGuiPlugin madGuiPlugin,
                                 WorkflowGuiPlugin workflowGuiPlugin) {
        this.applicationCore = pluginManager;
        this.madGuiPlugin = madGuiPlugin;
        workflowGuiPlugin.getConfiguration()
              .setTaskManagerJobIcon(SegmentationJobRequest.SEGMENTATION_REQUEST_TYPE,
                                     new ImageIcon(getClass().getResource("/images/job.segmentation.png")));
    }


    @Override
    protected void registerLanguageBundles(TranslationManager translationManager) {
        translationManager.addBundle("net.codjo.segmentation.gui.i18n", Language.FR);
        translationManager.addBundle("net.codjo.segmentation.gui.i18n", Language.EN);
    }


    @Override
    public void initGui(GuiConfiguration guiConfiguration) throws Exception {
        super.initGui(guiConfiguration);
        loadPreferences();
        guiConfiguration.getGuiContext().putProperty(SEGMENTATION_GUI_PLUGIN_OPERATIONS, getOperations());
        guiConfiguration.getGuiContext()
              .putProperty(SEGMENTATION_AXIS_TREE_MAX_DEPTH, configuration.getMaximumNodeDepth());
        registerActions(guiConfiguration, guiConfiguration.getGuiContext());
    }


    private void registerActions(GuiConfiguration guiConfiguration, MutableGuiContext guiContext)
          throws Exception {
        String applicationPreferenceId = configuration.getClassificationPreferenceId();

        extendPreference(getPreference(CLASSIFICATION_WINDOW_PREFERENCE_ID), applicationPreferenceId);

        ClassificationSettingsAction classificationSettingsAction
              = new ClassificationSettingsAction(guiContext, CLASSIFICATION_WINDOW_PREFERENCE_ID,
                                                 configuration.getClassificationListCustomizer(),
                                                 configuration.getSettingsCustomizer());

        guiConfiguration.registerAction(this, SETTINGS_ACTION, classificationSettingsAction);

        String applicationWizardPreferenceId = configuration.getClassificationWizardPreferenceId();
        extendPreference(getPreference(CLASS_WIZARD_WINDOW_PREFERENCE_ID), applicationWizardPreferenceId);

        String postSegmentationTreatment = configuration.getPostSegmentationTreatment();
        String anomalyPreferenceId = configuration.getClassificationAnomalyPreferenceId();
        if (anomalyPreferenceId == null) {
            throw new Exception(MISSING_ANOMALY_ERROR);
        }

        if (applicationWizardPreferenceId == null) {
            applicationWizardPreferenceId = CLASS_WIZARD_WINDOW_PREFERENCE_ID;
        }

        ClassificationWizardAction wizardAction =
              new ClassificationWizardAction(guiContext,
                                             applicationWizardPreferenceId,
                                             anomalyPreferenceId,
                                             getConfiguration().getAnomalyLogWindowCustomizer(),
                                             getConfiguration().getWizardCustomizer(),
                                             postSegmentationTreatment);
        guiConfiguration.registerAction(this, WIZARD_ACTION, wizardAction);

        SegmentationResultAction resultsAction =
              new SegmentationResultAction(guiContext, getConfiguration().getResultCustomizer());
        guiConfiguration.registerAction(this, RESULTS_ACTION, resultsAction);

        ImportParametersAction importParametersAction =
              new ImportParametersAction(guiContext,
                                         applicationCore.getAgentContainer(),
                                         applicationCore.getGlobalComponent(UserId.class));
        guiConfiguration.registerAction(this, IMPORT_PARAMETERS_ACTION, importParametersAction);

        ExportParametersAction exportParametersAction =
              new ExportParametersAction(guiContext,
                                         applicationCore.getAgentContainer(),
                                         applicationCore.getGlobalComponent(UserId.class));
        guiConfiguration.registerAction(this, EXPORT_PARAMETERS_ACTION, exportParametersAction);
    }


    private void extendPreference(Preference libPreference, String applicationPreferenceId) {
        if (applicationPreferenceId != null) {
            Preference applicationPreference = getPreference(applicationPreferenceId);
            libPreference.getColumns().addAll(applicationPreference.getColumns());
            libPreference.getHiddenColumns().addAll(applicationPreference.getHiddenColumns());

            if (applicationPreference.getDetailWindowClass() != null) {
                libPreference.setDetailWindowClass(applicationPreference.getDetailWindowClass());
            }
            if (applicationPreference.getDelete() != null) {
                libPreference.setDelete(applicationPreference.getDelete());
            }
            if (applicationPreference.getInsert() != null) {
                libPreference.setInsert(applicationPreference.getInsert());
            }
            if (applicationPreference.getUpdate() != null) {
                libPreference.setUpdate(applicationPreference.getUpdate());
            }
            if (applicationPreference.getSelectAll() != null) {
                libPreference.setSelectAll(applicationPreference.getSelectAll());
            }
            if (applicationPreference.getSelectByPk() != null) {
                libPreference.setSelectByPk(applicationPreference.getSelectByPk());
            }
            addPreference(libPreference);
        }
    }


    private void loadPreferences() {
        if (containsPreferenceId(CLASSIFICATION_WINDOW_PREFERENCE_ID)
            && containsPreferenceId(CLASS_STRUCT_WINDOW_PREFERENCE_ID)) {
            return;
        }
        InputSource inputSource = new InputSource(getClass().getResourceAsStream(PREFERENCE_FILE_NAME));
        madGuiPlugin.getConfiguration().addPreferenceMapping(inputSource);
    }


    @Override
    public void start(AgentContainer agentContainer)
          throws Exception {
        super.start(agentContainer);
        this.container = agentContainer;
    }


    public SegmentationGuiOperations getOperations() {
        return operations;
    }


    public SegmentationGuiPluginConfiguration getConfiguration() {
        return configuration;
    }


    private class SegmentationGuiOperationsImpl implements SegmentationGuiOperations {
        public void startSegmentation(Map<String, String> parameters)
              throws BatchException, ContainerFailureException {
            SegmentationJobRequest request = new SegmentationJobRequest();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                request.putParameter(entry.getKey(), entry.getValue());
            }

            startSegmentation(request, null);
        }


        public void startSegmentation(SegmentationJobRequest request, JobEventHandler listener)
              throws BatchException, ContainerFailureException {
            ScheduleLauncher launcher = new ScheduleLauncher(getUserId());
            launcher.setWorkflowConfiguration(configuration.getWorkflowConfiguration());
            if (listener != null) {
                launcher.setJobEventHandler(listener);
            }

            launcher.executeWorkflow(container, request.toRequest());
        }


        private UserId getUserId() {
            return applicationCore.getGlobalComponent(UserId.class);
        }
    }

    public enum FieldType {
        BOOLEAN,
        TEXT;
    }
}
