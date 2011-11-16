package net.codjo.segmentation.gui.plugin;
import net.codjo.segmentation.gui.results.SegmentationResultCustomizer;
import net.codjo.segmentation.gui.settings.ClassificationListCustomizer;
import net.codjo.segmentation.gui.settings.SegmentationSettingsCustomizer;
import net.codjo.segmentation.gui.wizard.AnomalyLogWindowCustomizer;
import net.codjo.segmentation.gui.wizard.SegmentationWizardCustomizer;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;
/**
 *
 */
public class SegmentationGuiPluginConfiguration {
    private String classificationPreferenceId;
    private String classificationAnomalyPreferenceId;
    private String classificationWizardPreferenceId;
    private String postSegmentationTreatment;

    private SegmentationWizardCustomizer wizardCustomizer;
    private ClassificationListCustomizer classificationListCustomizer;
    private SegmentationSettingsCustomizer settingsCustomizer;
    private SegmentationResultCustomizer resultCustomizer;
    private int maximumNodeDepth = 98;
    private AnomalyLogWindowCustomizer anomalyLogWindowCustomizer;
    private WorkflowConfiguration workflowConfiguration = new WorkflowConfiguration();


    public String getPostSegmentationTreatment() {
        return postSegmentationTreatment;
    }


    public void setPostSegmentationTreatment(String postSegmentationTreatment) {
        this.postSegmentationTreatment = postSegmentationTreatment;
    }


    public SegmentationResultCustomizer getResultCustomizer() {
        return resultCustomizer;
    }


    public void setResultCustomizer(SegmentationResultCustomizer resultCustomizer) {
        this.resultCustomizer = resultCustomizer;
    }


    public ClassificationListCustomizer getClassificationListCustomizer() {
        return classificationListCustomizer;
    }


    public void setClassificationListCustomizer(ClassificationListCustomizer customizer) {
        this.classificationListCustomizer = customizer;
    }


    public SegmentationSettingsCustomizer getSettingsCustomizer() {
        return settingsCustomizer;
    }


    public void setSettingsCustomizer(SegmentationSettingsCustomizer settingsCustomizer) {
        this.settingsCustomizer = settingsCustomizer;
    }


    public String getClassificationWizardPreferenceId() {
        return classificationWizardPreferenceId;
    }


    public void setClassificationWizardPreferenceId(String classificationWizardPreferenceId) {
        this.classificationWizardPreferenceId = classificationWizardPreferenceId;
    }


    public String getClassificationPreferenceId() {
        return classificationPreferenceId;
    }


    public void setClassificationPreferenceId(String classificationPreferenceId) {
        this.classificationPreferenceId = classificationPreferenceId;
    }


    public String getClassificationAnomalyPreferenceId() {
        return classificationAnomalyPreferenceId;
    }


    public void setClassificationAnomalyPreferenceId(String classificationAnomalyPreferenceId) {
        this.classificationAnomalyPreferenceId = classificationAnomalyPreferenceId;
    }


    public SegmentationWizardCustomizer getWizardCustomizer() {
        return wizardCustomizer;
    }


    public void setWizardCustomizer(SegmentationWizardCustomizer wizardCustomizer) {
        this.wizardCustomizer = wizardCustomizer;
    }


    public void setMaximumNodeDepth(int maxNodeDepth) {
        this.maximumNodeDepth = maxNodeDepth;
    }


    public int getMaximumNodeDepth() {
        return maximumNodeDepth;
    }


    public AnomalyLogWindowCustomizer getAnomalyLogWindowCustomizer() {
        return anomalyLogWindowCustomizer;
    }


    public void setAnomalyLogWindowCustomizer(AnomalyLogWindowCustomizer anomalyLogWindowCustomizer) {
        this.anomalyLogWindowCustomizer = anomalyLogWindowCustomizer;
    }


    public WorkflowConfiguration getWorkflowConfiguration() {
        return workflowConfiguration;
    }
}
