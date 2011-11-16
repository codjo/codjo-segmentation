/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Represente une structure de table dont les noms (table et champs) sont des variables
 */
public class DBStructureVariable extends DBStructureComposite implements Constants {
    private Map<String, String> variables = new HashMap<String, String>();


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     *
     * @return la liste des colonnes de la table tableName
     */
    @Override
    public List getColumnsFor(String tableName) {
        if (VAR_TABLE.equals(tableName)) {
            return new ArrayList<String>(variables.keySet());
        }
        return super.getColumnsFor(tableName);
    }


    /**
     * DOCUMENT ME!
     *
     * @param tableName nom de la table
     * @param sqlField  nom de la colonne
     *
     * @return le libellé de la colonne sqlField de la table tableName
     */
    @Override
    public String getColumnLabelFor(String tableName, String sqlField) {
        if (VAR_TABLE.equals(tableName)) {
            if (variables.containsKey(sqlField)) {
                return variables.get(sqlField);
            }
            return sqlField;
        }
        else {
            return super.getColumnLabelFor(tableName, sqlField);
        }
    }


    /**
     * Affecte une variable à la structure
     *
     * @param variableName  nom de la variable
     * @param variableLabel libellé de la variable
     */
    public void addVariable(String variableName, String variableLabel) {
        this.variables.put(variableName, variableLabel);
    }
}
