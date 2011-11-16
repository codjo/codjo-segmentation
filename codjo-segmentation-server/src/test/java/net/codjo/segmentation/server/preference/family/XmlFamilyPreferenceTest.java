/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.sql.builder.DefaultQueryConfig;
import net.codjo.sql.builder.JoinKeyExpression;
import net.codjo.sql.builder.QueryConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link XmlFamilyPreference}.
 */
public class XmlFamilyPreferenceTest extends TestCase {
    private XmlFamilyPreference xmlFamilyPreference;


    @Override
    protected void setUp() {
        xmlFamilyPreference = new XmlFamilyPreference("familyId", "rootTable", "DEST_TABLE");
    }


    public void test_constructor() throws Exception {
        assertEquals("familyId", xmlFamilyPreference.getFamilyId());
        assertEquals("rootTable", xmlFamilyPreference.getRootTable());
    }


    public void test_getArgumentList() throws Exception {
        assertNull(xmlFamilyPreference.getArgumentNameList());

        List<String> argumentList = new ArrayList<String>();
        xmlFamilyPreference.setArgumentNameList(argumentList);
        assertSame(argumentList, xmlFamilyPreference.getArgumentNameList());
    }


    public void test_getFunctionHolderList() throws Exception {
        assertNull(xmlFamilyPreference.getFunctionHolderClassList());

        List<String> functionHolderList = new ArrayList<String>();
        xmlFamilyPreference.setFunctionHolderClassList(functionHolderList);
        assertSame(functionHolderList, xmlFamilyPreference.getFunctionHolderClassList());
    }


    public void test_getSelectConfig() throws Exception {
        XmlFamilyPreference familyPreference = this.xmlFamilyPreference;
        assertNull(familyPreference.getSelectConfig());

        QueryConfig selectConfig = new DefaultQueryConfig();
        familyPreference.setSelectConfig(selectConfig);
        assertSame(selectConfig, familyPreference.getSelectConfig());
    }


    public void test_getDeleteWhereClause() throws Exception {
        assertNull(xmlFamilyPreference.getDeleteConfig());

        final String deleteWhereClause = "deleteWhereClause";

        DefaultQueryConfig queryConfig = new DefaultQueryConfig();
        queryConfig.setRootExpression(new JoinKeyExpression(deleteWhereClause));
        xmlFamilyPreference.setDeleteConfig(queryConfig);
        assertSame(deleteWhereClause, xmlFamilyPreference.getDeleteConfig().getRootExpression().getWhereClause());
    }


    public void test_getFilter() throws Exception {
        XmlFamilyPreference familyPreference = this.xmlFamilyPreference;
        assertFalse(familyPreference.hasFilter());

        try {
            xmlFamilyPreference.getFilter();
            fail("NullPointerException attendue.");
        }
        catch (NullPointerException e) {
        }

        xmlFamilyPreference.setFilter(new RowFilter() {
            public boolean isRowExcluded(int segmentationId, Row row, Object filterValue) {
                return false;
            }


            public String getTableName() {
                return null;
            }


            public String getColumnName() {
                return null;
            }
        });

        assertTrue(xmlFamilyPreference.hasFilter());
        assertNotNull(xmlFamilyPreference.getFilter());

        xmlFamilyPreference.setFilter(null);
        assertFalse(xmlFamilyPreference.hasFilter());
    }


    public void test_getTableMetaData() throws Exception {
        assertNull(xmlFamilyPreference.getTableMetaData());

        TableMetaData tableMetaData = new TableMetaData(null, null);
        xmlFamilyPreference.setTableMetaData(tableMetaData);
        assertSame(tableMetaData, xmlFamilyPreference.getTableMetaData());
    }


    public void test_getResultTableName() throws Exception {
        try {
            xmlFamilyPreference.getResultTableName();
            fail("NullPointerException attendue.");
        }
        catch (NullPointerException e) {
        }

        List<ColumnMetaData> columnMetaDatas = null;
        TableMetaData tableMetaData = new TableMetaData("TABLE_NAME", columnMetaDatas);
        xmlFamilyPreference.setTableMetaData(tableMetaData);
        assertEquals(tableMetaData.getName(),
                     xmlFamilyPreference.getResultTableName());
    }


    public void test_getResultColumnNames() throws Exception {
        try {
            xmlFamilyPreference.getResultTableColumnNames();
            fail("NullPointerException attendue.");
        }
        catch (NullPointerException e) {
        }

        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(0);
        TableMetaData tableMetaData = new TableMetaData("TABLE_NAME", columnMetaDatas);
        xmlFamilyPreference.setTableMetaData(tableMetaData);
        assertTrue(Arrays.equals(tableMetaData.getColumnNames(),
                                 xmlFamilyPreference.getResultTableColumnNames()));
    }


    public void test_getResultColumnType() throws Exception {
        try {
            xmlFamilyPreference.getResultTableColumnType("column_name");
            fail("NullPointerException attendue.");
        }
        catch (NullPointerException e) {
        }

        String columnName = "column_name";
        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(1);
        columnMetaDatas.add(new ColumnMetaData(columnName, 13));
        TableMetaData tableMetaData = new TableMetaData("TABLE_NAME", columnMetaDatas);
        xmlFamilyPreference.setTableMetaData(tableMetaData);
        assertEquals(tableMetaData.getColumnType(columnName),
                     xmlFamilyPreference.getResultTableColumnType(columnName));

        try {
            xmlFamilyPreference.getResultTableColumnType("unknown_column_name");
            fail("IllegalArgumentException attendue.");
        }
        catch (IllegalArgumentException e) {
        }
    }
}
