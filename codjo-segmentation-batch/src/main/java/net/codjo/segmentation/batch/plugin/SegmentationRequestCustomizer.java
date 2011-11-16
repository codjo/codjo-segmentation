package net.codjo.segmentation.batch.plugin;
import net.codjo.plugin.batch.BatchException;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
/**
 *
 */
public interface SegmentationRequestCustomizer {
    void customize(SegmentationJobRequest request) throws BatchException;
}
