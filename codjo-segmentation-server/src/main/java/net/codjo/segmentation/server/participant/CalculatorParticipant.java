/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import net.codjo.expression.ExpressionException;
import net.codjo.segmentation.common.MidAuditKey;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.common.ComputeException;
import net.codjo.segmentation.server.participant.common.ExpressionsEvaluator;
import net.codjo.segmentation.server.participant.common.Page;
import net.codjo.segmentation.server.participant.common.SegmentationResult;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.participant.context.SegmentationContext;
import net.codjo.segmentation.server.participant.context.SegmentationReport;
import net.codjo.segmentation.server.participant.context.TaskTemplate;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.RowFilter;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.workflow.common.message.Arguments;
/**
 *
 */
public class CalculatorParticipant extends BackOfficeSegmentationParticipant {

    public CalculatorParticipant(ContextManager contextManager) {
        super(contextManager, TransactionType.TRANSACTIONAL, SegmentationLevels.TO_COMPUTE);
    }


    @Override
    protected void handleTodo(final Todo<TodoContent> todo, final Level fromLevel, final Connection connection) {
        final SegmentationReport report = getReport(todo);
        new TaskTemplate(report, getName()) {
            @Override
            protected void doRun() throws Exception {
                final SegmentationContext context = contextManager.getSegmentationContext(todo);
                final XmlFamilyPreference familyPreference = context.getFamilyPreference();

                final ExpressionsEvaluator expressionsEvaluator = context.createExpressionsEvaluator();
                final SegmentationResult segmentationResult = context.createSegmentationResult(connection);

                try {
                    final int pageId = todo.getContent().getPageId();
                    final Page page = context.removePage(pageId);
                    final int nbRows = page.getRowCount();
                    final int[] filterIndex = {-1};

                    for (int i = 0; i < nbRows; i++) {
                        final Row row = page.getRow(i);
                        new TaskTemplate(report, "row") {
                            @Override
                            protected void doRun() throws Exception {
                                filterIndex[0] = determineFilterIndex(filterIndex[0], familyPreference, row);

                                if (!isFiltered(familyPreference, context, row, filterIndex[0])) {
                                    Row resultRow = expressionsEvaluator.compute(row);
                                    segmentationResult.add(resultRow);
                                }
                            }


                            @Override
                            protected void handleComputeException(ComputeException exception) throws SQLException {
                                if (logger.isDebugEnabled()) {
                                    logComputeError(todo, exception, row);
                                }
                                segmentationResult.addError(exception);
                            }
                        }.runComputation();
                    }
                }
                finally {
                    segmentationResult.close();
                }

                send(write(createTodoAudit(fromLevel, familyPreference),
                           SegmentationLevels.INFORMATION)
                           .then()
                           .erase(todo, fromLevel));
            }


            @Override
            protected void handleException(Exception e) {
                logger.fatal("Calcul en erreur de " + todo.getContent(), e);
                send(informOfFailure(todo, fromLevel).dueTo(e));
            }
        }.run();
    }


    private void logComputeError(Todo<TodoContent> todo, ComputeException computeError, Row row) {
        StringBuilder result = new StringBuilder();
        result.append("Erreur de calcul ").append(todo.getContent()).append('\n')
              .append("\t * Ligne en erreur : ").append(row).append('\n')
              .append("\t * Ligne resultat : ").append(computeError.getResultRow()).append('\n');

        if (computeError.getExpressionException() != null) {
            ExpressionException root = computeError.getExpressionException();
            for (int i = 0; i < root.getNbError(); i++) {
                Exception exception = root.getException(i);
                StringWriter stackTrace = new StringWriter();
                exception.printStackTrace(new PrintWriter(stackTrace));
                result.append("\t *").append(root.getMessage(i)).append('\n')
                      .append(stackTrace).append('\n');
            }
        }

        logger.debug(result.toString());
    }


    private int determineFilterIndex(int filterIndex, XmlFamilyPreference familyPreference, Row row) {
        if (!familyPreference.hasFilter()) {
            return -1;
        }
        if (filterIndex != -1) {
            return filterIndex;
        }
        RowFilter filter = familyPreference.getFilter();
        return row.findColumnIndex(filter.getTableName(), filter.getColumnName());
    }


    private boolean isFiltered(XmlFamilyPreference familyPreference,
                               SegmentationContext context,
                               Row row,
                               int filterIndex) {
        if (filterIndex == -1) {
            return false;
        }
        RowFilter filter = familyPreference.getFilter();
        Object value = row.getColumnValue(filterIndex);
        //noinspection unchecked
        return filter.isRowExcluded(context.getSegmentationId(), row, value);
    }


    private Todo<Arguments> createTodoAudit(Level level, XmlFamilyPreference familyPreference) {
        Arguments arguments = createAudit(new String[][]{
              {MidAuditKey.LEVEL_KEY, level.getName()},
              {MidAuditKey.FAMILY_KEY, familyPreference.getFamilyId()}
        });
        return new Todo<Arguments>(arguments);
    }
}
