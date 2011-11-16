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
 * Action pour afficher ClassificationResultWindowGui
 */
public class ClassificationResultAction extends AbstractAction {
    JInternalFrame frame;
    private static final String LABEL = "Résultat de la segmentation";
    private static final String TOOLTIP = "Affiche le résultat de la segmentation";


    public ClassificationResultAction(GuiContext ctxt) {
        super(ctxt, LABEL, TOOLTIP);
    }


    @Override
    protected JInternalFrame buildFrame(GuiContext guiContext) throws Exception {
        return new ClassificationResultWindowLogic(guiContext).getGui();
    }
}
