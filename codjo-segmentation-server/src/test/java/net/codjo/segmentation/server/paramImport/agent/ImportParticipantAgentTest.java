/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.SubStep;
import net.codjo.mad.common.ZipUtil;
import net.codjo.segmentation.server.ParseException;
import net.codjo.segmentation.server.paramImport.AbstractParserManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureParserManager;
import net.codjo.tokio.TokioFixture;
import java.sql.Connection;
import junit.framework.TestCase;
/**
 *
 */
@SuppressWarnings({"InnerClassTooDeeplyNested"})
public class ImportParticipantAgentTest extends TestCase {
    private Story story = new Story();
    private ImportParticipantAgent agent;
    private TokioFixture tokioFixture = new TokioFixture(ImportParticipantAgentTest.class);


    public void test_register() throws Exception {
        story.record().startAgent("import-agent", agent);

        story.record().assertNumberOfAgentWithService(1, ImportParticipantAgent.IMPORT_SERVICE);

        story.execute();
    }


    public void test_importFile_classification() throws Exception {
        tokioFixture.insertInputInDb("importClassificationFile");

        story.record().startAgent("import-agent", agent);

        String importFile =
              "CLASSIFICATION;CLASSIFICATION_ID\tCLASSIFICATION_NAME\tCLASSIFICATION_TYPE\n"
              + "2\tAxe event 2\tEVENT\n"
              + "2\tAxe event repeated\tEVENT\n"
              + "4\tAxe event 3\tEVENT";

        final String quarantineExpected =
              "CLASSIFICATION_ID\tCLASSIFICATION_NAME\tCLASSIFICATION_TYPE\tANOMALY_LOG\n"
              + "2\tAxe event 2\tEVENT\tDoublon de l'id Axe dans le fichier\n"
              + "2\tAxe event repeated\tEVENT\tDoublon de l'id Axe dans le fichier\n";

        story.record().startTester("initiator")
              .sendMessage(createMessage(ZipUtil.zip(importFile)))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.AGREE))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.INFORM))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = ZipUtil.unzip(aclMessage.getByteSequenceContent());
                      assertEquals(quarantineExpected, contents);
                  }
              });

        story.execute();

        tokioFixture.assertAllOutputs("importClassificationFile");
    }


    public void test_importFile_classificationStructure() throws Exception {
        tokioFixture.insertInputInDb("importClassificationStructureFile");

        String importFile =
              "CLASSIFICATION_ID\tFORMULA\tSLEEVE_CODE\tSLEEVE_DUSTBIN\tSLEEVE_NAME\tTERMINAL_ELEMENT\n"
              + "1\tmanager == 1\t01-1\t0\tPoche 1\t1\n"
              + "1\t \t01-2\t1\tFourre-tout\t1\n"
              + "2\tmanager in (\"moner\")\t01-1\t0\tPoche 2\t1\n"
              + "2\tmanager not in (\"panicol\")\t01-2\t0\tPoche 3\t1\n";

        agent = new ImportParticipantAgentMock(importFile);

        story.record().startAgent("import-agent", agent);

        final String quarantineExpected =
              "CLASSIFICATION_ID\tFORMULA\tSLEEVE_CODE\tSLEEVE_DUSTBIN\tSLEEVE_NAME\tTERMINAL_ELEMENT\tANOMALY_LOG\n"
              + "2\tmanager in (\"moner\")\t01-1\t0\tPoche 2\t1\tCet axe ne comporte pas de poche fourre-tout\n"
              + "2\tmanager not in (\"panicol\")\t01-2\t0\tPoche 3\t1\tCet axe ne comporte pas de poche fourre-tout\n";

        story.record().startTester("initiator")
              .sendMessage(createMessage(ZipUtil.zip("CLASSIFICATION_STRUCTURE;" + importFile)))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.AGREE))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.INFORM))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      String contents = ZipUtil.unzip(aclMessage.getByteSequenceContent());
                      assertEquals(quarantineExpected, contents);
                  }
              });

        story.execute();

        tokioFixture.assertAllOutputs("importClassificationStructureFile");
    }


    public void test_corruptedMessageContents() throws Exception {
        story.record().startAgent("import-agent", agent);

        String contents =
              "CLASSIFICATION;CLASSIFICATION_ID\tCLASSIFICATION_NAME\tCLASSIFICATION_TYPE\tWRONG_AXE\n"
              + "2\tAxe event 2\tEVENT\n" + "3\tAxe event 3\tEVENT";

        AclMessage message = createMessage(ZipUtil.zip(contents));

        story.record().startTester("initiator").sendMessage(message).then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.NOT_UNDERSTOOD))
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage aclMessage) throws Exception {
                      assertTrue(aclMessage.getContent().contains(ParseException.BAD_FILE_FORMAT));
                  }
              });

        story.execute();
    }


    private AclMessage createMessage(byte[] content) {
        AclMessage message = new AclMessage(AclMessage.Performative.REQUEST);
        message.setByteSequenceContent(content);
        message.addReceiver(new Aid("import-agent"));
        return message;
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
        tokioFixture.doSetUp();
        agent = new ImportParticipantAgent() {
            @Override
            protected Connection getConnection(AclMessage message) {
                return tokioFixture.getConnection();
            }
        };
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
        tokioFixture.doTearDown();
    }


    private class ImportParticipantAgentMock extends ImportParticipantAgent {
        private String rawData;


        ImportParticipantAgentMock(String rawData) {
            this.rawData = rawData;
        }


        @Override
        protected Connection getConnection(AclMessage message) {
            return tokioFixture.getConnection();
        }


        @Override
        protected AbstractParserManager getParserManager() {
            return new ClassificationStructureParserManager(rawData) {
                int count = 1000;


                @Override
                protected String getTimestamp() {
                    return "" + count++;
                }
            };
        }
    }
}
