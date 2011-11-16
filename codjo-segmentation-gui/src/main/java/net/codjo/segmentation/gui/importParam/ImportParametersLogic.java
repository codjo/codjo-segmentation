package net.codjo.segmentation.gui.importParam;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.UserId;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.Preference;
import static net.codjo.mad.gui.request.PreferenceFactory.getPreference;
import net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent;
import static net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION;
import static net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION_STRUCTURE;
import net.codjo.segmentation.gui.importParam.agent.ImportInitiatorAgent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ImportParametersLogic {
    private ImportParametersGui gui;
    private final GuiContext guiContext;
    private final AgentContainer agentContainer;
    private final UserId userId;


    public ImportParametersLogic(GuiContext guiContext, AgentContainer agentContainer, UserId userId) {
        this.guiContext = guiContext;
        this.agentContainer = agentContainer;
        this.userId = userId;

        Preference classification = getPreference("ImportClassificationParameters");
        Preference sleeve = getPreference("ImportSleeveParameters");

        gui = new ImportParametersGui(guiContext, classification, sleeve);
        gui.setImportButtonListener(new ImportListener());
        gui.setValidFileListener(new ValidFileListener());
    }


    public ImportParametersGui getGui() {
        return gui;
    }


    private class ImportListener implements ActionListener {
        @SuppressWarnings({"InnerClassTooDeeplyNested"})
        public void actionPerformed(ActionEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    requestFileImport();
                }
            });
        }


        private void requestFileImport() {
            try {
                gui.getWaitingPanel().startAnimation();

                File file = gui.getImportFilePathField().getFile();

                AbstractImportExportInitiatorAgent.TreatmentType type = CLASSIFICATION_STRUCTURE;
                if (gui.isClassificationSelected()) {
                    type = CLASSIFICATION;
                }
                ImportInitiatorAgent agent = new ImportInitiatorAgent(file, type, userId);
                agent.addTreatmentProgressListener(gui);
                agentContainer.acceptNewAgent("import-initiator", agent).start();
            }
            catch (Exception exception) {
                gui.getWaitingPanel().stopAnimation();
                ErrorDialog.show(guiContext.getDesktopPane(), exception.getLocalizedMessage(), exception);
            }
        }
    }

    private class ValidFileListener implements DocumentListener {
        public void insertUpdate(DocumentEvent event) {
            setEnabledButton();
        }


        public void removeUpdate(DocumentEvent event) {
            setEnabledButton();
        }


        public void changedUpdate(DocumentEvent event) {
            setEnabledButton();
        }


        private void setEnabledButton() {
            gui.setEnabledImportButton(gui.getImportFilePathField().isValid());
        }
    }
}
