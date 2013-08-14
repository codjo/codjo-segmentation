/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
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
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.RowFilter;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.workflow.common.message.Arguments;
/**
 *
 */
public class CalculatorParticipant extends SegmentationParticipant<TodoContent> {

    public CalculatorParticipant(ContextManager contextManager) {
        super(contextManager, TransactionType.TRANSACTIONAL, SegmentationLevels.TO_COMPUTE);
    }


    @Override
    protected void handleTodo(Todo<TodoContent> todo, Level fromLevel, Connection connection) {
        try {
//            logger.info("Début du calcul de : " + todo.getContent());

            SegmentationContext context = contextManager.getSegmentationContext(todo);
            XmlFamilyPreference familyPreference = context.getFamilyPreference();

            ExpressionsEvaluator expressionsEvaluator = context.createExpressionsEvaluator();
            SegmentationResult segmentationResult = context.createSegmentationResult(connection);

            try {
                final int pageId = todo.getContent().getPageId();
                final Page page = context.removePage(pageId);
                final int nbRows = page.getRowCount();
                int nbComputeErrors = 0;
                int filterIndex = -1;

//                logger.info("Calcul de la page " + pageId);
                for (int i = 0; i < nbRows; i++) {
                    Row row = page.getRow(i);
                    try {
                        filterIndex = determineFilterIndex(filterIndex, familyPreference, row);

                        if (!isFiltered(familyPreference, context, row, filterIndex)) {
                            Row resultRow = expressionsEvaluator.compute(row);
                            segmentationResult.add(resultRow);
                        }
                    }
                    catch (ComputeException e) {
                        nbComputeErrors++;
                        if (logger.isDebugEnabled()) {
                            logComputeError(todo, e, row);
                        }
                        segmentationResult.addError(e);
                    }
                }
//                logger.info("Résultat du calcul de la page " + pageId + " : " + nbComputeErrors + "/"
//                            + nbRows
//                            + " lignes ont une erreur de calcul (voir les détails dans la base de données)");
            }
            finally {
                segmentationResult.close();
            }

            send(write(createTodoAudit(fromLevel, familyPreference), SegmentationLevels.INFORMATION)
                       .then()
                       .erase(todo, fromLevel));
//            logger.info("Fin du calcul de " + todo.getContent());
        }
        catch (Exception error) {
            logger.fatal("Calcul en erreur de " + todo.getContent(), error);
            send(informOfFailure(todo, fromLevel).dueTo(error));
        }
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
