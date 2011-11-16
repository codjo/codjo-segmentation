package net.codjo.segmentation.server.plugin;
import java.net.URL;
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
}
