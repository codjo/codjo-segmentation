package net.codjo.segmentation.server.participant.common;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.expression.ExpressionException;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.TableMetaData;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import junit.framework.TestCase;
public class SegmentationResultTest extends TestCase {
    private static final String DESTINATION_TABLE_NAME = "#RESULT";
    static final String[] RESULT_COLUMN_NAMES = new String[]{"COL_STR", "COL_NUM"};
    private JdbcFixture jdbc;
    private SegmentationResult result;


    @Override
    protected void setUp() throws Exception {
        jdbc = new DatabaseFactory().createJdbcFixture();
        jdbc.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        jdbc.doTearDown();
    }


    public void test_addError() throws Exception {
        createSegmentationResult();

        result.addError(new ComputeException(createResultRow("str", new BigDecimal(0))));
        result.close();

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{
              {"str", "0.0", "1", null}
        });
    }


    public void test_addError_expression() throws Exception {
        createSegmentationResult();

        ExpressionException error = new ExpressionException();
        error.addException("MY_COL", new Exception("Division par zéro"));

        result.addError(new ComputeException(error, createResultRow("str", new BigDecimal(0))));
        result.close();

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{
              {"str", "0.0", "1", "MY_COL a provoque l'erreur Division par zéro"}
        });
    }


    public void test_addError_2errors() throws Exception {
        createSegmentationResult();

        ComputeException error = createExceptionWith2Errors(createResultRow("str", new BigDecimal(0)));

        result.addError(error);
        result.close();

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{
              {"str", "0.0", "2", "message 1 a provoque l'erreur exception 1,\n"
                                  + "message 2 a provoque l'erreur exception 2"}
        });
    }


    public void test_add() throws Exception {
        createSegmentationResult();

        result.add(createResultRow("str", new BigDecimal("10.1")));
        result.close();

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{
              {"str", "10.1", "0", null}
        });
    }


    public void test_add_nullCase() throws Exception {
        createSegmentationResult();

        result.add(createResultRow("str", null));
        result.close();

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{
              {"str", null, "0", null}
        });
    }


    public void test_add_afterClose() throws Exception {
        createSegmentationResult();

        Row row = createResultRow("str", new BigDecimal("10.1"));

        result.close();

        try {
            result.add(row);

            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("La méthode add est appelé après un close()", ex.getMessage());
        }
    }


    public void test_add_databaseError() throws Exception {
        createSegmentationResult();
        jdbc.drop(SqlTable.table(DESTINATION_TABLE_NAME));
        Row row = createResultRow("str", new BigDecimal("10.1"));
        try {
            result.add(row);
            fail();
        }
        catch (Exception ex) {
            ;
        }
        try {
            result.addError(new ComputeException(row));
            fail();
        }
        catch (Exception ex) {
            ;
        }
    }


    public void test_add_badRow() throws Exception {
        createSegmentationResult();

        Row row = new Row(new String[]{"COL_NUM", "COL_STR"});

        try {
            result.add(row);

            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Row non compatible avec les colonnes résultat :"
                         + Arrays.asList(RESULT_COLUMN_NAMES), ex.getMessage());
        }
    }


    public void test_close_databaseError() throws Exception {
        createSegmentationResult();

        result.add(createResultRow("str", new BigDecimal("10.1")));

        jdbc.doTearDown();
        try {
            result.close();
            fail();
        }
        catch (Exception ex) {
            ;
        }
    }


    private void createSegmentationResult() throws SQLException {
        jdbc.executeUpdate("create table #RESULT ( COL_STR varchar(10), COL_NUM numeric(8,1) null"
                           + ", ANOMALY int, ANOMALY_LOG text null) ");

        XmlFamilyPreference preference = new XmlFamilyPreference("familyId", "ROOT_TABLE_NAME",
                                                                 DESTINATION_TABLE_NAME);
        preference.setTableMetaData(TableMetaData.create(DESTINATION_TABLE_NAME, jdbc.getConnection()));

        result = new SegmentationResult(jdbc.getConnection(), preference);
    }


    private Row createResultRow(String val1, BigDecimal val2) {
        return new Row(RESULT_COLUMN_NAMES, new Object[]{val1, val2});
    }


    private ComputeException createExceptionWith2Errors(Row resultRow) {
        ExpressionException expressionException = new ExpressionException();
        expressionException.addException("message 1", new Exception("exception 1"));
        expressionException.addException("message 2", new Exception("exception 2"));

        return new ComputeException(expressionException, resultRow);
    }
}
