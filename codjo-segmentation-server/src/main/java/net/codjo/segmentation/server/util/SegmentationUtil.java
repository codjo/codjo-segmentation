/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.util;
import net.codjo.sql.builder.DefaultFieldInfoList;
import net.codjo.sql.builder.FieldInfo;
import net.codjo.sql.builder.QueryBuilder;
import net.codjo.sql.builder.TableName;
import net.codjo.variable.TemplateInterpreter;
import net.codjo.variable.UnknownVariableException;
import net.codjo.util.string.StringUtil;
import java.util.Collection;
import java.util.List;
import java.util.Map;
/**
 *
 */
public class SegmentationUtil {
    private SegmentationUtil() {
    }


    public static void checkParameters(List<String> expectedParameter, Map<String, String> parameters) {
        for (String argumentName : expectedParameter) {
            if (!parameters.containsKey(argumentName)) {
                throw new IllegalArgumentException("'" + argumentName + "' missing.");
            }
        }
    }


    public static String determineFullColumnName(String tableName, String columnName) {
        return tableName + "$" + columnName;
    }


    public static String buildSelectQuery(QueryBuilder queryBuilder,
                                          Collection<String> fieldsFullColumnName,
                                          Map<String, String> parameters,
                                          DefaultFieldInfoList infoList) throws UnknownVariableException {
        int index = 1;
        for (String fullColumnName : fieldsFullColumnName) {
            infoList.add(buildFieldInfo(index++, fullColumnName));
        }

        String selectQuery = queryBuilder.buildQuery(infoList);
        return new TemplateInterpreter().evaluate(selectQuery, parameters);
    }


    private static FieldInfo buildFieldInfo(int index, String fullColumnName) {
        TableName tableName = new TableName(getTableName(fullColumnName));
        String columnName = getColumnName(fullColumnName);
        return new FieldInfo(tableName, columnName, "alias_" + index);
    }


    private static String getTableName(String fullColumnName) {
        int separatorPosition = fullColumnName.indexOf("$");
        if (separatorPosition != -1) {
            return fullColumnName.substring(0, separatorPosition);
        }
        return "#NO_TABLE#";
    }


    private static String getColumnName(String fullColumnName) {
        int separatorPosition = fullColumnName.indexOf("$");
        if (separatorPosition != -1) {
            return fullColumnName.substring(separatorPosition + 1);
        }
        return StringUtil.javaToSqlName(fullColumnName);
    }


    public static int getColumnIndexByName(String[] line, String columnName) {
        for (int index = 0; index < line.length; index++) {
            String expectedColumn = line[index];
            if (expectedColumn.equals(columnName)) {
                return index;
            }
        }
        return -1;
    }
}
