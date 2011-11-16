/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.expression.ExpressionException;
import net.codjo.segmentation.server.preference.family.Row;
/**
 * Exception lancé lorsque l'evaluation d'une ligne est en erreur.
 *
 * @see net.codjo.segmentation.server.participant.common.ExpressionsEvaluator#compute(net.codjo.segmentation.server.preference.family.Row)
 */
public class ComputeException extends Exception {
    private ExpressionException expressionException;
    private Row resultRow;


    public ComputeException(Row resultRow) {
        this.resultRow = resultRow;
    }


    public ComputeException(ExpressionException error, Row resultRow) {
        super(error);
        this.expressionException = error;
        this.resultRow = resultRow;
    }


    public Row getResultRow() {
        return resultRow;
    }


    public int getErrorCount() {
        if (expressionException != null) {
            return expressionException.getNbError();
        }
        else {
            return 1;
        }
    }


    public ExpressionException getExpressionException() {
        return expressionException;
    }
}
