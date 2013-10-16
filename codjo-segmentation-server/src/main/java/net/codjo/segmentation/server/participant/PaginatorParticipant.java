/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.codjo.expression.FindUses;
import net.codjo.expression.InvalidExpressionException;
import net.codjo.segmentation.common.MidAuditKey;
import net.codjo.segmentation.server.blackboard.message.BlackboardAction;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.blackboard.message.Write;
import net.codjo.segmentation.server.participant.common.Page;
import net.codjo.segmentation.server.participant.common.PageStructure;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.participant.context.FamilyContext;
import net.codjo.segmentation.server.participant.context.SegmentationContext;
import net.codjo.segmentation.server.participant.context.SegmentationReport;
import net.codjo.segmentation.server.participant.context.TaskTemplate;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.RowFilter;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.treatment.Expression;
import net.codjo.segmentation.server.util.SegmentationUtil;
import net.codjo.sql.builder.DefaultFieldInfoList;
import net.codjo.sql.builder.FieldInfo;
import net.codjo.sql.builder.QueryBuilder;
import net.codjo.sql.builder.QueryBuilderFactory;
import net.codjo.variable.UnknownVariableException;
import net.codjo.workflow.common.message.Arguments;

import static net.codjo.segmentation.server.util.SegmentationUtil.determineFullColumnName;
/**
 *
 */
public class PaginatorParticipant extends SegmentationParticipant<TodoContent> {
    public PaginatorParticipant(ContextManager contextManager) {
        super(contextManager, TransactionType.AUTO_COMMIT, SegmentationLevels.TO_PAGINATE);
    }


    @Override
    protected void handleTodo(Todo<TodoContent> todo, Level fromLevel, Connection connection) {
        logger.info("Pagination des données input : " + todo.getContent());
        new Paginator().run(todo, fromLevel, connection);
    }


    static Set<String> findUsedFieldInExpressions(List<SegmentationContext> contexts)
          throws InvalidExpressionException {
        FindUses findUses = new FindUses();

        for (SegmentationContext context : contexts) {
            for (Expression expression : context.getSegmentationPreference().getExpressions()) {
                findUses.add(expression.getExpression());
            }
        }
        Collection usedFields = findUses.buildReport().getUsedSourceColumns();

        //noinspection unchecked
        return new HashSet<String>(usedFields);
    }


    class Paginator {
        private String[] sourceColumnNames;
        private Set<String> fieldsUsedInExpressions;
        private Todo<TodoContent> currentTodo;
        private Level currentLevel;
        private List<SegmentationContext> segmentationContexts;
        private FamilyContext familyContext;
        private DefaultFieldInfoList fieldInfoList;
        private int pageToBeComputedCount = 0;
        private boolean hasNext = true;
        private XmlFamilyPreference familyPreference;


        public void run(final Todo<TodoContent> todo, final Level fromLevel, final Connection connection) {
            SegmentationReport report = getReport(todo);
            new TaskTemplate(report, getName()) {
                @Override
                protected void doRun() throws Exception {
                    initialize(todo, fromLevel);
                    pagineData(connection, this);
                }


                @Override
                protected void handleException(Exception e) {
                    logger.fatal("Pagination en erreur : " + todo.getContent(), e);
                    send(informOfFailure(todo, fromLevel).dueTo(e));
                }
            }.run();

            logger.info("Nombre de pages : " + pageToBeComputedCount);
        }


        void initialize(Todo<TodoContent> todo, Level level)
              throws InvalidExpressionException {
            currentTodo = todo;
            currentLevel = level;
            familyContext = contextManager.getFamilyContext(currentTodo);
            segmentationContexts = familyContext.getSegmentationContexts();
            fieldsUsedInExpressions = findUsedFieldInExpressions(segmentationContexts);
            familyPreference = familyContext.getFamilyPreference();
        }


