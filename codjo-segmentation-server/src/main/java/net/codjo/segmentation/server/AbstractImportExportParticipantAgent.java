package net.codjo.segmentation.server;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.DFService;
import net.codjo.agent.protocol.NotUnderstoodException;
import net.codjo.agent.protocol.RefuseException;
import net.codjo.agent.protocol.RequestParticipantHandler;
import net.codjo.sql.server.JdbcServiceUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;
/**
 *
 */
public abstract class AbstractImportExportParticipantAgent extends Agent {
    private static final Logger LOG = Logger.getLogger(AbstractImportExportParticipantAgent.class);
    private JdbcServiceUtil jdbcServiceUtil = new JdbcServiceUtil();
    private String serviceType;
    private String serviceName;


    protected AbstractImportExportParticipantAgent(String serviceType, String serviceName) {
        this.serviceType = serviceType;
        this.serviceName = serviceName;
    }


    @Override
    protected void setup() {
        registerToDf();
        addTreatmentBehaviour();
    }


    protected abstract void addTreatmentBehaviour();


    @Override
    protected void tearDown() {
        try {
            DFService.deregister(this);
        }
        catch (DFService.DFServiceException exception) {
            LOG.error("Impossible de s'enlever auprès du DF");
        }
    }


    private void registerToDf() {
        try {
            DFService.ServiceDescription service = new DFService.ServiceDescription(serviceType, serviceName);
            DFService.register(this, new DFService.AgentDescription(service));
        }
        catch (DFService.DFServiceException e) {
            LOG.fatal("Impossible de s'enregistrer auprès du DF", e);
            die();
        }
    }


    protected Connection getConnection(AclMessage message) throws SQLException {
        return jdbcServiceUtil.getConnectionPool(this, message).getConnection();
    }


    protected abstract class TreatmentParticipantHandler implements RequestParticipantHandler {
        public AclMessage handleRequest(AclMessage request) throws RefuseException, NotUnderstoodException {
            try {
                parseMessage(request);
            }
            catch (IOException exception) {
                LOG.error("Le contenu du message est invalide.", exception);
                throw new NotUnderstoodException("Le contenu du message est invalide.");
            }
            catch (ParseException exception) {
                LOG.error(exception.getLocalizedMessage(), exception);
                throw new NotUnderstoodException(exception.getLocalizedMessage());
            }
            catch (SQLException exception) {
                LOG.error(exception.getLocalizedMessage(), exception);
                throw new NotUnderstoodException(exception.getLocalizedMessage());
            }
            return request.createReply(AclMessage.Performative.AGREE);
        }


        protected abstract void parseMessage(AclMessage request) throws
                                                                 NotUnderstoodException,
                                                                 IOException, ParseException, SQLException;
    }
}
