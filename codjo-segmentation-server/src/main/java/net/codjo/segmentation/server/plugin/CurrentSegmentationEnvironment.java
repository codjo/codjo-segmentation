package net.codjo.segmentation.server.plugin;

import net.codjo.workflow.common.message.JobRequest;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
/**
 *
 */
public class CurrentSegmentationEnvironment {
    private static final Logger LOG = Logger.getLogger(CurrentSegmentationEnvironment.class);

    private final Object lock = new Object();
    private List<JobRequest> treatmentInProgress = new ArrayList<JobRequest>();


    public String getUserForJobRequest(JobRequest request) {
        synchronized (lock) {
            String userId = null;
            for (JobRequest jobRequest : treatmentInProgress) {
                if (jobRequest.getArguments().toString().equals(request.getArguments().toString())) {
                    userId = jobRequest.getInitiatorLogin();
                    break;
                }
            }
            return userId;
        }
    }


    public boolean addTreatment(JobRequest request) {
        synchronized (lock) {
            boolean canAddTreatment = true;
            LOG.debug("----------------------------------------------");

            for (JobRequest jobRequest : treatmentInProgress) {
                LOG.debug("jobRequest.getArguments() + \":\" + request.getArguments() = "
                                   + jobRequest.getArguments() + ":" + request.getArguments());
                if (jobRequest.getArguments().toString().equals(request.getArguments().toString())) {
                    canAddTreatment = false;
                    break;
                }
            }
            LOG.debug("----------------------------------------------");
            if (canAddTreatment) {
                treatmentInProgress.add(request);
            }
            return canAddTreatment;
        }
    }

    public void removeTreatment(JobRequest request) {
        synchronized (lock) {
            for (JobRequest jobRequest : treatmentInProgress) {
                if (jobRequest.getArguments().toString().equals(request.getArguments().toString())) {
                    treatmentInProgress.remove(jobRequest);
                    return;
                }
            }
        }
    }
}

