package net.codjo.segmentation.batch.plugin;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;
/**
 *
 */
public interface SegmentationBatchConfiguration {
    void setCustomizer(SegmentationRequestCustomizer customizer);


    SegmentationRequestCustomizer getCustomizer();


    WorkflowConfiguration getWorkflowConfiguration();
}
