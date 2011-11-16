/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDispatchManager {
    private Connection connection;
    private Map<String, String> columnTypes;


    public abstract String getDestinationTable();


    public final String getSqlTypeName(String columnName) throws SQLException {
        if (columnTypes == null) {
            initColumnTypes();
        }
        return columnTypes.get(columnName);
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }


    public void dispatch(String[][] arrayToDispatch) throws SQLException {
        if (arrayToDispatch.length == 0) {
            return;
        }

        String[] headers = arrayToDispatch[0];
        StringBuffer headerBuffer =
              new StringBuffer().append("insert into ").append(getDestinationTable()).append(" (");
        for (int columnIndex = 0; columnIndex < headers.length - 1; columnIndex++) {
            headerBuffer.append(headers[columnIndex]);
            if (columnIndex != headers.length - 2) {
                headerBuffer.append(", ");
            }
        }
        headerBuffer.append(") values ( ");

        for (int rowIndex = 1; rowIndex < arrayToDispatch.length; rowIndex++) {
            StringBuffer queryBuffer = new StringBuffer().append(headerBuffer);
            if ("true".equals(arrayToDispatch[rowIndex][arrayToDispatch[0].length - 1])) {
                continue;
            }
            for (int columnIndex = 0; columnIndex < arrayToDispatch[0].length - 1; columnIndex++) {

                String type = getSqlTypeName(headers[columnIndex]);
                String value = arrayToDispatch[rowIndex][columnIndex];

                if (value != null && needsQuotes(type)) {
                    queryBuffer.append("'").append(value).append("'");
                }
                else {
                    queryBuffer.append(value);
                }
                if (columnIndex < headers.length - 2) {
                    queryBuffer.append(", ");
                }
            }
            queryBuffer.append(")");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(queryBuffer.toString());
            stmt.close();
        }
    }


    private boolean needsQuotes(String type) {
        return (type.contains("char") || "text".equals(type) || type.contains("date"));
    }


    private void initColumnTypes() throws SQLException {
        columnTypes = new HashMap<String, String>();
        String request = "select col.name as columnName, fieldType.name as columnType "
                         + "from syscolumns col "
                         + "inner join systypes fieldType "
                         + "on col.usertype = fieldType.usertype "
                         + "where id=object_id('"+ getDestinationTable() + "')";

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(request);

        while (resultSet.next()) {
            columnTypes.put(resultSet.getString("columnName"), resultSet.getString("columnType"));
        }

        closeStatement(statement);
    }

     protected final void closeStatement(Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }
}
