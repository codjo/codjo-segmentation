package net.codjo.segmentation.server.paramImport.classification;
import net.codjo.segmentation.server.paramImport.AbstractDispatchManager;
/**
 *
 */
public class ClassificationDispatchManager extends AbstractDispatchManager {

    @Override
    public String getDestinationTable() {
        return "PM_CLASSIFICATION";
    }
}
