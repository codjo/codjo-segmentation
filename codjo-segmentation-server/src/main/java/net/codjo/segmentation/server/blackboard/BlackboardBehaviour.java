/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Behaviour;
import net.codjo.agent.DFService;
import net.codjo.agent.MessageTemplate;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import net.codjo.agent.UserId;
import net.codjo.agent.protocol.FailureException;
import net.codjo.agent.protocol.NotUnderstoodException;
import net.codjo.agent.protocol.SubscribeParticipant;
import net.codjo.agent.protocol.SubscribeParticipantHandler;
import net.codjo.segmentation.server.blackboard.message.BlackboardAction;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionVisitor;
import net.codjo.segmentation.server.blackboard.message.Erase;
import net.codjo.segmentation.server.blackboard.message.GetTodo;
import net.codjo.segmentation.server.blackboard.message.InformOfFailure;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.MessageCodec;
import net.codjo.segmentation.server.blackboard.message.Read;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.blackboard.message.Write;
import org.apache.log4j.Logger;
/**
 *
 */
public class BlackboardBehaviour extends Behaviour {
    public static final String BLACKBOARD_SERVICE = "blackboard-service";
    public static final String BLACKBOARD_PROTOCOL = "blackboard-protocol";
    private static final Logger LOG = Logger.getLogger(BlackboardBehaviour.class);
    private State state = State.REGISTER_TO_DF;
    private MessageCodec codec = new MessageCodec();
    private final MessageTemplate requestTemplate;
    private final ActionExecutor actionExecutor = new ActionExecutor();
    private final BlackboardManager blackboardManager;
    private BlackboardListener listener;
    private UserId userId;


    public BlackboardBehaviour(Level... levels) {
        this(new BlackboardListenerAdapter(), levels);
    }


    public BlackboardBehaviour(BlackboardListener listener, Level... levels) {
        this.listener = listener;
        blackboardManager = new BlackboardManager(levels);
        requestTemplate = MessageTemplate.and(matchPerformative(AclMessage.Performative.REQUEST),
                                              MessageTemplate.matchProtocol(BLACKBOARD_PROTOCOL));
    }


    @Override
    protected final void action() {
        switch (state) {
            case REGISTER_TO_DF:
                registerToDf();
                state = State.START_LISTENNING_SUBSCRIPTION;
                break;
            case START_LISTENNING_SUBSCRIPTION:
                startListenningSubscription();
                state = State.BLACKBOARD_RUNNING;
                break;
            case BLACKBOARD_RUNNING:
                listenBlackboardActionRequest();
                break;
        }
    }


    @Override
    public final boolean done() {
        return false;
    }


    public void postTodo(Level level, Todo todo) {
        Level realLevel = blackboardManager.getLevel(level);
        blackboardManager.addTodo(realLevel, todo);

        listener.todoWrited(realLevel, todo);
        if (!blackboardManager.todoExists(realLevel, todo)) {
            return;
        }

        AclMessage inform = new AclMessage(AclMessage.Performative.INFORM);
        inform.setContent(realLevel.getName());
        encodeUserId(inform);

        for (SubscribeParticipant.Subscription subscription : blackboardManager.getSubscription(level)) {
            subscription.reply(inform);
        }
    }


    public void removeTodo(Level level, Todo todo) {
        boolean previouslyFinished = blackboardManager.isFinished();
        blackboardManager.removeTodo(level, todo);
        if (!actionExecutor.isRunning()) {
            checkIfBlackboardIsFinished(previouslyFinished);
        }
    }


    public void doNotRegisterToDf() {
        state = State.START_LISTENNING_SUBSCRIPTION;
    }


    public void setListener(BlackboardListener listener) {
        this.listener = listener;
    }


    public void setUserId(UserId userId) {
        this.userId = userId;
    }


    protected int getParticipantCount(Level level) {
        return blackboardManager.getSubscription(level).size();
    }


    private void checkIfBlackboardIsFinished(boolean previouslyFinished) {
        if (blackboardManager.isFinished() && !previouslyFinished) {
            listener.blackboardFinished(blackboardManager.getLastTodos());
            blackboardManager.reset();
        }
    }


    private AclMessage encodeUserId(AclMessage aclMessage) {
        if (userId != null) {
            aclMessage.encodeUserId(userId);
        }
        return aclMessage;
    }


