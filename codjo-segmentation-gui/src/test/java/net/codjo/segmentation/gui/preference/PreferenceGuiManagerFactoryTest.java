/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import net.codjo.expression.help.FunctionHelp;
import net.codjo.mad.client.request.RequestIdManager;
import net.codjo.mad.client.request.util.ServerWrapper;
import net.codjo.mad.client.request.util.ServerWrapperFactory;
import net.codjo.mad.common.structure.DefaultStructureReader;
import net.codjo.mad.common.structure.StructureReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.gui.preference.PreferenceGuiManagerFactory}.
 */
public class PreferenceGuiManagerFactoryTest extends TestCase {
    private StructureReader madReader;


    @Override
    protected void setUp() throws Exception {
        String xml =
              "<structure>\n" + "    <table name=\"A\" sql=\"AP_A\">\n"
              + "        <field label=\"label A\" name=\"a\" sql=\"A\"/>\n"
              + "        <field label=\"label B\" name=\"b\" sql=\"B\"/>\n"
              + "        <field label=\"label C\" name=\"c\" sql=\"C\"/>\n"
              + "    </table>\n" + "    <table name=\"B\" sql=\"AP_B\">\n"
              + "        <field label=\"label A\" name=\"a\" sql=\"A\"/>\n"
              + "        <field label=\"label B\" name=\"b\" sql=\"B\"/>\n"
              + "    </table>\n" + "</structure>";
        madReader = new DefaultStructureReader(new StringReader(xml));
    }


    public void test_simple_noFamily() throws Exception {
        final String response = "<family-list></family-list>";

        ServerWrapperFactory.setPrototype(newWrapper(response));

        PreferenceGuiManagerFactory factory = new PreferenceGuiManagerFactory(null);
        PreferenceGuiManager manager = factory.getPreferenceGuiManager();
        assertNull(manager.getPreference("PortfolioSegmentation"));
    }


    public void test_simple_oneFamily() throws Exception {
        final String response =
              "<family-list><family id=\"PORTEFEUILLE\"></family></family-list>";

        ServerWrapperFactory.setPrototype(newWrapper(response));

        PreferenceGuiManagerFactory factory = new PreferenceGuiManagerFactory(null);
        PreferenceGuiManager manager = factory.getPreferenceGuiManager();
        assertNotNull(manager.getPreference("PORTEFEUILLE"));
        assertEquals(0, manager.getPreference("PORTEFEUILLE").getTables().size());
    }


    public void test_tables() throws Exception {
        final String response =
              "<family-list><family id=\"PORTEFEUILLE\">      "
              + "        <tables>                             "
              + "            <table name=\"AP_ROOT\"/>        "
              + "            <table name=\"AP_TOTO\"/>        "
              + "        </tables>                            "
              + "</family></family-list>                      ";

        ServerWrapperFactory.setPrototype(newWrapper(response));

        PreferenceGuiManagerFactory factory = new PreferenceGuiManagerFactory(null);
        PreferenceGuiManager manager = factory.getPreferenceGuiManager();
        PreferenceGui ptfPref = manager.getPreference("PORTEFEUILLE");
        assertNotNull(ptfPref);
        List tables = ptfPref.getTables();
        assertEquals(2, tables.size());
        Collections.sort(tables);
        assertEquals("AP_ROOT", tables.get(0));
        assertEquals("AP_TOTO", tables.get(1));
    }


    public void test_functionHelp() throws Exception {
        final String response =
              "<family-list><family id=\"PORTEFEUILLE\">                                  "
              + "        <help>                                                           "
              + "            <function name=\"foo\" arg=\"0\" help=\"une aide\" />        "
              + "            <function name=\"bar\" arg=\"5\" help=\"une aide bar\" />    "
              + "        </help>                                                          "
              + "</family></family-list>                                                  ";

        ServerWrapperFactory.setPrototype(newWrapper(response));

        PreferenceGuiManagerFactory factory = new PreferenceGuiManagerFactory(null);
        PreferenceGuiManager manager = factory.getPreferenceGuiManager();
        PreferenceGui ptfPref = manager.getPreference("PORTEFEUILLE");
        assertNotNull(ptfPref);
        List help = ptfPref.getAllFunctionsHelp();
        assertEquals(2, help.size());
        assertHelp((FunctionHelp)help.get(0), "foo", 0, "une aide");
        assertHelp((FunctionHelp)help.get(1), "bar", 5, "une aide bar");
    }


