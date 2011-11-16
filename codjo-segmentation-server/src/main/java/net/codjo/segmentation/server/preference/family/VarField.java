package net.codjo.segmentation.server.preference.family;
/**
 *
 */
public class VarField {
    String name;
    String sqlType;
    String label;


    public VarField(String name, String sqlType, String label) {

        this.name = name;
        this.sqlType = sqlType;
        this.label = label;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getSqlType() {
        return sqlType;
    }


    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }
}
