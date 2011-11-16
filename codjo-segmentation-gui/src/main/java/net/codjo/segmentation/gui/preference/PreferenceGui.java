/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import net.codjo.expression.FunctionManager;
import net.codjo.expression.help.FunctionHelp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Représente la structure des tables liées à une famille
 */
public class PreferenceGui implements Constants {
    private String familyId;
    private List<String> tablesList = new ArrayList<String>();
    private DBStructure structure;
    private List<FunctionHelp> allFunctionsHelp = new FunctionManager().getAllFunctionsHelp();
    private Map<String, String> resultColumns;
    private String resultRequetor;


    public String getResultRequetor() {
        return resultRequetor;
    }


    public void setResultRequetor(String resultRequetor) {
        this.resultRequetor = resultRequetor;
    }


    public PreferenceGui(String familyId) {
        this.familyId = familyId;
    }


    public String getFamilyId() {
        return familyId;
    }


    /**
     * Retourne la liste des tables
     *
     * @return la liste des tables rattachées à cette famille
     */
    public List<String> getTables() {
        return tablesList;
    }


    public void addTable(String tableName) {
        tablesList.add(tableName);
    }


    /**
     * Retourne la liste des colonnes de tableName
     *
     * @param tableName
     *
     * @return la liste des colonnes de tableName
     */
    public List getColumnsFor(String tableName) {
        return structure.getColumnsFor(tableName);
    }


    /**
     * Affecte un type de Structure
     *
     * @param dBStructure
     */
    public void setStructure(DBStructure dBStructure) {
        this.structure = dBStructure;
    }


    /**
     * Retourne le libellé de la colonne sqlField de la table tableName
     *
     * @param tableName
     * @param sqlField
     *
     * @return le libellé de la colonne sqlField de la table tableName
     */
    public String getColumnLabelFor(String tableName, String sqlField) {
        return structure.getColumnLabelFor(tableName, sqlField);
    }


    // TODO à passer à package protected des que la classe EditorAction est créée
    public List<FunctionHelp> getAllFunctionsHelp() {
        List<FunctionHelp> resultList = new ArrayList<FunctionHelp>();
        for (Object anAllFunctionsHelp : allFunctionsHelp) {
            FunctionHelp functionHelp = (FunctionHelp)anAllFunctionsHelp;
            resultList.add(new MyFunctionHelp(functionHelp));
        }
        return resultList;
    }


    public void setAllFunctionsHelp(List<FunctionHelp> allFunctionsHelp) {
        this.allFunctionsHelp = allFunctionsHelp;
    }


    public void setResultColumns(Map<String, String> columns) {
        resultColumns = columns;
    }


    public Map<String, String> getResultColumns() {
        return resultColumns;
    }


    private static class MyFunctionHelp extends FunctionHelp {

        MyFunctionHelp(FunctionHelp functionHelp) {
            super(functionHelp.getFunctionName(), functionHelp.getParameterNumber(), functionHelp.getHelp());
        }


        @Override
        public String toString() {
            return getFunctionName();
        }
    }
}
