package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class Level {
    private final String name;


    public Level(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Level level = (Level)object;

        return !(name != null ? !name.equals(level.name) : level.name != null);
    }


    @Override
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }


    @Override
    public String toString() {
        return "Level{" + name + '}';
    }


    int acceptVisitor(LevelVisitor visitor) {
        return visitor.visit(this);
    }
}
