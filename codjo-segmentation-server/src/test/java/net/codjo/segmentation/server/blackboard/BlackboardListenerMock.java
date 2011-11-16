package net.codjo.segmentation.server.blackboard;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.test.common.LogString;
import java.util.List;
/**
 *
 */
public class BlackboardListenerMock implements BlackboardListener {
    private LogString log;
    private boolean eraseWritedTodos = false;
    private BlackboardBehaviour blackboard;


    public BlackboardListenerMock(LogString log) {
        this.log = log;
    }


    public void todoWrited(Level level, Todo todo) {
        log.call("todoWrited", "level:" + level.getName(), "todo:" + todo.getId());
        if (eraseWritedTodos) {
            blackboard.removeTodo(level, todo);
        }
    }


    public void informOfFailure(Level level, Todo todo, String error) {
        log.call("informOfFailure", "level:" + level.getName(), "todo:" + todo.getId(), error);
    }


    public void blackboardFinished(List<Todo> lastTodos) {
        log.call("blackboardFinished", lastTodos);
    }


    public void mockEraseWritedTodos() {
        this.eraseWritedTodos = true;
    }


    public void setBlackboard(BlackboardBehaviour blackboard) {
        this.blackboard = blackboard;
    }
}
