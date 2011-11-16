package net.codjo.segmentation.server.paramExport.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.SubStep;
import net.codjo.test.common.fixture.DirectoryFixture;
import net.codjo.tokio.TokioFixture;
import net.codjo.util.file.FileUtil;
import java.io.File;
import java.sql.Connection;
import junit.framework.TestCase;
/**
 *
 */
public class ExportParticipantAgentTest extends TestCase {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String TAB = "\t";
    private TokioFixture tokioFixture = new TokioFixture(ExportParticipantAgentTest.class);
    private DirectoryFixture directoryFixture = DirectoryFixture.newTemporaryDirectoryFixture();
    private ExportParticipantAgent agent;
    private Story story = new Story();


    public void test_registerDf() throws Exception {
        story.record().startAgent("export-agent", agent);
        story.record().assertNumberOfAgentWithService(1, ExportParticipantAgent.EXPORT_SERVICE);
        story.execute();
    }


    public void test_exportFile_classification() throws Exception {
        tokioFixture.insertInputInDb("exportClassificationFile");

        story.record().startAgent("export-agent", agent);

        final String exportFilePath = directoryFixture.getAbsolutePath() + "\\CLASSIFICATION.txt";
        String message = "CLASSIFICATION;" + exportFilePath;

        story.record().startTester("initiator")
              .sendMessage(createMessage(message))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.AGREE))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.INFORM))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = aclMessage.getContent();
                      assertEquals("Export terminé", contents);
                      assertTrue(new File(exportFilePath).exists());
                      String expectedFileContent =
                            "CLASSIFICATION_ID" + TAB + "CLASSIFICATION_NAME" + TAB + "CLASSIFICATION_TYPE"
                            + TAB + "CUSTOM_FIELD" + NEW_LINE
                            + "1" + TAB + "Axe event 1" + TAB + "EVENT" + TAB + NEW_LINE
                            + "2" + TAB + "Axe event 2" + TAB + "EVENT" + TAB + NEW_LINE;
                      assertEquals(expectedFileContent, FileUtil.loadContent(new File(exportFilePath)));
                  }
              });

        story.execute();
    }


    public void test_exportFile_classificationStructure() throws Exception {
        tokioFixture.insertInputInDb("exportClassificationStructureFile");

        story.record().startAgent("export-agent", agent);

        final String exportFilePath = directoryFixture.getAbsolutePath() + "\\CLASSIFICATION_STRUCTURE.txt";
        String message = "CLASSIFICATION_STRUCTURE;" + exportFilePath;

        story.record().startTester("initiator")
              .sendMessage(createMessage(message))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.AGREE))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.INFORM))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = aclMessage.getContent();
                      assertEquals("Export terminé", contents);
                      assertTrue(new File(exportFilePath).exists());
                      String expectedFileContent =
                            "CLASSIFICATION_ID" + TAB + "SLEEVE_CODE" + TAB + "SLEEVE_NAME" + TAB
                            + "SLEEVE_DUSTBIN" + TAB + "TERMINAL_ELEMENT" + TAB + "FORMULA"
                            + TAB + "CUSTOM_FIELD" + NEW_LINE

                            + "1" + TAB + "01-1" + TAB + "Poche 1" + TAB + "0" + TAB + "1" + TAB
                            + "manager == 1" + TAB + NEW_LINE

                            + "1" + TAB + "01-2" + TAB + "Fourre-tout" + TAB + "1" + TAB + "1" + TAB + " "
                            + TAB + NEW_LINE;
                      assertEquals(expectedFileContent, FileUtil.loadContent(new File(exportFilePath)));
                  }
              });

        story.execute();
    }


    public void test_corruptedMessageContents() throws Exception {
        story.record().startAgent("export-agent", agent);

        String contents = "CLASSIFICATION;";

        AclMessage message = createMessage(contents);

        story.record().startTester("initiator").sendMessage(message).then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.NOT_UNDERSTOOD))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      assertTrue(aclMessage.getContent().contains("Message incorrect."));
                  }
              });

        story.execute();
    }


    private AclMessage createMessage(String content) {
        AclMessage message = new AclMessage(AclMessage.Performative.REQUEST);
        message.setContent(content);
        message.addReceiver(new Aid("export-agent"));
        return message;
    }


    @Override
    protected void setUp() throws Exception {
        tokioFixture.doSetUp();
        directoryFixture.doSetUp();
        story.doSetUp();
        agent = new ExportParticipantAgent() {
            @Override
            protected Connection getConnection(AclMessage message) {
                return tokioFixture.getConnection();
            }
        };
    }


    @Override
    protected void tearDown() throws Exception {
        tokioFixture.doTearDown();
        directoryFixture.doTearDown();
        story.doTearDown();
    }
}
