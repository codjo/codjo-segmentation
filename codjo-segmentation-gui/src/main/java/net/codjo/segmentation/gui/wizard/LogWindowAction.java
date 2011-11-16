/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.wizard;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.SimpleListGui;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
/**
 * Action permettant d'afficher les erreurs de segmentation
 */
public class LogWindowAction extends AbstractGuiAction {
    private static final Logger APP = Logger.getLogger(ProgressStep.class);
    private Result anomalieResult;
    private final String preferenceId;
    private AnomalyLogWindowCustomizer customizer;


    public LogWindowAction(GuiContext ctxt,
                           Result anomalieResult,
                           String preferenceId,
                           AnomalyLogWindowCustomizer customizer) {
        super(ctxt, "Anomalies segmentation", "Anomalies de segmentation");
        this.anomalieResult = anomalieResult;
        this.preferenceId = preferenceId;
        this.customizer = customizer;
    }


    public void actionPerformed(ActionEvent event) {
        displayNewWindow();
    }


    private void displayNewWindow() {
        try {
            LogWindowLogic logic =
                  new LogWindowLogic(getGuiContext(), anomalieResult, preferenceId);

            if (customizer != null) {
                customizer.customizeWindow(logic);
            }
            SimpleListGui gui = logic.getGui();
            gui.setFrameIcon(UIManager.getIcon("icon"));
            getDesktopPane().add(gui);
            gui.pack();
            gui.setVisible(true);
            gui.setSelected(true);
        }
        catch (Exception ex) {
            String message =
                  "Erreur lors de l'affichage des résultats de la segmentation.";
            APP.error(message, ex);
            ErrorDialog.show(getDesktopPane(), message, ex);
        }
    }
}
