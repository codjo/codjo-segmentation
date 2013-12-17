package net.codjo.segmentation.server.blackboard;
import java.sql.SQLException;
/**
 *
 */
public interface ErrorLogLimiter {
    public static final ErrorLogLimiter NONE = new ErrorLogLimiter() {
        public final boolean logError(SQLException e) {
            return true;
        }


        public final void close() {
        }


        public String toString() {
            return "NONE";
        }
    };


    boolean logError(SQLException e);


    void close();
}
