/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.wizard;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.SimpleListGui;
import net.codjo.mad.gui.request.PreferenceFactory;
/**
 */
public class LogWindowLogic {
    private final LogWindowGui gui;
    private final String preferenceId;
    private GuiContext guiContext;


    public LogWindowLogic(GuiContext guiContext, Result anomalieResult, String preferenceId)
          throws Exception {
        this.preferenceId = preferenceId;
        this.guiContext = guiContext;
        gui = new LogWindowGui("Anomalies de segmentation");
        loadTable(anomalieResult);
    }


    public SimpleListGui getGui() {
        return gui;
    }


    void loadTable(Result anomalieResult) throws RequestException {
        gui.init(guiContext, PreferenceFactory.getPreference(preferenceId));
        gui.getTable().setLoadResult(anomalieResult);
    }


    public GuiContext getGuiContext() {
        return guiContext;
    }
}
