package table;
import net.codjo.database.common.api.JdbcFixture;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
/**
 *
 */
public class AbstractTableTestCase {
    protected JdbcFixture jdbcFixture = JdbcFixture.newFixture();


    @Before
    public void setUp() {
        jdbcFixture.doSetUp();
    }


    @After
    public void tearDown() {
        jdbcFixture.doTearDown();
    }


    protected String generateString(int length) {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, (byte)'Z');
        return new String(bytes);
    }
}
