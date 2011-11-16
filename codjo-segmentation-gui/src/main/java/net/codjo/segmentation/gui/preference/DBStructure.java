/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.List;
/**
 * Interface de la structure d'une table
 */
public interface DBStructure {
    /**
     * DOCUMENT ME!
     *
     * @param tableName
     *
     * @return la liste des colonnes
     */
    public List getColumnsFor(String tableName);


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     * @param sqlField
     *
     * @return le libellé du champs sqlField
     */
    public String getColumnLabelFor(String tableName, String sqlField);
}
