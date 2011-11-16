package net.codjo.segmentation.server.blackboard;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import java.util.List;
/**
 *
 */
public class BlackboardListenerAdapter implements BlackboardListener {
    public void informOfFailure(Level level, Todo todo, String error) {
    }


    public void blackboardFinished(List<Todo> lastTodos) {
    }


    public void todoWrited(Level level, Todo todo) {
    }
}
