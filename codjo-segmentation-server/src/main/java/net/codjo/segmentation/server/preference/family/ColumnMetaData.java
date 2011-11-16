/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
/**
 * Metadata sur une colonne.
 */
public class ColumnMetaData {
    private String columnName;
    private int columnType;


    public ColumnMetaData(String columnName, int columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }


    public String getColumnName() {
        return columnName;
    }


    public int getColumnType() {
        return columnType;
    }


    @Override
    public String toString() {
        return "ColumnMetaData{columnName='" + columnName + "'" + ", columnType="
               + columnType + "}";
    }
}
