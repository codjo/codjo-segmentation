package net.codjo.segmentation.server.paramImport;
import java.sql.SQLException;
/**
 *
 */
class ControlManagerMock extends AbstractControlManager {

    @Override
    public int getMaxLengthForColumn(String column) {
        return 7;
    }


    @Override
    public String[] getPrimaryKeyColumns() {
        return new String[]{"CLASSIFICATION_ID"};
    }


    @Override
    public String getUnicitySQLQuery() {
        return "SELECT CLASSIFICATION_ID from PM_MY_CLASSIFICATION";
    }


    @Override
    public String getAnomalyLog(Anomaly anomaly, String... columnName) {
        if ("CLASSIFICATION_NAME".equals(columnName[0])) {
            return "Le libellé de l'axe est trop long";
        }
        else if ("CLASSIFICATION_ID".equals(columnName[0])
                 && Anomaly.UNICITY == anomaly) {
            return "L'id de l'axe existe déjà en base";
        }
        else if ("CLASSIFICATION_ID".equals(columnName[0])
                 && Anomaly.REPEATED_INDEX == anomaly) {
            return "Doublon de l'id Axe dans le fichier";
        }
        return null;
    }


    @Override
    protected void performSpecificControls() throws SQLException {

    }
}
