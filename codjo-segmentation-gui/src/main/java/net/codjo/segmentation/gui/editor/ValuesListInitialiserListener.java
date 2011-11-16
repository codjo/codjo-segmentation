package net.codjo.segmentation.gui.editor;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import net.codjo.mad.client.request.RequestException;
/**
 *
 */
public class ValuesListInitialiserListener implements ListSelectionListener {
    private ValueListPanel valueListPanel;


    public ValuesListInitialiserListener(ValueListPanel valueListPanel){
        this.valueListPanel = valueListPanel;
    }


    public void valueChanged(ListSelectionEvent event) {
        valueListPanel.getList().setListData(new String[]{});
        valueListPanel.validate();
    }
}
