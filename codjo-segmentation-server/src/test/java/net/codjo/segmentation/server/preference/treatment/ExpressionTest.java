/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.treatment;
import junit.framework.TestCase;
/**
 * Classe de test de {@link Expression}.
 */
public class ExpressionTest extends TestCase {
    private Expression expression;


    public void test_setGet() throws Exception {
        expression.setDestinationField("dest");
        assertEquals("dest", expression.getDestinationField());

        expression.setExpression("a+b");
        assertEquals("a+b", expression.getExpression());

        expression.setPriority(1);
        assertEquals(1, expression.getPriority());

        expression.setVariable(true);
        assertEquals(true, expression.isVariable());

        expression.setType(1);
        assertEquals(1, expression.getType());
    }


    public void test_constructor() throws Exception {
        expression = new Expression(1, "dest", "a+b");
        assertEquals(1, expression.getType());
        assertEquals("dest", expression.getDestinationField());
        assertEquals("a+b", expression.getExpression());
        assertEquals(false, expression.isVariable());
    }


    public void test_constructor_variable() throws Exception {
        expression = new Expression(1, "dest", "a+b", true);
        assertEquals(1, expression.getType());
        assertEquals("dest", expression.getDestinationField());
        assertEquals("a+b", expression.getExpression());
        assertEquals(true, expression.isVariable());
    }


    @Override
    protected void setUp() throws Exception {
        expression = new Expression();
    }
}
