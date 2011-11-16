package net.codjo.segmentation.server.paramImport.factory;
import net.codjo.segmentation.server.paramImport.classification.ClassificationDispatchManager;
import net.codjo.segmentation.server.paramImport.classification.ClassificationParserManager;
import net.codjo.segmentation.server.paramImport.classification.ClassificationControlManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureDispatchManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureControlManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureParserManager;
import net.codjo.segmentation.server.paramImport.AbstractParserManager;
import net.codjo.segmentation.server.paramImport.AbstractControlManager;
import net.codjo.segmentation.server.paramImport.AbstractDispatchManager;
/**
 *
 */
public class ImportManagerFactory {
    public final static String CLASSIFICATION_TYPE = "CLASSIFICATION";
    public static final String CLASSIFICATION_STRUCTURE_TYPE = "CLASSIFICATION_STRUCTURE";


    private ImportManagerFactory() {
    }


    public static AbstractParserManager createDataParserManager(String rawData) {
        AbstractParserManager manager = null;

        String type = getManagerType(rawData);
        String data = rawData.substring(rawData.indexOf(";") + 1, rawData.length());

        if (CLASSIFICATION_TYPE.equals(type)) {
            manager = new ClassificationParserManager(data);
        }
        else if (CLASSIFICATION_STRUCTURE_TYPE.equals(type)) {
            manager = new ClassificationStructureParserManager(data);
        }

        return manager;
    }


    public static AbstractControlManager createControlManager(String rawData) {
        AbstractControlManager manager = null;

        String type = getManagerType(rawData);

        if (CLASSIFICATION_TYPE.equals(type)) {
            manager = new ClassificationControlManager();
        }
        else if (CLASSIFICATION_STRUCTURE_TYPE.equals(type)) {
            manager = new ClassificationStructureControlManager();
        }

        return manager;
    }


    public static AbstractDispatchManager createDispatchManager(String rawData) {
        AbstractDispatchManager manager = null;

        String type = getManagerType(rawData);

        if (CLASSIFICATION_TYPE.equals(type)) {
            manager = new ClassificationDispatchManager();
        }
        else if (CLASSIFICATION_STRUCTURE_TYPE.equals(type)) {
            manager = new ClassificationStructureDispatchManager();
        }

        return manager;
    }


    private static String getManagerType(String rawData) {
        return rawData.substring(0, rawData.indexOf(";"));
    }
}
