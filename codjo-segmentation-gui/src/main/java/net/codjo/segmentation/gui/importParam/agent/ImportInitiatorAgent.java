package net.codjo.segmentation.gui.importParam.agent;
import net.codjo.agent.AclMessage;
import net.codjo.agent.DFService;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.RequestInitiator;
import net.codjo.mad.common.ZipUtil;
import net.codjo.segmentation.gui.AbstractImportExportInitiatorAgent;
import net.codjo.segmentation.gui.ImportExportUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class ImportInitiatorAgent extends AbstractImportExportInitiatorAgent {
    private String[][] quarantine;
    private static final Logger LOG = Logger.getLogger(ImportInitiatorAgent.class);


    public ImportInitiatorAgent(File importFile, TreatmentType importType, UserId userId)
          throws FileNotFoundException {
        super(importFile, importType, userId);
        if (importFile == null || !importFile.exists()) {
            throw new FileNotFoundException("Le fichier spécifié n'existe pas.");
        }
    }


    @Override
    protected void setup() {
        try {
            AclMessage message = initAgent("import-service");
            message.setByteSequenceContent(ZipUtil.zip(addTreatmentType(createContentFromFile())));
            addBehaviour(new RequestInitiator(this, new ImportInitiatorHandler(), message));
        }
        catch (DFService.DFServiceException exception) {
            LOG.error("Impossible de trouver l'agent d'import auprès du DF", exception);
            die();
        }
        catch (IOException exception) {
            LOG.error("Erreur lors de la lecture du fichier", exception);
            die();
        }
    }


    @SuppressWarnings({"NestedAssignment"})
    private String createContentFromFile() throws IOException {
        StringBuilder contentToSend = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(treatmentFile));
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            contentToSend.append(currentLine).append("\n");
        }
        reader.close();

        return contentToSend.toString();
    }


    public String[][] getQuarantine() {
        return quarantine;
    }


    private class ImportInitiatorHandler extends AbstractTreatmentInitiatorHandler {

        @Override
        protected void notifyInformAndDie(AclMessage inform) {
            try {
                String quarantineMessage = ZipUtil.unzip(inform.getByteSequenceContent());
                initializeQuarantine(quarantineMessage);
                ImportExportUtil.inform(progressListeners, getQuarantine());
            }
            catch (IOException exception) {
                LOG.error(exception.getLocalizedMessage(), exception);
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


        private void initializeQuarantine(String quarantineMessage) {
            StringTokenizer lineTokenizer = new StringTokenizer(quarantineMessage, "\n");
            int numberOfRows = lineTokenizer.countTokens();

            for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {

                StringTokenizer columnTokenizer = new StringTokenizer(lineTokenizer.nextToken(), "\t");
                int numberOfColumns = columnTokenizer.countTokens();

                for (int colIndex = 0; colIndex < numberOfColumns; colIndex++) {
                    if (quarantine == null) {
                        quarantine = new String[numberOfRows][numberOfColumns];
                    }
                    quarantine[rowIndex][colIndex] = columnTokenizer.nextToken();
                }
            }
        }
    }
}
