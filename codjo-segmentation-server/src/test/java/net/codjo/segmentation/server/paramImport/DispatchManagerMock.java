package net.codjo.segmentation.server.paramImport;
/**
 *
 */
public class DispatchManagerMock extends AbstractDispatchManager {

    @Override
    public String getDestinationTable() {
        return "PM_MY_CLASSIFICATION";
    }
}
