package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class InformOfFailure extends AbstractBlackboardAction {
    private String errorMessage;


    public InformOfFailure(Todo todo, Level level) {
        super(todo, level);
    }


    @Override
    public void acceptVisitor(BlackboardActionVisitor visitor) {
        visitor.visit(this);
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    public InformOfFailure dueTo(String message) {
        this.errorMessage = message;
        return this;
    }


    public InformOfFailure dueTo(String message, Exception error) {
        this.errorMessage = message + error.getLocalizedMessage();
        return this;
    }


    public InformOfFailure dueTo(Exception error) {
        this.errorMessage = error.getLocalizedMessage();
        return this;
    }
}
