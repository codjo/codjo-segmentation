/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package triggers;
import net.codjo.tokio.TokioFixture;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
/**
 */
public class PmClassificationUTest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(PmClassificationUTest.class);


    public void test_update() throws Exception {
        tokioFixture.insertInputInDb("update", true);
        executeUpdate(
              "update PM_CLASSIFICATION set CLASSIFICATION_NAME = 'Répartition par la fourchette' where CLASSIFICATION_ID = 3 ");
        executeUpdate(
              "update PM_CLASSIFICATION set CLASSIFICATION_TYPE = 'ACTION' where CLASSIFICATION_ID = 3 ");
        tokioFixture.assertAllOutputs("update");
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
    }


    private void executeUpdate(String query) throws SQLException {
        Connection connection = tokioFixture.getConnection();
        connection.createStatement().executeUpdate(query);
    }
}
