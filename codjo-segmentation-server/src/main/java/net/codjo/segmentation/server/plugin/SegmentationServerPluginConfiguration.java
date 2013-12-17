package net.codjo.segmentation.server.plugin;
import java.net.URL;
import java.util.concurrent.TimeUnit;
/**
 *
 */
public interface SegmentationServerPluginConfiguration {
    void setConfigurationFileUrl(URL url);


    void setMaxAnalyzerAgents(int maxAnalyzerAgents);


    void setMaxDeleteAgents(int maxDeleteAgents);


    void setMaxPaginatorAgents(int maxPaginatorAgents);


    void setMaxCalculatorAgents(int maxCalculatorAgents);


    void setMaxSegmentationJobAgents(int maxSegmentationJobAgent);


    /**
     * Set the value of the time window used by {@link net.codjo.segmentation.server.blackboard.DefaultErrorLogLimiter}.
     */
    void setTimeWindowValue(long timeWindowValue);


    /**
     * Set the unit of the time window used by {@link net.codjo.segmentation.server.blackboard.DefaultErrorLogLimiter}.
     */
    void setTimeWindowUnit(TimeUnit timeWindowUnit);
}
