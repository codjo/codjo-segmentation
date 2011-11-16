/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport.agent;
import net.codjo.agent.AclMessage;
import static net.codjo.agent.AclMessage.Performative.INFORM;
import static net.codjo.agent.AclMessage.Performative.REQUEST;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import net.codjo.agent.protocol.FailureException;
import net.codjo.agent.protocol.NotUnderstoodException;
import net.codjo.agent.protocol.RequestParticipant;
import net.codjo.mad.common.ZipUtil;
import net.codjo.segmentation.server.AbstractImportExportParticipantAgent;
import net.codjo.segmentation.server.ParseException;
import net.codjo.segmentation.server.paramImport.AbstractControlManager;
import net.codjo.segmentation.server.paramImport.AbstractDispatchManager;
import net.codjo.segmentation.server.paramImport.AbstractParserManager;
import static net.codjo.segmentation.server.paramImport.factory.ImportManagerFactory.createControlManager;
import static net.codjo.segmentation.server.paramImport.factory.ImportManagerFactory.createDataParserManager;
import static net.codjo.segmentation.server.paramImport.factory.ImportManagerFactory.createDispatchManager;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 *
 */
public class ImportParticipantAgent extends AbstractImportExportParticipantAgent {
    private static final Logger LOG = Logger.getLogger(ImportParticipantAgent.class);
    protected static final String IMPORT_SERVICE = "import-service";
    private String unzippedMessage;
    private String[][] dataArray;
    private String[] fileHeader;


    public ImportParticipantAgent() {
        super(IMPORT_SERVICE, "import");
    }


    @Override
    protected void addTreatmentBehaviour() {
        addBehaviour(
              new RequestParticipant(this, new ImportParticipantHandler(), matchPerformative(REQUEST)));
    }


    public String getUnzippedMessage() {
        return unzippedMessage;
    }


    public void setUnzippedMessage(String unzippedMessage) {
        this.unzippedMessage = unzippedMessage;
    }


    protected AbstractParserManager getParserManager() {
        return createDataParserManager(unzippedMessage);
    }


    private class ImportParticipantHandler extends TreatmentParticipantHandler {
        public AclMessage executeRequest(AclMessage request, AclMessage agreement)
              throws FailureException {
            try {
                AbstractControlManager control = createControlManager(unzippedMessage);
                control.setConnection(getConnection(request));
                control.setData(dataArray);
                control.setFileHeader(fileHeader);
                control.control();

                AbstractDispatchManager dispatch = createDispatchManager(unzippedMessage);
                dispatch.setConnection(getConnection(request));
                dispatch.dispatch(dataArray);

                AclMessage reply = request.createReply(INFORM);
                reply.setByteSequenceContent(ZipUtil.zip(control.getQuarantineStream()));

                dataArray = null;
                fileHeader = null;

                return reply;
            }
            catch (SQLException exception) {
                LOG.error(exception.getLocalizedMessage(), exception);
                throw new FailureException(exception.getLocalizedMessage());
            }
            catch (IOException exception) {
                LOG.error("Le contenu de la quarantaine est invalide.", exception);
                throw new FailureException("Le contenu de la quarantaine est invalide.");
            }
            catch (Exception exception) {
                LOG.error("Exception inattendue.", exception);
                throw new FailureException(exception.getLocalizedMessage());
            }
        }


        @Override
        public void parseMessage(AclMessage request)
              throws NotUnderstoodException, IOException, ParseException, SQLException {
            byte[] message = request.getByteSequenceContent();
            if (message == null || message.length == 0) {
                throw new NotUnderstoodException("Le contenu du fichier à importer est vide.");
            }
            setUnzippedMessage(ZipUtil.unzip(message));
            AbstractParserManager manager = getParserManager();
            manager.setConnection(getConnection(request));
            manager.parse();
            dataArray = manager.getDataArray();
            fileHeader = manager.getFileHeader();
        }
    }
}
