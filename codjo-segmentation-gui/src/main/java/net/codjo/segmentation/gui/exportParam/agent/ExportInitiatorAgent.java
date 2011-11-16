/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.exportParam.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.DFService;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.RequestInitiator;
import net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent;
import net.codjo.segmentation.gui.ImportExportUtil;
import java.io.File;
import org.apache.log4j.Logger;
/**
 *
 */
public class ExportInitiatorAgent extends AbstractImportExportInitiatorAgent {
    private static final Logger LOG = Logger.getLogger(ExportInitiatorAgent.class);


    public ExportInitiatorAgent(File exportFilePath, TreatmentType exportType, UserId userId) {
        super(exportFilePath, exportType, userId);
    }


    @Override
    protected void setup() {
        try {
            AclMessage message = initAgent("export-service");
            message.setContent(addTreatmentType(treatmentFile.getPath()));

            addBehaviour(
                  new RequestInitiator(this,
                                       new ExportInitiatorHandler(),
                                       message));
        }
        catch (DFService.DFServiceException e) {
            LOG.error("Impossible de trouver l'agent d'export auprès du DF", e);
            die();
        }
    }


    private class ExportInitiatorHandler extends AbstractTreatmentInitiatorHandler {

        @Override
        protected void notifyInformAndDie(AclMessage inform) {
            try {
                ImportExportUtil.inform(progressListeners, inform.getContent());
            }
            finally {
                die();
            }
        }


        @Override
        protected void notifyErrorAndDie(String errorMessage) {
            try {
                ImportExportUtil.informError(progressListeners, errorMessage);
            }
            finally {
                die();
            }
        }
    }
}

