package net.codjo.segmentation.server.plugin;
import net.codjo.expression.ExpressionManager;
import net.codjo.expression.FindUses;
import net.codjo.expression.InvalidExpressionException;
import net.codjo.mad.common.structure.SqlUtil;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.preference.family.RowFilter;
import net.codjo.segmentation.server.preference.family.TableMetaData;
import net.codjo.segmentation.server.preference.family.VarField;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.util.CompilationUtil;
import net.codjo.segmentation.server.util.CompilationUtil.CyclicVariableException;
import net.codjo.segmentation.server.util.SegmentationUtil;
import net.codjo.sql.builder.DefaultFieldInfoList;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.JoinKeyExpression;
import net.codjo.sql.builder.OrderByField;
import net.codjo.sql.builder.QueryBuilder;
import net.codjo.sql.builder.QueryBuilderFactory;
import net.codjo.sql.builder.QueryConfig;
import net.codjo.variable.UnknownVariableException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 */
public class ExpressionCompilerCommand extends HandlerCommand {
    private static final String VAR = "VAR_";
    private ContextManager contextManager;


    public ExpressionCompilerCommand(ContextManager contextManager) {
        this.contextManager = contextManager;
    }


    @Override
    public CommandResult executeQuery(CommandQuery query) throws SQLException, HandlerException {
        query.checkRequiredArguments("expression", "familyId");

        try {
            executeImpl(query);
            return createEmptyResult();
        }
        catch (Exception e) {
            throw new HandlerException(e);
        }
    }


    private void executeImpl(CommandQuery query)
          throws CompilationException, SQLException, CyclicVariableException, UnknownVariableException,
                 InvalidExpressionException {
        String expressionToCompile = query.getArgumentString("expression");
        String familyId = query.getArgumentString("familyId");

        expressionToCompile = CompilationUtil.replaceVariables(expressionToCompile,
                                                               getContext().getConnection());

        XmlFamilyPreference familyPreference = contextManager.getFamilyPreference(familyId);

        if (familyPreference.getTableMetaData() == null) {
            TableMetaData tableMetaData = TableMetaData.create(familyPreference.getDestinationTable(),
                                                               getContext().getConnection());

            familyPreference.setTableMetaData(tableMetaData);
        }

        ExpressionManager expressionManager = new ExpressionManager(familyPreference.createFunctionManager());
        expressionManager.setExpressionManagerName(expressionToCompile);

        Map<String, Integer> varColumn = new HashMap<String, Integer>();
        expressionManager.setVarColumn(varColumn);
        findVariablesColumnType(expressionToCompile, familyPreference, varColumn);

        Map<String, Integer> sourceColumn = new HashMap<String, Integer>();
        expressionManager.setSourceColumn(sourceColumn);

        Map<String, Integer> destColumn = new HashMap<String, Integer>();
        expressionManager.setDestColumn(destColumn);

        FindUses findUses = new FindUses();
        findUses.add(expressionToCompile);
        Collection usedSourceColumns = findUses.buildReport().getUsedSourceColumns();
        if (!usedSourceColumns.isEmpty()) {
            //noinspection unchecked
            findSourceColumnType(familyPreference, sourceColumn, new ArrayList<String>(usedSourceColumns));
        }

        destColumn.put("DEST_FIELD", Types.BIT);
        expressionManager.add("DEST_FIELD", expressionToCompile);
        expressionManager.compileExpressions();
    }


    private void findVariablesColumnType(String expressionToCompile,
                                         XmlFamilyPreference familyPreference,
                                         Map<String, Integer> varColumn) throws InvalidExpressionException {
        FindUses findVarUses = new FindUses(VAR);
        findVarUses.add(expressionToCompile);
        //noinspection unchecked
        Collection<String> usedVarColumns = findVarUses.buildReport().getUsedSourceColumns();
        for (String usedVarColumn : usedVarColumns) {
            for (VarField variable : familyPreference.getVariables()) {
                if (variable.getName().equals(VAR + usedVarColumn)) {
                    int sqlType = new SqlUtil().stringToSqlType(variable.getSqlType());
                    varColumn.put(VAR + usedVarColumn, sqlType);
                }
            }
        }
    }


    private void findSourceColumnType(XmlFamilyPreference familyPreference,
                                      Map<String, Integer> sourceColumn,
                                      List<String> usedSourceColumns)
          throws UnknownVariableException, SQLException {
        QueryBuilder builder = QueryBuilderFactory.newSelectQueryBuilder(
              new QueryConfigWithoutWhereClause(familyPreference.getSelectConfig()));
        List<String> fields = new ArrayList<String>(usedSourceColumns);
        if (familyPreference.hasFilter()) {
            RowFilter filter = familyPreference.getFilter();
            fields.add(SegmentationUtil.determineFullColumnName(filter.getTableName(),
                                                                filter.getColumnName()));
        }
        String selectQuery = SegmentationUtil.buildSelectQuery(builder,
                                                               fields,
                                                               Collections.<String, String>emptyMap(),
                                                               new DefaultFieldInfoList());

        Statement statement = getContext().getConnection().createStatement();
        try {
            ResultSet resultSet = statement.executeQuery(selectQuery);
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 0; i < usedSourceColumns.size(); i++) {
                sourceColumn.put(usedSourceColumns.get(i), metaData.getColumnType(i + 1));
            }
        }
        finally {
            statement.close();
        }
    }


    private static class QueryConfigWithoutWhereClause implements QueryConfig {
        private final QueryConfig selectConfig;


        private QueryConfigWithoutWhereClause(QueryConfig selectConfig) {
            this.selectConfig = selectConfig;
        }


        public Map<String, JoinKey> getJoinKeyMap() {
            return selectConfig.getJoinKeyMap();
        }


        public String getRootTableName() {
            return selectConfig.getRootTableName();
        }


        public JoinKeyExpression getRootExpression() {
            return new JoinKeyExpression();
        }


        public OrderByField[] getOrderByFields() {
            return new OrderByField[0];
        }
    }
}
