package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.DFService;
import net.codjo.agent.MessageTemplate;
import static net.codjo.agent.MessageTemplate.and;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchProtocol;
import net.codjo.agent.protocol.InitiatorHandler;
import net.codjo.agent.protocol.SubscribeInitiator;
import net.codjo.agent.protocol.SubscribeProtocol;
import net.codjo.segmentation.server.blackboard.message.BlackboardAction;
import net.codjo.segmentation.server.blackboard.message.InformOfFailure;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.MessageCodec;
import net.codjo.segmentation.server.blackboard.message.Read;
import net.codjo.segmentation.server.blackboard.message.Todo;
import org.apache.log4j.Logger;
/**
 *
 */
class ParticipantWrapperBehaviour extends Behaviour {
    private static final Logger LOG = Logger.getLogger(ParticipantWrapperBehaviour.class);
    private static final int MAX_TRY_COUNT = 100;
    private final BlackboardParticipant participant;
    private final Level listenedLevel;
    private final MessageTemplate requestTemplate;
    private boolean isStarting = true;
    private int searchBlackBoardTryCount = 0;
    private DFService.AgentDescription blackBoardDescription;
    private MessageCodec codec = new MessageCodec();
    private Aid currentBlackBoardAid;
    private AclMessage receivedMessage;


    protected ParticipantWrapperBehaviour(BlackboardParticipant participant,
                                          Level level,
                                          DFService.AgentDescription blackBoardDescription) {
        this.participant = participant;
        this.listenedLevel = level;
        this.blackBoardDescription = blackBoardDescription;
        requestTemplate = and(matchPerformative(AclMessage.Performative.INFORM),
                              matchProtocol(BlackboardBehaviour.BLACKBOARD_PROTOCOL));
    }


    @Override
    protected final void action() {
        if (isStarting) {
            subscribeToBlackboards();
        }
        else {
            listenRequestResponse();
        }
    }


    private void subscribeToBlackboards() {
        try {
            DFService.AgentDescription[] descriptions = DFService.search(getAgent(), blackBoardDescription);

            if (descriptions.length == 0) {
                searchBlackBoardTryCount++;
                if (searchBlackBoardTryCount < MAX_TRY_COUNT) {
                    block(100 * searchBlackBoardTryCount);
                    return;
                }
                LOG.fatal("Impossible de trouver un blackboard");
                getAgent().die();
                return;
            }

            AclMessage subscribe = new AclMessage(AclMessage.Performative.SUBSCRIBE);
            subscribe.setProtocol(SubscribeProtocol.ID);
            subscribe.setContent(listenedLevel.getName());
            for (DFService.AgentDescription description : descriptions) {
                subscribe.addReceiver(description.getAID());
            }
            getAgent().addBehaviour(
                  new SubscribeInitiator(getAgent(),
                                         new SubscriptionHandler(),
                                         subscribe));
            isStarting = false;
        }
        catch (DFService.DFServiceException e) {
            LOG.fatal("Recherche d'un blackboard en erreur", e);
            getAgent().die();
        }
    }


    private void listenRequestResponse() {
        AclMessage inform = getAgent().receive(requestTemplate);
        if (inform == null) {
            block();
            return;
        }

        try {
            currentBlackBoardAid = inform.getSender();
            Read read = codec.decodeRead(inform);
            receivedMessage = inform;

            Todo todo = null;
            Level level = null;
            try {
                todo = read.getTodo();
                level = read.getLevel();
                //noinspection unchecked
                participant.handleTodo(todo, level);
            }
            catch (Throwable e) {
                send(new InformOfFailure(todo, level).dueTo("Erreur : " + e.getLocalizedMessage()));
                throw e;
            }
        }
        catch (Throwable e) {
            LOG.error("Erreur lors de l'execution de la Todo :\n" + inform.toFipaACLString(), e);
        }
    }


    @Override
    public final boolean done() {
        return false;
    }


    void send(BlackboardAction action) {
        AclMessage request = new AclMessage(AclMessage.Performative.REQUEST);
        request.setProtocol(BlackboardBehaviour.BLACKBOARD_PROTOCOL);
        request.addReceiver(currentBlackBoardAid);
        codec.encode(request, action);

        getAgent().send(request);
    }


    public AclMessage getReceivedMessage() {
        return receivedMessage;
    }


    public Level getListenedLevel() {
        return listenedLevel;
    }


    public DFService.AgentDescription getBlackBoardDescription() {
        return blackBoardDescription;
    }


    static DFService.AgentDescription createDefaultBlackBoardDescription() {
        return new DFService.AgentDescription(
              new DFService.ServiceDescription(BlackboardBehaviour.BLACKBOARD_SERVICE));
    }


    private class SubscriptionHandler implements InitiatorHandler {
        public void handleAgree(AclMessage agree) {
            currentBlackBoardAid = agree.getSender();
            receivedMessage = agree;
            participant.handleConnectedToBlackBoard(agree);
        }


        public void handleRefuse(AclMessage refuse) {
            // Todo
        }


        public void handleInform(AclMessage inform) {
            currentBlackBoardAid = inform.getSender();
            receivedMessage = inform;
            participant.handleTodoNotification(new Level(inform.getContent()));
        }


        public void handleFailure(AclMessage failure) {
            // Todo
        }


        public void handleOutOfSequence(AclMessage outOfSequenceMessage) {
            // Todo
        }


        public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
            // Todo
        }
    }
}
