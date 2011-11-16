package net.codjo.segmentation.server.blackboard;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import java.util.List;
/**
 *
 */
public interface BlackboardListener {
    void todoWrited(Level level, Todo todo);


    void informOfFailure(Level level, Todo todo, String error);


    void blackboardFinished(List<Todo> lastTodos);
}
