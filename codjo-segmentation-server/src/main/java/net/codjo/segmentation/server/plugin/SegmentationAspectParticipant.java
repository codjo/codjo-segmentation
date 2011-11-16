package net.codjo.segmentation.server.plugin;
import net.codjo.aspect.AspectManager;
import net.codjo.aspect.util.PointRunner;
import net.codjo.aspect.util.PointRunnerException;
import net.codjo.aspect.util.TransactionalPoint;
import net.codjo.sql.server.ConnectionPool;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.common.message.JobAudit.Anomaly;
import net.codjo.workflow.common.message.JobException;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.protocol.JobProtocolParticipant;
import java.sql.Connection;

class SegmentationAspectParticipant extends JobProtocolParticipant {
    private final JdbcServiceUtil jdbcServiceUtil;
    private final AspectManager aspectManager;


    SegmentationAspectParticipant(JdbcServiceUtil jdbcServiceUtil, AspectManager aspectManager) {
        this.jdbcServiceUtil = jdbcServiceUtil;
        this.aspectManager = aspectManager;
    }


    @Override
    protected void handlePRE(final JobRequest request) {
        transactionalPointCreate(request, createPreAudit(request));
    }


    @Override
    protected void handlePOST(JobRequest request, JobException failure) {
        transactionalPointCreate(request, createPostAudit(request, failure));
    }


    private void transactionalPointCreate(final JobRequest request, final JobAudit audit) {
        try {
            ConnectionPool connectionPool = null;
            Connection connection = null;
            try {
                connectionPool = jdbcServiceUtil.getConnectionPool(getAgent(), getRequestMessage());
                connection = connectionPool.getConnection();

                SegmentationAspectContext segmentationAspectContext = new SegmentationAspectContext();
                segmentationAspectContext.setConnection(connection);
                segmentationAspectContext.setArguments(request.getArguments());
                segmentationAspectContext.setAudit(audit);
                segmentationAspectContext.setRequestId(request.getId());
                segmentationAspectContext.setUserId(getRequestMessage().decodeUserId());
                segmentationAspectContext.setAgentContainer(getAgent().getAgentContainer());

                String pointId = "segmentation." + String.valueOf(audit.getType()).toLowerCase();
                TransactionalPoint transactionalPoint = new TransactionalPoint(pointId, aspectManager);
                transactionalPoint.run(segmentationAspectContext.toAspectContext(), new PointRunner() {
                    public void run() throws PointRunnerException {
                        sendAudit(audit);
                    }
                });
            }
            finally {
                if (connectionPool != null && connection != null) {
                    connectionPool.releaseConnection(connection);
                }
            }
        }
        catch (Exception ex) {
            StringBuilder buffer;
            if(null == ex.getMessage()) {
                buffer = new StringBuilder(ex.getClass().getName());
            }
            else {
                buffer = new StringBuilder(ex.getMessage());
            }
            if (audit.hasError()) {
                buffer.append("\n");
                Anomaly error = audit.getError();
                buffer.append(error.getMessage());
                buffer.append(error.getDescription());
            }
            Anomaly anomaly = new Anomaly("Erreur durant l'execution d'un aspect", buffer.toString());
            JobAudit auditError = new JobAudit(audit.getType());
            auditError.setError(anomaly);
            sendAudit(auditError);
        }
    }
}
