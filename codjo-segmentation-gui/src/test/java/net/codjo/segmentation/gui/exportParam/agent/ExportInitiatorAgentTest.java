package net.codjo.segmentation.gui.exportParam.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.SubStep;
import net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent;
import net.codjo.segmentation.gui.ImportExportProgresListener;
import net.codjo.test.common.LogString;
import net.codjo.test.common.PathUtil;
import java.io.File;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 */
public class ExportInitiatorAgentTest extends TestCase {

    private Story story = new Story();
    private ExportInitiatorAgent agent;
    private LogString log;
    private UserId userId;


    public void test_requestExport_classification() throws Exception {
        File exportFile = PathUtil.find(getClass(), "exportClassification.txt");

        agent = new ExportInitiatorAgent(exportFile,
                                         AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION,
                                         userId);
        agent.addTreatmentProgressListener(new ExportInitiatorAgentTest.ExportProgressListenerMock());

        final String expectedContents =
              "CLASSIFICATION;" + exportFile.getAbsolutePath();

        story.record()
              .startTester("export-participant")
              .registerToDF("export-service")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = aclMessage.getContent();
                      assertEquals(expectedContents, contents);
                  }
              })
              .replyWith(AclMessage.Performative.AGREE, "export en cours...")
              .replyWithContent(AclMessage.Performative.INFORM, "OK");

        story.record().assertNumberOfAgentWithService(1, "export-service");

        story.record().startAgent("export-initiator", agent);

        story.execute();
    }


    public void test_requestExport_classificationStructure() throws Exception {
        File exportFile = PathUtil.find(getClass(), "exportClassificationStructure.txt");

        agent = new ExportInitiatorAgent(exportFile,
                                         AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION_STRUCTURE,
                                         userId);
        agent.addTreatmentProgressListener(new ExportProgressListenerMock());

        final String expectedContents = "CLASSIFICATION_STRUCTURE;" + exportFile.getAbsolutePath();

        story.record()
              .startTester("export-participant")
              .registerToDF("export-service")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = aclMessage.getContent();
                      assertEquals(expectedContents, contents);
                  }
              })
              .replyWith(AclMessage.Performative.AGREE, "export en cours...")
              .replyWithContent(AclMessage.Performative.INFORM, "OK");

        story.record().assertNumberOfAgentWithService(1, "export-service");

        story.record().startAgent("export-initiator", agent);

        story.execute();
    }


    public void test_notifyListener() throws Exception {
        File importFile = PathUtil.find(getClass(), "exportClassification.txt");
        agent = new ExportInitiatorAgent(importFile,
                                         AbstractImportExportInitiatorAgent.TreatmentType.CLASSIFICATION,
                                         userId);
        agent.addTreatmentProgressListener(new ExportInitiatorAgentTest.ExportProgressListenerMock());

        story.record()
              .startTester("export-participant")
              .registerToDF("export-service")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .replyWith(AclMessage.Performative.NOT_UNDERSTOOD, "error message");

        story.record().assertNumberOfAgentWithService(1, "export-service");

        story.record().startAgent("export-initiator", agent);

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                log.assertContent("handleError(handleNotUnderstood received: error message)");
            }
        });

        story.execute();
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


    private class ExportProgressListenerMock implements ImportExportProgresListener {
        public void handleInform(String infoMessage) {
            log.call("handleInform", infoMessage);
        }


        public void handleInform(String[][] quarantine) {
            ;
        }


        public void handleError(String errorMessage) {
            log.call("handleError", errorMessage);
        }
    }
}
