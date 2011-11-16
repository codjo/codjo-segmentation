package net.codjo.segmentation.server.paramExport.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.protocol.FailureException;
import net.codjo.agent.protocol.NotUnderstoodException;
import net.codjo.agent.protocol.RequestParticipant;
import net.codjo.segmentation.server.AbstractImportExportParticipantAgent;
import net.codjo.segmentation.server.ParseException;
import net.codjo.segmentation.server.paramExport.ExportManager;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.Logger;
/**
 *
 */
public class ExportParticipantAgent extends AbstractImportExportParticipantAgent {
    private static final Logger LOG = Logger.getLogger(ExportParticipantAgent.class);
    protected static final String EXPORT_SERVICE = "export-service";


    public ExportParticipantAgent() {
        super(EXPORT_SERVICE, "export");
    }


    @Override
    protected void addTreatmentBehaviour() {
        addBehaviour(
              new RequestParticipant(this, new ExportParticipantHandler(), MessageTemplate.matchPerformative(
                    AclMessage.Performative.REQUEST)));
    }


    private class ExportParticipantHandler extends TreatmentParticipantHandler {

        public AclMessage executeRequest(AclMessage request, AclMessage agreement)
              throws FailureException {
            try {
                ExportManager exportManager = new ExportManager();
                exportManager.doExport(getConnection(request), request.getContent());

                AclMessage reply = request.createReply(AclMessage.Performative.INFORM);
                reply.setContent("Export terminé");
                return reply;
            }
            catch (SQLException exception) {
                LOG.error(exception.getLocalizedMessage(), exception);
                throw new FailureException(exception.getLocalizedMessage());
            }
            catch (IOException exception) {
                LOG.error("Impossible d'écrire le fichier d'export.", exception);
                throw new FailureException("Impossible d'écrire le fichier d'export.");
            }
            catch (Exception exception) {
                LOG.error("Exception inattendue.", exception);
                throw new FailureException(exception.getLocalizedMessage());
            }
        }


        @Override
        public void parseMessage(AclMessage request)
              throws NotUnderstoodException, IOException, ParseException, SQLException {
            String message = request.getContent();
            if (message == null || message.length() == 0) {
                throw new NotUnderstoodException("Message vide.");
            }

            String[] args = message.split(";");
            if (args.length != 2) {
                throw new NotUnderstoodException("Message incorrect.");
            }

            String exportType = args[0];
            if (exportType == null || exportType.length() == 0) {
                throw new ParseException("Le type d'export est incorrect.");
            }

            String exportFilePath = args[1];
            if (exportFilePath == null || exportFilePath.length() == 0) {
                throw new ParseException("Le chemin du fichier d'export est incorrect.");
            }
        }
    }
}
