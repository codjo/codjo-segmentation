/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.plugin;
import net.codjo.agent.Behaviour;
import net.codjo.agent.DFService;
import net.codjo.aspect.AspectManager;
import net.codjo.segmentation.server.blackboard.BlackboardBehaviour;
import net.codjo.segmentation.server.blackboard.BlackboardListener;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.SegmentationLevels;
import static net.codjo.segmentation.server.participant.SegmentationParticipant.BLACKBOARD_SERVICE;
import static net.codjo.segmentation.server.participant.SegmentationParticipant.SEGMENTATION_SERVICE;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.common.message.JobException;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.protocol.JobProtocolParticipant;
import net.codjo.workflow.server.api.JobAgent;
import java.util.List;
import org.apache.log4j.Logger;

class SegmentationJobAgent extends JobAgent {
    private static final Logger LOG = Logger.getLogger(SegmentationJobAgent.class);


    SegmentationJobAgent(ContextManager manager,
                         AspectManager aspectManager,
                         JdbcServiceUtil jdbcServiceUtil) {
        this(manager, aspectManager, jdbcServiceUtil, MODE.NOT_DELEGATE);
    }


    SegmentationJobAgent(ContextManager manager,
                         AspectManager aspectManager,
                         JdbcServiceUtil jdbcServiceUtil, MODE mode) {
        super(mode);
        setAgentDescription(createAgentDescription());
        SegmentationAspectParticipant aspectParticipant = new SegmentationAspectParticipant(jdbcServiceUtil,
                                                                                            aspectManager);
        setJobProtocolParticipant(aspectParticipant);

        BlackboardBehaviour blackboard = new BlackboardBehaviour(SegmentationLevels.FIRST,
                                                                 SegmentationLevels.TO_DELETE,
                                                                 SegmentationLevels.TO_PAGINATE,
                                                                 SegmentationLevels.TO_COMPUTE,
                                                                 SegmentationLevels.INFORMATION,
                                                                 SegmentationLevels.FINAL);
        blackboard.doNotRegisterToDf();
        addBehaviour(blackboard);

        MyGlueBehaviour glue = new MyGlueBehaviour(aspectParticipant, blackboard, manager,
                                                   new CurrentSegmentationEnvironment());
        getJobProtocolParticipant().setExecuteJobBehaviour(glue);
        blackboard.setListener(glue);
    }


    private static DFService.AgentDescription createAgentDescription() {
        DFService.AgentDescription description = new DFService.AgentDescription();
        declareService(description, SEGMENTATION_SERVICE, "segmentation-service");
        declareService(description, BLACKBOARD_SERVICE, "segmentation-blackboard");
        return description;
    }


    private static void declareService(DFService.AgentDescription description, String service, String name) {
        description.addService(new DFService.ServiceDescription(service, name));
    }


    private static class MyGlueBehaviour extends Behaviour implements BlackboardListener {
        private final JobProtocolParticipant aspectParticipant;
        private final BlackboardBehaviour blackboard;
        private final ContextManager manager;
        private final CurrentSegmentationEnvironment currentSegmentationEnvironment;
        private State state = State.POST_TODO;
        private StringBuffer errorMessage = null;
        private boolean isDone = false;


        MyGlueBehaviour(JobProtocolParticipant aspectParticipant,
                        BlackboardBehaviour blackboard,
                        ContextManager manager,
                        CurrentSegmentationEnvironment environment) {
            this.aspectParticipant = aspectParticipant;
            this.blackboard = blackboard;
            this.manager = manager;
            this.currentSegmentationEnvironment = environment;
        }


        @Override
        protected void action() {
            JobRequest request = aspectParticipant.getRequest();
            switch (state) {
                case POST_TODO:
                    isDone = false;
                    errorMessage = null;
                    LOG.debug("SegmentationJobRequest recu");
                    if (currentSegmentationEnvironment.addTreatment(request)) {
                        blackboard.setUserId(aspectParticipant.getRequestMessage().decodeUserId());
                        blackboard.postTodo(SegmentationLevels.FIRST, new Todo<JobRequest>(request));
                        LOG.debug("SegmentationJobRequest ajouté");
                    }
                    else {
                        LOG.debug("SegmentationJobRequest refusé");
                        blackboard.setUserId(aspectParticipant.getRequestMessage().decodeUserId());
                        blackboard.postTodo(SegmentationLevels.FIRST, new Todo<JobRequest>(null));
                        errorMessage = new StringBuffer(
                              "Lancement impossible car il y a une segmentation en cours par ");
                        errorMessage.append(currentSegmentationEnvironment.getUserForJobRequest(
                              request));
                        errorMessage.append(" avec le(s) parametre(s) : ");
                        errorMessage.append(request.getArguments().toString());
                    }
                    state = State.RUNNING;
                    block();
                    break;
                case RUNNING:
                    block();
                    break;
                case DECLARE_JOB_DONE:
                    LOG.debug("DECLARE_JOB_DONE : Request traitée");
                    isDone = true;
                    state = State.POST_TODO;
                    manager.remove(request.getId());
                    currentSegmentationEnvironment.removeTreatment(request);
                    if (errorMessage == null) {
                        aspectParticipant.declareJobDone();
                    }
                    else {
                        //noinspection ThrowableInstanceNeverThrown
                        aspectParticipant.declareJobDone(new JobException(errorMessage.toString()));
                        errorMessage = null;
                    }
                    break;
            }
        }


        @Override
        public boolean done() {
            return isDone;
        }


        public void todoWrited(Level level, Todo todo) {
            if (level.equals(SegmentationLevels.INFORMATION)) {
                blackboard.removeTodo(level, todo);
                JobAudit jobAudit = new JobAudit(JobAudit.Type.MID);
                jobAudit.setArguments((Arguments)todo.getContent());
                aspectParticipant.sendAudit(jobAudit);
            }
        }


        public void informOfFailure(Level level, Todo todo, String error) {
            if (errorMessage == null) {
                errorMessage = new StringBuffer("Erreur rencontrée lors de la segmentation");
            }
            errorMessage.append("\n\t").append(error)
                  .append("\n\n* Localisation : ").append(level)
                  .append("\n* todo(").append(todo.getContent()).append(")");

            blackboard.removeTodo(level, todo);
        }


        public void blackboardFinished(List<Todo> lastTodos) {
            state = State.DECLARE_JOB_DONE;
            restart();
        }
    }
    enum State {
        POST_TODO,
        RUNNING,
        DECLARE_JOB_DONE
    }
}
