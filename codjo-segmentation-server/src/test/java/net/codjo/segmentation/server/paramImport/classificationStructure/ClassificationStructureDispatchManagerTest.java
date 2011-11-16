package net.codjo.segmentation.server.paramImport.classificationStructure;
import net.codjo.tokio.TokioFixture;
import junit.framework.TestCase;
/**
 *
 */
public class ClassificationStructureDispatchManagerTest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(getClass());


    public void test_dispatch() throws Exception {
        tokioFixture.insertInputInDb("dispatch");

        String[][] data = new String[][]{
              {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT",
               "FORMULA", "SLEEVE_ID", "SLEEVE_ROW_ID", "IS_QUARANTINE"},
              {"1", "01-1", "Poche 1", "0", "0", "manager == 3", "999", "997", "false"},
              {"1", "01-2", "Fourre-tout", "1", "1", null, "1000", "998", "", "false"}};

        ClassificationStructureDispatchManager manager = new ClassificationStructureDispatchManager();
        manager.setConnection(tokioFixture.getConnection());
        manager.dispatch(data);
        tokioFixture.assertAllOutputs("dispatch");
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
