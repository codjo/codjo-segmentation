package net.codjo.segmentation.server.preference.family;
import net.codjo.sql.builder.DefaultQueryConfig;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.JoinKeyExpression;
import net.codjo.sql.builder.QueryConfig;
import net.codjo.xml.fast.ClientContentHandler;
import java.util.Map;

class DefaultConfigBuilder implements ClientContentHandler {
    protected static final String WHERE_CLAUSE = "where-clause";
    protected static final String JOIN_KEY = "join-key";
    protected static final String PART = "part";
    protected static final String JOIN_LEFT = "left";
    protected static final String JOIN_RIGHT = "right";
    protected static final String JOIN_TYPE = "type";
    protected static final String JOIN_OPERATOR = "operator";

    private String configName;
    protected boolean inTag;
    protected DefaultQueryConfig queryConfig;
    private JoinKey joinKey;


    DefaultConfigBuilder(String configName, String rootTableName) {
        this.configName = configName;
        this.queryConfig = new DefaultQueryConfig();
        this.queryConfig.setRootTableName(rootTableName);
    }


    QueryConfig getConfig() {
        return queryConfig;
    }


    public void startElement(String name, Map attributes) {
        if (configName.equals(name)) {
            inTag = true;
        }
        else if (inTag) {
            String left = (String)attributes.get(JOIN_LEFT);
            String right = (String)attributes.get(JOIN_RIGHT);
            if (JOIN_KEY.equals(name)) {
                joinKey = new JoinKey(left, toType((String)attributes.get(JOIN_TYPE)), right);
                queryConfig.add(joinKey);
            }
            else if (PART.equals(name)) {
                joinKey.addPart(new JoinKey.Part(left, (String)attributes.get(JOIN_OPERATOR), right));
            }
        }
    }


    public void endElement(String name, String value) {
        if (configName.equals(name)) {
            inTag = false;
        }
        else if (inTag) {
            if (WHERE_CLAUSE.equals(name)) {
                queryConfig.setRootExpression(new JoinKeyExpression(value));
            }
        }
    }


    protected JoinKey.Type toType(String type) {
        if (JoinKey.Type.LEFT.toString().equalsIgnoreCase(type)) {
            return JoinKey.Type.LEFT;
        }
        else if (JoinKey.Type.RIGHT.toString().equalsIgnoreCase(type)) {
            return JoinKey.Type.RIGHT;
        }
        else if (JoinKey.Type.INNER.toString().equalsIgnoreCase(type)) {
            return JoinKey.Type.INNER;
        }
        else {
            throw new IllegalArgumentException("Type inconnu " + type);
        }
    }
}
