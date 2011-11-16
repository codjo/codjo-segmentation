package triggers;
import net.codjo.tokio.TokioFixture;
import junit.framework.TestCase;
/**
 */
public class PmClassificationITest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(PmClassificationITest.class);


    public void test_insert() throws Exception {
        tokioFixture.insertInputInDb("insert", true);
        tokioFixture.assertAllOutputs("insert");
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
    }
}
