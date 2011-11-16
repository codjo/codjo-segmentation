/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.List;
/**
 * Represente la structure d'une table
 */
public abstract class DBStructureComposite implements DBStructure {
    private DBStructure dbStructure;


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     *
     * @return la liste des colonnes de la table tableName
     */
    public List getColumnsFor(String tableName) {
        return dbStructure.getColumnsFor(tableName);
    }


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     * @param sqlField
     *
     * @return Le libellé de sqlField
     */
    public String getColumnLabelFor(String tableName, String sqlField) {
        return dbStructure.getColumnLabelFor(tableName, sqlField);
    }


    /**
     * Affecte un type de structure
     *
     * @param dbStructure
     */
    public void setSubDBStructure(DBStructure dbStructure) {
        this.dbStructure = dbStructure;
    }
}
