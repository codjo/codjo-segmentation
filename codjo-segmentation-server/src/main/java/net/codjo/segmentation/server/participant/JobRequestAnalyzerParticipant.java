/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.codjo.segmentation.common.MidAuditKey;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.segmentation.server.blackboard.message.BlackboardAction;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.participant.context.FamilyContext;
import net.codjo.segmentation.server.participant.context.SegmentationContext;
import net.codjo.segmentation.server.participant.context.SegmentationReport;
import net.codjo.segmentation.server.participant.context.SessionContext;
import net.codjo.segmentation.server.participant.context.TaskTemplate;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.TableMetaData;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.treatment.Expression;
import net.codjo.segmentation.server.preference.treatment.SegmentationPreference;
import net.codjo.segmentation.server.util.SegmentationUtil;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobRequest;
/**
 * Decoupe la JobRequest en todos par famille
 */
public class JobRequestAnalyzerParticipant extends SegmentationParticipant<JobRequest> {
    private static final String SEGMENTATIONS = SegmentationJobRequest.SEGMENTATION_IDS;
    private static final String SEGMENTATION_ID = "segmentationId";


    public JobRequestAnalyzerParticipant(ContextManager contextManager) {
        super(contextManager, TransactionType.AUTO_COMMIT, SegmentationLevels.FIRST);
    }


    @Override
    protected void handleTodo(final Todo<JobRequest> todo, final Level fromLevel, final Connection connection) {
        final SegmentationReport report = createReport();
        new TaskTemplate(report, getName()) {
            @Override
            protected void doRun() throws Exception {
                logger.info("Analyse de la requete : " + todo.getContent());
                SessionContext sessionContext = new SessionContext(report);
                contextManager.put(todo.getContent().getId(), sessionContext);

                final Arguments arguments = todo.getContent().getArguments();
                String[] segmentationIds = extractSegmentationId(arguments);

                for (final String segmentationId : segmentationIds) {
                    new TaskTemplate(this, "segment") {
                        protected void doRun() throws Exception {
                            Map<String, String> parameters = new TreeMap<String, String>();
                            parameters.putAll(arguments.toMap());
                            parameters.put(SEGMENTATION_ID, segmentationId);
                            parameters.remove(SEGMENTATIONS);

                            SegmentationPreference segmentationPreference =
                                  SegmentationPreference.createPreference(connection, Integer.parseInt(segmentationId),
                                                                          parameters);

                            FamilyContext familyContext = getFamilyContext(connection, segmentationPreference, todo);

                            SegmentationUtil.checkParameters(familyContext.getFamilyPreference().getArgumentNameList(),
                                                             arguments.toMap());

                            familyContext.putSegmentationContext(new SegmentationContext(
                                  segmentationPreference.getSegmentationId(),
                                  familyContext.getFamilyPreference(),
                                  segmentationPreference,
                                  parameters));
                        }
                    }.run();
                }

                Level nextLevel = nextLevel(fromLevel);

                if (hasComputeConflict(sessionContext, todo, fromLevel)) {
                    return;
                }

                BlackboardAction action = erase(todo, fromLevel);
                StringBuffer ids = new StringBuffer();
                for (String familyId : sessionContext.getFamilyIds()) {
                    action.then().write(createTodo(todo, familyId), nextLevel);
                    appendToBuffer(ids, familyId);
                }
                createAudit(action, fromLevel, ids);
                send(action);
            }


            @Override
            protected void handleException(Exception e) {
                logger.fatal("Analyse en erreur : " + todo.getContent(), e);
                send(informOfFailure(todo, fromLevel).dueTo(e));
            }
        }.run();
    }


    private boolean hasComputeConflict(SessionContext session, Todo<JobRequest> todo, Level fromLevel) {
        session.getFamilyContexts();
        for (FamilyContext familyContext : session.getFamilyContexts()) {
            for (SegmentationContext segmentationContext : familyContext.getSegmentationContexts()) {
                SegmentationContext conflictContext = hasComputeConflict(session, segmentationContext);
                if (conflictContext != null) {
                    send(informOfFailure(todo, fromLevel)
                               .dueTo("L'axe '" + conflictContext.getSegmentationPreference().getSegmentationName()
                                      + "' est en cours de calcul."));
                    return true;
                }
            }
        }
        return false;
    }


    private SegmentationContext hasComputeConflict(SessionContext current,
                                                   SegmentationContext currentSegmentation) {
        for (SessionContext session : contextManager.getSessions()) {
            if (session == current) {
                continue;
            }
            for (FamilyContext familyContext : session.getFamilyContexts()) {
                for (SegmentationContext segmentationContext : familyContext.getSegmentationContexts()) {
                    if (segmentationContext.getSegmentationId() == currentSegmentation.getSegmentationId()) {
                        return segmentationContext;
                    }
                }
            }
        }
        return null;
    }


    private void createAudit(BlackboardAction action, Level fromLevel, StringBuffer ids) {
        Arguments arguments = createAudit(new String[][]{
              {MidAuditKey.LEVEL_KEY, fromLevel.getName()},
              {MidAuditKey.FAMILY_KEY, ids.toString()}});
        action.then().write(new Todo<Arguments>(arguments), SegmentationLevels.INFORMATION);
    }


    private String[] extractSegmentationId(Arguments arguments) {
        String segmentationIdList = arguments.get(SEGMENTATIONS);
        if (segmentationIdList == null) {
            throw new IllegalArgumentException("L'argument '" + SEGMENTATIONS
                                               + "' listant les axes est absent de la requête.");
        }
        return segmentationIdList.replaceAll(" ", "").split(",");
    }


    private Todo<TodoContent> createTodo(Todo<JobRequest> initialTodo, String familyId) {
        return new Todo<TodoContent>(new TodoContent(initialTodo.getContent().getId(), familyId));
    }


    private FamilyContext getFamilyContext(Connection connection,
                                           SegmentationPreference segmentationPreference,
                                           Todo<JobRequest> todo)
          throws SQLException {
        JobRequest jobRequest = todo.getContent();
        String familyId = segmentationPreference.getFamily();
        SessionContext sessionContext = contextManager.get(jobRequest.getId());
        FamilyContext familyContext = sessionContext.get(familyId);

        if (familyContext != null) {
            return familyContext;
        }

        XmlFamilyPreference familyPreference = contextManager.getFamilyPreference(familyId);
        if (familyPreference == null) {
            throw new SQLException("no familyPreference found for familyId=" + familyId);
        }

        familyContext = new FamilyContext(familyPreference, jobRequest.getArguments().toMap());
        sessionContext.put(familyId, familyContext);

        // Todo : attention ce code n'est pas Thread safe
        //        il est possible que le metaData soit chargé plusieurs fois
        if (familyPreference.getTableMetaData() == null) {
            TableMetaData tableMetaData =
                  TableMetaData.create(familyPreference.getDestinationTable(), connection);

            List<String> usedColumnNames = getDestinationFieldsOf(segmentationPreference.getExpressions());
            tableMetaData.removeUnusedColumns(usedColumnNames);

            familyContext.getFamilyPreference().setTableMetaData(tableMetaData);
        }

        return familyContext;
    }


    private List<String> getDestinationFieldsOf(List<Expression> expressions) {
        List<String> result = new ArrayList<String>();
        for (Expression expression : expressions) {
            result.add(expression.getDestinationField());
        }
        return result;
    }


    private void appendToBuffer(StringBuffer ids, String familyId) {
        if (ids.length() > 0) {
            ids.append(',');
        }
        ids.append(familyId);
    }
}
