/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import net.codjo.expression.ExpressionManager;
import net.codjo.expression.FunctionManager;
import net.codjo.segmentation.server.blackboard.JdbcBlackboardParticipant;
import net.codjo.segmentation.server.blackboard.JdbcBlackboardParticipantTestCase;
import net.codjo.segmentation.server.participant.common.ComputeException;
import net.codjo.segmentation.server.participant.common.ExpressionsEvaluator;
import net.codjo.segmentation.server.participant.common.SegmentationResult;
import net.codjo.segmentation.server.participant.context.ContextManagerMock;
import net.codjo.segmentation.server.participant.context.FamilyContextMock;
import net.codjo.segmentation.server.participant.context.SegmentationContextMock;
import net.codjo.segmentation.server.participant.context.SessionContext;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreferenceMock;
import net.codjo.test.common.LogString;
import net.codjo.test.common.mock.ConnectionMock;
/**
 *
 */
public abstract class SegmentationParticipantTestCase<T extends JdbcBlackboardParticipant>
      extends JdbcBlackboardParticipantTestCase<T> {
    protected final TodoContent todoContent = new TodoContent("jobId", "family-id", 10, 100);
    protected ContextManagerMock contextManager = new ContextManagerMock();
    protected SegmentationContextMock segmentationContext;
    protected SessionContext sessionContext = new SessionContext(null, 0, TimeUnit.SECONDS);
    protected FamilyContextMock familyContext = new FamilyContextMock();


    protected SegmentationParticipantTestCase() {
    }


    protected SegmentationParticipantTestCase(JdbcType jdbcType) {
        super(jdbcType);
    }


    @Override
    protected String[] getListenedBlackboardDescriptionTypes() {
        return new String[]{"blackboard", "segmentation"};
    }


    @Override
    protected void doSetup() throws Exception {
        contextManager.put(todoContent.getRequestJobId(), sessionContext);
        sessionContext.put(todoContent.getFamilyId(), familyContext);
    }


    protected SegmentationContextMock declareSegmentationContext(int segmentationId)
          throws SQLException {
        ExpressionsEvaluator evaluator = new ExpressionsEvaluatorMock(new LogString("evaluator", log));
        SegmentationResult result = new SegmentationResultMock(new LogString("result", log));

        SegmentationContextMock mock = new SegmentationContextMock(segmentationId, evaluator, result);
        familyContext.putSegmentationContext(mock);
        return mock;
    }


    protected static class ExpressionsEvaluatorMock extends ExpressionsEvaluator {
        private final LogString logString;


        ExpressionsEvaluatorMock(LogString logString) {
            super(new ExpressionManager(new FunctionManager()), new String[0]);
            this.logString = logString;
        }


        @Override
        public Row compute(Row row) throws ComputeException {
            logString.call("evaluate", row);

            Object value = row.getColumnValue(0);
            Row resultRow = new Row(new String[]{"RESULT"}, new Object[]{value});

            if ("failure".equals(value)) {
                throw new ComputeException(resultRow);
            }

            return resultRow;
        }
    }

    protected static class SegmentationResultMock extends SegmentationResult {
        private final LogString logString;


        SegmentationResultMock(LogString logString) throws SQLException {
            super(new ConnectionMock().getStub(), new XmlFamilyPreferenceMock());
            this.logString = logString;
        }


        @Override
        public void add(Row row) {
            logString.call("add", row);
        }


        @Override
        public void addError(ComputeException error) {
            logString.call("addError", error.getResultRow());
        }


        @Override
        public void close() {
            logString.call("close");
        }


        @Override
        protected String buildInsertQuery() {
            return "insert...";
        }
    }
}
