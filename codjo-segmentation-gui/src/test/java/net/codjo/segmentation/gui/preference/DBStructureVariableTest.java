/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.gui.preference.DBStructureFilter}.
 */
public class DBStructureVariableTest extends TestCase {
    private DBStructureVariable structureVar;


    protected void setUp() throws Exception {
        structureVar = new DBStructureVariable();
    }


    public void test_getColums() throws Exception {
        structureVar.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public List getColumnsFor(String tableName) {
                List columnsList = new ArrayList();
                columnsList.add(tableName + ".COL1");
                columnsList.add(tableName + ".COL2");
                return columnsList;
            }
        });

        List colList = structureVar.getColumnsFor("AP_TOTO");
        assertEquals(2, colList.size());
        assertEquals("AP_TOTO.COL1", colList.get(0));
        assertEquals("AP_TOTO.COL2", colList.get(1));
    }


    public void test_getColumnLabelFor() throws Exception {
        structureVar.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public String getColumnLabelFor(String tableName, String sqlField) {
                return "logic " + tableName + " " + sqlField;
            }
        });

        assertEquals("logic AP_TOTO COL1",
                     structureVar.getColumnLabelFor("AP_TOTO", "COL1"));
    }


    public void test_getColumnLabelFor_var() throws Exception {
        structureVar.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure());

        structureVar.addVariable("VAR_1", "label");

        List colList = structureVar.getColumnsFor(DBStructureVariable.VAR_TABLE);
        assertEquals(1, colList.size());

        assertEquals("label",
                     structureVar.getColumnLabelFor(DBStructureVariable.VAR_TABLE, "VAR_1"));
        assertEquals("var_unknown",
                     structureVar.getColumnLabelFor(DBStructureVariable.VAR_TABLE, "var_unknown"));
    }


    public void test_getVariables() throws Exception {
        structureVar.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure());

        structureVar.addVariable("VAR_1", "label 0");
        structureVar.addVariable("VAR_1", "label 1");

        List colList = structureVar.getColumnsFor(DBStructureVariable.VAR_TABLE);

        assertEquals(1, colList.size());
        assertEquals("VAR_1", colList.get(0));
    }


    public void test_getVariables_list() throws Exception {
        structureVar.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure());

        structureVar.addVariable("VAR_1", "label VAR_1");
        structureVar.addVariable("VAR_2", "label VAR_2");

        List colList = structureVar.getColumnsFor(DBStructureVariable.VAR_TABLE);
        Collections.sort(colList);
        assertEquals(2, colList.size());
        assertEquals("VAR_1", colList.get(0));
        assertEquals("VAR_2", colList.get(1));
    }
}
