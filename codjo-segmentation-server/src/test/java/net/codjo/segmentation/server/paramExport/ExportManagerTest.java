package net.codjo.segmentation.server.paramExport;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.tokio.TokioFixture;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import junit.framework.TestCase;
/**
 *
 */
public class ExportManagerTest extends TestCase {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String TAB = "\t";
    private TokioFixture tokioFixture = new TokioFixture(ExportManagerTest.class);
    private Connection connection;
    private ExportManager exportManager;
    private static final String CLASSIFICATION_TABLE_NAME = "PM_MY_CLASSIFICATION";
    private static final String CLASSIFICATION_STRUCTURE_TABLE_NAME = "PM_MY_CLASSIFICATION_STRUCTURE";


    public void test_exportDataClassification() throws Exception {
        createTableClassification(CLASSIFICATION_TABLE_NAME);
        tokioFixture.insertInputInDb("exportAxe");

        StringBuffer result = exportManager
              .exportData(connection, CLASSIFICATION_TABLE_NAME, getClassificationFielMap());
        assertEquals(
              "0" + TAB + "Label   1" + TAB + "Type 1" + NEW_LINE
              + "2" + TAB + "Label 2" + TAB + "Type 2" + NEW_LINE,
              result.toString());
    }


    public void test_exportDataClassificationStructure() throws Exception {
        createTableClassificationStructure(CLASSIFICATION_STRUCTURE_TABLE_NAME);
        tokioFixture.insertInputInDb("exportPoche");

        exportManager.setClassificationStructureTableName(CLASSIFICATION_STRUCTURE_TABLE_NAME);

        StringBuffer result = exportManager
              .exportData(connection, CLASSIFICATION_STRUCTURE_TABLE_NAME,
                          getClassificationStructureFieldMap());
        assertEquals(
              "1" + TAB + "01-1" + TAB + "Poche 1" + TAB + "0" + TAB + "1" + TAB
              + "manager == 1" + NEW_LINE

              + "1" + TAB + "01-2" + TAB + "Fourre-tout" + TAB + "1" + TAB + "1" + TAB + " "
              + NEW_LINE,
              result.toString());
    }


    public void test_createHeaderClassification() throws Exception {
        createTableClassification(CLASSIFICATION_TABLE_NAME);

        StringBuffer result = exportManager.createHeader(getClassificationFielMap());
        assertEquals(
              "CLASSIFICATION_ID" + TAB + "CLASSIFICATION_NAME" + TAB + "CLASSIFICATION_TYPE" + NEW_LINE,
              result.toString());
    }


    public void test_createHeaderClassificationStructure() throws Exception {
        createTableClassificationStructure(CLASSIFICATION_STRUCTURE_TABLE_NAME);

        StringBuffer result = exportManager.createHeader(getClassificationStructureFieldMap());
        assertEquals(
              "CLASSIFICATION_ID" + TAB + "SLEEVE_CODE" + TAB + "SLEEVE_NAME" + TAB
              + "SLEEVE_DUSTBIN" + TAB + "TERMINAL_ELEMENT" + TAB + "FORMULA" + NEW_LINE,
              result.toString());
    }


    public void test_getFielMap_unknownTable() throws Exception {
        try {
            exportManager.getFielMap(connection, "PM_MY_BIDON");
            fail("Table introuvable");
        }
        catch (SQLException ex) {
            assertEquals("La table PM_MY_BIDON est introuvable", ex.getLocalizedMessage());
        }
    }


    public void test_getFielMapClassification() throws Exception {
        createTableClassification(CLASSIFICATION_TABLE_NAME);

        Map<String, String> actualMap = exportManager.getFielMap(connection, CLASSIFICATION_TABLE_NAME);
        assertEquals(getClassificationFielMap(), actualMap);
    }


    public void test_getFielMapClassificationStructure() throws Exception {
        createTableClassificationStructure(CLASSIFICATION_STRUCTURE_TABLE_NAME);

        exportManager.setClassificationStructureTableName(CLASSIFICATION_STRUCTURE_TABLE_NAME);
        Map<String, String> actualMap = exportManager
              .getFielMap(connection, CLASSIFICATION_STRUCTURE_TABLE_NAME);
        assertEquals(getClassificationStructureFieldMap(), actualMap);
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
        connection = tokioFixture.getConnection();
        exportManager = new ExportManager();
    }


    private Map<String, String> getClassificationFielMap() {
        Map<String, String> fieldMap = new LinkedHashMap<String, String>();
        fieldMap.put("CLASSIFICATION_ID", "int");
        fieldMap.put("CLASSIFICATION_NAME", "varchar");
        fieldMap.put("CLASSIFICATION_TYPE", "varchar");
        return fieldMap;
    }


    private Map<String, String> getClassificationStructureFieldMap() {
        Map<String, String> fieldMap = new LinkedHashMap<String, String>();
        fieldMap.put("CLASSIFICATION_ID", "int");
        fieldMap.put("SLEEVE_CODE", "varchar");
        fieldMap.put("SLEEVE_NAME", "varchar");
        fieldMap.put("SLEEVE_DUSTBIN", "bit");
        fieldMap.put("TERMINAL_ELEMENT", "bit");
        fieldMap.put("FORMULA", "text");
        return fieldMap;
    }


    private void createTableClassification(String tableName) throws SQLException {
        tokioFixture.getJdbcFixture().create(SqlTable.table(tableName), "CLASSIFICATION_ID int null,"
                                                                        + "CLASSIFICATION_NAME varchar(50) null,"
                                                                        + "CLASSIFICATION_TYPE varchar(12) null");
    }


    private void createTableClassificationStructure(String tableName) throws SQLException {
        tokioFixture.getJdbcFixture().create(SqlTable.table(tableName), "SLEEVE_ID      int  not null,"
                                                                        + "SLEEVE_ROW_ID      varchar(20)  not null,"
                                                                        + "CLASSIFICATION_ID      int  not null,"
                                                                        + "SLEEVE_CODE      varchar(50)  not null,"
                                                                        + "SLEEVE_NAME      varchar(50)  null,"
                                                                        + "SLEEVE_DUSTBIN      bit default 0  not null,"
                                                                        + "TERMINAL_ELEMENT      bit default 0  not null,"
                                                                        + "FORMULA      text  null");
    }


    private void dropTable() throws SQLException {
        tokioFixture.getJdbcFixture().drop(SqlTable.table(CLASSIFICATION_TABLE_NAME));
        tokioFixture.getJdbcFixture().drop(SqlTable.table(CLASSIFICATION_STRUCTURE_TABLE_NAME));
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
        dropTable();
    }
}
