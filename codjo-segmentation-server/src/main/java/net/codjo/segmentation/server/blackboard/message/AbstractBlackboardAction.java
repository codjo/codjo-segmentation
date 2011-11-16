package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
abstract class AbstractBlackboardAction extends BlackboardAction {
    protected final Todo todo;
    protected final Level level;


    protected AbstractBlackboardAction(Todo todo, Level level) {
        this.todo = todo;
        this.level = level;
    }


    public Todo getTodo() {
        return todo;
    }


    public Level getLevel() {
        return level;
    }
}
