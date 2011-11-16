/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import junit.framework.TestCase;
import org.xml.sax.InputSource;
/**
 * Classe de test de <code>XmlPreferenceLoader</code>.
 */
public class XmlPreferenceLoaderTest extends TestCase {
    private XmlPreferenceLoader loader;


    public void test_load() throws Exception {
        // Chargement
        loader.load(new InputSource(
              XmlPreferenceLoaderTest.class.getResourceAsStream("XmlPreferenceLoaderTest.xml")));

        // Assert
        assertEquals("Toutes les familles sont chargés", 3, loader.familyCount());
        assertNotNull("La famille 'PortfolioSegmentation' est présente",
                      loader.getFamilyPreference("PortfolioSegmentation"));
        assertNotNull("La famille 'SecuritySegmentation' est présente",
                      loader.getFamilyPreference("SecuritySegmentation"));

        // Family PortfolioSegmentation
        XmlFamilyPreference preference = loader.getFamilyPreference("SecuritySegmentation");
        assertEquals("SecuritySegmentation", preference.getFamilyId());
        assertEquals("AP_SECURITY", preference.getRootTable());
        assertEquals("AP_DEST_SECURITY", preference.getDestinationTable());

        // Family PortfolioSegmentation
        preference = loader.getFamilyPreference("PortfolioSegmentation");
        assertEquals("PortfolioSegmentation", preference.getFamilyId());
        assertEquals("AP_PORTFOLIO", preference.getRootTable());
        assertEquals("AP_DEST_PORTFOLIO", preference.getDestinationTable());
        assertEquals("[photo, segmentationId]", preference.getArgumentNameList().toString());

        assertEquals("[net.codjo.segmentation.server.preference.family.FunctionHolderMock, "
                     + "net.codjo.segmentation.server.preference.family.DefaultFunctionHolder]",
                     preference.getFunctionHolderClassList().toString());
        assertEquals(3, preference.getFunctionHolderList().size());

        assertEquals("PHOTO = '$photo$' and AXE_ID = $segmentationId$",
                     preference.getDeleteConfig().getRootExpression().getWhereClause());
        assertNotNull("La config-select est chargé", preference.getSelectConfig());
        assertEquals("AP_PORTFOLIO", preference.getSelectConfig().getRootTableName());

        assertEquals(2, preference.getVariables().size());
        assertEquals("varchar", preference.getVariables().get(0).getSqlType());
        assertEquals("PTF_CONSOLIDE", preference.getVariables().get(0).getName());
        assertEquals("Portefeuille", preference.getVariables().get(0).getLabel());

        assertEquals("int", preference.getVariables().get(1).getSqlType());
        assertEquals("PHOTO_CONSOLIDE", preference.getVariables().get(1).getName());
        assertEquals("Photo", preference.getVariables().get(1).getLabel());
    }


    public void test_select_filter() throws Exception {
        // Chargement
        loader.load(new InputSource(XmlPreferenceLoaderTest.class.getResourceAsStream(
              "XmlPreferenceLoaderTest.xml")));
        assertNotNull("La famille 'OutstandSegmentation' est présente",
                      loader.getFamilyPreference("OutstandSegmentation"));

        // Family OutstandSegmentation
        XmlFamilyPreference preference =
              loader.getFamilyPreference("OutstandSegmentation");
        assertFalse(preference.hasFilter());
//        assertNotNull("Un filtre n'a pas été positionné", preference.getFilter());
//        assertEquals(NoRowFilter.class.getName(),
//            preference.getFilter().getClass().getName());
//        RowFilter filter = preference.getFilter();
//        assertFalse(filter.isRowExcluded(null, null));
    }


    public void test_resultConfig() throws Exception {
         loader.load(new InputSource(XmlPreferenceLoaderTest.class.getResourceAsStream(
              "XmlPreferenceLoaderTest.xml")));
         XmlFamilyPreference preference =
              loader.getFamilyPreference("OutstandSegmentation");
        TableFieldInfo fieldInfo = preference.getResultColumns().get(0);

        assertEquals(1, preference.getResultColumns().size());
        assertEquals("myKey", fieldInfo.getColumnJavaName());
        assertEquals("SEG_RESULT_EVENT", fieldInfo.getColumnTable());
        assertEquals("MY_KEY", fieldInfo.getColumnSqlName());
        assertEquals("Ma clé", fieldInfo.getColumnLabel());

    }


    @Override
    protected void setUp() throws Exception {
        loader = new XmlPreferenceLoader();
    }
}
