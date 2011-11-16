package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import net.codjo.agent.Aid;
import net.codjo.agent.DFService;
import net.codjo.agent.DFService.AgentDescription;
import net.codjo.agent.MessageTemplate.MatchExpression;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchWith;
import net.codjo.agent.test.DummyAgent;
import static net.codjo.agent.test.MessageBuilder.message;
import net.codjo.agent.test.Story;
import net.codjo.segmentation.server.blackboard.message.InformOfFailure;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.MessageCodec;
import net.codjo.segmentation.server.blackboard.message.Read;
import net.codjo.segmentation.server.blackboard.message.Todo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class ParticipantWrapperBehaviourTest {
    private Story story = new Story();


    @Before
    public void setUp() throws Exception {
        story.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }


    @Test
    public void test_participantFailure() throws Exception {
        Level level = new Level("my-level");
        ErrorParticipantMock participant = new ErrorParticipantMock(level, "Error !!!");

        story.record()
              .startTester("my-tester")
              .registerToDF(createAgentDescription()).then()
              .send(message(Performative.INFORM)
                    .to(new Aid("my-participant"))
                    .usingProtocol(BlackboardBehaviour.BLACKBOARD_PROTOCOL)
                    .withContent(encodeRead(level, new Todo()))).then()
              .receiveMessage(matchPerformative(Performative.SUBSCRIBE)).then()
              .receiveMessage(matchWith(new MatchExpression() {
                  public boolean match(AclMessage aclMessage) {
                      InformOfFailure informOfFailure
                            = (InformOfFailure)new MessageCodec().decode(aclMessage);
                      return "Erreur : Error !!!".equals(informOfFailure.getErrorMessage());
                  }
              }));

        story.record()
              .startAgent("my-participant",
                          new DummyAgent(new ParticipantWrapperBehaviour(participant,
                                                                         level,
                                                                         createAgentDescription())));

        story.execute();
    }


    private String encodeRead(Level level, Todo todo) {
        AclMessage tmpMessage = new AclMessage(Performative.INFORM);
        new MessageCodec().encodeRead(tmpMessage, new Read(level, todo));
        return tmpMessage.getContent();
    }


    private AgentDescription createAgentDescription() {
        return new AgentDescription(new DFService.ServiceDescription("blackboard-service",
                                                                     "test-application"));
    }


    private static class ErrorParticipantMock extends BlackboardParticipant {
        private final String errorMessage;


        private ErrorParticipantMock(Level level, String errorMessage) {
            super(level);
            this.errorMessage = errorMessage;
        }


        @Override
        protected void handleTodo(Todo todo, Level fromLevel) {
            throw new Error(errorMessage);
        }
    }
}
