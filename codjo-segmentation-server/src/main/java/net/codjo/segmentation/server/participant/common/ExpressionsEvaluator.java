/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.expression.ExpressionException;
import net.codjo.expression.ExpressionManager;
import net.codjo.segmentation.server.preference.family.Row;
/**
 * Evaluateur d'expression utilisant la librairie <code>codjo-expression</code>.
 */
public class ExpressionsEvaluator {
    private final ExpressionManager expressionManager;
    private final String[] resultColumnNames;


    public ExpressionsEvaluator(ExpressionManager expressionManager, String[] resultColumnNames) {
        this.resultColumnNames = resultColumnNames;
        this.expressionManager = expressionManager;
    }


    public Row compute(Row rowToEvaluate) throws ComputeException {
        expressionManager.clear();
        for (int i = 0; i < rowToEvaluate.getColumnNames().length; i++) {
            String colName = rowToEvaluate.getColumnNames()[i];
            Object colValue = rowToEvaluate.getColumnValue(i);
            expressionManager.setFieldSourceValue(colName, colValue);
        }

        try {
            expressionManager.compute();
        }
        catch (ExpressionException error) {
            throw new ComputeException(error, buildResultRow());
        }

        return buildResultRow();
    }


    private Row buildResultRow() {
        Row result = new Row(resultColumnNames);
        for (int i = 0; i < resultColumnNames.length; i++) {
            Object value = expressionManager.getComputedValue(resultColumnNames[i]);
            result.setColumnValue(i, value);
        }
        return result;
    }
}
