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
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link AxisTreeLogic}.
 */
public class AxisTreeLogicTest extends TestCase {
    private AxisTreeLogic axisTreeLogic;
    private ListDataSource sonDataSource;
    private Row sonRow1;
    private Row sonRow3;
    private Row sonRow2;
    private Row sonRow4;
    private Row sonRow5;
    private Row sonRow6;


    /**
     * Test permettant de vérifier la mise à jour du ListDataSource après sélection d'un élément de l'arbre.
     *
     * @throws Exception
     */
    public void test_selection() throws Exception {
        final AxisTree gui = axisTreeLogic.getGui();
        gui.expandRow(0);
        gui.expandRow(1);
        assertNull(sonDataSource.getSelectedRow());

        gui.setSelectionRow(0);
        assertNull(sonDataSource.getSelectedRow());

        gui.setSelectionRow(1);
        assertEquals(sonRow1, sonDataSource.getSelectedRow());

        axisTreeLogic.getGui().setSelectionRow(0);
        assertNull(sonDataSource.getSelectedRow());

        gui.setSelectionRow(2);
        assertEquals(sonRow2, sonDataSource.getSelectedRow());

        gui.setSelectionRow(3);
        assertEquals(sonRow3, sonDataSource.getSelectedRow());
    }


    public void test_addSleeve() throws Exception {
        axisTreeLogic.getGui().setSelectionRow(1);
        assertEquals(6, sonDataSource.getRowCount());
        axisTreeLogic.addSleeve();
        assertEquals(7, sonDataSource.getRowCount());
    }


    public void test_addFolder() throws Exception {
        axisTreeLogic.getGui().setSelectionRow(1);
        assertEquals(6, sonDataSource.getRowCount());
        axisTreeLogic.addFolder();
        assertEquals(7, sonDataSource.getRowCount());
    }


    public void test_delete() throws Exception {
        final AxisTree gui = axisTreeLogic.getGui();
        gui.expandRow(0);
        gui.expandRow(3);
        gui.expandRow(1);

        axisTreeLogic.getGui().setSelectionRow(3);
        assertEquals(6, sonDataSource.getRowCount());
        axisTreeLogic.delete();
        assertEquals(sonRow3, sonDataSource.getRemovedRow()[0]);
        assertEquals(5, sonDataSource.getRowCount());
        assertEquals("01-2", sonRow4.getFieldValue("sleeveCode"));
        assertEquals("02-2.1", sonRow5.getFieldValue("sleeveCode"));
        assertEquals("03-2.1.1", sonRow6.getFieldValue("sleeveCode"));

        axisTreeLogic.getGui().setSelectionRow(1);
        axisTreeLogic.delete();
        assertEquals(3, sonDataSource.getRowCount());

        axisTreeLogic.getGui().setSelectionRow(0);
        axisTreeLogic.delete();
        assertEquals(0, sonDataSource.getRowCount());
    }


    @Override
    protected void setUp() throws Exception {
        DefaultGuiContext defaultGuiContext = new DefaultGuiContext();
        defaultGuiContext.setUser(new UserMock());

        DetailDataSource fatherDataSource = new DetailDataSource(defaultGuiContext);
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

        AxisTree axisTree = new AxisTree();
        axisTree.setClassificationStructureGui(new ClassificationStructureGui("test"));
        axisTreeLogic =
              new AxisTreeLogic(fatherDataSource, sonDataSource, axisTree);
    }


    private Row buildRow(String code, String name) {
        Row row = new Row();
        List fields = new ArrayList();
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
