/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Représente la structure d'une table avec la possibilité de filtrer les colonnes.
 */
public class DBStructureFilter extends DBStructureComposite {
    private Map<String, List<String>> filterMap = new HashMap<String, List<String>>();


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     *
     * @return les colonnes de tableName en appliquant le filtre
     */
    @Override
    public List getColumnsFor(String tableName) {
        // Demande au sous-dbstructure
        List columnsList = super.getColumnsFor(tableName);

        // filter
        Collection exclude = filterMap.get(tableName);
        if (exclude != null) {
            columnsList.removeAll(exclude);
        }

        // retourne la liste filtrée
        return columnsList;
    }


    /**
     * Permet d'exclure la colonne colName de la table tableName
     *
     * @param tableName nom de la table à filtrer
     * @param colName   nom de la colonne à exclure
     */
    public void setFilter(String tableName, String colName) {
        List<String> excludedColumns = new ArrayList<String>();
        excludedColumns.add(colName);
        filterMap.put(tableName, excludedColumns);
    }


    /**
     * Permet d'exclure la liste des colonnes excludedColumns de la table tableName
     *
     * @param tableName       tableName nom de la table à filtrer
     * @param excludedColumns Liste des colonnes à exclure
     */
    public void setFilter(String tableName, List<String> excludedColumns) {
        filterMap.put(tableName, excludedColumns);
    }


    public void addFilter(String tableName, String colName) {
        List<String> excludedColumns = filterMap.get(tableName);
        if (excludedColumns == null) {
            excludedColumns = new ArrayList<String>();
            filterMap.put(tableName, excludedColumns);
        }
        excludedColumns.add(colName);
    }
}
