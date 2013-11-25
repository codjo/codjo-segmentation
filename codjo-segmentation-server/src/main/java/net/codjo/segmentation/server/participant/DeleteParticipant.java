package net.codjo.segmentation.server.participant;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import net.codjo.segmentation.common.MidAuditKey;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.participant.context.FamilyContext;
import net.codjo.segmentation.server.participant.context.SegmentationContext;
import net.codjo.segmentation.server.participant.context.SegmentationReport;
import net.codjo.segmentation.server.participant.context.TaskTemplate;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.JoinKey.Part;
import net.codjo.sql.builder.JoinKey.Type;
import net.codjo.sql.builder.QueryConfig;
import net.codjo.variable.TemplateInterpreter;
import net.codjo.variable.UnknownVariableException;
import net.codjo.workflow.common.message.Arguments;

import static net.codjo.segmentation.server.participant.SegmentationLevels.INFORMATION;
import static net.codjo.segmentation.server.participant.SegmentationLevels.TO_DELETE;

public class DeleteParticipant extends BackOfficeSegmentationParticipant {

    public DeleteParticipant(ContextManager contextManager) {
        super(contextManager, TransactionType.AUTO_COMMIT, TO_DELETE);
    }


    @Override
    protected void handleTodo(final Todo<TodoContent> todo, final Level fromLevel, final Connection connection) {
        SegmentationReport report = getReport(todo);
        new TaskTemplate(report, getName()) {
            @Override
            protected void doRun() throws Exception {
                logger.info("Suppression des tables de résultat : " + todo.getContent());

                FamilyContext familyContext = contextManager.getFamilyContext(todo);

                send(write(createAudit(fromLevel, false, familyContext.getFamilyPreference()),
                           INFORMATION));

                for (final SegmentationContext context : familyContext.getSegmentationContexts()) {
                    new TaskTemplate(this, "segment") {
                        @Override
                        protected void doRun() throws Exception {
                            deletePreviousResults(context, connection);
                        }
                    }.run();
                }

                send(write(createAudit(fromLevel, true, familyContext.getFamilyPreference()),
                           INFORMATION)
                           .then()
                           .write(todo, nextLevel(fromLevel))
                           .then()
                           .erase(todo, fromLevel));
            }


            @Override
            protected void handleException(Exception e) {
                logger.fatal("Suppression en erreur : " + todo.getContent(), e);
                send(informOfFailure(todo, fromLevel).dueTo(e));
            }
        }.run();
    }


    private Todo<Arguments> createAudit(Level fromLevel,
                                        boolean isLast,
                                        XmlFamilyPreference familyPreference) {
        Arguments arguments = createAudit(new String[][]{
              {MidAuditKey.LEVEL_KEY, fromLevel.getName()},
              {MidAuditKey.FAMILY_KEY, familyPreference.getFamilyId()},
              {MidAuditKey.IS_LAST_KEY, Boolean.toString(isLast)},
        });
        return new Todo<Arguments>(arguments);
    }


    private void deletePreviousResults(SegmentationContext segmentationContext, Connection connection)
          throws UnknownVariableException, SQLException {
        XmlFamilyPreference familyPreference = segmentationContext.getFamilyPreference();
        QueryConfig queryConfig = familyPreference.getDeleteConfig();

        StringBuilder buffer = new StringBuilder();
        if (queryConfig.getJoinKeyMap().isEmpty()) {
            buffer.append("delete from ").append(queryConfig.getRootTableName());
        }
        else {
            buffer.append("delete ").append(queryConfig.getRootTableName());
            buffer.append(" from ").append(queryConfig.getRootTableName());

            Map<String, JoinKey> joinKeys = queryConfig.getJoinKeyMap();
            Collection<String> jointed = new TreeSet<String>();
            for (JoinKey joinKey : joinKeys.values()) {
                String leftTable = joinKey.getLeftTableName();
                String rightTable = joinKey.getRightTableName();

                if (!jointed.contains(leftTable + rightTable)) {
                    jointed.add(leftTable + rightTable);

                    if (Type.LEFT == joinKey.getJoinType()) {
                        buffer.append(" left join ");
                    }
                    else if (Type.RIGHT == joinKey.getJoinType()) {
                        buffer.append(" right join ");
                    }
                    else {
                        buffer.append(" inner join ");
                    }
                    buffer.append(rightTable).append(" on ");
                    boolean multipleParts = false;
                    for (Object o : joinKey.getPartList()) {
                        Part part = (Part)o;
                        if (multipleParts) {
                            buffer.append(" and ");
                        }
                        buffer.append(rightTable).append(".").append(part.getRightColumn());
                        buffer.append(part.getOperator());
                        buffer.append(leftTable).append(".").append(part.getLeftColumn());
                        multipleParts = true;
                    }
                }
            }
        }
        String whereClause = queryConfig.getRootExpression().getWhereClause();
        String interpretedClause = new TemplateInterpreter().evaluate(whereClause, segmentationContext.getParameters());
        buffer.append(" where ");
        buffer.append(interpretedClause);

        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate(buffer.toString());
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
}
