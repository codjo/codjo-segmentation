package storedProcedures;
import net.codjo.database.common.api.JdbcFixture;
import java.sql.CallableStatement;
import java.sql.Types;
import junit.framework.TestCase;
public class SpSegTranscodeSleeveCodeTest extends TestCase {
    private JdbcFixture jdbcFixture = JdbcFixture.newFixture();


    public void test_sp_SEG_Transcode_Sleeve_Code()
          throws Exception {
        CallableStatement callableStatement =
              jdbcFixture.getConnection().prepareCall("{call sp_SEG_Transcode_Sleeve_Code ?, ?)}");
        callableStatement.setString(1, "01-1.2.3");
        callableStatement.registerOutParameter(2, Types.VARCHAR);
        callableStatement.executeUpdate();
        assertEquals("01_1_2_3", callableStatement.getString(2));
    }


    @Override
    protected void setUp() throws Exception {
        jdbcFixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        jdbcFixture.doTearDown();
    }
}
