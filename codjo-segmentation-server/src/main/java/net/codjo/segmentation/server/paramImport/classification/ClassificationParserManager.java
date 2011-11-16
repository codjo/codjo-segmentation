package net.codjo.segmentation.server.paramImport.classification;
import net.codjo.segmentation.server.paramImport.AbstractParserManager;
/**
 *
 */
public class ClassificationParserManager extends AbstractParserManager {
    public static final String CLASSIFICATION_TABLE = "PM_CLASSIFICATION";
    public static final String CLASSIFICATION_ID = "CLASSIFICATION_ID";
    public static final String CLASSIFICATION_NAME = "CLASSIFICATION_NAME";
    public static final String CLASSIFICATION_TYPE = "CLASSIFICATION_TYPE";


    public ClassificationParserManager(String rawData) {
        super(rawData);
    }


    @Override
    public String getTableName() {
        return CLASSIFICATION_TABLE;
    }
}