        private void pagineData(Connection connection, TaskTemplate parentTask)
              throws SQLException, UnknownVariableException {
            final Statement statement = connection.createStatement();
            try {
                final int[] pageId = {1};
                final Page[] page = {new Page()};

                final String selectQuery = buildSelectQuery(familyContext);
                logger.info("selectQuery=" + selectQuery);

                final ResultSet[] resultSet = new ResultSet[1];
                new TaskTemplate(parentTask, "select") {
                    @Override
                    protected void doRun() throws Exception {
                        resultSet[0] = statement.executeQuery(selectQuery);
                    }
                }.run();

                try {
                    PageStructure pageStructure = createPageStructure(resultSet[0].getMetaData());
                    for (SegmentationContext context : familyContext.getSegmentationContexts()) {
                        context.setPageStructure(pageStructure);
                    }

                    hasNext = resultSet[0].next();
                    while (hasNext) {
                        new TaskTemplate(parentTask, "processRow") {
                            @Override
                            protected void doRun() throws Exception {
                                if (page[0].isFull()) {
                                    send(createComputeTodos(pageId[0]++, page[0]));
                                    page[0] = new Page();
                                }
                                page[0].addRow(extractRow(resultSet[0]));
                                hasNext = resultSet[0].next();
                            }
                        }.run();
                    }
                }
                finally {
                    resultSet[0].close();
                }

                if (page[0].getRowCount() > 0) {
                    send(createComputeTodos(pageId[0], page[0]).then().erase(currentTodo, currentLevel));
                }
                else {
                    send(erase(currentTodo, currentLevel));
                }
            }
            finally {
                statement.close();
            }
        }


        private Row extractRow(ResultSet resultSet)
              throws SQLException {
            Object[] values = new Object[sourceColumnNames.length];
            for (int i = 1; i <= sourceColumnNames.length; i++) {
                values[i - 1] = resultSet.getObject(i);
            }
            return new Row(sourceColumnNames, values);
        }


        private BlackboardAction createComputeTodos(int pageId, Page page) {
            Level nextLevel = nextLevel(currentLevel);
            Write write = null;
            for (SegmentationContext context : segmentationContexts) {
                context.putPage(pageId, page);
                pageToBeComputedCount++;
                boolean isLast = !hasNext && isLastContext(context);
                if (write == null) {
                    write = write(createTodoAudit(isLast), SegmentationLevels.INFORMATION)
                          .then()
                          .write(createComputeTodo(pageId, context.getSegmentationId()), nextLevel);
                }
                else {
                    write.then()
                          .write(createTodoAudit(isLast), SegmentationLevels.INFORMATION)
                          .then()
                          .write(createComputeTodo(pageId, context.getSegmentationId()), nextLevel);
                }
            }
            return write;
        }


        private Todo<TodoContent> createComputeTodo(int pageId, int segmentationId) {
            TodoContent todoContent = currentTodo.getContent();
            return new Todo<TodoContent>(new TodoContent(todoContent, segmentationId, pageId));
        }


        String buildSelectQuery(FamilyContext context) throws UnknownVariableException {
            fieldInfoList = new DefaultFieldInfoList();
            QueryBuilder queryBuilder
                  = QueryBuilderFactory.newSelectQueryBuilder(familyPreference.getSelectConfig());
            List<String> fields = new ArrayList<String>(fieldsUsedInExpressions);
            if (familyPreference.hasFilter()) {
                RowFilter filter = familyPreference.getFilter();
                fields.add(SegmentationUtil.determineFullColumnName(filter.getTableName(),
                                                                    filter.getColumnName()));
            }
            return SegmentationUtil.buildSelectQuery(queryBuilder,
                                                     fields,
                                                     context.getParameters(),
                                                     fieldInfoList);
        }


        private PageStructure createPageStructure(ResultSetMetaData metaData)
              throws SQLException {
            int columnCount = fieldInfoList.size();
            sourceColumnNames = new String[columnCount];
            Map<String, Integer> columnTypesByName = new HashMap<String, Integer>(columnCount);

            for (int i = 0; i < fieldInfoList.size(); i++) {
                FieldInfo info = fieldInfoList.getFieldInfo(i);
                String field = determineFullColumnName(info.getDBTableName(), info.getDBFieldName());
                sourceColumnNames[i] = field;
                columnTypesByName.put(field, metaData.getColumnType(i + 1));
            }

            return new PageStructure(columnTypesByName);
        }


        private boolean isLastContext(SegmentationContext context) {
            return context == segmentationContexts.get(segmentationContexts.size() - 1);
        }


        private Todo<Arguments> createTodoAudit(boolean isLast) {
            Arguments arguments = PaginatorParticipant.this.createAudit(new String[][]{
                  {MidAuditKey.LEVEL_KEY, currentLevel.getName()},
                  {MidAuditKey.FAMILY_KEY, familyPreference.getFamilyId()},
                  {MidAuditKey.PAGE_COUNT_KEY, Integer.toString(pageToBeComputedCount)},
                  {MidAuditKey.IS_LAST_KEY, Boolean.toString(isLast)}
            });
            return new Todo<Arguments>(arguments);
        }
    }
}
