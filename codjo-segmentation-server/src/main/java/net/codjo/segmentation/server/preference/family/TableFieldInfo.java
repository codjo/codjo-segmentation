/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.util.string.StringUtil;
/**
 * 
 */
public class TableFieldInfo {
    private String columnLabel;
    private String columnJavaName;
    private String columnSqlName;
    private String columnTable;

    TableFieldInfo(String columnLabel, String columnSqlName, String columnTable) {
        this.columnLabel = columnLabel;
        this.columnSqlName = columnSqlName;
        this.columnJavaName = StringUtil.sqlToJavaName(columnSqlName);
        this.columnTable = columnTable;
    }

    public String getColumnLabel() {
        return columnLabel;
    }


    public String getColumnJavaName() {
        return columnJavaName;
    }


    public String getColumnSqlName() {
        return columnSqlName;
    }


    public String getColumnTable() {
        return columnTable;
    }
}
