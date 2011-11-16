package net.codjo.segmentation.server.util;
import net.codjo.variable.UnknownVariableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 */
public class CompilationUtil {
    private static final String REPLACE_VARIABLES_PATTERN = "(.*)INC_\\$\\$(\\d*)\\$(\\w*)(.*)";
    private static final String REPLACE_VARIABLES_SELECT
          = "select FORMULA from PM_CLASSIFICATION_STRUCTURE where CLASSIFICATION_ID=? and SLEEVE_ROW_ID=?";


    private CompilationUtil() {
    }


    public static String replaceVariables(String expressionToCompile, Connection connection)
          throws SQLException, CyclicVariableException, UnknownVariableException {
        Pattern pattern = Pattern.compile(REPLACE_VARIABLES_PATTERN);
        PreparedStatement preparedStatement = connection.prepareStatement(REPLACE_VARIABLES_SELECT);
        try {
            return replaceVariables(expressionToCompile, pattern, preparedStatement, new ArrayList<String>());
        }
        finally {
            preparedStatement.close();
        }
    }


    private static String replaceVariables(String expressionToCompile,
                                           Pattern pattern,
                                           PreparedStatement preparedStatement, List<String> seenVariables)
          throws SQLException, CyclicVariableException, UnknownVariableException {
        Matcher matcher = pattern.matcher(expressionToCompile);
        if (matcher.find()) {
            String prefix = matcher.group(1);
            String classificationId = matcher.group(2);
            String sleeveRowId = matcher.group(3);
            String suffix = matcher.group(4);
            String variable = String.format("INC_$$%s$%s", classificationId, sleeveRowId);
            if (seenVariables.contains(variable)) {
                throw new CyclicVariableException(variable);
            }
            preparedStatement.setInt(1, Integer.parseInt(classificationId));
            preparedStatement.setString(2, sleeveRowId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new UnknownVariableException(variable);
            }

            List<String> newSeenVariables = new ArrayList<String>(seenVariables);
            newSeenVariables.add(variable);
            String result = new StringBuilder()
                  .append(prefix)
                  .append(replaceVariables(resultSet.getString(1),
                                           pattern,
                                           preparedStatement,
                                           newSeenVariables))
                  .append(suffix).toString();

            return replaceVariables(result, pattern, preparedStatement, seenVariables);
        }
        else {
            return expressionToCompile;
        }
    }


    public static class CyclicVariableException extends Exception {
        CyclicVariableException(String message) {
            super(String.format("La variable '%s' est une référence cyclique.", message));
        }
    }
}
