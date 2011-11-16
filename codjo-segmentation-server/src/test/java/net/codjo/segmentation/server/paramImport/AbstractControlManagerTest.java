/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.tokio.TokioFixture;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

public class AbstractControlManagerTest extends TestCase {
    private TokioFixture tokioFixture = new TokioFixture(AbstractControlManagerTest.class);
    private ControlManagerMock manager;


    public void test_controlOk() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "CLASSIFICATION_NAME", "CLASSIFICATION_TYPE", "IS_QUARANTINE"},
                    {"0", "Label 1", "Type 1", "false"},
                    {"2", "Label 2", "Type 2", "false"}
              };

        manager.setData(data);
        manager.control();

        assertEquals(0, manager.getQuarantine().length);
    }


    public void test_longLabel() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "CLASSIFICATION_NAME", "CLASSIFICATION_TYPE", "IS_QUARANTINE"},
                    {"0", "Label 1", "Type 1", "false"},
                    {"2", "Label too long for you babe!", "Type 2", "false"}
              };

        manager.setData(data);
        manager.control();
        String[][] quarantine = manager.getQuarantine();

        assertEquals(2, quarantine.length);

        assertEquals("CLASSIFICATION_ID", quarantine[0][0]);
        assertEquals("CLASSIFICATION_NAME", quarantine[0][1]);
        assertEquals("CLASSIFICATION_TYPE", quarantine[0][2]);
        assertEquals("ANOMALY_LOG", quarantine[0][3]);

        assertEquals("2", quarantine[1][0]);
        assertEquals("Label too long for you babe!", quarantine[1][1]);
        assertEquals("Type 2", quarantine[1][2]);
        assertEquals("Le libellé de l'axe est trop long", quarantine[1][3]);

        assertEquals("false", data[1][3]);
        assertEquals("true", data[2][3]);
    }


    public void test_repeatedIds() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "CLASSIFICATION_NAME", "CLASSIFICATION_TYPE", "IS_QUARANTINE"},
                    {"2", "Label 2", "Type 2", "false"},
                    {"2", "Label 3", "Type 3", "false"},
                    {"3", "Label 4", "Type 4", "false"},
                    {"2", "Label 5", "Type 5", "false"}
              };

        manager.setData(data);
        manager.control();
        String[][] quarantine = manager.getQuarantine();

        assertEquals(4, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("Label 2", quarantine[1][1]);
        assertEquals("Type 2", quarantine[1][2]);
        assertEquals("Doublon de l'id Axe dans le fichier", quarantine[1][3]);

        assertEquals("2", quarantine[2][0]);
        assertEquals("Label 3", quarantine[2][1]);
        assertEquals("Type 3", quarantine[2][2]);
        assertEquals("Doublon de l'id Axe dans le fichier", quarantine[2][3]);

        assertEquals("2", quarantine[3][0]);
        assertEquals("Label 5", quarantine[3][1]);
        assertEquals("Type 5", quarantine[3][2]);
        assertEquals("Doublon de l'id Axe dans le fichier", quarantine[3][3]);
    }


    public void test_unicity() throws Exception {
        tokioFixture.insertInputInDb("controlUnicity");

        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "CLASSIFICATION_NAME", "CLASSIFICATION_TYPE", "IS_QUARANTINE"},
                    {"1", "Label 1", "Type 1", "false"},
                    {"2", "Label 2", "Type 2", "false"},
                    {"3", "Label 3", "Type 3", "false"}
              };

        manager.setData(data);
        manager.control();
        String[][] quarantine = manager.getQuarantine();

        assertEquals(3, quarantine.length);

        assertEquals("1", quarantine[1][0]);
        assertEquals("Label 1", quarantine[1][1]);
        assertEquals("Type 1", quarantine[1][2]);
        assertEquals("L'id de l'axe existe déjà en base", quarantine[1][3]);

        assertEquals("3", quarantine[2][0]);
        assertEquals("Label 3", quarantine[2][1]);
        assertEquals("Type 3", quarantine[2][2]);
        assertEquals("L'id de l'axe existe déjà en base", quarantine[2][3]);
    }


    public void test_getQuarantineStream() throws Exception {
        String expected
              = "AXE_ID\tLABEL_AXE\tFAMILY_AXE\tANOMALY_LOG\n2\tAxe event 2\tEVENT\twrong axe id\n3\tAxe event 3\tEVENT\tanother dummy log\n";

        List<String[]> quarantineList = new ArrayList<String[]>();
        quarantineList.add(new String[]{"AXE_ID", "LABEL_AXE", "FAMILY_AXE", "CUSTOM_FIELD", "IS_QUARANTINE",
                                        "ANOMALY_LOG"});
        quarantineList.add(new String[]{"2", "Axe event 2", "EVENT", null, "true", "wrong axe id"});
        quarantineList.add(new String[]{"3", "Axe event 3", "EVENT", null, "true", "another dummy log"});

        manager.setData(new String[][]{
              {"AXE_ID", "LABEL_AXE", "FAMILY_AXE", "CUSTOM_FIELD", "IS_QUARANTINE", "ANOMALY_LOG"}});
        manager.setFileHeader(new String[]{"AXE_ID", "LABEL_AXE", "FAMILY_AXE"});
        manager.setQuarantine(quarantineList);

        assertEquals(expected, manager.getQuarantineStream());
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
        tokioFixture.getJdbcFixture()
              .create(SqlTable.table("PM_MY_CLASSIFICATION"), "CLASSIFICATION_ID int not null,");
        manager = new ControlManagerMock();
        manager.setConnection(tokioFixture.getConnection());
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
        tokioFixture.getJdbcFixture().drop(SqlTable.table("PM_MY_CLASSIFICATION"));
    }
}
