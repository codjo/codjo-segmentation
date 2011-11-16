/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.Request;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.ResultManager;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.factory.CommandFactory;
import net.codjo.mad.gui.request.factory.RequestFactory;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
/**
 * 
 */
public class CopyAction extends AbstractGuiAction {
    private static final String DUPLICATION_PK = "classificationId";
    private static final Logger APP = Logger.getLogger(CopyAction.class);
    private TableSelectionListener listener = new TableSelectionListener();
    private RequestTable table;

    public CopyAction(GuiContext ctxt, RequestTable table) {
        super(ctxt, "", "", "mad.pageCopy");
        setEnabled(false);
        this.table = table;
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(listener);
    }

    @Override
    protected boolean isActivable() {
        if (table == null) {
            return false;
        }
        ListSelectionModel lsm = table.getSelectionModel();
        return !lsm.isSelectionEmpty() && table.getPreference().getDelete() != null;
    }


    public void actionPerformed(ActionEvent event) {
        try {
            table.cancelAllEditors();
            sendCopyRequest();
        }
        catch (Exception ex) {
            APP.error("Erreur interne", ex);
            ErrorDialog.show(table, "erreur interne", ex);
        }
    }


    private Request buildCopyRowRequest(RequestFactory factory, Row row) {
        factory.init(getSelectedRowPkValues(row));
        return factory.buildRequest(null);
    }


    private FieldsList getSelectedRowPkValues(Row row) {
        FieldsList fields = new FieldsList();
        fields.addField(DUPLICATION_PK, row.getFieldValue(DUPLICATION_PK));
        return fields;
    }


    protected void sendCopyRequest() throws RequestException {
        CommandFactory factory = new CommandFactory("duplicateAxis");
        Row[] rows = table.getAllSelectedDataRows();

        List<Request> list = new ArrayList<Request>();
        for (Row row : rows) {
            list.add(buildCopyRowRequest(factory, row));
        }

        ResultManager result = table.getRequestSender().send(list.toArray(new Request[] {}));
        if (result.hasError()) {
            ErrorDialog.show(table, "Impossible de dupliquer la ligne",
                result.getErrorResult().getLabel() + "(" + result.getErrorResult().getType() + ")");
        }
        else {
            table.load();
        }
    }

    private class TableSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            setEnabled(true);
        }
    }
}
