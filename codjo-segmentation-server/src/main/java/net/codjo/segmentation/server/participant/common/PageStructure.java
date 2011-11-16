/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import java.util.Map;
/**
 * Recense les informations (structure) sur la page que l'on souhaite évaluer.
 */
public class PageStructure {
    private Map<String, Integer> columnTypesByName;


    public PageStructure(Map<String, Integer> columnTypesByName) {
        this.columnTypesByName = columnTypesByName;
    }


    public Map<String, Integer> getColumnTypesByName() {
        return columnTypesByName;
    }
}
