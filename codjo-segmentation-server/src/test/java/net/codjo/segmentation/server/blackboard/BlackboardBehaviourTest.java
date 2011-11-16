package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.AclMessage;
import net.codjo.agent.DFService;
import net.codjo.agent.UserId;
import net.codjo.agent.test.AgentAssert;
import static net.codjo.agent.test.AgentAssert.log;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.Story;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.test.common.LogString;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 */
public class BlackboardBehaviourTest extends TestCase {
    private static final Level LEVEL_A = new Level("level-A");
    private static final Level LEVEL_B = new Level("level-B");
    private static final Level LEVEL_FINAL = new Level("final");
    private Story story = new Story();
    private LogString log = new LogString();
    private BlackboardBehaviour blackboardBehaviour;
    private BlackboardListenerMock listener;


    public void test_subscription() throws Exception {
        final BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A);

        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().assertAgentWithService(new String[]{"blackboard"}, "blackboard-service");

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                assertEquals(1, blackboardBehaviour.getParticipantCount(LEVEL_A));
            }
        });

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                blackboardBehaviour.setUserId(UserId.createId("john", "mysecret"));
                blackboardBehaviour.postTodo(LEVEL_A, new Todo(1));
            }
        });

        story.record().addAssert(log(log, "handleConnectedToBlackBoard(AGREE), "
                                          + "listener.todoWrited(level:level-A, todo:1), "
                                          + "handleTodoNotification(level-A), message avec userid(john)"));

        story.execute();
    }


    public void test_writeTodo() throws Exception {
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(10), LEVEL_A));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-A, todo:1)"
                                          + ", handleTodoNotification(level-A)"));

        story.execute();
    }


    public void test_writeTodo_erasedInListener() throws Exception {
        listener.mockEraseWritedTodos();
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(1), LEVEL_A));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-A, todo:1)"
                                          + ", listener.blackboardFinished([])"));

        story.execute();
    }


    public void test_writeTodoNextLevel() throws Exception {
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_B) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(10), nextLevel(LEVEL_A)));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-B, todo:1), "
                                          + "handleTodoNotification(level-B)"));

        story.execute();
    }


    public void test_getTodo() throws Exception {
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_B) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(1), LEVEL_A)
                      .then()
                      .write(new Todo(2), LEVEL_B));
            }


            @Override
            public void handleTodoNotification(Level level) {
                super.handleTodoNotification(level);
                send(getTodo(nextLevel(LEVEL_A)));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-A, todo:1), "
                                          + "listener.todoWrited(level:level-B, todo:2), "
                                          + "handleTodoNotification(level-B)"
                                          + ", handleTodo(level-B, todo:2)"));

        story.execute();
    }


    public void test_eraseTodo() throws Exception {
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(1), LEVEL_B)
                      .then()
                      .erase(new Todo(1), nextLevel(LEVEL_A)));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-B, todo:1), "
                                          + "listener.blackboardFinished([])"));

        story.execute();
    }


    public void test_informOfFailure() throws Exception {
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(1), LEVEL_B)
                      .then()
                      .informOfFailure(new Todo(1), nextLevel(LEVEL_A)).dueTo("a un acces BD"));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-B, todo:1), "
                                          + "listener.informOfFailure(level:level-B, todo:1, a un acces BD)"));

        story.execute();
    }


    public void test_specificAgentDescription() throws Exception {
        DFService.AgentDescription searchDescription = new DFService.AgentDescription();
        searchDescription.addService(new DFService.ServiceDescription("type-a"));
        searchDescription.addService(new DFService.ServiceDescription("type-b"));

        final DummyAgent blackBoardAgent = new DummyAgent(blackboardBehaviour);
        blackboardBehaviour.doNotRegisterToDf();

        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A, searchDescription) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(10), LEVEL_A));
            }
        };

        story.record().startAgent("blackboard", blackBoardAgent);
        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                DFService.AgentDescription description = new DFService.AgentDescription();
                description.addService(new DFService.ServiceDescription("type-a", "name-a"));
                description.addService(new DFService.ServiceDescription("type-b", "name-b"));
                DFService.register(blackBoardAgent, description);
            }
        });
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "listener.todoWrited(level:level-A, todo:1), "
                                          + "handleTodoNotification(level-A)"));

        story.execute();
    }


    public void test_blackboardFinished() throws Exception {
        BlackboardParticipant participant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                send(write(new Todo(1), LEVEL_A));
            }


            @Override
            public void handleTodoNotification(Level level) {
                send(erase(new Todo(1), LEVEL_A)
                      .then()
                      .write(new Todo(2), LEVEL_B));
            }
        };
        BlackboardParticipant participantLevelB = new BlackboardParticipantMock(LEVEL_B) {

            @Override
            public void handleTodoNotification(Level level) {
                super.handleTodoNotification(level);
                send(getTodo(level));
            }


            @Override
            protected void handleTodo(Todo todo, Level fromLevel) {
                send(erase(todo, fromLevel));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker-levelb", new DummyAgent(participantLevelB.toBehaviour()));
        story.record().startAgent("worker", new DummyAgent(participant.toBehaviour()));

        story.record().addAssert(log(log, "handleConnectedToBlackBoard(AGREE), "
                                          + "listener.todoWrited(level:level-A, todo:1), "
                                          + "listener.todoWrited(level:level-B, todo:2), "
                                          + "handleTodoNotification(level-B), "
                                          + "listener.blackboardFinished([])"));

        story.execute();
    }


    public void test_blackboardFinished_alreadyFinished() throws Exception {
        BlackboardParticipant getterParticipant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                super.handleConnectedToBlackBoard(message);
                send(getTodo(LEVEL_A));
            }
        };
        BlackboardParticipant writerParticipant = new BlackboardParticipantMock(LEVEL_A) {

            @Override
            protected void handleConnectedToBlackBoard(AclMessage message) {
                super.handleConnectedToBlackBoard(message);
                send(write(new Todo(1), LEVEL_B));
            }
        };

        story.record().startAgent("blackboard", new DummyAgent(blackboardBehaviour));
        story.record().startAgent("worker-get", new DummyAgent(getterParticipant.toBehaviour()));

        story.record().addAssert(log(log, "handleConnectedToBlackBoard(AGREE)"));

        story.record().startAgent("worker-write", new DummyAgent(writerParticipant.toBehaviour()));

        story.record().addAssert(log(log, "handleConnectedToBlackBoard(AGREE), "
                                          + "handleConnectedToBlackBoard(AGREE), "
                                          + "listener.todoWrited(level:level-B, todo:1)"));

        story.execute();
    }


    @Override
    protected void setUp() throws Exception {
        listener = new BlackboardListenerMock(new LogString("listener", log));
        blackboardBehaviour = new BlackboardBehaviour(listener, LEVEL_A, LEVEL_B, LEVEL_FINAL);
        listener.setBlackboard(blackboardBehaviour);
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private class BlackboardParticipantMock extends BlackboardParticipant {
        BlackboardParticipantMock(Level level, DFService.AgentDescription blackBoardDescription) {
            super(level, blackBoardDescription);
        }


        BlackboardParticipantMock(Level level) {
            super(level);
        }


        @Override
        protected void handleConnectedToBlackBoard(AclMessage message) {
            log.call("handleConnectedToBlackBoard", message.getPerformative());
        }


        @Override
        public void handleTodoNotification(Level level) {
            log.call("handleTodoNotification", level.getName());
            if (getReceivedMessage().decodeUserId() != null) {
                log.info("message avec userid(" + getReceivedMessage().decodeUserId().getLogin() + ")");
            }
        }


        @Override
        protected void handleTodo(Todo todo, Level fromLevel) {
            log.call("handleTodo", fromLevel.getName(), "todo:" + todo.getId());
        }
    }
}
