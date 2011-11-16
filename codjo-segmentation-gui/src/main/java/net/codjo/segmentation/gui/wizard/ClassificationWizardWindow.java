/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.wizard;
import net.codjo.gui.toolkit.wizard.Wizard;
import net.codjo.gui.toolkit.wizard.WizardPanel;
import net.codjo.gui.toolkit.LabelledItemPanel;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.RequestTable;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
/**
 * Created by IntelliJ IDEA. User: LAJMI Date: 14 mai 2004 Time: 17:50:07 To change this template use File |
 * Settings | File Templates.
 */
public class ClassificationWizardWindow extends JInternalFrame {
    private Wizard wizard = new MyWizard();
    private WizardPanel wizardPanel = new WizardPanel();
    private GuiContext context;
    private final String preferenceId;
    private final String anomalyPreferenceId;
    private final String postSegmentationTreatment;
    ClassificationStep classificationStep;
    ProgressStep progressStep;
    private final AnomalyLogWindowCustomizer anomalyLogWindowCustomizer;


    public ClassificationWizardWindow(GuiContext context,
                                      String label,
                                      String preferenceId,
                                      String anomalyPreferenceId,
                                      AnomalyLogWindowCustomizer anomalyLogWindowCustomizer,
                                      String postSegmentationTreatment) {
        super(label, true, true, true, true);
        this.context = context;
        this.preferenceId = preferenceId;
        this.anomalyPreferenceId = anomalyPreferenceId;
        this.postSegmentationTreatment = postSegmentationTreatment;
        this.anomalyLogWindowCustomizer=anomalyLogWindowCustomizer;
        this.setContentPane(wizardPanel);
        addCloseListener();

        initWizardStep();

        wizardPanel.setWizard(wizard);
        setPreferredSize(new Dimension(600, 540));
    }


    private void addCloseListener() {
        wizardPanel.getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
    }


    private void initWizardStep() {
        classificationStep = new ClassificationStep(preferenceId);
        wizard.addStep(classificationStep);
        progressStep = new ProgressStep(context, anomalyPreferenceId, postSegmentationTreatment,anomalyLogWindowCustomizer);
        wizard.setFinalStep(progressStep);
    }


    public LabelledItemPanel getMainPanel() {
        return classificationStep.mainPanel;
    }


    public RequestTable getClassificationTable() {
        return classificationStep.getClassificationTable();
    }


    public void addCustomField(String label,
                               JTextField newComponent,
                               ComponentValidator updater,
                               SegmentationJobRequestFiller requestFiller) {
        classificationStep.addCustomField(label, newComponent, updater);
        progressStep.addRequestFiller(requestFiller);
    }


    public void setInfoField(String label) {
        classificationStep.setInfoField(label);
    }


    public void addListSelectionListener(ListSelectionListener listener) {
        classificationStep.addListSelectionListener(listener);
    }


    public Row[] getClassificationTableSelectedRows() {
        return classificationStep.getClassificationTableSelectedRows();
    }


    public GuiContext getGuiContext() {
        return context;
    }


    public interface SegmentationJobRequestFiller {
        void fillRequest(Map<String, String> mapParameters);
    }

    public interface ComponentValidator {
        boolean isComponentValid();
    }

    private class MyWizard extends Wizard {

        @Override
        protected boolean canGoPrevious() {
            return isWizardFinished();
        }


        @Override
        public void previousStep() {
            setWizardFinished(false);
            wizardPanel.addStepStatePropertyListenerToApplyButton();
            ClassificationStep step = (ClassificationStep)getCurrentStep();
            wizardPanel.repaintStep(step);
            fireStepStatePropertyChange(step.isFulfilled());
        }
    }
}
