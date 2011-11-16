package net.codjo.segmentation.gui.exportParam;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.UserId;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent;
import static net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION;
import static net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION_STRUCTURE;
import net.codjo.segmentation.gui.exportParam.agent.ExportInitiatorAgent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.SwingUtilities;
/**
 *
 */

public class ExportParametersLogic {
    private ExportParametersGui gui;
    private final GuiContext guiContext;
    private final AgentContainer agentContainer;
    private final UserId userId;


    public ExportParametersLogic(GuiContext guiContext, AgentContainer agentContainer, UserId userId) {
        this.guiContext = guiContext;
        this.agentContainer = agentContainer;
        this.userId = userId;
        gui = new ExportParametersGui();
        gui.setExportButtonListener(new ExportListener());
    }


    public ExportParametersGui getGui() {
        return gui;
    }


    private class ExportListener implements ActionListener {
        @SuppressWarnings({"InnerClassTooDeeplyNested"})
        public void actionPerformed(ActionEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    gui.getWaitingPanel().startAnimation();
                    requestFileExport();
                }
            });
        }


        private void requestFileExport() {
            try {
                File file = gui.getExportFile();
                AbstractImportExportInitiatorAgent.TreatmentType type = CLASSIFICATION_STRUCTURE;
                if (gui.isClassificationSelected()) {
                    type = CLASSIFICATION;
                }
                ExportInitiatorAgent agent = new ExportInitiatorAgent(file, type, userId);
                agent.addTreatmentProgressListener(gui);
                agentContainer.acceptNewAgent("export-initiator", agent).start();
            }
            catch (ContainerFailureException exception) {
                gui.getWaitingPanel().stopAnimation();
                ErrorDialog.show(guiContext.getDesktopPane(), exception.getLocalizedMessage(), exception);
            }
        }
    }
}
