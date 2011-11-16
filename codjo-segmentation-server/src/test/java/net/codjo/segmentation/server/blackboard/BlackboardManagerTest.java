package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.AclMessage;
import net.codjo.agent.protocol.SubscribeParticipant;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionBuilder;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import java.util.List;
import junit.framework.TestCase;
/**
 *
 */
public class BlackboardManagerTest extends TestCase {
    private static final Level LEVEL_A = new Level("a");


    public void test_addTodo() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("b"));

        assertNull(manager.startFirstTodo(LEVEL_A));

        Todo todo = new Todo();
        manager.addTodo(LEVEL_A, todo);
        assertEquals(1, todo.getId());

        assertTrue(manager.todoExists(LEVEL_A, todo));
        assertFalse(manager.todoExists(LEVEL_A, new Todo(5)));
        assertSame(todo, manager.startFirstTodo(LEVEL_A));
    }


    public void test_addTodo_nextLevel() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("final"));

        BlackboardActionBuilder builder = new BlackboardActionBuilder(false);

        manager.addTodo(builder.nextLevel(LEVEL_A), new Todo());

        assertEquals(1, manager.getLastTodos().size());
    }


    public void test_removeTodo() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("b"));

        Todo todo = new Todo();
        manager.addTodo(LEVEL_A, todo);
        manager.removeTodo(LEVEL_A, todo);

        assertNull(manager.startFirstTodo(LEVEL_A));
    }


    public void test_runningTodo() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("b"));

        Todo firstTodo = new Todo();
        manager.addTodo(LEVEL_A, firstTodo);
        manager.addTodo(LEVEL_A, new Todo());

        manager.startFirstTodo(LEVEL_A);
        assertEquals(1, manager.getRunningTodoCount());

        manager.removeTodo(LEVEL_A, new Todo(firstTodo.getId()));
        assertEquals(0, manager.getRunningTodoCount());

        manager.removeTodo(LEVEL_A, new Todo(69));
        assertEquals(0, manager.getRunningTodoCount());
    }


    public void test_isFinished() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("b"), new Level("final"));

        assertTrue(manager.isFinished());

        Todo todo = new Todo();
        manager.addTodo(LEVEL_A, todo);

        assertFalse(manager.isFinished());

        manager.startFirstTodo(LEVEL_A);

        assertFalse(manager.isFinished());

        manager.addTodo(new Level("final"), new Todo<String>("last result"));
        manager.removeTodo(LEVEL_A, todo);

        assertTrue(manager.isFinished());

        List<Todo> lastTodos = manager.getLastTodos();

        assertEquals(1, lastTodos.size());
        assertEquals("last result", lastTodos.get(0).getContent());
    }


    public void test_reset() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("b"), new Level("final"));

        manager.addTodo(LEVEL_A, new Todo());
        manager.removeTodo(LEVEL_A, new Todo(1));

        manager.addTodo(new Level("final"), new Todo());

        assertTrue(manager.isFinished());

        manager.reset();

        assertTrue(manager.getLastTodos().isEmpty());

        assertNextIdRestartFrom(0, manager);
    }


    public void test_addSubscriber() throws Exception {
        BlackboardManager manager = new BlackboardManager(LEVEL_A, new Level("b"));
        SubscriptionMock subscription = new SubscriptionMock();

        assertEquals(0, manager.getSubscription(LEVEL_A).size());

        manager.addSubscription(LEVEL_A, subscription);

        assertEquals(1, manager.getSubscription(LEVEL_A).size());
        assertSame(subscription, manager.getSubscription(LEVEL_A).get(0));

        manager.removeSubscription(LEVEL_A, subscription);

        assertEquals(0, manager.getSubscription(LEVEL_A).size());
    }


    private void assertNextIdRestartFrom(int expected, BlackboardManager manager) {
        Todo todo = new Todo();
        manager.addTodo(LEVEL_A, todo);
        assertEquals(expected + 1, todo.getId());
    }


    private static class SubscriptionMock implements SubscribeParticipant.Subscription {

        public AclMessage getMessage() {
            return null;
        }


        public void reply(AclMessage messageToSend) {
        }


        public void close() {
        }
    }
}
