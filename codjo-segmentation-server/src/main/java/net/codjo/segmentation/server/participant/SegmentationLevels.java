package net.codjo.segmentation.server.participant;
import net.codjo.segmentation.common.SegmentationLevelNames;
import net.codjo.segmentation.server.blackboard.message.Level;
/**
 *
 */
public interface SegmentationLevels {
    Level FIRST = new Level(SegmentationLevelNames.ANALYZE_LEVEL);
    Level TO_DELETE = new Level(SegmentationLevelNames.DELETE_LEVEL);
    Level TO_PAGINATE = new Level(SegmentationLevelNames.PAGINATE_LEVEL);
    Level TO_COMPUTE = new Level(SegmentationLevelNames.COMPUTE_LEVEL);
    Level INFORMATION = new Level("information");
    Level FINAL = new Level("final");
}
