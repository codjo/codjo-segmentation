package net.codjo.segmentation.server.participant.context;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.segmentation.server.participant.common.Page;
import net.codjo.segmentation.server.participant.common.SegmentationResult;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.TableMetaData;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import junit.framework.TestCase;
public class SegmentationContextTest extends TestCase {
    private JdbcFixture jdbc = JdbcFixture.newFixture();


    public void test_page() throws Exception {
        SegmentationContext context = new SegmentationContext(69, null, null, null);

        assertNull(context.removePage(69));

        Page page = new Page();
        context.putPage(69, page);
        assertSame(page, context.removePage(69));

        assertNull(context.removePage(69));
    }


    public void test_result() throws Exception {
        jdbc.executeUpdate("create table #RESULT ( "
                           + "   COL_STR varchar(10), "
                           + "   ANOMALY int, "
                           + "   ANOMALY_LOG text null) ");

        XmlFamilyPreference preference = new XmlFamilyPreference("familyId", "ROOT_TABLE_NAME", "#RESULT");
        preference.setTableMetaData(TableMetaData.create("#RESULT", jdbc.getConnection()));

        SegmentationContext context = new SegmentationContext(1, preference, null, null);

        SegmentationResult segmentationResult = context.createSegmentationResult(jdbc.getConnection());

        assertNotNull(segmentationResult);
        segmentationResult.add(new Row(new String[]{"COL_STR"}, new Object[]{"valeur"}));

        jdbc.assertContent(SqlTable.table("#RESULT"), new String[][]{{"valeur"}});
    }


    public void test_expressionsEvaluator_failWithoutPageStructure() throws Exception {
        SegmentationContext context = new SegmentationContext(1, null, null, null);
        try {
            context.createExpressionsEvaluator();
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("La structure des pages n'est pas renseignee.", ex.getLocalizedMessage());
        }
    }


    @Override
    protected void setUp() throws Exception {
        jdbc.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        jdbc.doTearDown();
    }
}
