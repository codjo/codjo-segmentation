/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.segmentation.server.util.SegmentationUtil;
/**
 * Une Row de données.
 */
public class Row {
    private Object[] values;
    private String[] columnNames;


    public Row(String[] columnNames) {
        this(columnNames, new Object[columnNames.length]);
    }


    public Row(String[] columnNames, Object[] values) {
        if (columnNames.length != values.length) {
            throw new IllegalArgumentException("Nombre de colonnes et de valeurs différentes");
        }
        this.columnNames = columnNames;
        this.values = values;
    }


    public Object getColumnValue(String tableName, String columnName) {
        return values[findColumnIndex(tableName, columnName)];
    }


    public Object getColumnValue(int index) {
        return values[index];
    }


    public void setColumnValue(int index, Object value) {
        values[index] = value;
    }


    public int findColumnIndex(String tableName, String columnName) {
        String realName = SegmentationUtil.determineFullColumnName(tableName, columnName);
        for (int i = 0; i < columnNames.length; i++) {
            String name = columnNames[i];
            if (name.equals(realName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Colonne " + tableName + "." + columnName + " est introuvable");
    }


    public String[] getColumnNames() {
        return columnNames;
    }


    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("row{");
        for (int i = 0; i < columnNames.length; i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            buffer.append(columnNames[i]).append("=").append(values[i]);
        }
        return buffer.append("}").toString();
    }
}
