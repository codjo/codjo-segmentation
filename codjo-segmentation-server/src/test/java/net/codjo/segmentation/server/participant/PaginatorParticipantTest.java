package net.codjo.segmentation.server.participant;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.segmentation.server.blackboard.message.BlackboardActionStringifier;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.common.Page;
import net.codjo.segmentation.server.participant.common.PageStructure;
import net.codjo.segmentation.server.participant.common.SegmentationPreferenceMock;
import net.codjo.segmentation.server.participant.context.SegmentationContextMock;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.RowFilter;
import net.codjo.segmentation.server.preference.family.TableMetaData;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.treatment.Expression;
import net.codjo.sql.builder.DefaultQueryConfig;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.JoinKeyExpression;
import net.codjo.workflow.common.message.Arguments;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class PaginatorParticipantTest extends SegmentationParticipantTestCase<PaginatorParticipant> {
    private static final String ROOT_TABLE_NAME = "AP_ROOT";
    private static final String DESTINATION_TABLE_NAME = "AP_DESTINATION";
    private static final String FILTER_TABLE_NAME = "AP_FILTER";
    private static final String FILTER_COLUMN_NAME = "FILTER_LIST";


    public PaginatorParticipantTest() {
        super(JdbcType.JDBC_TOKIO);
    }


    public void test_buildSelectQuery() throws Exception {
        String expectedQuery =
              "select "
              + ROOT_TABLE_NAME + ".COL_STR as alias_1 , "
              + ROOT_TABLE_NAME + ".COL_NUM as alias_2"
              + " from " + ROOT_TABLE_NAME
              + " where "
              + ROOT_TABLE_NAME + ".PHOTO='200511'"
              + " and " + ROOT_TABLE_NAME + ".SEGMENTATION_ID=1";

        assertSelectQuery(expectedQuery);
    }


    public void test_buildSelectQuery_with_filter() throws Exception {
        String expectedQuery =
              "select "
              + ROOT_TABLE_NAME + ".COL_STR as alias_1 , "
              + ROOT_TABLE_NAME + ".COL_NUM as alias_2 , "
              + FILTER_TABLE_NAME + "." + FILTER_COLUMN_NAME + " as alias_3"
              + " from (" + ROOT_TABLE_NAME + " inner join " + FILTER_TABLE_NAME
              + " on (" + ROOT_TABLE_NAME + ".COL_NUM = " + FILTER_TABLE_NAME + ".COL_NUM))"
              + " where "
              + ROOT_TABLE_NAME + ".PHOTO='200511'"
              + " and " + ROOT_TABLE_NAME + ".SEGMENTATION_ID=1";

        segmentationContext.getFamilyPreference().setFilter(newAllRowsFilter(FILTER_TABLE_NAME,
                                                                             FILTER_COLUMN_NAME));
        assertSelectQuery(expectedQuery);
    }


    public void test_pagine_noPage() throws Exception {
        executeHandleTodo(new Todo<TodoContent>(1, todoContent));

        log.assertContent("erase(Todo{1}, Level{to-paginate})");
    }


    public void test_pagine_onePage() throws Exception {
        jdbc.executeUpdate("insert into " + ROOT_TABLE_NAME + " values ('OPCVM AGF', 41, '200511',1)");
        jdbc.executeUpdate("insert into " + ROOT_TABLE_NAME + " values ('ACTION AGF', 53, '200511',1)");

        executeHandleTodo(new Todo<TodoContent>(500, todoContent));

        log.assertContent(
              "write(audit{IS_LAST_KEY=true, family=familyId, level=to-paginate, page-count=1}, Level{information})"
              + ", write(Todo{page:1, segmentationId:10}, NextLevel{Level{to-paginate}})"
              + ", erase(Todo{500}, Level{to-paginate})");

        Page page = segmentationContext.removePage(1);
        assertNotNull(page);
        assertEquals(2, page.getRowCount());
        assertRowStructure("[AP_ROOT$COL_STR, AP_ROOT$COL_NUM]", page.getRow(0));
    }


    public void test_pagine_onePageWithFilter() throws Exception {
        jdbc.executeUpdate("insert into " + ROOT_TABLE_NAME + " values ('OPCVM AGF', 41, '200511',1)");
        jdbc.executeUpdate("insert into " + ROOT_TABLE_NAME + " values ('ACTION AGF', 53, '200511',1)");
        segmentationContext.getFamilyPreference().setFilter(newAllRowsFilter(ROOT_TABLE_NAME, "PHOTO"));

        executeHandleTodo(new Todo<TodoContent>(500, todoContent));

        log.assertContent(
              "write(audit{IS_LAST_KEY=true, family=familyId, level=to-paginate, page-count=1}, Level{information})"
              + ", write(Todo{page:1, segmentationId:10}, NextLevel{Level{to-paginate}})"
              + ", erase(Todo{500}, Level{to-paginate})");

        Page page = segmentationContext.removePage(1);
        assertNotNull(page);
        assertEquals(2, page.getRowCount());
        assertRowStructure("[AP_ROOT$COL_STR, AP_ROOT$COL_NUM, AP_ROOT$PHOTO]", page.getRow(0));
    }


    public void test_pagine_twoPages() throws Exception {
        swapToHsqldb();

        for (int i = 0; i < 1001; i++) {
            jdbc.executeUpdate("insert into " + ROOT_TABLE_NAME + " values ('OPCVM', " + i + ", '200511',1)");
        }

        executeHandleTodo(new Todo<TodoContent>(500, todoContent));

        log.assertContent(
              "write(audit{IS_LAST_KEY=false, family=familyId, level=to-paginate, page-count=1}, Level{information})"
              + ", write(Todo{page:1, segmentationId:10}, NextLevel{Level{to-paginate}})"
              + ", - "
              + ", write(audit{IS_LAST_KEY=true, family=familyId, level=to-paginate, page-count=2}, Level{information})"
              + ", write(Todo{page:2, segmentationId:10}, NextLevel{Level{to-paginate}})"
              + ", erase(Todo{500}, Level{to-paginate})");

        assertNotNull(segmentationContext.removePage(1));
        assertNotNull(segmentationContext.removePage(2));
    }


    public void test_pagine_onePage_twoSegmentations() throws Exception {
        swapToHsqldb();
        SegmentationContextMock segmentationContext2 = declareSegmentationContext(11);
        fillContext(segmentationContext2);

        jdbc.executeUpdate("insert into " + ROOT_TABLE_NAME + " values ('OPCVM AGF', 41, '200511',1)");

        executeHandleTodo(new Todo<TodoContent>(500, todoContent));

        log.assertContent(
              "write(audit{IS_LAST_KEY=false, family=familyId, level=to-paginate, page-count=1}, Level{information})"
              + ", write(Todo{page:1, segmentationId:10}, NextLevel{Level{to-paginate}})"
              + ", write(audit{IS_LAST_KEY=true, family=familyId, level=to-paginate, page-count=2}, Level{information})"
              + ", write(Todo{page:1, segmentationId:11}, NextLevel{Level{to-paginate}})"
              + ", erase(Todo{500}, Level{to-paginate})");

        assertNotNull(segmentationContext.removePage(1));
        assertNotNull(segmentationContext2.removePage(1));

        PageStructure structure = segmentationContext2.getPageStructure();
        assertEquals("{AP_ROOT$COL_STR=12, AP_ROOT$COL_NUM=4}", structure.getColumnTypesByName().toString());
    }


    @Override
    protected void doSetup() throws Exception {
        super.doSetup();
        segmentationContext = declareSegmentationContext(todoContent.getSegmentationId());
        createTables();
        fillContext(segmentationContext);
        setActionStringifier(new BlackboardActionStringifier(log) {

            @Override
            protected String toString(Todo todo) {
                if (todo.getId() != -1) {
                    return super.toString(todo);
                }
                else if (todo.getContent() instanceof TodoContent) {
                    TodoContent content = (TodoContent)todo.getContent();
                    return "Todo{page:" + content.getPageId()
                           + ", segmentationId:" + content.getSegmentationId() + "}";
                }
                else {
                    Arguments content = (Arguments)todo.getContent();
                    Map<String, String> argumentMap = content.toMap();
                    for (String s : argumentMap.keySet()) {

                    }
                    return "audit" + argumentMap;
                }
            }
        });
    }


    @Override
    protected Level getListenedLevel() {
        return new Level("to-paginate");
    }


    private void swapToHsqldb() throws Exception {
        jdbc.doTearDown();
//        jdbc = JdbcFixture.newHsqlDbFixture();
        // TODO Uutilisation de hsqldb par défaut pour les lib ??? 
        jdbc = new DatabaseFactory().createJdbcFixture();
        jdbc.doSetUp();
        doSetup();
    }


    private void assertRowStructure(String expected, Row row) {
        assertEquals(expected,
                     Arrays.asList(row.getColumnNames()).toString());
    }


    private void assertSelectQuery(String expectedQuery) throws Exception {
        assertEquals(expectedQuery, new MyPaginatorParticipant().doBuildSelectQuery());
    }


    @Override
    protected PaginatorParticipant createParticipant() {
        return new PaginatorParticipant(contextManager);
    }


    private void fillContext(SegmentationContextMock context) throws Exception {
        Map<String, String> parameters = new HashMap<String, String>(2);
        parameters.put("photo", "200511");
        parameters.put("segmentation.id", "1");

        context.mockGetParams(parameters);
        familyContext.mockGetParams(parameters);

        List<Expression> expressions = new ArrayList<Expression>();
        expressions.add(new Expression(0, null, "(SRC_" + ROOT_TABLE_NAME + "$COL_STR==\"OPCVM AGF\")"));
        expressions.add(new Expression(0, null, "(SRC_" + ROOT_TABLE_NAME + "$COL_NUM==13)"));

        SegmentationPreferenceMock segmentationPreferenceMock = new SegmentationPreferenceMock();
        segmentationPreferenceMock.mockGetExpressions(expressions);

        context.mockGetSegmentationPreference(segmentationPreferenceMock);

        XmlFamilyPreference familyPreference = new XmlFamilyPreference("familyId", "ROOT_TABLE_NAME",
                                                                       DESTINATION_TABLE_NAME);
        familyPreference.setFunctionHolderClassList(new ArrayList<String>(0));
        familyPreference.setTableMetaData(TableMetaData.create(DESTINATION_TABLE_NAME, jdbc.getConnection()));

        context.mockGetXmlFamilyPreference(familyPreference);
        familyContext.mockGetXmlFamilyPreference(familyPreference);

        DefaultQueryConfig queryConfig = new DefaultQueryConfig();
        queryConfig.setRootTableName(ROOT_TABLE_NAME);
        queryConfig.setRootExpression(new JoinKeyExpression(ROOT_TABLE_NAME
                                                            + ".PHOTO='$photo$' and " + ROOT_TABLE_NAME
                                                            + ".SEGMENTATION_ID=$segmentation.id$"));
        JoinKey joinKey =
              new JoinKey(ROOT_TABLE_NAME, JoinKey.Type.INNER, FILTER_TABLE_NAME);
        joinKey.addPart(new JoinKey.Part("COL_NUM", "=", "COL_NUM"));
        queryConfig.add(joinKey);

        familyPreference.setSelectConfig(queryConfig);
    }


    private void createTables() throws SQLException {
        jdbc.create(SqlTable.table(ROOT_TABLE_NAME),
                    "COL_STR varchar(10), COL_NUM int, PHOTO varchar(27), SEGMENTATION_ID int");
        jdbc.create(SqlTable.table(DESTINATION_TABLE_NAME),
                    "COL_STR varchar(10), COL_NUM int, ANOMALY int, ANOMALY_LOG varchar(255)");
        jdbc.create(SqlTable.table(FILTER_TABLE_NAME), "COL_NUM int, FILTER_LIST varchar(255)");
    }


    private RowFilter newAllRowsFilter(final String tableName, final String columnName) {
        return new RowFilter() {
            public boolean isRowExcluded(int segmentationId, Row row, Object filterValue) {
                return true;
            }


            public String getTableName() {
                return tableName;
            }


            public String getColumnName() {
                return columnName;
            }
        };
    }


    private class MyPaginatorParticipant extends PaginatorParticipant {
        private MyPaginatorParticipant() {
            super(PaginatorParticipantTest.this.contextManager);
        }


        public String doBuildSelectQuery() throws Exception {
            Paginator paginator = new Paginator();
            paginator.initialize(new Todo<TodoContent>(todoContent), getListenedLevel());
            return paginator.buildSelectQuery(familyContext);
        }
    }
}
