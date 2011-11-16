package net.codjo.segmentation.server.plugin;
/**
 *
 */
/*
* codjo.net
*
* Common Apache License 2.0
*/
import net.codjo.expression.FunctionHolder;
import net.codjo.test.common.XmlUtil;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * Classe de test de {@link PreferenceGuiHome}.
 */
public class PreferenceGuiHomeTest extends TestCase {
    private static final String FAMILY_END = "</family></family-list>";
    private static final String FAMILY_START =
          "<family-list><family id=\"PORTEFEUILLE\" root=\"AP_PORTFOLIO\" destination=\"AP_SEG_PORTFOLIO\">";
    private static final String EXPECTED_FAMILY_START =
          "<family-list><family id=\"PORTEFEUILLE\">";


    public void test_simple() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String response = home.buildResponse(newSource("<family-list></family-list>"));
        XmlUtil.assertEquals("<family-list></family-list>", response);
    }


    public void test_noResponse() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String response = home.buildResponse(new InputSource(getClass().getResourceAsStream(
              "/net/codjo/segmentation/server/preference/family/XmlPreferenceLoaderTest.xml")));
        assertXPathExists(response, "//family-list/family[@id='PortfolioSegmentation']");
        assertXPathExists(response, "//family-list/family[@id='SecuritySegmentation']");
    }


    public void test_simple_onefamily() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input = FAMILY_START + FAMILY_END;
        String expected = EXPECTED_FAMILY_START + FAMILY_END;

        String response = home.buildResponse(newSource(input));
        XmlUtil.assertEquals(expected, response);
    }


    public void test_simple_withDefaultTable() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input =
              FAMILY_START + "<select-config>\n"
              + "            <where-clause>AP_PORTFOLIO.PERIOD='$period$'</where-clause>\n"
              + "        </select-config>" + FAMILY_END;
        String expected =
              EXPECTED_FAMILY_START + "<tables>" + "<table name=\"AP_PORTFOLIO\"/>"
              + "</tables>" + FAMILY_END;

        String response = home.buildResponse(newSource(input));

        XmlUtil.assertEquivalent(expected, response);
    }


    public void test_simple_withTable() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input =
              FAMILY_START + "<select-config>\n"
              + "            <join-key left=\"AP_ROOT\" type=\"inner\" right=\"AP_TOTO\">"
              + "                <part left=\"COL_R1\" operator=\"=\" right=\"COL_A1\"/>      "
              + "            </join-key>                                                      "
              + "            <join-key left=\"AP_ROOT\" type=\"inner\" right=\"AP_B\">"
              + "                <part left=\"COL_R1\" operator=\"=\" right=\"COL_A1\"/>      "
              + "            </join-key>                                                      "
              + "        </select-config>" + FAMILY_END;
        String expected =
              EXPECTED_FAMILY_START + "<tables>" + "<table name=\"AP_B\"/>"
              + "<table name=\"AP_PORTFOLIO\"/>" + "<table name=\"AP_TOTO\"/>"
              + "<table name=\"AP_ROOT\"/>" + "</tables>" + FAMILY_END;

        String response = home.buildResponse(newSource(input));
        XmlUtil.assertEquivalent(expected, response);
    }


    public void test_simple_withFunctions() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input =
              FAMILY_START + "<functions><holder>" + Fake.class.getName()
              + "</holder></functions>" + FAMILY_END;

        String response = home.buildResponse(newSource(input));

        String inFuncNode = "//family-list/family/help/function[@name='in']";
        assertXPathExists(response, inFuncNode);
        assertXPathAttributeValue(response, inFuncNode + "/@help",
                                  "Description : renvoie vrai si la colonne passée en premier paramètre contient une des valeurs passées dans les paramètres suivants\n"
                                  + "Usage : in(colonne, valeur1, valeur2, ...)\n"
                                  + "Exemple : in(Pays, \"FRA\", \"DEU\", \"USA\", \"JPN\")");

        String doStuffNode = "//family-list/family/help/function[@name='fake.doStuff']";
        assertXPathAttributeValue(response, doStuffNode + "/@arg", "0");
        assertXPathAttributeValue(response, doStuffNode + "/@help", "Usage : fake.doStuff()");
    }


    public void test_simple_withVariables() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input =
              FAMILY_START + "<gui>" + "<variables>"
              + "<variable name=\"PTF_CONSOLIDE\" label=\"Portefeuille\" comment=\"La var PTf de la table non nulle\"/>"
              + "<variable name=\"PERIODE_CONSOLIDE\" label=\"Periode\" />"
              + "</variables>" + "</gui>" + FAMILY_END;
        String expected =
              EXPECTED_FAMILY_START + "<variables>"
              + "<variable name=\"PTF_CONSOLIDE\" label=\"Portefeuille\" />"
              + "<variable name=\"PERIODE_CONSOLIDE\" label=\"Periode\" />"
              + "</variables>" + FAMILY_END;

        String response = home.buildResponse(newSource(input));

        XmlUtil.assertEquivalent(expected, response);
    }


    public void test_simple_withFilters() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input =
              FAMILY_START + "<gui>" + "<filters>"
              + "<filter table=\"AP_BOBO\" column=\"COL0\"/>"
              + "<filter table=\"AP_TOTO\" column=\"COL3\"/>"
              + "<filter table=\"AP_TOTO\" column=\"COL4\"/>" + "</filters>" + "</gui>"
              + FAMILY_END;
        String expected =
              EXPECTED_FAMILY_START + "<filters>"
              + "<filter table=\"AP_BOBO\" column=\"COL0\" />"
              + "<filter table=\"AP_TOTO\" column=\"COL3\" />"
              + "<filter table=\"AP_TOTO\" column=\"COL4\" />" + "</filters>" + FAMILY_END;

        String response = home.buildResponse(newSource(input));

        XmlUtil.assertEquivalent(expected, response);
    }


    public void test_withResults() throws Exception {
        PreferenceGuiHome home = new PreferenceGuiHome();
        String input =
              FAMILY_START + "<result-config>\n"
              + "            <join-key left=\"AP_ROOT\" type=\"inner\" right=\"AP_TOTO\">     "
              + "                <part left=\"COL_R1\" operator=\"=\" right=\"COL_A1\"/>      "
              + "            </join-key>                                                      "
              + "            <join-key left=\"AP_ROOT\" type=\"inner\" right=\"AP_B\">        "
              + "                <part left=\"COL_R1\" operator=\"=\" right=\"COL_A1\"/>      "
              + "            </join-key>                                                      "
              + "            <column label=\"Libellé\" value=\"AP_ROOT.COL_LABEL\"/>          "
              + "            <column label=\"Nom\" value=\"AP_ROOT.COL_NAME\"/>          "
              + "        </result-config>" + FAMILY_END;
        String expected =
              EXPECTED_FAMILY_START
              + "<result>"
              + "<table name=\"AP_B\"/>"
              + "<table name=\"AP_SEG_PORTFOLIO\"/>"
              + "<table name=\"AP_TOTO\"/>"
              + "<table name=\"AP_ROOT\"/>"
              + "<column label=\"Libellé\" value=\"AP_ROOT.COL_LABEL\"/>          "
              + "<column label=\"Nom\" value=\"AP_ROOT.COL_NAME\"/>          "
              + "</result>"
              + FAMILY_END;

        String response = home.buildResponse(newSource(input));

        XmlUtil.assertEquivalent(expected, response);
    }


    private void assertXPathAttributeValue(String response, String xPath, String expected) throws Exception {
        Node node = XPathAPI.selectSingleNode(toDocument(response), xPath);
        assertEquals(expected, node.getNodeValue());
    }


    private void assertXPathExists(String response, String xPath) throws Exception {
        Node node = XPathAPI.selectSingleNode(toDocument(response), xPath);
        assertNotNull(node);
    }


    private Document toDocument(String string)
          throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(newSource(string));
    }


    private InputSource newSource(String xml) {
        return new InputSource(new StringReader(xml));
    }


    public static class Fake implements FunctionHolder {
        public String getName() {
            return "fake";
        }


        public List<String> getAllFunctions() {
            return null;
        }


        public void doStuff() {
        }
    }
}