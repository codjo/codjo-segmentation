package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
class NextLevel extends Level {
    private final Level current;


    NextLevel(Level level) {
        super(null);
//        super("next(" + level.getName() + ")");
        this.current = level;
    }


    public Level getCurrent() {
        return current;
    }


    @Override
    int acceptVisitor(LevelVisitor visitor) {
        return visitor.visit(this);
    }


    @Override
    public String toString() {
        return "NextLevel{" + current + '}';
    }
}
