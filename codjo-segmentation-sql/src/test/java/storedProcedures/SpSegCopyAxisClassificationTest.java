/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package storedProcedures;
import net.codjo.tokio.TokioTestCase;
import java.sql.Connection;
/**
 * Test de la procédures stockée : sp_SEG_Change_Dustbin_Code.
 */
public class SpSegCopyAxisClassificationTest extends TokioTestCase {
    public void test_sp_SEG_Copy_AxisClassification()
          throws Exception {
        tokioFixture.insertInputInDb("SegmentationSettingsCopyTest");
        Connection connection = tokioFixture.getConnection();
        String query = "exec sp_SEG_Copy_AxisClassification @CLASSIFICATION_ID=3";
        connection.createStatement().executeUpdate(query);
        tokioFixture.assertAllOutputs("SegmentationSettingsCopyTest");
    }
}
