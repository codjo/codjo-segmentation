/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.sql.builder.DefaultQueryConfig;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.JoinKeyExpression;
import net.codjo.sql.builder.QueryConfig;
import net.codjo.xml.fast.ClientContentHandler;
import java.util.Map;
/**
 * Handler permettant de construire un XmlQueryConfigList.
 */
class SelectConfigBuilder implements ClientContentHandler {
    private DefaultQueryConfig queryConfig;
    private JoinKey joinKey;
    private boolean inTag;


    SelectConfigBuilder() {
        queryConfig = new DefaultQueryConfig();
    }


    public void setRootTableName(String rootTableName) {
        queryConfig.setRootTableName(rootTableName);
    }


    public QueryConfig getConfig() {
        return queryConfig;
    }


    public void startElement(String name, Map attributes) {
        if ("select-config".equals(name)) {
            inTag = true;
        } else if(inTag) {
        if ("join-key".equals(name)) {
            joinKey = new JoinKey((String)attributes.get("left"),
                                  convert((String)attributes.get("type")),
                                  (String)attributes.get("right"));
            queryConfig.add(joinKey);
        }
        else if ("part".equals(name)) {
            joinKey.addPart(new JoinKey.Part((String)attributes.get("left"),
                                             (String)attributes.get("operator"),
                                             (String)attributes.get("right")));
        }
    }}


    public void endElement(String name, String value) {
        if ("select-config".equals(name)) {
            inTag = false;
        }
        else if (inTag) {
            if ("where-clause".equals(name)) {
                queryConfig.setRootExpression(new JoinKeyExpression(value));
            }
        }
    }


    private JoinKey.Type convert(String joinType) {
        if ("left".equals(joinType)) {
            return JoinKey.Type.LEFT;
        }
        else if ("right".equals(joinType)) {
            return JoinKey.Type.RIGHT;
        }
        else if ("inner".equals(joinType)) {
            return JoinKey.Type.INNER;
        }
        else {
            throw new IllegalArgumentException("Type inconnu " + joinType);
        }
    }
}
