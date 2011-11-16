package net.codjo.segmentation.server.blackboard.message;
import net.codjo.test.common.LogString;
/**
 *
 */
public class BlackboardActionStringifier implements BlackboardActionVisitor {
    protected LogString log = new LogString();


    public BlackboardActionStringifier(LogString log) {
        this.log = log;
    }


    public void logify(BlackboardAction action) {
        // todo a mettre un comportement intelligent dans acceptVisitor des actions
        if (action.hasBlackBoardActionBuilder()) {
            action.then().visit(this);
        }
        else {
            action.acceptVisitor(this);
        }
    }


    public void visit(Write write) {
        log.call("write", toString(write.getTodo()), toString(write.getLevel()));
    }


    public void visit(GetTodo getTodo) {
        log.call("getTodo", toString(getTodo.getLevel()));
    }


    public void visit(Erase erase) {
        log.call("erase", toString(erase.getTodo()), toString(erase.getLevel()));
    }


    public void visit(InformOfFailure failure) {
        log.call("informOfFailure", toString(failure.getLevel()), toString(failure.getTodo()));
    }


    protected String toString(Todo todo) {
        return "Todo{" + todo.getId() + "}";
    }


    protected String toString(Level level) {
        return level.toString();
    }
}
