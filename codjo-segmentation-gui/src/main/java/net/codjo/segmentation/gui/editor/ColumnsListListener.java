package net.codjo.segmentation.gui.editor;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.factory.SelectFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JList;
import org.apache.log4j.Logger;
/**
 * Listener Coté roses qui va écouter la selection des colonnes.
 *
 * @version $Revision: 1.2 $
 */
public class ColumnsListListener implements ActionListener {

    ValueListPanel valueListPanel;
    WaitingPanel waitingPanel;
    JList fieldListPanel;
    Map fieldsMap;
    Map<String, String[]> cacheValuesListMap = new HashMap<String, String[]>();
    Logger log = Logger.getLogger(ColumnsListListener.class);


    public ColumnsListListener(ValueListPanel valueListPanel,
                               Map fieldsMap,
                               JList fieldListPanel,
                               WaitingPanel waitingPanel) {
        this.waitingPanel = waitingPanel;
        this.valueListPanel = valueListPanel;
        this.fieldListPanel = fieldListPanel;
        this.fieldsMap = fieldsMap;
    }


    public void actionPerformed(ActionEvent event) {
        if (fieldListPanel.getSelectedIndex() != -1) {
            waitingPanel.exec(new Runnable() {
                public void run() {
                    String value = (String)fieldListPanel.getModel()
                          .getElementAt(fieldListPanel.getSelectedIndex());

                    try {
                        String[] lov = loadValuesList(getKey(value));
                        valueListPanel.getList().setListData(lov);
                    }
                    catch (RequestException ex) {
                        log.error(ex);
                    }

                    valueListPanel.validate();
                }
            });
        }
    }


    private String[] loadValuesList(String tableColumn)
          throws RequestException {
        String[] cachedList = cacheValuesListMap.get(tableColumn);

        if (null != cachedList) {
            return cachedList;
        }
        else {
            if ("VAR".equals(tableColumn.substring(0, 3))) {
                return new String[]{};
            }
            int indexSeparator = tableColumn.indexOf("$");
            String table = tableColumn.substring(4, indexSeparator);
            String column = tableColumn.substring(indexSeparator + 1);

            ListDataSource listDataSource = new ListDataSource();
            listDataSource.setColumns(new String[]{"value"});
            SelectFactory loadFactory = new SelectFactory("selectValuesForFieldName");
            FieldsList fieldList = new FieldsList();
            fieldList.addField("tableName", table);
            fieldList.addField("columnName", column);
            loadFactory.init(fieldList);
            listDataSource.setLoadFactory(loadFactory);

            listDataSource.load();
            int rowCount = listDataSource.getLoadResult().getRowCount();
            String[] values = new String[rowCount];
            for (int i = 0; i < rowCount; i++) {
                values[i] = listDataSource.getLoadResult().getValue(i, "value");
            }

            cacheValuesListMap.put(tableColumn, values);
            return values;
        }
    }


    private String getKey(String value) {
        for (Object obj : fieldsMap.keySet()) {
            String key = (String)obj;
            if (value.equals(fieldsMap.get(key))) {
                return key;
            }
        }
        return null;
    }
}
