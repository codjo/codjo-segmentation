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
 * Test du trigger de mise à jour de la table PM_CLASSIFICATION_STRUCTURE.
 *
 * @version $Revision: 1.2 $
 */
public class PmClassStructureUTest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(PmClassStructureUTest.class);


    public void test_update_sleeve_name() throws Exception {
        tokioFixture.insertInputInDb("MISE_A_JOUR_NOM_DE_POCHE", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET SLEEVE_NAME='NEW NAME', SLEEVE_DUSTBIN=1 WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("MISE_A_JOUR_NOM_DE_POCHE");
    }


    public void test_update_sleeve_code_for_dustbin() throws Exception {
        tokioFixture.insertInputInDb("MISE_A_JOUR_CODE_POCHE_FOURRETOUT", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET SLEEVE_CODE='01-2' WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("MISE_A_JOUR_CODE_POCHE_FOURRETOUT");
    }


    public void test_update_sleeve_code() throws Exception {
        tokioFixture.insertInputInDb("MISE_A_JOUR_CODE_POCHE", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET SLEEVE_CODE='01-2' WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("MISE_A_JOUR_CODE_POCHE");
    }


    public void test_update_formula() throws Exception {
        tokioFixture.insertInputInDb("MISE_A_JOUR_FORMULE_UNIQUEMENT", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA='action.PREV_AMOUNT > 90000' WHERE SLEEVE_ID=111");
        tokioFixture.assertAllOutputs("MISE_A_JOUR_FORMULE_UNIQUEMENT");
    }


    public void test_update_dustbin_to_normal() throws Exception {
        tokioFixture.insertInputInDb("MISE_A_JOUR_FOURRE_TOUT_EN_NORMAL", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA='action.PREV_AMOUNT > 300000', SLEEVE_DUSTBIN=0 WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("MISE_A_JOUR_FOURRE_TOUT_EN_NORMAL");
    }


    public void test_update_normal_to_dustbin() throws Exception {
        tokioFixture.insertInputInDb("MISE_A_JOUR_NORMAL_EN_FOURRE_TOUT", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA=NULL, SLEEVE_DUSTBIN=1 WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("MISE_A_JOUR_NORMAL_EN_FOURRE_TOUT");
    }


    public void test_update_with_simpleIncludes() throws Exception {
        tokioFixture.insertInputInDb("WITH_SIMPLE_INCLUDE", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA='SRC_SEG_INPUT_ACTION$REFERENTIAL_FUND_PRICE>=10000 && INC_$$4$1234567891234 && VAR_myVariable==\"1\"' WHERE CLASSIFICATION_ID=3 and SLEEVE_CODE='01-1'");
        tokioFixture.assertAllOutputs("WITH_SIMPLE_INCLUDE");
    }


    public void test_update_with_includes() throws Exception {
        tokioFixture.insertInputInDb("WITH_INCLUDE", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA='INC_$$4$1234567891234 || INC_$$4$1234567891235' WHERE SLEEVE_ID=110");
        tokioFixture.assertAllOutputs("WITH_INCLUDE");
    }


    public void test_update_dustbin_with_includes() throws Exception {
        tokioFixture.insertInputInDb("DUSTBIN_WITH_INCLUDE", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET SLEEVE_DUSTBIN = 0, FORMULA='INC_$$4$1234567891234 and INC_$$4$1234567891235' WHERE SLEEVE_ID=120");
        tokioFixture.assertAllOutputs("DUSTBIN_WITH_INCLUDE");
    }


    public void test_update_recursive_with_includes() throws Exception {
        tokioFixture.insertInputInDb("MAJ_FORMULE_WITH_RECURSIVE_INCLUDE", false);
        tokioFixture.executeQuery(
              "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA='999==123' WHERE SLEEVE_ID=130");
        tokioFixture.assertAllOutputs("MAJ_FORMULE_WITH_RECURSIVE_INCLUDE");
    }


    public void test_update_cyclic_prohibited() throws Exception {
        tokioFixture.insertInputInDb("TEST_POCHE_NON_RECURSIVE", false);

        try {
            tokioFixture.executeQuery(
                  "UPDATE PM_CLASSIFICATION_STRUCTURE SET FORMULA='INC_$$2$1234567891239' WHERE SLEEVE_ID=130");
            fail();
        }
        catch (SQLException e) {
            assertEquals(
                  "Vous ne pouvez pas créer de référence cyclique entre la poche 'AXE 4 POCHE 1' de cet axe "
                  + "et la poche 'AXE 3 POCHE 1' de l'axe 'Répartition par fourchettes de coûts'."
                  , e.getLocalizedMessage());
        }
        tokioFixture.assertAllOutputs("TEST_POCHE_NON_RECURSIVE");
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
