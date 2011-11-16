package net.codjo.segmentation.server.blackboard.message;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public class BlackboardActionBuilder {
    private final List<BlackboardAction> actions;


    protected BlackboardActionBuilder(BlackboardAction action) {
        this(true);
        actions.add(action);
    }


    public BlackboardActionBuilder(boolean connectToActions) {
        if (connectToActions) {
            actions = new ArrayList<BlackboardAction>();
        }
        else {
            actions = null;
        }
    }


    public Write write(Todo todo, Level level) {
        return connectActionToBuilder(new Write(todo, level));
    }


    public Erase erase(Todo todo, Level level) {
        return connectActionToBuilder(new Erase(todo, level));
    }


    public InformOfFailure informOfFailure(Todo todo, Level level) {
        return connectActionToBuilder(new InformOfFailure(todo, level));
    }


    public GetTodo getTodo(Level level) {
        return connectActionToBuilder(new GetTodo(level));
    }


    public Level nextLevel(Level level) {
        return new NextLevel(level);
    }


    private <T extends BlackboardAction> T connectActionToBuilder(T action) {
        if (actions == null) {
            return action;
        }
        action.setBuilder(this);
        actions.add(action);
        return action;
    }


    void visit(BlackboardActionVisitor visitor) {
        for (BlackboardAction action : actions) {
            action.acceptVisitor(visitor);
        }
    }
}