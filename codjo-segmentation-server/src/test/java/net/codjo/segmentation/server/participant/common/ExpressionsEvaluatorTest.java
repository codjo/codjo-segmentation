/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.expression.ExpressionException;
import net.codjo.expression.ExpressionManager;
import net.codjo.expression.FunctionHolder;
import net.codjo.expression.FunctionManager;
import net.codjo.expression.InvalidExpressionException;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.treatment.Expression;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
/**
 * Classe de test de {@link ExpressionsEvaluator}.
 */
public class ExpressionsEvaluatorTest extends TestCase {
    private static final IllegalArgumentException BAD_FUNCTION_ERROR =
          new IllegalArgumentException("badFunction error");
    private FunctionManager functionManager = new FunctionManager();


    public void test_compute() throws Exception {
        Expression[] expressions =
              new Expression[]{
                    new Expression(Types.INTEGER, "variable", "SRC_COL_B + 2", true),
                    new Expression(Types.INTEGER, "result", "SRC_COL_A + variable - 2"),
                    new Expression(Types.INTEGER, "unused", "variable + 2")
              };
        ColumnType[] srcColumns =
              new ColumnType[]{
                    new ColumnType("COL_A", Types.INTEGER),
                    new ColumnType("COL_B", Types.INTEGER)
              };
        String[] resultColumnNames = new String[]{"result"};

        ExpressionsEvaluator evaluator =
              new ExpressionsEvaluator(createExpressionManager(functionManager,
                                                               expressions, srcColumns),
                                       resultColumnNames);

        Row row = new Row(new String[]{"COL_A", "COL_B"}, new Object[]{10, 20});

        Row result = evaluator.compute(row);

        assertSame(resultColumnNames, result.getColumnNames());
        assertEquals(30, result.getColumnValue(0));
    }


    public void test_compute_error() throws Exception {
        Expression[] expressions =
              new Expression[]{
                    new Expression(Types.INTEGER, "result", "SRC_COL_A"),
                    new Expression(Types.INTEGER, "pete", "utils.badFunction(SRC_COL_A)")
              };
        ColumnType[] srcColumns =
              new ColumnType[]{new ColumnType("COL_A", Types.INTEGER)};
        String[] resultColumnNames = new String[]{"result", "pete"};

        ExpressionsEvaluator evaluator =
              new ExpressionsEvaluator(createExpressionManager(functionManager,
                                                               expressions, srcColumns),
                                       resultColumnNames);

        Row row = new Row(new String[]{"COL_A"}, new Object[]{5});

        try {
            evaluator.compute(row);
            fail();
        }
        catch (ComputeException ex) {
            assertEquals("ExpressionException(1 erreur(s), pete a provoque l'erreur badFunction error, ...)",
                         ex.getMessage());

            ExpressionException cause = (ExpressionException)ex.getCause();
            assertEquals(1, cause.getNbError());
            assertSame(BAD_FUNCTION_ERROR, cause.getException(0));

            Row result = ex.getResultRow();
            assertSame(resultColumnNames, result.getColumnNames());
            assertEquals(5, result.getColumnValue(0));
            assertEquals(0, result.getColumnValue(1));
        }
    }


    private ExpressionManager createExpressionManager(FunctionManager manager,
                                                      Expression[] expressions, ColumnType[] srcColumns)
          throws InvalidExpressionException {
        ExpressionManager expressionManager = new ExpressionManager(manager);
        expressionManager.setSourceColumn(toMap(srcColumns));
        initExpressions(expressionManager, expressions);
        expressionManager.compileExpressions();
        return expressionManager;
    }


    private static void initExpressions(ExpressionManager expressionManager,
                                        Expression[] expressions) {
        Map<String, Integer> varCol = new HashMap<String, Integer>();
        Map<String, Integer> destCol = new HashMap<String, Integer>();
        expressionManager.setVarColumn(varCol);
        expressionManager.setDestColumn(destCol);

        for (Expression expression : expressions) {
            String destField = expression.getDestinationField();
            if (expression.isVariable()) {
                varCol.put(destField, expression.getType());
            }
            else {
                destCol.put(destField, expression.getType());
            }

            expressionManager.add(destField, expression.getExpression());
        }
    }


    @Override
    protected void setUp() throws Exception {
        functionManager.addFunctionHolder(new UtilsForTest());
    }


    private static Map<String, Integer> toMap(ColumnType[] srcColumns) {
        Map<String, Integer> cols = new HashMap<String, Integer>();
        for (ColumnType srcColumn : srcColumns) {
            cols.put(srcColumn.getName(), srcColumn.getType());
        }
        return cols;
    }


    private class ColumnType {
        private String name;
        private int type;


        ColumnType(String name, int type) {
            this.name = name;
            this.type = type;
        }


        public String getName() {
            return name;
        }


        public int getType() {
            return type;
        }
    }

    public static class UtilsForTest implements FunctionHolder {
        public List<String> getAllFunctions() {
            return null;
        }


        public String getName() {
            return "utils";
        }


        public int badFunction(int ignored) {
            throw BAD_FUNCTION_ERROR;
        }
    }
}
