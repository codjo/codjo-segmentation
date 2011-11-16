/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.gui.preference.DBStructureFilter}.
 */
public class DBStructureFilterTest extends TestCase {
    private DBStructureFilter filter;


    protected void setUp() throws Exception {
        filter = new DBStructureFilter();
    }


    public void test_getColums() throws Exception {
        filter.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public List getColumnsFor(String tableName) {
                List columnsList = new ArrayList();
                columnsList.add(tableName + ".COL1");
                columnsList.add(tableName + ".COL2");
                return columnsList;
            }
        });

        List colList = filter.getColumnsFor("AP_TOTO");
        assertEquals(2, colList.size());
        assertEquals("AP_TOTO.COL1", colList.get(0));
        assertEquals("AP_TOTO.COL2", colList.get(1));
    }


    public void test_getFilteredColums() throws Exception {
        filter.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public List getColumnsFor(String tableName) {
                List columnsList = new ArrayList();
                columnsList.add(tableName + ".COL1");
                columnsList.add(tableName + ".COL2");
                return columnsList;
            }
        });

        filter.setFilter("AP_TOTO", "AP_TOTO.COL1");

        List colList = filter.getColumnsFor("AP_TOTO");

        assertEquals(1, colList.size());
        assertEquals("AP_TOTO.COL2", colList.get(0));
    }


    public void test_getFilteredColums_all() throws Exception {
        filter.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public List getColumnsFor(String tableName) {
                List columnsList = new ArrayList();
                columnsList.add(tableName + ".COL1");
                columnsList.add(tableName + ".COL2");
                return columnsList;
            }
        });

        List excludedColumns = new ArrayList();
        excludedColumns.add("AP_TOTO.COL1");
        excludedColumns.add("AP_TOTO.COL2");

        filter.setFilter("AP_TOTO", excludedColumns);

        List colList = filter.getColumnsFor("AP_TOTO");

        assertEquals(0, colList.size());
    }


    public void test_getFilteredColums_add() throws Exception {
        filter.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public List getColumnsFor(String tableName) {
                List columnsList = new ArrayList();
                columnsList.add(tableName + ".COL1");
                columnsList.add(tableName + ".COL2");
                return columnsList;
            }
        });

        filter.addFilter("AP_TOTO", "AP_TOTO.COL1");
        filter.addFilter("AP_TOTO", "AP_TOTO.COL2");

        List colList = filter.getColumnsFor("AP_TOTO");

        assertEquals(0, colList.size());
    }


    public void test_getColumnLabelFor() throws Exception {
        filter.setSubDBStructure(new PreferenceGuiTest.FakeDBStructure() {
            public String getColumnLabelFor(String tableName, String sqlField) {
                return "logic " + tableName + " " + sqlField;
            }
        });

        assertEquals("logic AP_TOTO COL1", filter.getColumnLabelFor("AP_TOTO", "COL1"));
    }
}
