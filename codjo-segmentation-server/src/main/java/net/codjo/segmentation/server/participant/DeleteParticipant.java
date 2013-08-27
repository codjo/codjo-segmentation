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
import net.codjo.segmentation.server.participant.context.SegmentationReport.Task;
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

public class DeleteParticipant extends SegmentationParticipant<TodoContent> {

    public DeleteParticipant(ContextManager contextManager) {
        super(contextManager, TransactionType.AUTO_COMMIT, TO_DELETE);
    }


    @Override
    protected void handleTodo(Todo<TodoContent> todo, Level fromLevel, Connection connection) {
        SegmentationReport report = getReport(todo);
        Task task = report.createTask(getName());

        try {
            logger.info("Suppression des tables de résultat : " + todo.getContent());

            FamilyContext familyContext = contextManager.getFamilyContext(todo);

            send(write(createAudit(fromLevel, false, familyContext.getFamilyPreference()), INFORMATION));

            for (SegmentationContext context : familyContext.getSegmentationContexts()) {
                Task segmentTask = task.createTask("segment");
                try {
                    deletePreviousResults(context, connection);
                }
                finally {
                    segmentTask.close();
                }
            }

            send(write(createAudit(fromLevel, true, familyContext.getFamilyPreference()), INFORMATION)
                       .then()
                       .write(todo, nextLevel(fromLevel))
                       .then()
                       .erase(todo, fromLevel));
        }
        catch (Exception e) {
            logger.fatal("Suppression en erreur : " + todo.getContent(), e);
            send(informOfFailure(todo, fromLevel).dueTo(e));
        }
        finally {
            task.close();
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug(new StringBuilder("Delete query: ").append(buffer).toString());
        }

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
