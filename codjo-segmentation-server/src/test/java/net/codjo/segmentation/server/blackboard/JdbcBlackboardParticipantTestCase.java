/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.blackboard;
import net.codjo.agent.DFService;
import net.codjo.agent.test.DummyAgent;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.segmentation.server.blackboard.message.BlackboardAction;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionStringifier;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.test.common.LogString;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import junit.framework.TestCase;
public abstract class JdbcBlackboardParticipantTestCase<T extends JdbcBlackboardParticipant>
      extends TestCase {
    protected enum JdbcType {
        JDBC_TOKIO,
        JDBC_SYBASE,
        JDBC_HSQL;
    }
    protected JdbcFixture jdbc;
    protected LogString log = new LogString();
    private BlackboardActionStringifier actionStringifier = new BlackboardActionStringifier(log);


    protected JdbcBlackboardParticipantTestCase() {
        this(JdbcType.JDBC_TOKIO);
    }


    protected JdbcBlackboardParticipantTestCase(JdbcType jdbcType) {
        switch (jdbcType) {
            case JDBC_HSQL:
            case JDBC_SYBASE:
                try {
                    jdbc = new DatabaseFactory().createJdbcFixture();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case JDBC_TOKIO:
                jdbc = JdbcFixture.newFixture();
                break;
        }
    }


    protected abstract Level getListenedLevel();


    protected abstract T createParticipant();


    protected abstract String[] getListenedBlackboardDescriptionTypes();


    public void test_level() throws Exception {
        T participant = createParticipant();
        Level actualLevel = ((ParticipantWrapperBehaviour)participant.toBehaviour()).getListenedLevel();
        assertEquals(getListenedLevel(), actualLevel);
    }


    public void test_blackboardDescription() throws Exception {
        T participant = createParticipant();
        DFService.AgentDescription description =
              ((ParticipantWrapperBehaviour)participant.toBehaviour()).getBlackBoardDescription();

        Set<String> result = new TreeSet<String>();
        Iterator iterator = description.getAllServices();
        while (iterator.hasNext()) {
            DFService.ServiceDescription serviceDescription = (DFService.ServiceDescription)iterator.next();
            result.add(serviceDescription.getType());
        }

        Set<String> expected = new TreeSet<String>();
        expected.addAll(Arrays.asList(getListenedBlackboardDescriptionTypes()));

        assertEquals(expected.toString(), result.toString());
    }


    protected T executeHandleTodo(Todo todo) throws SQLException {
        T participant = createParticipant();

        ParticipantWrapperBehaviourMock mock =
              new ParticipantWrapperBehaviourMock(participant, getListenedLevel());
        mock.setAgent(new DummyAgent());

        participant.setBehaviour(mock);

        //noinspection unchecked
        participant.handleTodo(todo, getListenedLevel(), jdbc.getConnection());

        for (int i = 0; i < mock.actions.size(); i++) {
            BlackboardAction action = mock.actions.get(i);
            if (i > 0) {
                log.info("- ");
            }
            actionStringifier.logify(action);
        }

        return participant;
    }


    protected void setActionStringifier(BlackboardActionStringifier actionStringifier) {
        this.actionStringifier = actionStringifier;
    }


    @Override
    protected final void setUp() throws Exception {
        jdbc.doSetUp();
        try {
            doSetup();
        }
        catch (Exception e) {
            jdbc.doTearDown();
            throw e;
        }
    }


    protected void doSetup() throws Exception {
    }


    @Override
    protected final void tearDown() throws Exception {
        try {
            doTearDown();
        }
        finally {
            jdbc.doTearDown();
        }
    }


    protected void doTearDown() throws Exception {
    }


    private class ParticipantWrapperBehaviourMock extends ParticipantWrapperBehaviour {
        List<BlackboardAction> actions = new ArrayList<BlackboardAction>();


        protected ParticipantWrapperBehaviourMock(BlackboardParticipant participant, Level level) {
            super(participant, level, createDefaultBlackBoardDescription());
        }


        @Override
        void send(BlackboardAction action) {
            actions.add(action);
        }
    }
}