    public void test_variables() throws Exception {
        final String response =
              "<family-list><family id=\"PORTEFEUILLE\">                                  "
              + "        <variables>                                                      "
              + "            <variable name=\"PERIODE_CONSOLIDE\" label=\"Periode\" />    "
              + "            <variable name=\"BOBO\" label=\"boris\" />                   "
              + "        </variables>                                                     "
              + "</family></family-list>                                                  ";
        ServerWrapperFactory.setPrototype(newWrapper(response));

        PreferenceGuiManagerFactory factory = new PreferenceGuiManagerFactory(null);
        PreferenceGuiManager manager = factory.getPreferenceGuiManager();
        PreferenceGui ptfPref = manager.getPreference("PORTEFEUILLE");

        // Colonnes
        List tables = ptfPref.getColumnsFor(PreferenceGui.VAR_TABLE);
        assertEquals(2, tables.size());
        Collections.sort(tables);
        assertEquals("BOBO", tables.get(0));
        assertEquals("PERIODE_CONSOLIDE", tables.get(1));

        // Label
        assertEquals("Periode",
                     ptfPref.getColumnLabelFor(DBStructureVariable.VAR_TABLE, "PERIODE_CONSOLIDE"));
        assertEquals("boris",
                     ptfPref.getColumnLabelFor(DBStructureVariable.VAR_TABLE, "BOBO"));

        // tables
        assertEquals(1, ptfPref.getTables().size());
        assertEquals(DBStructureVariable.VAR_TABLE, ptfPref.getTables().get(0));
    }


    public void test_filter() throws Exception {
        final String response =
              "<family-list><family id=\"PORTEFEUILLE\">                              "
              + "        <filters>                                                    "
              + "            <filter table=\"AP_A\" column=\"A\" />                   "
              + "            <filter table=\"AP_A\" column=\"B\" />                   "
              + "        </filters>                                                   "
              + "</family></family-list>                                              ";
        ServerWrapperFactory.setPrototype(newWrapper(response));

        PreferenceGuiManagerFactory factory = new PreferenceGuiManagerFactory(madReader);
        PreferenceGuiManager manager = factory.getPreferenceGuiManager();
        PreferenceGui ptfPref = manager.getPreference("PORTEFEUILLE");

        // Colonnes
        List tables = ptfPref.getColumnsFor("AP_A");
        assertEquals(1, tables.size());
        assertEquals("C", tables.get(0));

        // Label
        assertEquals("label C", ptfPref.getColumnLabelFor("AP_A", "C"));
    }


    private ServerWrapper newWrapper(final String response) {
        return new ServerWrapper() {
            public void init(String serviceName)
                  throws Exception {
            }


            public ServerWrapper copy() {
                return this;
            }


            public void close() {
            }


            public String sendWaitResponse(String text, long timeout) {
                assertTrue("Envoie de la requete 'getSegmentationConfig'",
                           text.indexOf("getSegmentationConfig") != -1);
                return getRequestResult(response);
            }
        };
    }


    private String getRequestResult(String response) {
        return "<?xml version=\"1.0\"?>" + "<results><result request_id=\""
               + (Integer.valueOf(RequestIdManager.getInstance().getNewRequestId()).intValue()
                  - 1) + "\"> <primarykey/> <row> <field name=\"sss\">"
               + replace(replace(response, '<', "&lt;"), '>', "&gt;")
               + "</field> </row></result></results>";
    }


    private String replace(String response, char ch, String with) {
        int idx = response.indexOf(ch);
        if (idx != -1) {
            return response.substring(0, idx) + with
                   + replace(response.substring(idx + 1), ch, with);
        }
        return response;
    }


    private void assertHelp(FunctionHelp functionHelp, String name, int argNb, String help) {
        assertEquals(name, functionHelp.getFunctionName());
        assertEquals(argNb, functionHelp.getParameterNumber());
        assertEquals(help, functionHelp.getHelp());
    }
}
