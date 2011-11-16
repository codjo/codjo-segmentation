/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.treatment;
import static net.codjo.segmentation.server.preference.treatment.SegmentationPreference.createPreference;
import net.codjo.tokio.TokioFixture;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static java.sql.Types.INTEGER;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
public class SegmentationPreferenceTest extends TestCase {
    private static final Map<String, String> NO_PARAMETERS = new HashMap<String, String>(0);
    private TokioFixture fixture = new TokioFixture(SegmentationPreferenceTest.class);


    public void test_load_segmentation() throws Exception {
        insertPmSegmentation(1, "TEST", "ACTION");
        insertPmExpression(1, 1, "DEST_1", "DEST_EXPRESS", 1, 0, null);
        insertPmExpression(2, 1, "SLEEVE_CODE", "CPFT", 999, 0, null);

        int segmentationId = 1;
        SegmentationPreference segmentationPreference = createPreference(fixture.getConnection(),
                                                                         segmentationId,
                                                                         NO_PARAMETERS);

        assertEquals(segmentationId, segmentationPreference.getSegmentationId());
        assertEquals("TEST", segmentationPreference.getSegmentationName());
        assertEquals("ACTION", segmentationPreference.getFamily());
    }


    public void test_load_segmentation_ko() throws Exception {
        int segmentationId = 1;

        try {
            createPreference(fixture.getConnection(), segmentationId, NO_PARAMETERS);

            fail("IllegalArgumentException attendue.");
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Le traitement " + segmentationId + " est inconnu", ex.getMessage());
        }
    }


    public void test_load_expressions() throws Exception {
        insertPmSegmentation(1, "TEST", "ACTION");
        insertPmExpression(1, 1, "DEST_1", "DEST_EXPRESS", 1, 0, null);
        insertPmExpression(2, 1, "VAR_1", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(3, 1, "SLEEVE_CODE", "CPFT", 999, 0, null);
        int segmentationId = 1;

        SegmentationPreference segmentationPreference =
              createPreference(fixture.getConnection(), segmentationId, NO_PARAMETERS);

        assertEquals(3, segmentationPreference.getExpressions().size());

        Expression expression = segmentationPreference.getExpressions().get(0);
        assertEquals("DEST_1", expression.getDestinationField());
        assertEquals("DEST_EXPRESS", expression.getExpression());
        assertEquals(1, expression.getPriority());
        assertEquals(0, expression.getType());
        assertEquals(false, expression.isVariable());

        expression = segmentationPreference.getExpressions().get(1);
        assertEquals("VAR_1", expression.getDestinationField());
        assertEquals("VAR_EXPRESS", expression.getExpression());
        assertEquals(2, expression.getPriority());
        assertEquals(7, expression.getType());
        assertEquals(true, expression.isVariable());
    }


    public void test_load_expressions_with_variables() throws Exception {
        insertPmSegmentation(1, "TEST", "ACTION");
        insertPmExpression(1, 1, "DEST_1", "\"$photo$\" == \"$segmentation.id$\"", 1, 0, null);
        insertPmExpression(2, 1, "SLEEVE_CODE", "CPFT", 999, 0, null);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("photo", "200511");
        parameters.put("segmentation.id", "1");

        SegmentationPreference segmentationPreference = createPreference(fixture.getConnection(),
                                                                         1,
                                                                         parameters);

        assertEquals(2, segmentationPreference.getExpressions().size());

        Expression expression = segmentationPreference.getExpressions().get(0);
        assertEquals("\"200511\" == \"1\"", expression.getExpression());
    }


    public void test_load_expressions_ko() throws Exception {
        insertPmSegmentation(1, "TEST", "ACTION");
        int segmentationId = 1;

        try {
            createPreference(fixture.getConnection(), segmentationId, NO_PARAMETERS);

            fail("IllegalArgumentException attendue.");
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Aucune expression pour le traitement " + segmentationId, ex.getMessage());
        }
    }


    public void testSleeveCodeTransformation() throws SQLException, NullDustbinException {
        insertPmSegmentation(1, "TEST", "ACTION");
        insertPmExpression(1, 1, "SLEEVE_CODE", "cpft", 2, 1, 7);
        insertPmExpression(2, 1, "VAR_01_1", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(3, 1, "VAR_01_2", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(4, 1, "VAR_001_3", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(5, 1, "VAR_01_3_15666", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(6, 1, "VAR_01_3_01_02", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(7, 1, "TAR_01_3_01_02", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(8, 1, "VAR_AP_SLEEVE_02", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(9, 1, "VAR_AP_SLEEVE-02", "VAR_EXPRESS", 2, 1, 7);
        insertPmExpression(10, 1, "VAR_AP_SLEEVE", "VAR_EXPRESS", 2, 1, 7);

        SegmentationPreference preference = createPreference(fixture.getConnection(), 1, NO_PARAMETERS);
        for (Expression expression : preference.getExpressions()) {
            if ("SLEEVE_CODE".equals(expression.getDestinationField())) {
                assertEquals(
                      "utils.caseOf(new boolean[] {VAR_01_1,VAR_01_2,VAR_001_3,VAR_01_3_15666,VAR_01_3_01_02}, new String[] {\"01-1\",\"01-2\",\"001-3\",\"01-3.15666\",\"01-3.01.02\"}, \"cpft\")",
                      expression.getExpression());
                return;
            }
        }
        fail("Aucune expression SLEEVE_CODE n'a été trouvée ");
    }


    private void insertPmExpression(Integer idExpression,
                                    Integer idSegmentation,
                                    String destinationField,
                                    String expr,
                                    Integer priority,
                                    Integer variable,
                                    Integer variableType)
          throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql =
                  "insert into PM_EXPRESSION (EXPRESSION_ID,SEGMENTATION_ID,DESTINATION_FIELD,EXPRESSION,PRIORITY,IS_VARIABLE,VARIABLE_TYPE)"
                  + " values (?, ?, ?, ?, ?, ?, ?)";
            statement = fixture.getConnection().prepareStatement(sql);
            statement.setInt(1, idExpression);
            statement.setInt(2, idSegmentation);
            statement.setString(3, destinationField);
            statement.setString(4, expr);
            statement.setInt(5, priority);
            statement.setInt(6, variable);
            if (variableType == null) {
                statement.setNull(7, INTEGER);
            }
            else {
                statement.setInt(7, variableType);
            }
            statement.execute();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    private void insertPmSegmentation(Integer idSegmentation,
                                      String segmentationName,
                                      String family)
          throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql
                  = "insert into PM_SEGMENTATION (SEGMENTATION_ID, SEGMENTATION_NAME, FAMILY) values (?, ?, ?)";
            statement = fixture.getConnection().prepareStatement(sql);
            statement.setInt(1, idSegmentation);
            statement.setString(2, segmentationName);
            statement.setString(3, family);
            statement.execute();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    public void testReplace() {
        assertEquals("01-01.01.01", SegmentationPreference.replace("VAR_01_01_01_01"));
        assertEquals("0256-09", SegmentationPreference.replace("VAR_0256_09"));
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        deleteTables();
    }


    @Override
    protected void tearDown() throws Exception {
        deleteTables();
        fixture.doTearDown();
    }


    private void deleteTables() throws SQLException {
        fixture.executeQuery("delete from PM_EXPRESSION");
        fixture.executeQuery("delete from PM_SEGMENTATION");
    }
}
