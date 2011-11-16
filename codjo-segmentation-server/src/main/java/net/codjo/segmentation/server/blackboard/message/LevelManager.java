/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class LevelManager {
    private final Level[] levels;
    private IndexFinder finder = new IndexFinder();


    public LevelManager(Level... levels) {
        this.levels = levels;
    }


    public int indexOf(Level level) {
        int index = level.acceptVisitor(finder);
        if (index >= levels.length) {
            return -1;
        }
        return index;
    }


    public Level getLevel(int index) {
        if (index >= levels.length || index < 0) {
            return null;
        }
        return levels[index];
    }


    public Level getLastLevel() {
        return levels[levels.length - 1];
    }


    private class IndexFinder implements LevelVisitor {
        public int visit(Level level) {
            return findLevelIndex(level);
        }


        public int visit(NextLevel level) {
            int index = level.getCurrent().acceptVisitor(this);
            if (index == -1) {
                return -1;
            }
            return index + 1;
        }


        private int findLevelIndex(Level level) {
            for (int i = 0; i < levels.length; i++) {
                if (level.equals(levels[i])) {
                    return i;
                }
            }
            return -1;
        }
    }
}
