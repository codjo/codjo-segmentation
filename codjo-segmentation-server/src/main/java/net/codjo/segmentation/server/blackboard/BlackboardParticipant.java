package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.DFService;
import net.codjo.segmentation.server.blackboard.message.BlackboardAction;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionBuilder;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import org.apache.log4j.Logger;
/**
 *
 */
public abstract class BlackboardParticipant<T> extends BlackboardActionBuilder {
    protected final Logger logger = Logger.getLogger(getClass());
    private ParticipantWrapperBehaviour behaviour;


    protected BlackboardParticipant(Level level, DFService.AgentDescription blackBoardDescription) {
        super(false);
        behaviour = new ParticipantWrapperBehaviour(this, level, blackBoardDescription);
    }


    protected BlackboardParticipant(Level level) {
        this(level, ParticipantWrapperBehaviour.createDefaultBlackBoardDescription());
    }


    protected void handleConnectedToBlackBoard(AclMessage message) {
    }


    protected void handleTodoNotification(Level level) {
        send(getTodo(level));
    }


    protected abstract void handleTodo(Todo<T> todo, Level fromLevel);


    protected void handleTechnicalError(TechnicalError errorType, AclMessage message, Throwable error) {
        logger.warn("Erreur technique '" + errorType + "' : " + message.toFipaACLString(), error);
    }


    protected void send(BlackboardAction action) {
        behaviour.send(action);
    }


    protected Agent getAgent() {
        return behaviour.getAgent();
    }


    protected AclMessage getReceivedMessage() {
        return behaviour.getReceivedMessage();
    }


    public final Behaviour toBehaviour() {
        return behaviour;
    }


    void setBehaviour(ParticipantWrapperBehaviour behaviour) {
        this.behaviour = behaviour;
    }


    public enum TechnicalError {
        SUBSCRIBE_REFUSED
    }
}
