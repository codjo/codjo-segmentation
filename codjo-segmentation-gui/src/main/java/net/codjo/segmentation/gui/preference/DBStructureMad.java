/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import net.codjo.mad.common.structure.FieldStructure;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Structure associée à un StructureReader chargé de lire une structure sous forme xml
 */
public class DBStructureMad implements DBStructure {
    private StructureReader structureReader;


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     *
     * @return la liste des colonnes de tableName
     */
    public List getColumnsFor(String tableName) {
        TableStructure table = structureReader.getTableBySqlName(tableName);
        Map fieldsMap = table.getFieldsBySqlKey();

        return new ArrayList<Object>(fieldsMap.keySet());
    }


    /**
     * DOCUMENT ME!
     *
     * @param tableName
     * @param sqlField
     *
     * @return le libellé de la colonne sqlField
     */
    public String getColumnLabelFor(String tableName, String sqlField) {
        TableStructure table = structureReader.getTableBySqlName(tableName);
        FieldStructure field = table.getFieldBySql(sqlField);
        return field.getLabel();
    }


    /**
     * Associe un StructureReader qui lit une structure sous forme xml
     *
     * @param structureReader
     */
    public void setMad(StructureReader structureReader) {
        this.structureReader = structureReader;
    }
}
