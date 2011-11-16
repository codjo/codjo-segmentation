/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionStringifier;
import net.codjo.segmentation.server.blackboard.message.InformOfFailure;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.context.ContextManagerMock;
import net.codjo.segmentation.server.participant.context.FamilyContext;
import net.codjo.segmentation.server.participant.context.SegmentationContext;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreferenceMock;
import net.codjo.test.common.AssertUtil;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobRequest;
import java.sql.SQLException;
/**
 *
 */
public class JobRequestAnalyzerParticipantTest
      extends SegmentationParticipantTestCase<JobRequestAnalyzerParticipant> {
    public void test_oneSegmentation() throws Exception {
        XmlFamilyPreferenceMock preference = new XmlFamilyPreferenceMock();
        preference.mockGetArgumentNameList("segmentations", "period");
        contextManager.mockGetFamilyPreference("my-family", preference);

        insertSegmentation(jdbc, "1", "my-family", 1, "period=$period$", "name");

        JobRequest request = new JobRequest("segmentation");
        request.setId("job-200256456");
        request.setArguments(new Arguments());
        request.getArguments().put("segmentations", "1");
        request.getArguments().put("period", "200501");

        executeHandleTodo(new Todo<JobRequest>(1, request));

        log.assertContent("erase(Todo{1}, Level{initial-job-request}), "
                          + "write(todo{jobId:job-200256456, familyId:my-family}, NextLevel{Level{initial-job-request}}), "
                          + "write(audit{family=my-family, level=initial-job-request}, Level{information})");

        sessionContext = contextManager.get(request.getId());
        assertNotNull(sessionContext);

        FamilyContext context = sessionContext.get("my-family");
        assertNotNull(context);
        assertEquals("{period=200501, segmentations=1}", context.getParameters().toString());
        assertNotNull(context.getFamilyPreference());
        assertSame(contextManager.getFamilyPreference("my-family"), context.getFamilyPreference());

        assertEquals(1, context.getSegmentationContexts().size());
        SegmentationContext segmentation = context.getSegmentationContext(1);
        assertEquals("{period=200501, segmentationId=1}", segmentation.getParameters().toString());
        assertSame(contextManager.getFamilyPreference("my-family"), segmentation.getFamilyPreference());
        assertEquals(1, segmentation.getSegmentationId());
        assertEquals(1, segmentation.getSegmentationPreference().getSegmentationId());

        AssertUtil.assertEquals(new String[]{"name"}, preference.getTableMetaData().getColumnNames());
    }


    public void test_twoSegmentations() throws Exception {
        contextManager.mockGetFamilyPreference("family-a", new XmlFamilyPreferenceMock());
        contextManager.mockGetFamilyPreference("family-b", new XmlFamilyPreferenceMock());

        insertSegmentation(jdbc, "1", "family-a", 10, "period=$period$", "DEST_1");
        insertSegmentation(jdbc, "2", "family-b", 20, "period=$period$", "DEST_1");
        insertSegmentation(jdbc, "3", "family-a", 30, "period=$period$", "DEST_1");

        JobRequest request = new JobRequest("segmentation");
        request.setId("job-200256456");
        request.setArguments(new Arguments());
        request.getArguments().put("segmentations", "1,   2 , 3");
        request.getArguments().put("period", "200501");

        executeHandleTodo(new Todo<JobRequest>(1, request));

        log.assertContent("erase(Todo{1}, Level{initial-job-request}), "
                          + "write(todo{jobId:job-200256456, familyId:family-a}, NextLevel{Level{initial-job-request}}), "
                          + "write(todo{jobId:job-200256456, familyId:family-b}, NextLevel{Level{initial-job-request}}), "
                          + "write(audit{family=family-a,family-b, level=initial-job-request}, Level{information})");

        sessionContext = contextManager.get(request.getId());
        assertNotNull(sessionContext);

        FamilyContext context = sessionContext.get("family-a");
        assertEquals(2, context.getSegmentationContexts().size());
        assertNotNull(context.getSegmentationContext(1));
        assertNotNull(context.getSegmentationContext(3));

        context = sessionContext.get("family-b");
        assertEquals(1, context.getSegmentationContexts().size());
        assertNotNull(context.getSegmentationContext(2));

        AssertUtil.assertEquals(new String[]{},
                                context.getFamilyPreference().getTableMetaData().getColumnNames());
    }


    public void test_differentSegmentations_withSameSegmentationId() throws Exception {
        // Lancement de la premiere seg sur l'axe 1
        test_oneSegmentation();
        log.clear();
        // Lance une deuxième qui doit se mettre en echec
        JobRequest request = new JobRequest("segmentation");
        request.setId("other");
        request.setArguments(new Arguments());
        request.getArguments().put("segmentations", "1");
        request.getArguments().put("period", "200501");

        executeHandleTodo(new Todo<JobRequest>(2, request));

        log.assertContent("informOfFailure(Level{initial-job-request}, Todo{2})"
                          + ", dueto(L'axe 'TEST' est en cours de calcul.)");
    }


    @Override
    protected void doSetup() throws Exception {
        super.doSetup();
        jdbc.executeUpdate("delete from PM_EXPRESSION");
        jdbc.executeUpdate("delete from PM_SEGMENTATION");
        contextManager = new ContextManagerMock();
        setActionStringifier(new MyBlackboardActionStringifier());
    }


    @Override
    protected void doTearDown() throws Exception {
        jdbc.executeUpdate("delete from PM_EXPRESSION");
        jdbc.executeUpdate("delete from PM_SEGMENTATION");
    }


    @Override
    protected Level getListenedLevel() {
        return new Level("initial-job-request");
    }


    @Override
    protected JobRequestAnalyzerParticipant createParticipant() {
        return new JobRequestAnalyzerParticipant(contextManager);
    }


    public static void insertSegmentation(JdbcFixture jdbc,
                                          String segmentationId,
                                          String familyId,
                                          int expressionId,
                                          String expression,
                                          String destField) throws SQLException {
        jdbc.executeUpdate("insert into PM_SEGMENTATION (SEGMENTATION_ID, SEGMENTATION_NAME, FAMILY)"
                           + " values (" + segmentationId + ", 'TEST', '" + familyId + "')");
        jdbc.executeUpdate("insert into PM_EXPRESSION "
                           + "(EXPRESSION_ID,SEGMENTATION_ID,DESTINATION_FIELD,EXPRESSION,PRIORITY,IS_VARIABLE,VARIABLE_TYPE)"
                           + " values (" + expressionId + ", " + segmentationId + ", '" + destField + "', '"
                           + expression
                           + "', 1, 0, null)");
        jdbc.executeUpdate("insert into PM_EXPRESSION "
                           + "(EXPRESSION_ID,SEGMENTATION_ID,DESTINATION_FIELD,EXPRESSION,PRIORITY,IS_VARIABLE,VARIABLE_TYPE)"
                           + " values (" + expressionId + 1 + ", " + segmentationId
                           + ", 'SLEEVE_CODE', 'cpft', 999, 0, null)");
    }


    private class MyBlackboardActionStringifier extends BlackboardActionStringifier {
        private MyBlackboardActionStringifier() {
            super(JobRequestAnalyzerParticipantTest.this.log);
        }


        @Override
        protected String toString(Todo todo) {
            if (todo.getId() != -1) {
                return super.toString(todo);
            }
            else if (todo.getContent() instanceof TodoContent) {
                TodoContent content = (TodoContent)todo.getContent();
                return "todo{jobId:" + content.getRequestJobId() + ", familyId:"
                       + content.getFamilyId() + "}";
            }
            else {
                Arguments content = (Arguments)todo.getContent();
                return "audit" + content.toMap();
            }
        }


        @Override
        public void visit(InformOfFailure failure) {
            super.visit(failure);
            log.call("dueto", failure.getErrorMessage());
        }
    }
}
