/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.protocol.SubscribeParticipant;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.LevelManager;
import net.codjo.segmentation.server.blackboard.message.Todo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 */
class BlackboardManager {
    private final LevelManager levelManager;
    private Map<Level, List<Todo>> todos;
    private Map<Level, List<SubscribeParticipant.Subscription>> subscriptions;
    private List<Todo> runningTodo = new ArrayList<Todo>();
    private long nextId = 1;


    BlackboardManager(Level... levels) {
        levelManager = new LevelManager(levels);

        todos = new HashMap<Level, List<Todo>>(levels.length);
        subscriptions = new HashMap<Level, List<SubscribeParticipant.Subscription>>();
        for (Level level : levels) {
            todos.put(level, new ArrayList<Todo>());
            subscriptions.put(level, new ArrayList<SubscribeParticipant.Subscription>());
        }
    }


    public Level getLevel(Level level) {
        return levelManager.getLevel(levelManager.indexOf(level));
    }


    public Todo startFirstTodo(Level level) {
        level = getLevel(level);

        if (!todos.containsKey(level)) {
            return null;
        }

        List<Todo> todoList = todos.get(level);
        if (todoList.isEmpty()) {
            return null;
        }

        Todo todo = todoList.remove(0);
        runningTodo.add(todo);
        return todo;
    }


    public void addTodo(Level level, Todo todo) {
        getList(todos, getLevel(level)).add(todo);
        todo.setId(nextId++);
    }


    public int getRunningTodoCount() {
        return runningTodo.size();
    }


    public void removeTodo(Level level, Todo todo) {
        getList(todos, getLevel(level)).remove(todo);
        runningTodo.remove(todo);
    }


    public boolean isFinished() {
        return runningTodo.size() == 0
               && !hasMoreTodo();
    }


    public List<Todo> getLastTodos() {
        return todos.get(levelManager.getLastLevel());
    }


    public void reset() {
        getLastTodos().clear();
        nextId = 1;
    }


    public boolean todoExists(Level level, Todo todo) {
        List<Todo> list = getList(todos, getLevel(level));
        return list.contains(todo);
    }


    public List<SubscribeParticipant.Subscription> getSubscription(Level level) {
        return subscriptions.get(getLevel(level));
    }


    public void addSubscription(Level level, SubscribeParticipant.Subscription subscription) {
        getList(subscriptions, getLevel(level)).add(subscription);
    }


    public void removeSubscription(Level level, SubscribeParticipant.Subscription subscription) {
        getList(subscriptions, getLevel(level)).remove(subscription);
    }


    private <T> List<T> getList(Map<Level, List<T>> map, Level level) {
        List<T> list = map.get(level);
        if (list == null) {
            throw new IllegalArgumentException("Level " + level.getName() + " inconnu");
        }
        return list;
    }


    private boolean hasMoreTodo() {
        for (Map.Entry<Level, List<Todo>> entry : todos.entrySet()) {
            if (entry.getKey() != levelManager.getLastLevel()
                && !entry.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
