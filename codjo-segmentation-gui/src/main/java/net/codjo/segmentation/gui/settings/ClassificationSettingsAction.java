/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.gui.framework.AbstractAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.MutableGuiContext;
import static net.codjo.segmentation.gui.settings.ClassificationListCustomizer.CLASSIFICICATION_LIST_CUSTOMIZER;
import static net.codjo.segmentation.gui.settings.SegmentationSettingsCustomizer.SEGMENTATION_SETTINGS_CUSTOMIZER;
import javax.swing.JInternalFrame;

public class ClassificationSettingsAction extends AbstractAction {
    protected static final String LABEL = "Axes d'analyse";
    private final String preferenceId;


    public ClassificationSettingsAction(MutableGuiContext context,
                                        String preferenceId,
                                        ClassificationListCustomizer classificationListCustomizer,
                                        SegmentationSettingsCustomizer settingsCustomizer) {
        super(context, "", "");
        this.preferenceId = preferenceId;
        context.putProperty(CLASSIFICICATION_LIST_CUSTOMIZER, classificationListCustomizer);
        context.putProperty(SEGMENTATION_SETTINGS_CUSTOMIZER, settingsCustomizer);
    }


    @Override
    protected JInternalFrame buildFrame(GuiContext guiContext) throws Exception {
        return new ClassificationListLogic(guiContext, LABEL, preferenceId).getGui();
    }
}
