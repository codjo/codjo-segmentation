package net.codjo.segmentation.server.blackboard.message;
import net.codjo.test.common.LogString;
/**
 *
 */
public class BlackboardActionVisitorMock implements BlackboardActionVisitor {
    protected LogString log = new LogString();


    public BlackboardActionVisitorMock() {
    }


    public BlackboardActionVisitorMock(LogString log) {
        this.log = log;
    }


    public void visit(Write write) {
        log.call("visit", "write(" + toString(write.getTodo()) + ")");
    }


    public void visit(GetTodo getTodo) {
        log.call("visit", "getTodo(" + toString(getTodo.getLevel()) + ")");
    }


    public void visit(Erase erase) {
        log.call("visit", "erase(" + toString(erase.getLevel()) + ", " + toString(erase.getTodo()) + ")");
    }


    public void visit(InformOfFailure failure) {
        log.call("visit", "informOfFailure(" + toString(failure.getLevel())
                          + ", " + toString(failure.getTodo()) + ")");
    }


    private String toString(Todo todo) {
        if (todo.getId() != -1) {
            return "todo:" + todo.getId();
        }
        else {
            return "todo:" + todo.getContent();
        }
    }


    private String toString(Level todo) {
        return "level:" + todo.getName();
    }
}
