package net.codjo.segmentation.server.preference.family;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
public class TableMetaDataTest extends TestCase {
    private JdbcFixture jdbcFixture;


    @Override
    protected void setUp() throws Exception {
        jdbcFixture = new DatabaseFactory().createJdbcFixture();
        jdbcFixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        jdbcFixture.doTearDown();
    }


    public void test_constructor() throws Exception {
        TableMetaData table = new TableMetaData("AP_TRUC", new ArrayList<ColumnMetaData>());

        assertEquals("AP_TRUC", table.getName());
    }


    public void test_createTableMetaData() throws Exception {
        jdbcFixture.executeUpdate(
              "create table #RESULT ( COL_STR varchar(10), COL_NUM numeric(8,5), ANOMALY int, ANOMALY_LOG varchar(255)) ");

        TableMetaData table =
              TableMetaData.create("#RESULT", jdbcFixture.getConnection());

        assertEquals("#RESULT", table.getName());
        assertEquals(Types.VARCHAR, table.getColumnType("COL_STR"));
        assertEquals(Types.NUMERIC, table.getColumnType("COL_NUM"));
    }


    public void test_getColumnNames_fromDatabase()
          throws Exception {
        jdbcFixture.executeUpdate(
              "create table #RESULT ( COL_STR varchar(10), COL_NUM numeric(8,5), ANOMALY int, ANOMALY_LOG varchar(255)) ");

        TableMetaData table =
              TableMetaData.create("#RESULT", jdbcFixture.getConnection());

        String[] expectedColumns = new String[]{"COL_STR", "COL_NUM"};

        assertEquals(Arrays.asList(expectedColumns), Arrays.asList(table.getColumnNames()));
    }


    public void test_getColumnNames_fromMetadata()
          throws Exception {
        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(2);
        columnMetaDatas.add(new ColumnMetaData("col1", 1));
        columnMetaDatas.add(new ColumnMetaData("col2", 2));
        TableMetaData table = new TableMetaData("AP_TRUC", columnMetaDatas);

        String[] expectedColumns = new String[]{"col1", "col2"};
        assertEquals(Arrays.asList(expectedColumns), Arrays.asList(table.getColumnNames()));
    }


    public void test_getColumnType() throws Exception {
        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(2);
        columnMetaDatas.add(new ColumnMetaData("col1", 1));
        columnMetaDatas.add(new ColumnMetaData("col2", 2));
        TableMetaData table = new TableMetaData("AP_TRUC", columnMetaDatas);

        assertEquals(1, table.getColumnType("col1"));
        assertEquals(2, table.getColumnType("col2"));
    }


    public void test_getColumnType_error() throws Exception {
        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(1);
        columnMetaDatas.add(new ColumnMetaData("col1", 1));
        TableMetaData table = new TableMetaData("AP_TRUC", columnMetaDatas);

        try {
            table.getColumnType("unknown");

            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("La colonne 'unknown' n'appartient pas à la table 'AP_TRUC'",
                         ex.getMessage());
        }
    }


    public void test_removeUnusedColumns_null() throws Exception {
        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(2);
        columnMetaDatas.add(new ColumnMetaData("col1", 1));
        columnMetaDatas.add(new ColumnMetaData("col2", 2));
        TableMetaData table = new TableMetaData("AP_TRUC", columnMetaDatas);

        table.removeUnusedColumns(null);
        assertEquals(new ArrayList(), Arrays.asList(table.getColumnNames()));
    }


    public void test_removeUnusedColumns() throws Exception {
        List<ColumnMetaData> columnMetaDatas = new ArrayList<ColumnMetaData>(2);
        columnMetaDatas.add(new ColumnMetaData("col1", 1));
        columnMetaDatas.add(new ColumnMetaData("col2", 2));
        TableMetaData table = new TableMetaData("AP_TRUC", columnMetaDatas);

        List<String> usedColumns = Arrays.asList("col2", "col3");
        table.removeUnusedColumns(usedColumns);

        List<String> expectedColumns = Arrays.asList("col2");
        assertEquals(expectedColumns, Arrays.asList(table.getColumnNames()));
    }
}
