package net.codjo.segmentation.server.blackboard;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.sql.server.JdbcServiceUtilMock;
import net.codjo.test.common.LogString;
import java.sql.Connection;
import junit.framework.TestCase;
/**
 *
 */
public class JdbcBlackboardParticipantTest extends TestCase {
    private LogString log = new LogString();


    public void test_handleTodo() throws Exception {
        JdbcServiceUtilMock mock = new JdbcServiceUtilMock(log);

        JdbcBlackboardParticipant participant =
              new JdbcBlackboardParticipant(mock,
                                            JdbcBlackboardParticipant.TransactionType.AUTO_COMMIT,
                                            new Level("my-level")) {
                  @Override
                  protected void handleTodo(Todo todo, Level fromLevel, Connection connection) {
                      log.call("handleTodo", fromLevel.getName(), todo.getId(),
                               connection.getClass().getSimpleName());
                  }
              };

        //noinspection unchecked
        participant.handleTodo(new Todo(1), new Level("level"));

        log.assertContent("getConnectionPool(null, message:null), handleTodo(level, 1, ConnectionMock)");
    }
}
