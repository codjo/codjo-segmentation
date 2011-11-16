/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.Field;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.DefaultGuiContext;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.security.common.api.UserMock;
import java.util.ArrayList;
import junit.framework.TestCase;
/**
 * Classe de test de {@link AxisTreeModel}.
 */
public class AxisTreeModelTest extends TestCase {
    private AxisTreeModel axisTreeModel;
    private DetailDataSource fatherDataSource;
    private ListDataSource sonDataSource;
    private Row sonRow1;
    private Row sonRow3;
    private Row sonRow2;
    private Row sonRow4;
    private Row sonRow5;
    private Row sonRow6;
    private AxisTree gui;


    public void test_findDoubleSleeveName() throws Exception {
        gui.setSelectionRow(1);
        assertNull(axisTreeModel.findDoubleSleeveName());
        axisTreeModel.addNewSleeve(true);
        assertNull(axisTreeModel.findDoubleSleeveName());
        axisTreeModel.addNewSleeve(true);
        assertNotNull(axisTreeModel.findDoubleSleeveName());
    }


    @Override
    protected void setUp() throws Exception {
        DefaultGuiContext defaultGuiContext = new DefaultGuiContext();
        defaultGuiContext.setUser(new UserMock());

        fatherDataSource = new DetailDataSource(defaultGuiContext);
        fatherDataSource.declare("classificationName");
        fatherDataSource.declare("classificationId");
        fatherDataSource.setFieldValue("classificationName", "root");
        fatherDataSource.setFieldValue("classificationId", "1");

        sonDataSource = new ListDataSource();
        sonDataSource.setColumns(new String[]{
              "sleeveId", "sleeveRowId", "sleeveCode", "sleeveName", "formula",
              "terminalElement", "sleeveDustbin"
        });

        Result result = new Result();
        sonRow1 = buildRow("01-1", "1");
        result.addRow(sonRow1);
        sonRow2 = buildRow("02-1.1", "11");
        result.addRow(sonRow2);
        sonRow3 = buildRow("01-2", "2");
        result.addRow(sonRow3);
        sonRow4 = buildRow("01-3", "3");
        result.addRow(sonRow4);
        sonRow5 = buildRow("02-3.1", "31");
        result.addRow(sonRow5);
        sonRow6 = buildRow("03-3.1.1", "311");
        result.addRow(sonRow6);

        sonDataSource.setLoadResult(result);

        gui = new AxisTree();
        gui.setClassificationStructureGui(new ClassificationStructureGui("test"));
        axisTreeModel = new AxisTreeModel(fatherDataSource, sonDataSource, gui);
        gui.setModel(axisTreeModel);
    }


    private Row buildRow(String code, String name) {
        Row row = new Row();
        ArrayList fields = new ArrayList();
        fields.add(new Field("sleeveId", name));
        fields.add(new Field("sleeveRowId", name));
        fields.add(new Field("sleeveCode", code));
        fields.add(new Field("sleeveName", name));
        fields.add(new Field("formula", name));
        fields.add(new Field("terminalElement", name));
        fields.add(new Field("sleeveDustbin", name));
        row.setFields(fields);
        return row;
    }
}
