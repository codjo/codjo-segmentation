package net.codjo.segmentation.server.util;
import net.codjo.tokio.TokioFixture;
import net.codjo.variable.UnknownVariableException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class CompilationUtilTest {
    private TokioFixture tokio = new TokioFixture(getClass());


    @Before
    public void setUp() throws Exception {
        tokio.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        tokio.doTearDown();
    }


    @Test
    public void test_replaceVariables() throws Exception {
        tokio.insertInputInDb("replaceVariables");

        assertReplaceVariables("tititi tatata = tototo", "tititi tatata = tototo");

        assertReplaceVariables("tititi tatata = tototo", "tititi tatata = INC_$$1$1234");

        assertReplaceVariables("tititi tatata = tototo", "tititi tatata = INC_$$1$1111");

        try {
            CompilationUtil.replaceVariables("tititi tatata = INC_$$1$2222", tokio.getConnection());
            fail();
        }
        catch (CompilationUtil.CyclicVariableException e) {
            assertEquals("La variable 'INC_$$1$2222' est une référence cyclique.",
                         e.getLocalizedMessage());
        }

        assertReplaceVariables("tototo = tototo", "INC_$$1$1111 = INC_$$1$1234");

        try {
            CompilationUtil.replaceVariables("tititi tatata = INC_$$1$7890", tokio.getConnection());
            fail();
        }
        catch (UnknownVariableException e) {
            assertEquals("La variable \"INC_$$1$7890\" est inconnue ",
                         e.getLocalizedMessage());
        }
    }


    private void assertReplaceVariables(String expected, String expression) throws Exception {
        assertEquals(expected, CompilationUtil.replaceVariables(expression, tokio.getConnection()));
    }
}
