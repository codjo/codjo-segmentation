/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
/**
 * Interface permettant de filtrer les lignes à calculer
 */
public interface RowFilter<T> {
    /**
     * Retourne <code>true</code> si la ligne doit être filtrée
     *
     * @param segmentationId identifiant de l'axe
     * @param row            La ligne a tester
     * @param filterValue    La valeur de la colonne du filtre
     *
     * @return <code>true</code> la ligne est filtrée.
     */
    public boolean isRowExcluded(int segmentationId, Row row, T filterValue);


    /**
     * Retourne le nom de la table de configuration du filtre
     *
     * @return Le nom de la table de configuration du filtre
     */
    public String getTableName();


    /**
     * Retourne le nom de la colonne de configuration du filtre
     *
     * @return Le nom de la colonne de configuration du filtre
     */
    public String getColumnName();
}
