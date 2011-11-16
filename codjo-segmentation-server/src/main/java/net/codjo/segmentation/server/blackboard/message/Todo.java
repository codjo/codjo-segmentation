package net.codjo.segmentation.server.blackboard.message;
/**
 *
 */
public class Todo<T> {

    private long id;
    private T content;


    public Todo() {
        this(-1, null);
    }


    public Todo(T content) {
        this(-1, content);
    }


    public Todo(long id) {
        this(id, null);
    }


    public Todo(long id, T content) {
        this.id = id;
        this.content = content;
    }


    public void setId(long id) {
        this.id = id;
    }


    public long getId() {
        return id;
    }


    public T getContent() {
        return content;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Todo todo = (Todo)object;
        return id == todo.id;
    }


    @Override
    public int hashCode() {
        return (int)(id ^ (id >>> 32));
    }
}
