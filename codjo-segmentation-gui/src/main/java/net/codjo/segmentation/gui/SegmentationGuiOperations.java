package net.codjo.segmentation.gui;
import net.codjo.agent.ContainerFailureException;
import net.codjo.mad.client.request.RequestException;
import net.codjo.plugin.batch.BatchException;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.workflow.common.subscribe.JobEventHandler;
import java.util.Map;
/**
 *
 */
public interface SegmentationGuiOperations {

    void startSegmentation(Map<String, String> parameters)
          throws RequestException, BatchException, ContainerFailureException;


    /**
     * Lance une segmentation.
     *
     * Pour le listener, dans le cas d'une utilisation dans une IHM, utilisez SwingWrapper.wrapp(listener).
     *
     * @param request  requete d'execution
     * @param listener listener d'écoute
     *
     * @throws BatchException            Erreur durant la segmentation
     * @throws net.codjo.plugin.batch.TimeoutBatchException
     *                                   Le traitement ne se termine pas
     * @throws ContainerFailureException
     */
    public void startSegmentation(SegmentationJobRequest request, JobEventHandler listener)
          throws BatchException, ContainerFailureException;
}
