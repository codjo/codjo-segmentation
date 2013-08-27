package net.codjo.segmentation.server.participant;
import net.codjo.agent.DFService;
import net.codjo.segmentation.server.blackboard.JdbcBlackboardParticipant;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.participant.context.SegmentationReport;
import net.codjo.segmentation.server.participant.context.SegmentationReporter;
import net.codjo.segmentation.server.participant.context.SessionContext;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.workflow.common.message.Arguments;
/**
 *
 */
public abstract class SegmentationParticipant<T> extends JdbcBlackboardParticipant<T> {
    public static final String SEGMENTATION_SERVICE = "segmentation";
    public static final String BLACKBOARD_SERVICE = "blackboard";
    protected final ContextManager contextManager;
    private final Level level;


    protected SegmentationParticipant(ContextManager contextManager, TransactionType type, Level level) {
        super(new JdbcServiceUtil(), type, level, createSegmentationDescription());
        this.contextManager = contextManager;
        this.level = level;
    }


    private static DFService.AgentDescription createSegmentationDescription() {
        DFService.AgentDescription description = new DFService.AgentDescription();
        description.addService(new DFService.ServiceDescription(SEGMENTATION_SERVICE));
        description.addService(new DFService.ServiceDescription(BLACKBOARD_SERVICE));
        return description;
    }


    protected Arguments createAudit(String[][] content) {
        Arguments argument = new Arguments();
        for (String[] property : content) {
            argument.put(property[0], property[1]);
        }
        return argument;
    }


    protected final SegmentationReport createReport() {
        return contextManager.createReport();
    }


    protected final SegmentationReport getReport(Todo<TodoContent> todo) {
        SessionContext context = contextManager.getSessionContext(todo);
        return (context == null) ? SegmentationReporter.NONE.create() : context.getReport();
    }


    protected String getName() {
        return level.getName();
    }
}
