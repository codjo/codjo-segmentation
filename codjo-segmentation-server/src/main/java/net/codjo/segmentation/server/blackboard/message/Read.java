package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class Read {
    private final Todo todo;
    private final Level level;


    public Read(Level level, Todo todo) {
        this.level = level;
        this.todo = todo;
    }


    public Todo getTodo() {
        return todo;
    }


    public Level getLevel() {
        return level;
    }
}
