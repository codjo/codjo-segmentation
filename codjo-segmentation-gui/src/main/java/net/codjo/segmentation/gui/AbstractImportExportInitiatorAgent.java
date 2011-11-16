package net.codjo.segmentation.gui;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.DFService;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.InitiatorHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public abstract class AbstractImportExportInitiatorAgent extends Agent {
    protected List<ImportExportProgresListener> progressListeners = new ArrayList<ImportExportProgresListener>();
    protected TreatmentType type;
    protected UserId userId;
    protected File treatmentFile;

    public enum TreatmentType {
        CLASSIFICATION,
        CLASSIFICATION_STRUCTURE;
    }


    protected AbstractImportExportInitiatorAgent(File treatmentFile, TreatmentType type, UserId userId) {
        this.treatmentFile = treatmentFile;
        this.type = type;
        this.userId = userId;
    }


    protected AclMessage initAgent(String service) throws DFService.DFServiceException {
        DFService.AgentDescription[] descriptions = DFService.searchForService(this, service);
        Aid exportAgentAid = descriptions[0].getAID();
        AclMessage message = new AclMessage(AclMessage.Performative.REQUEST);
        message.addReceiver(exportAgentAid);
        message.encodeUserId(userId);
        return message;
    }


    protected String addTreatmentType(String contentToSend) {
        if (type == TreatmentType.CLASSIFICATION) {
            contentToSend = "CLASSIFICATION;" + contentToSend;
        }
        else {
            contentToSend = "CLASSIFICATION_STRUCTURE;" + contentToSend;
        }
        return contentToSend;
    }


    public void addTreatmentProgressListener(ImportExportProgresListener listener) {
        progressListeners.add(listener);
    }


    @Override
    protected abstract void setup();


    @Override
    protected void tearDown() {
        ;
    }


    protected abstract class AbstractTreatmentInitiatorHandler implements InitiatorHandler {

        public void handleAgree(AclMessage agree) {
            ;
        }


        public void handleRefuse(AclMessage refuse) {
            notifyErrorAndDie("handleRefuse received: " + refuse.getContent());
        }


        public void handleInform(AclMessage inform) {
            notifyInformAndDie(inform);
        }


        public void handleFailure(AclMessage failure) {
            notifyErrorAndDie("handleFailure received: " + failure.getContent());
        }


        public void handleOutOfSequence(AclMessage outOfSequenceMessage) {
            notifyErrorAndDie("handleOutOfSequence received: " + outOfSequenceMessage.getContent());
        }


        public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
            notifyErrorAndDie("handleNotUnderstood received: " + notUnderstoodMessage.getContent());
        }


        protected abstract void notifyInformAndDie(AclMessage inform);


        protected abstract void notifyErrorAndDie(String errorMessage);
    }
}

