/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.treatment;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * Classe permettant de charger le paramétrage base d'un traitement donné.
 */
public class SegmentationPreference {
    private String family;
    private String segmentationName;
    private int segmentationId;
    private List<Expression> expressions;
    private static final String VAR_PREFIX = "VAR_";
    private static final Pattern PATTERN_EXPRESSION = Pattern.compile("[0-9_]+");


    public static SegmentationPreference createPreference(Connection connection,
                                                          int segmentationId,
                                                          Map<String, String> parameters)
          throws SQLException, NullDustbinException {
        return new SegmentationPreference(connection, segmentationId, parameters);
    }


    protected SegmentationPreference(Connection connection,
                                     int segmentationId,
                                     Map<String, String> parameters)
          throws SQLException, NullDustbinException {
        this.segmentationId = segmentationId;
        loadSegmentation(connection);
        loadExpressions(connection, parameters);
    }


    public String getFamily() {
        return family;
    }


    public String getSegmentationName() {
        return segmentationName;
    }


    public int getSegmentationId() {
        return segmentationId;
    }


    public List<Expression> getExpressions() {
        return expressions;
    }


    protected void loadSegmentation(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        String sql =
              "select SEGMENTATION_NAME, FAMILY from PM_SEGMENTATION where SEGMENTATION_ID = "
              + segmentationId;
        try {
            ResultSet resultSet = statement.executeQuery(sql);

            if (!resultSet.next()) {
                throw new IllegalArgumentException("Le traitement " + segmentationId + " est inconnu");
            }

            family = resultSet.getString("FAMILY");
            segmentationName = resultSet.getString("SEGMENTATION_NAME");
        }
        finally {
            statement.close();
        }
    }


    protected void loadExpressions(Connection connection, Map<String, String> parameters)
          throws SQLException, NullDustbinException {
        Statement statement = connection.createStatement();
        expressions = new ArrayList<Expression>();
        try {
            String sql =
                  "select DESTINATION_FIELD,EXPRESSION,PRIORITY,IS_VARIABLE,VARIABLE_TYPE "
                  + "from PM_EXPRESSION where SEGMENTATION_ID = " + segmentationId;

            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String expression = replaceParameters(resultSet.getString("EXPRESSION"), parameters);

                Expression currentExpression =
                      new Expression(resultSet.getInt("VARIABLE_TYPE"),
                                     resultSet.getString("DESTINATION_FIELD"), expression,
                                     resultSet.getBoolean("IS_VARIABLE"));
                currentExpression.setPriority(resultSet.getInt("PRIORITY"));
                expressions.add(currentExpression);
            }
        }
        finally {
            statement.close();
        }
        if (expressions.isEmpty()) {
            throw new IllegalArgumentException("Aucune expression pour le traitement " + segmentationId);
        }
        computeSleeveCodeExpression();
    }


    private void computeSleeveCodeExpression() throws NullDustbinException {
        String dustbinCode = null;
        Expression sleeveCodeExpression = null;
        List<String> varList = new ArrayList<String>();
        for (Expression expression : expressions) {
            String field = expression.getDestinationField();

            if ("SLEEVE_CODE".equals(field)) {
                dustbinCode = expression.getExpression();
                sleeveCodeExpression = expression;
            }
            else if (field.startsWith(VAR_PREFIX) &&
                     PATTERN_EXPRESSION.matcher(field.substring(VAR_PREFIX.length())).matches()) {
                varList.add(field);
            }
        }
        if (dustbinCode == null) {
            throw new NullDustbinException(segmentationId, segmentationName);
        }
        sleeveCodeExpression.setExpression(buildSleeveCode(dustbinCode, varList));
    }


    private String buildSleeveCode(String dustbinCode, List<String> varList) {
        StringBuilder builderVar = new StringBuilder();
        StringBuilder builderExpression = new StringBuilder();
        Iterator<String> iterator = varList.iterator();
        while (iterator.hasNext()) {
            String var = iterator.next();
            builderVar.append(var);
            builderExpression.append("\"").append(replace(var)).append("\"");
            if (iterator.hasNext()) {
                builderVar.append(",");
                builderExpression.append(",");
            }
        }
        builderExpression.append("}");
        builderVar.append("}");

        return "utils.caseOf(new boolean[] {" + builderVar + ", new String[] {" + builderExpression + ", \""
               + dustbinCode + "\")";
    }


    protected static String replace(String value) {
        value = value.substring(VAR_PREFIX.length());
        value = value.replaceFirst("_", "-");
        return value.replaceAll("_", ".");
    }


    private String replaceParameters(String stringToEvaluate, Map<String, String> parameters) {
        for (String parameter : parameters.keySet()) {
            stringToEvaluate = stringToEvaluate.replaceAll("\\$" + parameter + "\\$",
                                                           parameters.get(parameter));
        }
        return stringToEvaluate;
    }
}
