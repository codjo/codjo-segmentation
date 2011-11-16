package net.codjo.segmentation.server.participant.context;
import java.util.Map;
import java.util.TreeMap;
/**
 *
 */
public class AbstractContext<K, T> {
    protected final Object lock = new Object();
    private Map<K, T> contexts = new TreeMap<K, T>();


    public void put(K id, T context) {
        synchronized (lock) {
            contexts.put(id, context);
        }
    }


    public T get(K id) {
        synchronized (lock) {
            return contexts.get(id);
        }
    }


    public T remove(K id) {
        synchronized (lock) {
            return contexts.remove(id);
        }
    }


    protected Map<K, T> getContexts() {
        return contexts;
    }
}
