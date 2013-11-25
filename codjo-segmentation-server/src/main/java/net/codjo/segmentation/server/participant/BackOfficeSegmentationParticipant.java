package net.codjo.segmentation.server.participant;
import net.codjo.segmentation.server.blackboard.ErrorLogLimiter;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.participant.context.TodoContent;
/**
 *
 */
public abstract class BackOfficeSegmentationParticipant extends SegmentationParticipant<TodoContent> {
    protected BackOfficeSegmentationParticipant(ContextManager contextManager, TransactionType type, Level level) {
        super(contextManager, type, level);
    }


    @Override
    protected final ErrorLogLimiter getErrorLogLimiter(Todo<TodoContent> todo) {
        return contextManager.getSessionContext(todo).getErrorLogLimiter();
    }
}
