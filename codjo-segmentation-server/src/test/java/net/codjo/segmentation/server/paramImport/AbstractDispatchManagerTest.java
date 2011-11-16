package net.codjo.segmentation.server.paramImport;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.tokio.TokioFixture;
import junit.framework.TestCase;
/**
 *
 */
public class AbstractDispatchManagerTest extends TestCase {

    private TokioFixture tokioFixture = new TokioFixture(AbstractDispatchManagerTest.class);
    private DispatchManagerMock manager;


    public void test_getSqlTypeName() throws Exception {
        assertEquals("int", manager.getSqlTypeName("CLASSIFICATION_ID"));
        assertEquals("varchar", manager.getSqlTypeName("CLASSIFICATION_NAME"));
        assertEquals("varchar", manager.getSqlTypeName("CLASSIFICATION_TYPE"));
    }


    public void test_dispatch() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "CLASSIFICATION_NAME", "CLASSIFICATION_TYPE", "IS_QUARANTINE"},
                    {"0", "Label 1", "Type 1", "false"},
                    {"1", "Do not insert me", "Type 1", "true"},
                    {"1", "Do not insert me neither", "Type 1", "true"},
                    {"2", "Label 2", "Type 2", "false"}
              };

        manager.dispatch(data);

        tokioFixture.assertAllOutputs("dispatch");
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
        tokioFixture.getJdbcFixture()
              .create(SqlTable.table("PM_MY_CLASSIFICATION"), "CLASSIFICATION_ID int null,"
                                                              + "CLASSIFICATION_NAME varchar(50) null,"
                                                              + "CLASSIFICATION_TYPE varchar(12) null");
        manager = new DispatchManagerMock();
        manager.setConnection(tokioFixture.getConnection());
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
        tokioFixture.getJdbcFixture().drop(SqlTable.table("PM_MY_CLASSIFICATION"));
    }
}
