package net.codjo.segmentation.server.participant.context;
import java.io.Closeable;
/**
 *
 */
public interface SegmentationReport extends Closeable {
    public static interface Task {
        Task createTask(String name);


        void reportError();


        void close();
    }


    Task createTask(String name);


    void close();
}
