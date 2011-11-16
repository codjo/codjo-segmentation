package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
interface LevelVisitor {
    int visit(Level level);


    int visit(NextLevel level);
}
