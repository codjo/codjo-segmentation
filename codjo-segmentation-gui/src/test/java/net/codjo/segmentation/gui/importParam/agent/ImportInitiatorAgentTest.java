package net.codjo.segmentation.gui.importParam.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.SubStep;
import net.codjo.mad.common.ZipUtil;
import static net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION;
import static net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION_STRUCTURE;
import net.codjo.segmentation.gui.ImportExportProgresListener;
import net.codjo.test.common.LogString;
import net.codjo.test.common.PathUtil;
import java.io.File;
import java.io.FileNotFoundException;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 */
public class ImportInitiatorAgentTest extends TestCase {

    private Story story = new Story();
    private ImportInitiatorAgent agent;
    private LogString log;
    private UserId userId;


    public void test_requestImport_classification() throws Exception {

        File importFile = PathUtil.find(getClass(), "importClassification.txt");

        agent = new ImportInitiatorAgent(importFile,
                                         CLASSIFICATION,
                                         userId);
        agent.addTreatmentProgressListener(new ImportProgressListenerMock());

        final String expectedContents =
              "CLASSIFICATION;CLASSIFICATION_ID\tCLASSIFICATION_NAME\tCLASSIFICATION_TYPE\n"
              + "1\tAxe event 1\tEVENT1\n"
              + "2\tAxe event 2\tEVENT2\n";

        String quarantineResults =
              "CLASSIFICATION_ID\tCLASSIFICATION_NAME\tCLASSIFICATION_TYPE\tANOMALY_LOG\n"
              + "1\tAxe event 1\tEVENT1\tThe label is too long\n";

        story.record()
              .startTester("import-participant")
              .registerToDF("import-service")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = ZipUtil.unzip(aclMessage.getByteSequenceContent());
                      assertEquals(expectedContents, contents);
                  }
              })
              .replyWith(AclMessage.Performative.AGREE, "import en cours...")
              .replyWithByteSequence(AclMessage.Performative.INFORM, ZipUtil.zip(quarantineResults));

        story.record().assertNumberOfAgentWithService(1, "import-service");

        story.record().startAgent("import-initiator", agent);

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                String[][] quarantine = agent.getQuarantine();
                assertNotNull(quarantine);
                assertEquals("CLASSIFICATION_ID", quarantine[0][0]);
                assertEquals("CLASSIFICATION_NAME", quarantine[0][1]);
                assertEquals("CLASSIFICATION_TYPE", quarantine[0][2]);
                assertEquals("ANOMALY_LOG", quarantine[0][3]);

                assertEquals("1", quarantine[1][0]);
                assertEquals("Axe event 1", quarantine[1][1]);
                assertEquals("EVENT1", quarantine[1][2]);
                assertEquals("The label is too long", quarantine[1][3]);
            }
        });

        story.execute();
    }


    public void test_requestImport_classificationStructure() throws Exception {

        File importFile = PathUtil.find(getClass(), "importClassificationStructure.txt");

        agent = new ImportInitiatorAgent(importFile, CLASSIFICATION_STRUCTURE, userId);
        agent.addTreatmentProgressListener(new ImportProgressListenerMock());

        final String expectedContents =
              "CLASSIFICATION_STRUCTURE;CLASSIFICATION_ID\tSLEEVE_CODE\tSLEEVE_NAME\tSLEEVE_DUSTBIN\tTERMINAL_ELEMENT\tFORMULA\n"
              + "2\t01-1\tPoche 2\t0\t1\tmanager == 3\n"
              + "2\t01-2\tPoche 3\t0\t1\tmanager == 4\n";

        String quarantineResults =
              "SLEEVE_ID\tSLEEVE_ROW_ID\tCLASSIFICATION_ID\tSLEEVE_CODE\tSLEEVE_NAME\tSLEEVE_DUSTBIN\tTERMINAL_ELEMENT\tFORMULA\tANOMALY_LOG\n"
              + "1001\t1001\t2\t01-1\tPoche 2\t0\t1\tmanager == 3\tCet axe ne comporte pas de poche fourre-tout\n"
              + "1002\t1002\t2\t01-2\tPoche 3\t0\t1\tmanager == 4\tCet axe ne comporte pas de poche fourre-tout\n";

        story.record()
              .startTester("import-participant")
              .registerToDF("import-service")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = ZipUtil.unzip(aclMessage.getByteSequenceContent());
                      assertEquals(expectedContents, contents);
                  }
              })
              .replyWith(AclMessage.Performative.AGREE, "import en cours...")
              .replyWithByteSequence(AclMessage.Performative.INFORM, ZipUtil.zip(quarantineResults));

        story.record().assertNumberOfAgentWithService(1, "import-service");

        story.record().startAgent("import-initiator", agent);

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                String[][] quarantine = agent.getQuarantine();
                assertNotNull(quarantine);

                assertEquals(3, quarantine.length);

                assertEquals("SLEEVE_ID", quarantine[0][0]);
                assertEquals("SLEEVE_ROW_ID", quarantine[0][1]);
                assertEquals("CLASSIFICATION_ID", quarantine[0][2]);
                assertEquals("SLEEVE_CODE", quarantine[0][3]);
                assertEquals("SLEEVE_NAME", quarantine[0][4]);
                assertEquals("SLEEVE_DUSTBIN", quarantine[0][5]);
                assertEquals("TERMINAL_ELEMENT", quarantine[0][6]);
                assertEquals("FORMULA", quarantine[0][7]);
                assertEquals("ANOMALY_LOG", quarantine[0][8]);

                assertEquals("1001", quarantine[1][0]);
                assertEquals("1001", quarantine[1][1]);
                assertEquals("2", quarantine[1][2]);
                assertEquals("01-1", quarantine[1][3]);
                assertEquals("Poche 2", quarantine[1][4]);
                assertEquals("0", quarantine[1][5]);
                assertEquals("1", quarantine[1][6]);
                assertEquals("manager == 3", quarantine[1][7]);
                assertEquals("Cet axe ne comporte pas de poche fourre-tout", quarantine[1][8]);

                assertEquals("1002", quarantine[2][0]);
                assertEquals("Poche 3", quarantine[2][4]);
                assertEquals("Cet axe ne comporte pas de poche fourre-tout", quarantine[2][8]);
            }
        });

        story.execute();
    }


    public void test_notifyListener() throws Exception {
        File importFile = PathUtil.find(getClass(), "importClassification.txt");
        agent = new ImportInitiatorAgent(importFile,
                                         CLASSIFICATION,
                                         userId);
        agent.addTreatmentProgressListener(new ImportProgressListenerMock());

        story.record()
              .startTester("import-participant")
              .registerToDF("import-service")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .replyWith(AclMessage.Performative.NOT_UNDERSTOOD, "error message");

        story.record().assertNumberOfAgentWithService(1, "import-service");

        story.record().startAgent("import-initiator", agent);

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                log.assertContent("handleError(handleNotUnderstood received: error message)");
            }
        });

        story.execute();
    }


    public void test_constructor_wrongFile() throws Exception {
        try {
            agent = new ImportInitiatorAgent(new File("wrong file"), CLASSIFICATION, userId);
            fail("Exception attendue");
        }
        catch (FileNotFoundException exception) {
            assertEquals("Le fichier spécifié n'existe pas.", exception.getLocalizedMessage());
        }
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
        log = new LogString();
        userId = UserId.createId("login", "pwd");
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private class ImportProgressListenerMock implements ImportExportProgresListener {
        public void handleInform(String infoMessage) {
            ;
        }


        public void handleInform(String[][] quarantine) {
            log.call("handleInform", quarantine.length);
        }


        public void handleError(String errorMessage) {
            log.call("handleError", errorMessage);
        }
    }
}
