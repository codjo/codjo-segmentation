package table;
import static net.codjo.database.common.api.structure.SqlTable.table;
import org.junit.Test;
/**
 *
 */
public class PmClassStructureTest extends AbstractTableTestCase {
    private static final String PM_CLASSIFICATION_STRUCTURE = "PM_CLASSIFICATION_STRUCTURE";


    @Test
    public void test_rowSizeLimit() throws Exception {
        jdbcFixture.delete(table(PM_CLASSIFICATION_STRUCTURE));

        try {
            jdbcFixture.executeUpdate(String.format(
                  "insert into PM_CLASSIFICATION_STRUCTURE (SLEEVE_ID, SLEEVE_ROW_ID, CLASSIFICATION_ID, SLEEVE_CODE, FORMULA) values (1, '1', 1, '1', '%s')",
                  generateString(15000)));
        }
        finally {
            jdbcFixture.delete(table(PM_CLASSIFICATION_STRUCTURE));
        }
    }
}
