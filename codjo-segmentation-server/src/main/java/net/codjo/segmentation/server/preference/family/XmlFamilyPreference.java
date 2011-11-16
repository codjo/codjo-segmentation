/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.expression.FunctionHolder;
import net.codjo.expression.FunctionManager;
import net.codjo.sql.builder.QueryConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Représente le paramétrage statique attaché à une famille.
 *
 * @version $Revision: 1.5 $
 */
public class XmlFamilyPreference {
    private final String familyId;
    private final String rootTable;
    private String destinationTable;
    private QueryConfig deleteConfig;
    private QueryConfig selectConfig;
    private QueryConfig resultConfig;
    private TableMetaData tableMetaData;
    private List<String> functionHolderClassList;
    private List<FunctionHolder> functionHolderList;
    private List<String> argumentNameList;
    private RowFilter filter;
    private List<TableFieldInfo> resultColumns;
    private List<VarField> variables;


    public XmlFamilyPreference(String familyId, String rootTable, String destinationTable) {
        this.familyId = familyId;
        this.rootTable = rootTable;
        this.destinationTable = destinationTable;
    }


    public QueryConfig getSelectConfig() {
        return selectConfig;
    }


    public void setSelectConfig(QueryConfig selectConfig) {
        this.selectConfig = selectConfig;
    }


    public QueryConfig getDeleteConfig() {
        return deleteConfig;
    }


    public void setDeleteConfig(QueryConfig deleteConfig) {
        this.deleteConfig = deleteConfig;
    }


    public String getRootTable() {
        return rootTable;
    }


    public String getDestinationTable() {
        return destinationTable;
    }


    public TableMetaData getTableMetaData() {
        return tableMetaData;
    }


    public void setTableMetaData(TableMetaData tableMetaData) {
        this.tableMetaData = tableMetaData;
    }


    public String getResultTableName() {
        return tableMetaData.getName();
    }


    public String[] getResultTableColumnNames() {
        return tableMetaData.getColumnNames();
    }


    public int getResultTableColumnType(String columnName) {
        return tableMetaData.getColumnType(columnName);
    }


    public final String getFamilyId() {
        return familyId;
    }


    List<FunctionHolder> getFunctionHolderList() {
        return functionHolderList;
    }


    final List<String> getFunctionHolderClassList() {
        return functionHolderClassList;
    }


    public final void setFunctionHolderClassList(List<String> functionHolderClassList) {
        this.functionHolderClassList = functionHolderClassList;
    }


    public List<String> getArgumentNameList() {
        return argumentNameList;
    }


    public final void setArgumentNameList(List<String> argumentNameList) {
        this.argumentNameList = argumentNameList;
    }


    public final RowFilter getFilter() {
        if (!hasFilter()) {
            throw new NullPointerException();
        }
        return filter;
    }


    public final void setFilter(RowFilter filter) {
        this.filter = filter;
    }


    public final boolean hasFilter() {
        return filter != null;
    }


    void compileConfiguration() throws BadConfigurationException {
        if (functionHolderClassList == null) {
            functionHolderList = Collections.emptyList();
            return;
        }
        try {
            functionHolderList = new ArrayList<FunctionHolder>(functionHolderClassList.size() + 1);
            functionHolderList.add(new DefaultFunctionHolder());
            for (String functionHolderClassName : getFunctionHolderClassList()) {
                functionHolderList.add((FunctionHolder)Class.forName(functionHolderClassName).newInstance());
            }
        }
        catch (Exception e) {
            throw new BadConfigurationException(familyId, e);
        }
    }


    public FunctionManager createFunctionManager() {
        FunctionManager functionManager = new FunctionManager();
        functionManager.addFunctionHolder(new DefaultFunctionHolder());
        for (FunctionHolder functionHolder : functionHolderList) {
            functionManager.addFunctionHolder(functionHolder);
        }
        return functionManager;
    }


    public void setResultConfig(QueryConfig config) {
        resultConfig = config;
    }


    public QueryConfig getResultConfig() {
        return resultConfig;
    }


    public void setResultColumns(List<TableFieldInfo> resultColumns) {
        this.resultColumns = resultColumns;
    }


    public List<TableFieldInfo> getResultColumns() {
        return resultColumns;
    }


    public List<VarField> getVariables() {
        return variables;
    }


    public void setVariables(List<VarField> variables) {
        this.variables = variables;
    }
}
