/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package triggers;
import net.codjo.tokio.TokioFixture;
import java.sql.SQLException;
import junit.framework.TestCase;
/**
 * Test du trigger de suppression de la table PM_CLASSIFICATION_STRUCTURE.
 *
 * @version $Revision: 1.3 $
 */
public class PmClassStructureDTest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(PmClassStructureDTest.class);


    public void test_delete_last_sleeve() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_DERNIERE_POCHE", false);
        tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=112");
        tokioFixture.assertAllOutputs("SUPPRESSION_DERNIERE_POCHE");
    }


    public void test_delete_first_sleeve() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_PREMIERE_POCHE", false);
        tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=111");
        tokioFixture.assertAllOutputs("SUPPRESSION_PREMIERE_POCHE");
    }


    public void test_delete_dustbin_sleeve() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_POCHE_FOURETOUT", false);
        tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("SUPPRESSION_POCHE_FOURETOUT");
    }


    public void test_delete_noeud() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_NOEUD", false);
        tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=110");
        tokioFixture.assertAllOutputs("SUPPRESSION_NOEUD");
    }


    public void test_delete_include() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_AVEC_INCLUDE", false);
        try {
            tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=130");
            fail();
        }
        catch (SQLException e) {
            assertEquals("Vous ne pouvez pas supprimer la poche 'PLUS DE 100 KEUR' "
                         + "car elle est utilisée dans la poche 'PLUS DE 100 KEUR' de l'axe 'Répartition par fourchettes de coûts'.",
                         e.getLocalizedMessage());
        }
        tokioFixture.assertAllOutputs("SUPPRESSION_AVEC_INCLUDE");
    }


    public void test_delete_include_recursif() throws Exception {
        tokioFixture.insertInputInDb("SUPPRESSION_AVEC_INCLUDE_RECURSIF", false);
        try {
            tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=130");
            fail();
        }
        catch (SQLException e) {
            assertEquals("Vous ne pouvez pas supprimer la poche 'PLUS DE 100 KEUR' "
                         + "car elle est utilisée dans la poche 'PLUS DE 100 KEUR' de l'axe 'Répartition par fourchettes de coûts'.",
                         e.getLocalizedMessage());
        }
        try {
            tokioFixture.executeQuery("DELETE FROM PM_CLASSIFICATION_STRUCTURE WHERE SLEEVE_ID=110");
            fail();
        }
        catch (SQLException e) {
            assertEquals("Vous ne pouvez pas supprimer la poche 'PLUS DE 100 KEUR' "
                         + "car elle est utilisée dans la poche 'PLUS DE 100 KEUR' de l'axe 'Axe 3'.",
                         e.getLocalizedMessage());
        }
        tokioFixture.assertAllOutputs("SUPPRESSION_AVEC_INCLUDE_RECURSIF");
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
        tokioFixture.insertInputInDb("common", true);
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
    }
}
