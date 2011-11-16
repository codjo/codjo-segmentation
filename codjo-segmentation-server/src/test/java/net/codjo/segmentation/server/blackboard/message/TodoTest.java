package net.codjo.segmentation.server.blackboard.message;
import junit.framework.TestCase;
/**
 *
 */
public class TodoTest extends TestCase {
    public void test_equalsHashcode() throws Exception {
        Todo todo = new Todo(1);
        assertTrue(todo.equals(new Todo(1)));
        assertEquals(todo.hashCode(), new Todo(1).hashCode());

        assertFalse(todo.equals(new Todo(2)));
        assertFalse(todo.equals(new Todo(3)));

        //noinspection ObjectEqualsNull
        assertFalse(todo.equals(null));
        assertFalse(todo.equals(new Object()));
    }
}
