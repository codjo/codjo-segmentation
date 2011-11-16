package net.codjo.segmentation.server.preference.family;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ResultConfigBuilder extends DefaultConfigBuilder {
    private List<TableFieldInfo> columns;


    ResultConfigBuilder(String rootTableName) {
        super("result-config", rootTableName);
        columns = new ArrayList<TableFieldInfo>();
    }


    @Override
    public void startElement(String name, Map attributes) {
        super.startElement(name, attributes);
        if (inTag && "column".equals(name)) {
            columns.add(new TableFieldInfo((String)attributes.get("label"),
                                           (String)attributes.get("value"),
                                           (String)attributes.get("table")));
        }
    }


    List<TableFieldInfo> getTableColumns() {
        return columns;
    }
}
