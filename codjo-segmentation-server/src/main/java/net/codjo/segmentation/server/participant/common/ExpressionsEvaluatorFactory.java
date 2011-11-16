/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.expression.ExpressionManager;
import net.codjo.expression.InvalidExpressionException;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.treatment.SegmentationPreference;
import net.codjo.segmentation.server.preference.treatment.Expression;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Factory Permettant de créer un {@link ExpressionsEvaluator} pour la couche treatment.
 */
public final class ExpressionsEvaluatorFactory {
    private ExpressionsEvaluatorFactory() {
    }


    public static ExpressionsEvaluator create(XmlFamilyPreference familyPreference,
                                              SegmentationPreference segmentationPreference,
                                              PageStructure pageStructure)
          throws InvalidExpressionException {
        ExpressionManager expressionManager = createExpressionManager(pageStructure,
                                                                      familyPreference,
                                                                      segmentationPreference);

        return new ExpressionsEvaluator(expressionManager,
                                        familyPreference.getResultTableColumnNames());
    }


    private static ExpressionManager createExpressionManager(PageStructure pageStructure,
                                                             XmlFamilyPreference familyPreference,
                                                             SegmentationPreference segmentationPreference)
          throws InvalidExpressionException {

        ExpressionManager expressionManager = new ExpressionManager(familyPreference.createFunctionManager());
        expressionManager.setExpressionManagerName(segmentationPreference.getSegmentationName());
        expressionManager.setSourceColumn(pageStructure.getColumnTypesByName());

        Map<String, Integer> variableTypesByName = new HashMap<String, Integer>();
        expressionManager.setVarColumn(variableTypesByName);

        Map<String, Integer> destinationTypesByName = new HashMap<String, Integer>();
        expressionManager.setDestColumn(destinationTypesByName);

        List<Expression> expressions = segmentationPreference.getExpressions();
        for (Expression expression : expressions) {
            String destinationField = expression.getDestinationField();
            if (expression.isVariable()) {
                variableTypesByName.put(destinationField, expression.getType());
            }
            else {
                int destinationFieldType =
                      familyPreference.getResultTableColumnType(destinationField);
                destinationTypesByName.put(destinationField, destinationFieldType);
            }

            expressionManager.add(destinationField, expression.getExpression());
        }
        expressionManager.compileExpressions();
        return expressionManager;
    }
}
