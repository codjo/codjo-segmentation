package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class Erase extends AbstractBlackboardAction {
    public Erase(Todo todo, Level level) {
        super(todo, level);
    }


    @Override
    public void acceptVisitor(BlackboardActionVisitor visitor) {
        visitor.visit(this);
    }
}
