package net.codjo.segmentation.gui.importParam;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.DataSource;
import net.codjo.mad.gui.request.RequestTable;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.SwingUtilities;

public class ClearLogAction extends AbstractGuiAction {
    private static final String CLEAR_LOG_ACTION = ".clearLogAction";
    private DataSource datasource;


    public ClearLogAction(GuiContext ctxt, RequestTable table) {
        super(ctxt, "", "", "clearLog.gif");
        putValue(Action.NAME, table.getName() + CLEAR_LOG_ACTION);
        this.datasource = table.getDataSource();
    }


    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(
              new Runnable() {
                  public void run() {
                      datasource.clear();
                  }
              }
        );
    }
}
