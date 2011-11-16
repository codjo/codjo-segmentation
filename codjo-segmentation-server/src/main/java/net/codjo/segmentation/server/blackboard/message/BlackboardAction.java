package net.codjo.segmentation.server.blackboard.message;
import java.io.Serializable;
/**
 *
 */
public abstract class BlackboardAction implements Serializable {
    private transient BlackboardActionBuilder then;


    public final BlackboardActionBuilder then() {
        if (then == null) {
            then = new BlackboardActionBuilder(this);
        }
        return then;
    }


    boolean hasBlackBoardActionBuilder() {
        return then != null;
    }


    void setBuilder(BlackboardActionBuilder then) {
        this.then = then;
    }


    public abstract void acceptVisitor(BlackboardActionVisitor visitor);
}
