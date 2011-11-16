package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class Write extends AbstractBlackboardAction {

    public Write(Todo todo, Level level) {
        super(encapsulate(todo), level);
    }


    private static Todo encapsulate(Todo todo) {
        if (todo.getId() != -1) {
            return new Todo<Object>(todo.getContent());
        }
        else {
            return todo;
        }
    }


    @Override
    public void acceptVisitor(BlackboardActionVisitor visitor) {
        visitor.visit(this);
    }
}
