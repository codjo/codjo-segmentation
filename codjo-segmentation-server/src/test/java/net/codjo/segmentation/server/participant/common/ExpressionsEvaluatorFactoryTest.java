package net.codjo.segmentation.server.participant.common;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.expression.InvalidExpressionException;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.TableMetaData;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreferenceMock;
import net.codjo.segmentation.server.preference.treatment.Expression;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
public class ExpressionsEvaluatorFactoryTest extends TestCase {
    private static final String DESTINATION_TABLE_NAME = "AP_DESTINATION";
    private JdbcFixture jdbc;


    @Override
    protected void setUp() throws Exception {
        jdbc = new DatabaseFactory().createJdbcFixture();
        jdbc.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        jdbc.drop(SqlTable.table(DESTINATION_TABLE_NAME));
        jdbc.doTearDown();
    }


    public void test_create() throws Exception {
        createDestinationTable();

        SegmentationPreferenceMock segmentationPreference = new SegmentationPreferenceMock();
        List<Expression> expressions = new ArrayList<Expression>(1);
        expressions.add(new Expression(Types.INTEGER, "result", "SRC_COL_A - 2"));
        segmentationPreference.mockGetExpressions(expressions);

        XmlFamilyPreference familyPreference = new XmlFamilyPreferenceMock("familyId", "ROOT_TABLE_NAME",
                                                                           DESTINATION_TABLE_NAME);
        familyPreference.setFunctionHolderClassList(new ArrayList<String>(0));
        familyPreference.setTableMetaData(TableMetaData.create(DESTINATION_TABLE_NAME, jdbc.getConnection()));

        PageStructure structure = new PageStructure(Collections.singletonMap("COL_A", Types.INTEGER));

        ExpressionsEvaluator expressionsEvaluator =
              ExpressionsEvaluatorFactory.create(familyPreference, segmentationPreference, structure);

        Row result = expressionsEvaluator.compute(new Row(new String[]{"COL_A"}, new Object[]{3}));

        assertEquals(1, result.getColumnValue(0));
    }


    public void test_create_invalidExpression() throws Exception {
        createDestinationTable();

        SegmentationPreferenceMock segmentationPreference = new SegmentationPreferenceMock();
        List<Expression> expressions = new ArrayList<Expression>(1);
        expressions.add(new Expression(Types.BOOLEAN, "result", "isNull(SRC_COL_A - 2)"));
        segmentationPreference.mockGetExpressions(expressions);

        XmlFamilyPreference familyPreference = new XmlFamilyPreferenceMock("familyId", "ROOT_TABLE_NAME",
                                                                           DESTINATION_TABLE_NAME);
        familyPreference.setFunctionHolderClassList(new ArrayList<String>(0));
        familyPreference.setTableMetaData(TableMetaData.create(DESTINATION_TABLE_NAME, jdbc.getConnection()));

        PageStructure structure = new PageStructure(Collections.singletonMap("COL_A", Types.INTEGER));

        try {
            ExpressionsEvaluatorFactory.create(familyPreference, segmentationPreference, structure);
            fail();
        }
        catch (InvalidExpressionException ex) {
            assertEquals("Impossible de compiler : null. Une ou plusieurs de ses expressions comportent des erreurs.", ex.getMessage());

        }
    }


    private void createDestinationTable() throws SQLException {
        jdbc.executeUpdate("create table " + DESTINATION_TABLE_NAME
                           + " (result int, ANOMALY int, ANOMALY_LOG varchar(255))");
    }
}
