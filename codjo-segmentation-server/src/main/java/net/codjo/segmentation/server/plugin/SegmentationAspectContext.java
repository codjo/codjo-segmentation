package net.codjo.segmentation.server.plugin;
import net.codjo.aspect.AspectContext;
import net.codjo.aspect.util.TransactionalPoint;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.agent.UserId;
import net.codjo.agent.AgentContainer;
import java.sql.Connection;
public class SegmentationAspectContext {
    private static final String ARGUMENTS_KEY = "arguments";
    private static final String AUDIT_KEY = "audit";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String AGENT_CONTAINER_KEY = "agentContainer";
    private AspectContext context;


    public SegmentationAspectContext() {
        this(new AspectContext());
    }


    public SegmentationAspectContext(AspectContext context) {
        this.context = context;
    }


    public Connection getConnection() {
        return (Connection)context.get(TransactionalPoint.CONNECTION);
    }


    public void setConnection(Connection connection) {
        context.put(TransactionalPoint.CONNECTION, connection);
    }


    public Arguments getArguments() {
        return (Arguments)context.get(ARGUMENTS_KEY);
    }


    public void setArguments(Arguments arguments) {
        context.put(ARGUMENTS_KEY, arguments);
    }


    public JobAudit getAudit() {
        return (JobAudit)context.get(AUDIT_KEY);
    }


    public void setAudit(JobAudit audit) {
        context.put(AUDIT_KEY, audit);
    }


    public String getRequestId() {
        return (String)context.get(REQUEST_ID_KEY);
    }


    public void setRequestId(String requestId) {
        context.put(REQUEST_ID_KEY, requestId);
    }


    public UserId getUserId() {
        return (UserId)context.get(USER_ID_KEY);
    }


    public void setUserId(UserId userId) {
        context.put(USER_ID_KEY, userId);
    }


    public AgentContainer getAgentContainer() {
        return (AgentContainer)context.get(AGENT_CONTAINER_KEY);
    }


    public void setAgentContainer(AgentContainer agentContainer) {
        context.put(AGENT_CONTAINER_KEY, agentContainer);
    }

    public AspectContext toAspectContext() {
        return context;
    }
}
