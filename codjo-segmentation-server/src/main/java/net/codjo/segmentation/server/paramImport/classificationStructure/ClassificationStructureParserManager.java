package net.codjo.segmentation.server.paramImport.classificationStructure;
import net.codjo.segmentation.server.paramImport.AbstractParserManager;
import static net.codjo.segmentation.server.util.SegmentationUtil.getColumnIndexByName;
/**
 *
 */
public class ClassificationStructureParserManager extends AbstractParserManager {
    public static final String TABLE_NAME = "PM_CLASSIFICATION_STRUCTURE";


    public ClassificationStructureParserManager(String rawData) {
        super(rawData);
    }


    @Override
    public String[][] getDataArray() {
        if (dataArray == null) {
            return null;
        }

        for (int rowIndex = 0; rowIndex < dataArray.length; rowIndex++) {
            if (rowIndex > 0) {
                String timestamp = getTimestamp();
                dataArray[rowIndex][getColumnIndexByName(dataArray[0], "SLEEVE_ID")] = timestamp;
                dataArray[rowIndex][getColumnIndexByName(dataArray[0], "SLEEVE_ROW_ID")] = timestamp;
            }
        }

        return dataArray;
    }


    @Override
    public String getTableName() {
        return TABLE_NAME;
    }


    protected String getTimestamp() {
        return String.valueOf((Math.abs((int)System.nanoTime())));
    }
}
