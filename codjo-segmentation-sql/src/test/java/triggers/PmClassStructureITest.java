/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package triggers;
import net.codjo.tokio.TokioFixture;
import junit.framework.TestCase;
/**
 * Test du trigger d'insertion de la table PM_CLASSIFICATION_STRUCTURE.
 *
 * @version $Revision: 1.2 $
 */
public class PmClassStructureITest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(PmClassStructureITest.class);


    public void test_dustbin_first() throws Exception {
        tokioFixture.insertInputInDb("DUSTBIN_FIRST", false);
        tokioFixture.assertAllOutputs("DUSTBIN_FIRST");
    }


    public void test_dustbin_last() throws Exception {
        tokioFixture.insertInputInDb("DUSTBIN_LAST", false);
        tokioFixture.assertAllOutputs("DUSTBIN_LAST");
    }


    public void test_no_dustbin() throws Exception {
        tokioFixture.insertInputInDb("NO_DUSTBIN", false);
        tokioFixture.assertAllOutputs("NO_DUSTBIN");
    }


    public void test_with_include() throws Exception {
        tokioFixture.insertInputInDb("WITH_INCLUDE", false);
        tokioFixture.assertAllOutputs("WITH_INCLUDE");
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
