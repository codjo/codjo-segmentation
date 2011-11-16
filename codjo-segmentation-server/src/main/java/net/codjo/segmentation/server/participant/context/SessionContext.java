package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.blackboard.message.Todo;
import java.util.Collection;
import java.util.Set;
/**
 *
 */
public class SessionContext extends AbstractContext<String, FamilyContext> {

    public SessionContext() {
    }


    public FamilyContext getFamilyContext(Todo<TodoContent> todo) {
        synchronized (lock) {
            return get(todo.getContent().getFamilyId());
        }
    }


    public Set<String> getFamilyIds() {
        synchronized (lock) {
            return getContexts().keySet();
        }
    }


    public Collection<FamilyContext> getFamilyContexts() {
        synchronized (lock) {
            return getContexts().values();
        }
    }
}
