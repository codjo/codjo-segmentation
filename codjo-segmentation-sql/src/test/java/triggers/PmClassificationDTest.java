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
public class PmClassificationDTest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(PmClassificationDTest.class);


    public void test_delete() throws Exception {
        tokioFixture.insertInputInDb("delete", true);
        executeUpdate("delete from PM_CLASSIFICATION where CLASSIFICATION_ID = 3 ");
        tokioFixture.assertAllOutputs("delete");
    }


    public void test_delete_with_includes() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_AVEC_INCLUDE", true);
        try {
            tokioFixture.executeQuery("delete from PM_CLASSIFICATION where CLASSIFICATION_ID = 4");
            fail();
        }
        catch (SQLException e) {
            assertEquals("Vous ne pouvez pas supprimer la poche 'PLUS DE 100 KEUR' "
                         + "car elle est utilisée dans la poche 'PLUS DE 100 KEUR' de l'axe 'Répartition par fourchettes de coûts'.",
                         e.getLocalizedMessage());
        }
        tokioFixture.assertAllOutputs("SUPPRESSION_AVEC_INCLUDE");
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
