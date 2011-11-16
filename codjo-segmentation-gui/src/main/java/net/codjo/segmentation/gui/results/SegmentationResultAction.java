/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.results;
import net.codjo.mad.gui.framework.AbstractAction;
import net.codjo.mad.gui.framework.GuiContext;
import javax.swing.JInternalFrame;
/**
 * Action pour afficher SegmentationResultWindowGui
 */
public class SegmentationResultAction extends AbstractAction {
    JInternalFrame frame;
    private static final String LABEL = "Résultat de la segmentation";
    private static final String TOOLTIP = "Affiche le résultat de la segmentation";
    private final SegmentationResultCustomizer customizer;


    public SegmentationResultAction(GuiContext ctxt, SegmentationResultCustomizer customizer) {
        super(ctxt, LABEL, TOOLTIP);
        this.customizer = customizer;
    }


    @Override
    protected JInternalFrame buildFrame(GuiContext guiContext) throws Exception {
        SegmentationResultWindowLogic logic = new SegmentationResultWindowLogic(guiContext, LABEL);
        if (customizer != null) {
            customizer.customizeWindow(logic);
        }
        return logic.getGui();
    }
}
