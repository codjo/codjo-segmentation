package net.codjo.segmentation.server.paramImport.classificationStructure;
import net.codjo.segmentation.server.paramImport.AbstractDispatchManager;
/**
 *
 */
public class ClassificationStructureDispatchManager extends AbstractDispatchManager {
    @Override
    public String getDestinationTable() {
        return "PM_CLASSIFICATION_STRUCTURE";
    }
}
