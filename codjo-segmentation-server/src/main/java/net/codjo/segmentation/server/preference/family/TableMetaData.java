/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/**
 * Metadata d'une table.
 */
public class TableMetaData {
    private final String name;
    private final List<ColumnMetaData> columnMetaDatas;


    protected TableMetaData(String name, List<ColumnMetaData> columnMetaDatas) {
        this.name = name;
        this.columnMetaDatas = columnMetaDatas;
    }


    /**
     * Fournit les méta-données liées à la table 'tableName' sans les colonnes d'anomalie.
     *
     * @return TableMetaData
     */
    public static TableMetaData create(String tableName, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        List<ColumnMetaData> columnMetaData = new ArrayList<ColumnMetaData>();
        try {
            ResultSet resultSet =
                  statement.executeQuery("select * from " + tableName + " where 1 = 2");
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                String colName = resultSetMetaData.getColumnName(i);
                int colType = resultSetMetaData.getColumnType(i);
                if (!"ANOMALY".equals(colName) && !"ANOMALY_LOG".equals(colName)) {
                    columnMetaData.add(new ColumnMetaData(colName, colType));
                }
            }
        }
        finally {
            statement.close();
        }
        return new TableMetaData(tableName, columnMetaData);
    }


    public String getName() {
        return name;
    }


    public String[] getColumnNames() {
        List<String> names = new ArrayList<String>();
        for (ColumnMetaData columnMetaData1 : columnMetaDatas) {
            names.add(columnMetaData1.getColumnName());
        }
        return names.toArray(new String[columnMetaDatas.size()]);
    }


    public int getColumnType(String columnName) {
        for (ColumnMetaData columnMetaData : columnMetaDatas) {
            if (columnName.equals(columnMetaData.getColumnName())) {
                return columnMetaData.getColumnType();
            }
        }
        throw new IllegalArgumentException("La colonne '" + columnName
                                           + "' n'appartient pas à la table '" + getName() + "'");
    }


    /**
     * Supprime les colonnes absentes de la liste des colonnes utilisees (paramètre 'usedColumns').
     *
     * @param usedColumns - Liste des colonnes utilisées.
     */
    public void removeUnusedColumns(List usedColumns) {
        List<ColumnMetaData> columnsToRemove = new ArrayList<ColumnMetaData>();
        for (ColumnMetaData columnMetaData : columnMetaDatas) {
            if (usedColumns == null
                || !usedColumns.contains(columnMetaData.getColumnName())) {
                columnsToRemove.add(columnMetaData);
            }
        }
        columnMetaDatas.removeAll(columnsToRemove);
    }
}
