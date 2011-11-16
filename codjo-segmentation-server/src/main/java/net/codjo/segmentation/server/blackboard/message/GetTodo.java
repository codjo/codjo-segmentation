package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class GetTodo extends BlackboardAction {
    private final Level level;


    public GetTodo(Level level) {
        this.level = level;
    }


    public Level getLevel() {
        return level;
    }


    @Override
    public void acceptVisitor(BlackboardActionVisitor visitor) {
        visitor.visit(this);
    }
}
