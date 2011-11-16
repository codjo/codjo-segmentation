package net.codjo.segmentation.gui.wizard;
import net.codjo.mad.gui.framework.AbstractAction;
import net.codjo.mad.gui.framework.GuiContext;
import javax.swing.JInternalFrame;
/**
 * Affichage d'une liste avec détail des axes de segmentation.
 */
public class ClassificationWizardAction extends AbstractAction {
    private static final String LABEL = "Assistant segmentation";
    private final String preferenceId;
    private final String anomalyPreferenceId;
    private final String postSegmentationTreatment;
    private final SegmentationWizardCustomizer segmentationWizardCustomizer;
    private final AnomalyLogWindowCustomizer anomalyLogWindowCustomizer;


    public ClassificationWizardAction(GuiContext guiContext,
                                      String preferenceId,
                                      String anomalyPreferenceId,
                                      AnomalyLogWindowCustomizer anomalyLogWindowCustomizer,
                                      SegmentationWizardCustomizer customizer,
                                      String postSegmentationTreatment) {
        super(guiContext, "", "");
        this.preferenceId = preferenceId;
        this.anomalyPreferenceId = anomalyPreferenceId;
        this.segmentationWizardCustomizer = customizer;
        this.anomalyLogWindowCustomizer = anomalyLogWindowCustomizer;
        this.postSegmentationTreatment = postSegmentationTreatment;
    }


    @Override
    protected JInternalFrame buildFrame(GuiContext guiContext) throws Exception {
        ClassificationWizardWindow window = new ClassificationWizardWindow(guiContext,
                                                                           LABEL,
                                                                           preferenceId,
                                                                           anomalyPreferenceId,
                                                                           anomalyLogWindowCustomizer,
                                                                           postSegmentationTreatment);
        if (segmentationWizardCustomizer != null) {
            segmentationWizardCustomizer.customizeWindow(window);
        }
        return window;
    }
}
