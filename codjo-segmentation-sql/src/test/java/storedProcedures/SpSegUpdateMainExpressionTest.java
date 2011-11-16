package storedProcedures;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.database.common.api.structure.SqlTable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import junit.framework.TestCase;
public class SpSegUpdateMainExpressionTest extends TestCase {
    private JdbcFixture jdbcFixture = JdbcFixture.newFixture();


    public void test_sp_SEG_Update_Main_Expression()
          throws Exception {
        jdbcFixture.delete(SqlTable.table("PM_SEGMENTATION"));

        jdbcFixture.executeUpdate("insert into PM_SEGMENTATION values (1, 'AXE TEST', 'MA FAMILLE', NULL)");
        jdbcFixture.executeUpdate("insert into PM_EXPRESSION values "
                                  + "(1, 1, 'SLEEVE_CODE', 'utils.caseOf(new boolean[] {}, new String[] {},\"\")', 999, 1, NULL)");

        CallableStatement callableStatement =
              jdbcFixture.getConnection().prepareCall("{call sp_SEG_Update_Main_Expression ?, ?)}");
        callableStatement.setInt(1, 1);
        callableStatement.setString(2,
                                    "utils.caseOf(new boolean[] {VAR_01_1_1}, new String[] {\"01-1.1\"},\"01-1.2.3\")");
        callableStatement.executeUpdate();

        ResultSet resultSet =
              jdbcFixture.executeQuery("select EXPRESSION from PM_EXPRESSION "
                                       + "where SEGMENTATION_ID = 1 and DESTINATION_FIELD = 'SLEEVE_CODE'");
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertEquals("utils.caseOf(new boolean[] {VAR_01_1_1}, new String[] {\"01-1.1\"},\"01-1.2.3\")",
                     resultSet.getString(1));
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
