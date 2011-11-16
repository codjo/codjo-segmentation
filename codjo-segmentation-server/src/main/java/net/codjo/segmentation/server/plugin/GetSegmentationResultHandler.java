package net.codjo.segmentation.server.plugin;
import net.codjo.database.api.Database;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.mad.server.handler.sql.Getter;
import net.codjo.mad.server.handler.sql.SqlHandler;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.preference.family.TableFieldInfo;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.sql.builder.DefaultFieldInfoList;
import net.codjo.sql.builder.FieldInfo;
import net.codjo.sql.builder.QueryBuilder;
import net.codjo.sql.builder.QueryBuilderFactory;
import net.codjo.sql.builder.TableName;
import net.codjo.variable.TemplateInterpreter;
import net.codjo.variable.UnknownVariableException;
import java.util.List;
import java.util.Map;
/**
 *
 */
public class GetSegmentationResultHandler extends SqlHandler {
    private final ContextManager contextManager;


    public GetSegmentationResultHandler(ContextManager contextManager, Database database) {
        super(new String[0], "", database);
        this.contextManager = contextManager;
    }


    @Override
    protected String buildQuery(final Map<String, String> arguments) throws HandlerException {
        XmlFamilyPreference familyPreference = contextManager.getFamilyPreference(arguments.get("familyId"));

        DefaultFieldInfoList fieldInfoList = new DefaultFieldInfoList();

        List<TableFieldInfo> columns = familyPreference.getResultColumns();
        clearGetters();
        int index = 1;

        for (TableFieldInfo tableFieldInfo : columns) {
            String columnJavaName = tableFieldInfo.getColumnJavaName();
            TableName tableName = new TableName(tableFieldInfo.getColumnTable());
            String columnSqlName = tableFieldInfo.getColumnSqlName();
            fieldInfoList.add(new FieldInfo(tableName, columnSqlName, index));
            addGetter(columnJavaName, new Getter(index));
            index++;
        }

        QueryBuilder builder = QueryBuilderFactory.newSelectQueryBuilder(familyPreference.getResultConfig());

        String selectQuery = builder.buildQuery(fieldInfoList);

        try {
            return new TemplateInterpreter().evaluate(selectQuery, arguments);
        }
        catch (UnknownVariableException e) {
            throw new HandlerException(e);
        }
    }
}
