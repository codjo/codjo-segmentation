package table;
import static net.codjo.database.common.api.structure.SqlTable.table;
import org.junit.Test;
/**
 *
 */
public class PmExpressionTest extends AbstractTableTestCase {
    private static final String PM_EXPRESSION = "PM_EXPRESSION";


    @Test
    public void test_rowSizeLimit() throws Exception {
        jdbcFixture.delete(table(PM_EXPRESSION));

        try {
            jdbcFixture.executeUpdate(String.format(
                  "insert into PM_EXPRESSION (EXPRESSION_ID, SEGMENTATION_ID, DESTINATION_FIELD, PRIORITY, EXPRESSION) values (1, 1, '1', 1, '%s')",
                  generateString(15000)));
        }
        finally {
            jdbcFixture.delete(table(PM_EXPRESSION));
        }
    }
}