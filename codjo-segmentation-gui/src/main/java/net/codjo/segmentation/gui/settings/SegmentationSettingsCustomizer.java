package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.gui.request.DetailDataSource;
/**
 *
 */
public interface SegmentationSettingsCustomizer {
    public static final String SEGMENTATION_SETTINGS_CUSTOMIZER = "SegmentationSettingsCustomizer";

    void doPreDataSourceInit(ClassificationStructureLogic structureLogic, DetailDataSource dataSource)
          throws RequestException;


    void doPostDataSourceInit(ClassificationStructureLogic structureLogic, DetailDataSource dataSource)
          throws RequestException;
}
