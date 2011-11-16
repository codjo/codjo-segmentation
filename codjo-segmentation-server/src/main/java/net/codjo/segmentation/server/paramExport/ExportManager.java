/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramExport;
import net.codjo.util.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 *
 */
public class ExportManager {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String TAB = "\t";
    private String classificationStructureTableName = "PM_CLASSIFICATION_STRUCTURE";
    private List<String> excludeStructColNames = new ArrayList<String>();


    public ExportManager() {
        excludeStructColNames.add("SLEEVE_ID");
        excludeStructColNames.add("SLEEVE_ROW_ID");
    }


    void setClassificationStructureTableName(String classificationStructureTableName) {
        this.classificationStructureTableName = classificationStructureTableName;
    }


    public void doExport(Connection connection, String requestContent)
          throws SQLException, IOException {
        String[] args = requestContent.split(";");
        String exportType = args[0];
        String exportFilePath = args[1];
        String tableName = "PM_" + exportType;

        Map<String, String> fieldMap = getFielMap(connection, tableName);
        if (!fieldMap.isEmpty()) {
            StringBuffer result = createHeader(fieldMap);
            result.append(exportData(connection, tableName, fieldMap));
            writeExportFile(exportFilePath, result);
        }
    }


    Map<String, String> getFielMap(Connection connection, String tableName)
          throws SQLException {
        Map<String, String> fieldMap = new LinkedHashMap<String, String>();

        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet =
                  statement.executeQuery("select                                         "
                                         + " c.name as columnName,                       "
                                         + " t.name as columnType                        "
                                         + " from syscolumns c                           "
                                         + "	    left join systypes t                 "
                                         + "		    on (c.usertype = t.usertype)     "
                                         + " where c.id = object_id('" + tableName + "') "
                                         + " order by c.colid");
            while (resultSet.next()) {
                fieldMap.put(resultSet.getString("columnName"), resultSet.getString("columnType"));
            }
            if (tableName.equals(classificationStructureTableName)) {
                for (String colName : excludeStructColNames) {
                    fieldMap.remove(colName);
                }
            }
            if (fieldMap.isEmpty()) {
                throw new SQLException("La table " + tableName + " est introuvable");
            }
            return fieldMap;
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    StringBuffer createHeader(Map<String, String> fieldMap) throws SQLException {
        StringBuffer header = new StringBuffer();
        for (String columnName : fieldMap.keySet()) {
            header.append(columnName).append(TAB);
        }
        if (header.length() != 0) {
            header.replace(header.length() - 1, header.length(), NEW_LINE);
        }
        return header;
    }


    StringBuffer exportData(Connection connection, String tableName, Map<String, String> fieldMap)
          throws SQLException {

        StringBuffer data = new StringBuffer();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement
                  .executeQuery("select " + buildSelectDataFromClause(fieldMap) + " from " + tableName);
            while (resultSet.next()) {
                for (String columnName : fieldMap.keySet()) {
                    Object object = resultSet.getObject(columnName);
                    if (object != null) {
                        String str = convertValue(fieldMap.get(columnName), object);
                        str = str.replaceAll(TAB, " ");
                        str = str.replaceAll(NEW_LINE, " ");
                        data.append(str).append(TAB);
                    }
                    else {
                        data.append(TAB);
                    }
                }
                data.replace(data.length() - 1, data.length(), NEW_LINE);
            }
            return data;
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    private String buildSelectDataFromClause(Map<String, String> fieldMap) {
        StringBuilder selectClause = new StringBuilder();

        for (String columnName : fieldMap.keySet()) {
            selectClause.append(columnName).append(",");
        }
        selectClause.replace(selectClause.length() - 1, selectClause.length(), "");
        return selectClause.toString();
    }


    private String convertValue(String sqlType, Object value) {
        if ("bit".equals(sqlType)) {
            return "true".equals(value.toString()) ? "1" : "0";
        }
        else {
            return value.toString();
        }
    }


    private void writeExportFile(String filePath, StringBuffer exportContent)
          throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        FileUtil.saveContent(file, exportContent.toString());
    }
}
