/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.sql.builder.JoinKey;
import net.codjo.sql.builder.QueryConfig;
import net.codjo.xml.fast.XmlParser;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.xml.sax.InputSource;
/**
 * Classe de test de <code>SelectConfigBuilderTest</code>.
 */
public class SelectConfigBuilderTest extends TestCase {
    public void test_constructor() throws Exception {
        QueryConfig config =
              loadXmlConfiguration("AP_ROOT", "SelectConfigBuilderTest.xml");

        Map jkMap = config.getJoinKeyMap();

        assertEquals("AP_ROOT", config.getRootTableName());
        assertEquals("AP_PORTFOLIO.PHOTO='$photo$'",
                     config.getRootExpression().getWhereClause());

        assertEquals("Nb de clef de jointure", 3, jkMap.size());

        JoinKey joinKey =
              assertJoinKey(jkMap, "AP_A", "AP_ROOT", JoinKey.Type.INNER, "AP_A");
        assertEquals(1, joinKey.getPartList().size());
        assertPart(joinKey.getPartList(), 0, "COL_R1", "=", "COL_A1");

        joinKey = assertJoinKey(jkMap, "AP_B", "AP_A", JoinKey.Type.LEFT, "AP_B");
        assertEquals(2, joinKey.getPartList().size());
        assertPart(joinKey.getPartList(), 0, "COL_A1", "=", "COL_B1");
        assertPart(joinKey.getPartList(), 1, "COL_A2", "<", "COL_B2");

        joinKey = assertJoinKey(jkMap, "AP_C", "AP_C", JoinKey.Type.RIGHT, "AP_ROOT");
        assertEquals(1, joinKey.getPartList().size());
        assertPart(joinKey.getPartList(), 0, "COL_C1", ">=", "COL_R1");
    }


    private QueryConfig loadXmlConfiguration(String rootTableName, String xmlFileName)
          throws Exception {
        XmlParser xmlParser = new XmlParser();
        SelectConfigBuilder selectConfigBuilder = new SelectConfigBuilder();
        selectConfigBuilder.setRootTableName(rootTableName);
        xmlParser.parse(new InputSource(SelectConfigBuilder.class.getResourceAsStream(
              xmlFileName)), selectConfigBuilder);
        QueryConfig config = selectConfigBuilder.getConfig();
        assertNotNull(config);
        return config;
    }


    public void test_select_bug() throws Exception {
        QueryConfig selectConfig =
              loadXmlConfiguration("AP_ROOT", "SelectConfigBuilderTestSelectBug.xml");

        assertNull(selectConfig.getRootExpression());
    }


    private void assertPart(List partList, int partIndex, String leftColumn,
                            String operator, String rightColumn) {
        JoinKey.Part part = ((JoinKey.Part)partList.get(partIndex));
        assertEquals(leftColumn, part.getLeftColumn());
        assertEquals(operator, part.getOperator());
        assertEquals(rightColumn, part.getRightColumn());
    }


    private JoinKey assertJoinKey(Map joinKeyMap, String mapTable, String leftTableName,
                                  JoinKey.Type joinType, String rightTableName) {
        JoinKey joinKey = (JoinKey)joinKeyMap.get(mapTable);

        assertNotNull("Jointure pour " + mapTable + " est présente", joinKey);
        assertEquals(leftTableName, joinKey.getLeftTableName());
        assertEquals(joinType, joinKey.getJoinType());
        assertEquals(rightTableName, joinKey.getRightTableName());
        return joinKey;
    }
}
