/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.treatment;
/**
 * Expression permettant de calculer une valeur.
 */
public class Expression {
    private String destinationField;
    private String expression;
    private int priority;
    private boolean variable;
    private int type;


    public Expression() {
    }


    public Expression(int type, String destinationField, String expression) {
        this.type = type;
        this.destinationField = destinationField;
        this.expression = expression;
    }


    public Expression(int type, String destinationField, String expression,
                      boolean variable) {
        this.type = type;
        this.destinationField = destinationField;
        this.expression = expression;
        this.variable = variable;
    }


    public String getDestinationField() {
        return destinationField;
    }


    public void setDestinationField(String destinationField) {
        this.destinationField = destinationField;
    }


    public void setExpression(String expression) {
        this.expression = expression;
    }


    public String getExpression() {
        return expression;
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }


    public int getPriority() {
        return priority;
    }


    public void setVariable(boolean variable) {
        this.variable = variable;
    }


    public boolean isVariable() {
        return variable;
    }


    public void setType(int type) {
        this.type = type;
    }


    public int getType() {
        return type;
    }
}
