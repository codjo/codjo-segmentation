package net.codjo.segmentation.server.participant;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionStringifier;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.common.SegmentationPreferenceMock;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreferenceMock;
import net.codjo.sql.builder.DefaultQueryConfig;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.JoinKey.Part;
import net.codjo.sql.builder.JoinKeyExpression;
import java.util.HashMap;
import java.util.Map;

public class DeleteParticipantTest extends SegmentationParticipantTestCase<DeleteParticipant> {
    private static final String DESTINATION_TABLE_NAME = "RESULT";
    private static final String DESTINATION_TABLE_DEF = "AXE_ID int, PHOTO varchar(6)";
    private static final String JOINED_TABLE_NAME = "JOINED";
    private static final String JOINED_TABLE_DEF = "PHOTO varchar(6), CONDITION bit";


    public DeleteParticipantTest() {
        super(JdbcType.JDBC_HSQL);
    }


    public void test_deleteExistingResults_oneSegmentation() throws Exception {
        jdbc.executeUpdate("insert " + DESTINATION_TABLE_NAME + " values (1, '200511')");
        jdbc.executeUpdate("insert " + DESTINATION_TABLE_NAME + " values (2, '200511')");

        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("segmentationId", "1");
        arguments.put("arg1", "200511");
        segmentationContext.mockGetParams(arguments);

        DefaultQueryConfig deleteConfig = new DefaultQueryConfig();
        deleteConfig.setRootTableName(DESTINATION_TABLE_NAME);
        deleteConfig.setRootExpression(new JoinKeyExpression("PHOTO = '$arg1$' and AXE_ID = $segmentationId$"));
        XmlFamilyPreferenceMock preferenceMock = new XmlFamilyPreferenceMock("familyMocked", "ap_mock",
                                                                             DESTINATION_TABLE_NAME);
        preferenceMock.setDeleteConfig(deleteConfig);
        familyContext.mockGetXmlFamilyPreference(preferenceMock);
        segmentationContext.mockGetXmlFamilyPreference(preferenceMock);

        executeHandleTodo(new Todo<TodoContent>(69, todoContent));

        log.assertContent(
              "write(todo{Arguments{IS_LAST_KEY=false, family=familyMocked, level=to-delete}}, Level{information})"
              + ", - , "
              + "write(todo{Arguments{IS_LAST_KEY=true, family=familyMocked, level=to-delete}}, Level{information}), "
              + "write(todo{TodoContent{jobId/family-id/10/100}}, NextLevel{Level{to-delete}}), "
              + "erase(Todo{69}, Level{to-delete})");

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{{"2", "200511"}});
    }


    public void test_deleteExistingResults_joins() throws Exception {
        jdbc.executeUpdate("insert " + DESTINATION_TABLE_NAME + " values (1, '200511')");
        jdbc.executeUpdate("insert " + DESTINATION_TABLE_NAME + " values (1, '200512')");
        jdbc.executeUpdate("insert " + DESTINATION_TABLE_NAME + " values (2, '200511')");

        jdbc.executeUpdate("insert " + JOINED_TABLE_NAME + " values ('200510', 1)");
        jdbc.executeUpdate("insert " + JOINED_TABLE_NAME + " values ('200511', 0)");
        jdbc.executeUpdate("insert " + JOINED_TABLE_NAME + " values ('200512', 1)");

        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("segmentationId", "1");
        arguments.put("cond", "1");
        segmentationContext.mockGetParams(arguments);

        DefaultQueryConfig deleteConfig = new DefaultQueryConfig();
        deleteConfig.setRootTableName(DESTINATION_TABLE_NAME);
        deleteConfig.setRootExpression(new JoinKeyExpression("AXE_ID = $segmentationId$ and CONDITION = $cond$"));
        JoinKey joinKey = new JoinKey(DESTINATION_TABLE_NAME, JoinKey.Type.INNER, JOINED_TABLE_NAME);
        joinKey.addPart(new Part("PHOTO", "=", "PHOTO"));
        deleteConfig.add(joinKey);
        XmlFamilyPreferenceMock preferenceMock = new XmlFamilyPreferenceMock("familyMocked", "ap_mock", DESTINATION_TABLE_NAME);
        preferenceMock.setDeleteConfig(deleteConfig);
        familyContext.mockGetXmlFamilyPreference(preferenceMock);
        segmentationContext.mockGetXmlFamilyPreference(preferenceMock);

        executeHandleTodo(new Todo<TodoContent>(42, todoContent));

        jdbc.assertContent(SqlTable.table(DESTINATION_TABLE_NAME), new String[][]{{"1", "200511"}, {"2", "200511"}});
    }


    @Override
    protected Level getListenedLevel() {
        return new Level("to-delete");
    }


    @Override
    protected DeleteParticipant createParticipant() {
        return new DeleteParticipant(contextManager);
    }


    @Override
    protected void doSetup() throws Exception {
        super.doSetup();
        segmentationContext = declareSegmentationContext(todoContent.getSegmentationId());
        segmentationContext.mockGetSegmentationPreference(new SegmentationPreferenceMock());

        setActionStringifier(new BlackboardActionStringifier(log) {

            @Override
            protected String toString(Todo todo) {
                if (todo.getId() != -1) {
                    return super.toString(todo);
                }
                else {
                    return "todo{" + todo.getContent().toString() + "}";
                }
            }
        });

        jdbc.drop(SqlTable.table(DESTINATION_TABLE_NAME));
        jdbc.drop(SqlTable.table(JOINED_TABLE_NAME));
        jdbc.create(SqlTable.table(DESTINATION_TABLE_NAME), DESTINATION_TABLE_DEF);
        jdbc.create(SqlTable.table(JOINED_TABLE_NAME), JOINED_TABLE_DEF);
    }


    @Override
    protected void doTearDown() throws Exception {
        jdbc.drop(SqlTable.table(DESTINATION_TABLE_NAME));
        jdbc.drop(SqlTable.table(JOINED_TABLE_NAME));
        super.doTearDown();
    }
}
