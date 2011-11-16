/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport.classification;
import net.codjo.segmentation.server.paramImport.AbstractControlManager;
import java.sql.SQLException;
/**
 *
 */
public class ClassificationControlManager extends AbstractControlManager {

    @Override
    public int getMaxLengthForColumn(String column) {
        if (ClassificationParserManager.CLASSIFICATION_NAME.equals(column)) {
            return 50;
        }
        else if (ClassificationParserManager.CLASSIFICATION_TYPE.equals(column)) {
            return 12;
        }
        return -1;
    }


    @Override
    public String[] getPrimaryKeyColumns() {
        return new String[]{ClassificationParserManager.CLASSIFICATION_ID};
    }


    @Override
    public String getUnicitySQLQuery() {
        return "select " + ClassificationParserManager.CLASSIFICATION_ID + " from "
               + ClassificationParserManager.CLASSIFICATION_TABLE;
    }


    @Override
    public String getAnomalyLog(Anomaly anomaly, String... columnName) {
        switch (anomaly) {
            case MAX_LENGTH:
                if (ClassificationParserManager.CLASSIFICATION_NAME.equals(columnName[0])) {
                    return "Le libellé de l'axe est trop long";
                }
                if (ClassificationParserManager.CLASSIFICATION_TYPE.equals(columnName[0])) {
                    return "La famille est trop longue";
                }
            case UNICITY:
                return "L'id de l'axe existe déjà en base";
            case REPEATED_INDEX:
                return "Doublon de l'id Axe dans le fichier";
            default:
                return null;
        }
    }


    @Override
    protected void performSpecificControls() throws SQLException {
        ;
    }
}
