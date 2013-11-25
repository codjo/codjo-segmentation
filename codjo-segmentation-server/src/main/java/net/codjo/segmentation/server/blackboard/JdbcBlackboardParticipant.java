package net.codjo.segmentation.server.blackboard;
import java.sql.Connection;
import java.sql.SQLException;
import net.codjo.agent.DFService;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.sql.server.ConnectionPool;
import net.codjo.sql.server.JdbcServiceUtil;
/**
 *
 */
public abstract class JdbcBlackboardParticipant<T> extends BlackboardParticipant<T> {
    public enum TransactionType {
        AUTO_COMMIT,
        TRANSACTIONAL
    }
    private final JdbcServiceUtil jdbcUtil;
    private final TransactionType type;


    protected JdbcBlackboardParticipant(JdbcServiceUtil jdbcUtil, TransactionType type, Level level) {
        super(level);
        this.jdbcUtil = jdbcUtil;
        this.type = type;
    }


    protected JdbcBlackboardParticipant(JdbcServiceUtil jdbcUtil, TransactionType type, Level level,
                                        DFService.AgentDescription blackBoardDescription) {
        super(level, blackBoardDescription);
        this.jdbcUtil = jdbcUtil;
        this.type = type;
    }


    @Override
    protected final void handleTodo(Todo<T> todo, Level fromLevel) {
        if (todo == null) {
            return;
        }

        Connection connection;
        try {
            ConnectionPool pool = jdbcUtil.getConnectionPool(getAgent(), getReceivedMessage());
            connection = pool.getConnection();
            try {
                if (type == TransactionType.TRANSACTIONAL) {
                    connection.setAutoCommit(false);
                }

                handleTodo(todo, fromLevel, connection);

                // todo gestion des rollback ? ici dans les sous-classes ?

                if (type == TransactionType.TRANSACTIONAL) {
                    connection.commit();
                    connection.setAutoCommit(true);
                }
            }
            finally {
                pool.releaseConnection(connection);
            }
        }
        catch (SQLException e) {
            if (getActualLogLimiter(todo).logError(e)) {
                logger.warn(getClass().getSimpleName() + " a rencontré une erreur d'accès BD", e);
            }
            send(informOfFailure(todo, fromLevel).dueTo("Erreur technique : " + e.getLocalizedMessage()));
        }
    }


    protected abstract void handleTodo(Todo<T> todo, Level fromLevel, Connection connection)
          throws SQLException;


    private final ErrorLogLimiter getActualLogLimiter(Todo<T> todo) {
        ErrorLogLimiter errorLogLimiter = null;

        try {
            errorLogLimiter = getErrorLogLimiter(todo);
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return (errorLogLimiter == null) ? ErrorLogLimiter.NONE : errorLogLimiter;
    }


    protected abstract ErrorLogLimiter getErrorLogLimiter(Todo<T> todo);
}
