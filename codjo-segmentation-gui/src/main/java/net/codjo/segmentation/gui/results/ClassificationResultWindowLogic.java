/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.results;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.RequestComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
/**
 * Classe permettant de Manipuler les listener sur ClassificationResultWindowGui.
 */
public class ClassificationResultWindowLogic {
    private final ClassificationResultWindowGui gui;
    private GuiContext guiContext;
    private FilterActionListener filterActionListener = new FilterActionListener();


    public ClassificationResultWindowLogic(GuiContext guiContext)
          throws Exception {
        this.guiContext = guiContext;
        gui = new ClassificationResultWindowGui(guiContext);
        gui.getAnomalyFilter().addActionListener(filterActionListener);
        gui.getAxeFilter().addActionListener(filterActionListener);

        RequestComboBox combo = gui.getAxeFilter();
        combo.setColumns(new String[]{"classificationId", "classificationName", "classificationType"});
        combo.setRendererFieldName("classificationName");
        combo.setModelFieldName("classificationId");
        combo.setSelectFactoryId("selectAllClassification");
        combo.load();
    }


    protected GuiContext getGuiContext() {
        return guiContext;
    }


    protected void addCustomFilterField(String label, JTextField component) {
        component.addActionListener(filterActionListener);
        gui.addCustomField(label, component);
    }


    public ClassificationResultWindowGui getGui() {
        return gui;
    }


    protected void reloadRequestTable() throws RequestException {
        Row selectedRow = gui.getAxeFilter().getDataSource().getSelectedRow();
        if (selectedRow == null) {
            gui.getClassificationResultTable().getDataSource().clear();
        }
    }




    private class FilterActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                reloadRequestTable();
            }
            catch (RequestException e1) {
                ErrorDialog.show(guiContext.getDesktopPane(), "Erreur de load", e1);
            }
        }
    }
}
