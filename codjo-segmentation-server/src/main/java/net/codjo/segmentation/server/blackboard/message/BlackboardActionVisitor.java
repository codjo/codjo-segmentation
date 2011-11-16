package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public interface BlackboardActionVisitor {
    void visit(Write write);


    void visit(GetTodo getTodo);


    void visit(Erase erase);


    void visit(InformOfFailure informOfFailure);
}