    private void startListenningSubscription() {
        SubscribeParticipant subscribe =
              new SubscribeParticipant(getAgent(),
                                       new SubscriptionHandler(),
                                       matchPerformative(AclMessage.Performative.SUBSCRIBE));
        getAgent().addBehaviour(subscribe);
    }


    private void registerToDf() {
        try {
            DFService.ServiceDescription service =
                  new DFService.ServiceDescription(BLACKBOARD_SERVICE, "blackboard");
            DFService.register(getAgent(), new DFService.AgentDescription(service));
        }
        catch (DFService.DFServiceException e) {
            LOG.fatal("Impossible de s'enregistrer auprès du DF", e);
            getAgent().die();
        }
    }


    private void listenBlackboardActionRequest() {
        AclMessage aclMessage = getAgent().receive(requestTemplate);
        if (aclMessage == null) {
            block();
            return;
        }

        boolean previouslyFinished = blackboardManager.isFinished();
        actionExecutor.setRunning(true);
        try {
            BlackboardAction blackboardAction = codec.decode(aclMessage);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Blackboard " + getAgent().getAID() + " traite les actions suivantes \n"
                          + aclMessage.getContent());
            }

            blackboardAction.acceptVisitor(actionExecutor.using(aclMessage));
            checkIfBlackboardIsFinished(previouslyFinished && !actionExecutor.hasWroteTodos());
        }
        catch (Throwable e) {
            LOG.error("Action en cours d'execution :\n" + aclMessage.toFipaACLString());
            LOG.error("Erreur lors de l'execution des actions ", e);
        }
        finally {
            actionExecutor.setRunning(false);
        }
    }


    private class SubscriptionHandler implements SubscribeParticipantHandler {
        public void handleSubscribe(SubscribeParticipant.Subscription subscription)
              throws NotUnderstoodException {
            try {

                Level level = new Level(subscription.getMessage().getContent());
                blackboardManager.addSubscription(level, subscription);
                subscription.reply(new AclMessage(AclMessage.Performative.AGREE));
            }
            catch (Exception e) {
                throw new NotUnderstoodException("Echec lors de l'inscription de l'agent : "
                                                 + subscription.getMessage().getSender());
            }
        }


        public void handleCancel(SubscribeParticipant.Subscription subscription) throws FailureException {
            try {
                Level level = new Level(subscription.getMessage().getContent());
                blackboardManager.removeSubscription(level, subscription);
                subscription.reply(encodeUserId(new AclMessage(AclMessage.Performative.INFORM)));
            }
            catch (Exception e) {
                throw new FailureException("Echec lors de la desinscription de l'agent : "
                                           + subscription.getMessage().getSender());
            }
        }
    }
    private class ActionExecutor implements BlackboardActionVisitor {
        private AclMessage requestMessage;
        private boolean running = false;
        private boolean wroteTodos = false;


        public void visit(Write write) {
            wroteTodos = true;
            postTodo(write.getLevel(), write.getTodo());
        }


        public void visit(GetTodo getTodo) {
            Todo todo = blackboardManager.startFirstTodo(getTodo.getLevel());
            if (todo == null) {
                return;
            }
            AclMessage response = requestMessage.createReply(AclMessage.Performative.INFORM);
            codec.encodeRead(response, new Read(blackboardManager.getLevel(getTodo.getLevel()), todo));
            getAgent().send(encodeUserId(response));
        }


        public void visit(Erase erase) {
            blackboardManager.removeTodo(erase.getLevel(), erase.getTodo());
        }


        public void visit(InformOfFailure informOfFailure) {
            listener.informOfFailure(blackboardManager.getLevel(informOfFailure.getLevel()),
                                     informOfFailure.getTodo(),
                                     informOfFailure.getErrorMessage());
        }


        public BlackboardActionVisitor using(AclMessage aclMessage) {
            wroteTodos = false;
            this.requestMessage = aclMessage;
            return this;
        }


        public boolean isRunning() {
            return running;
        }


        public void setRunning(boolean running) {
            this.running = running;
        }


        public boolean hasWroteTodos() {
            return wroteTodos;
        }
    }
    private enum State {
        REGISTER_TO_DF,
        START_LISTENNING_SUBSCRIPTION,
        BLACKBOARD_RUNNING
    }
}
