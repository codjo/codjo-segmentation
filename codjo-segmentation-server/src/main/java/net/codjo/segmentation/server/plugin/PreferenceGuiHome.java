package net.codjo.segmentation.server.plugin;
/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
import net.codjo.expression.FunctionHolder;
import net.codjo.expression.FunctionManager;
import net.codjo.expression.help.FunctionHelp;
import net.codjo.xml.XmlException;
import net.codjo.xml.fast.ClientContentHandler;
import net.codjo.xml.fast.XmlParser;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 */
class PreferenceGuiHome {
    private Logger log = Logger.getLogger(PreferenceGuiHome.class);
    private String configFile;


    PreferenceGuiHome() {
        configFile = "/META-INF/segmentation-configs.xml";
    }


    public String buildResponse(InputSource confFile)
          throws ParserConfigurationException, TransformerException, IOException,
                 XmlException {
        DocumentBuilder builder = initFactory();
        Document root =
              builder.getDOMImplementation().createDocument(null, "family-list", null);

        XmlParser xmlParser = new XmlParser();
        RootBuilder reader = new RootBuilder(root);
        xmlParser.parse(confFile, reader);
        return domToString(root);
    }


    private String domToString(Document rootDocument)
          throws TransformerException {
        StringWriter writer = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer =
              tFactory.newTransformer(new StreamSource(
                    new StringReader(
                          "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                          + "    <xsl:output method=\"xml\" encoding=\"ISO-8859-1\"/>"
                          + "    <xsl:template match=\"/\" > <xsl:copy-of select=\".\"/> </xsl:template>"
                          + "</xsl:stylesheet>")));
        transformer.transform(new DOMSource(rootDocument), new StreamResult(writer));
        return writer.toString();
    }


    private DocumentBuilder initFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new MyErrorHandler());
        return builder;
    }


    public String buildResponse()
          throws IOException, TransformerException, XmlException,
                 ParserConfigurationException {
        InputSource source =
              new InputSource(getClass().getResourceAsStream(configFile));
        return buildResponse(source);
    }


    /**
     * Permet de construire/repmlire le noeud racine 'family-list'.
     */
    private static class RootBuilder implements ClientContentHandler {
        private static final String FAMILY = "family";
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String LABEL = "label";
        private static final String TABLE = "table";
        private static final String COLUMN = "column";
        private static final String REQUETOR = "requetor";
        private Logger log = Logger.getLogger(PreferenceGuiHome.class);
        private Document root;
        private Element familyNode;
        private Set<String> selectTableList;
        private Set<String> resultTableList;
        private Map<String, String> resultColumns;
        private FunctionManager functionManager;
        private Map<String, String> varList;
        private Map<String, List<String>> filterList;
        private boolean inSelectConfig, inResultConfig;


        RootBuilder(Document root) {
            this.root = root;
        }


        public void startElement(String name, Map attributes) {
            if (FAMILY.equals(name)) {
                familyNode = root.createElement(FAMILY);
                familyNode.setAttribute(ID, (String)attributes.get(ID));
                selectTableList = new TreeSet<String>();
                selectTableList.add((String)attributes.get("root"));
                resultTableList = new TreeSet<String>();
                resultTableList.add((String)attributes.get("destination"));
                functionManager = new FunctionManager();
                varList = new HashMap<String, String>();
                filterList = new HashMap<String, List<String>>();
                resultColumns = new LinkedHashMap<String, String>();
            }
            else if ("join-key".equals(name)) {
                if (inSelectConfig) {
                    selectTableList.add((String)attributes.get("left"));
                    selectTableList.add((String)attributes.get("right"));
                }
                else if (inResultConfig) {
                    resultTableList.add((String)attributes.get("left"));
                    resultTableList.add((String)attributes.get("right"));
                }
            }
            else if ("variable".equals(name)) {
                varList.put((String)attributes.get(NAME), (String)attributes.get(LABEL));
            }
            else if ("filter".equals(name)) {
                String table = (String)attributes.get(TABLE);
                List<String> colList = filterList.get(table);
                if (colList == null) {
                    colList = new ArrayList<String>();
                }
                colList.add((String)attributes.get("column"));
                filterList.put(table, colList);
            }
            else if ("column".equals(name)) {
                resultColumns.put((String)attributes.get("label"), (String)attributes.get("value"));
            }
            else if ("select-config".equals(name)) {
                inSelectConfig = true;
            }
            else if ("result-config".equals(name)) {
                inResultConfig = true;
            }
        }


        public void endElement(String name, String value) {
            if (FAMILY.equals(name)) {
                root.getDocumentElement().appendChild(familyNode);
            }
            else if ("select-config".equals(name)) {
                inSelectConfig = false;
                appendTablesNode();
            }
            else if ("result-config".equals(name)) {
                inResultConfig = false;
                appendResultNode();
            }
            else if ("functions".equals(name)) {
                appendHelpNode();
            }
            else if ("variables".equals(name)) {
                appendVariablesNode();
            }
            else if ("filters".equals(name)) {
                appendFiltersNode();
            }
            else if ("holder".equals(name)) {
                try {
                    functionManager.addFunctionHolder((FunctionHolder)Class.forName(
                          value.trim()).newInstance());
                }
                catch (Exception error) {
                    log.error("Impossible d'instancier le functionHolder " + value.trim(),
                              error);
                }
            }
            else if ("requetor".equals(name)) {
                appendRequetorNode(value);
            }
        }


        private void appendRequetorNode(String requetorId) {
            Node requetorNode = root.createElement(REQUETOR);
            requetorNode.setTextContent(requetorId);
            familyNode.appendChild(requetorNode);
        }


        private void appendFiltersNode() {
            Node filtersNode = root.createElement("filters");
            for (Map.Entry<String, List<String>> entry : filterList.entrySet()) {

                List<String> colList = entry.getValue();
                for (String aColList : colList) {
                    Element variableNode = root.createElement("filter");
                    variableNode.setAttribute(TABLE, entry.getKey());
                    variableNode.setAttribute("column", aColList);
                    filtersNode.appendChild(variableNode);
                }
            }
            familyNode.appendChild(filtersNode);
        }


        private void appendVariablesNode() {
            Node variablesNode = root.createElement("variables");

            for (Map.Entry<String, String> entry : varList.entrySet()) {
                Element filterNode = root.createElement("variable");
                filterNode.setAttribute(NAME, entry.getKey());
                filterNode.setAttribute(LABEL, entry.getValue());
                variablesNode.appendChild(filterNode);
            }
            familyNode.appendChild(variablesNode);
        }


        private void appendHelpNode() {
            Node helpNode = root.createElement("help");

            List funcList = functionManager.getAllFunctionsHelp();
            for (Object aFuncList : funcList) {
                FunctionHelp func = (FunctionHelp)aFuncList;
                Element funcHelpNode = root.createElement("function");
                funcHelpNode.setAttribute(NAME, func.getFunctionName());
                funcHelpNode.setAttribute("arg", Integer.toString(func.getParameterNumber()));
                funcHelpNode.setAttribute("help", func.getHelp());
                helpNode.appendChild(funcHelpNode);
            }
            familyNode.appendChild(helpNode);
        }


        private void appendTablesNode() {
            Node tableListNode = root.createElement("tables");
            addTableNodes(selectTableList, tableListNode);
            familyNode.appendChild(tableListNode);
        }


        private void appendResultNode() {
            Node tableListNode = root.createElement("result");
            addTableNodes(resultTableList, tableListNode);
            for (Map.Entry<String, String> entry : resultColumns.entrySet()) {
                Element columnNode = root.createElement(COLUMN);
                columnNode.setAttribute("label", entry.getKey());
                columnNode.setAttribute("value", entry.getValue());
                tableListNode.appendChild(columnNode);
            }
            familyNode.appendChild(tableListNode);
        }


        private void addTableNodes(Set<String> tableList, Node tableListNode) {
            for (String aTable : tableList) {
                Element tableNode = root.createElement(TABLE);
                tableNode.setAttribute(NAME, aTable);
                tableListNode.appendChild(tableNode);
            }
        }
    }

    /**
     * ErrorHandler juste au cas ou.
     */
    private class MyErrorHandler implements ErrorHandler {
        public void error(SAXParseException sAXParseException)
              throws SAXException {
            log.error("ERROR : ");
            print(sAXParseException);
        }


        public void fatalError(SAXParseException sAXParseException)
              throws SAXException {
            log.error("FATAL : ");
            print(sAXParseException);
        }


        public void warning(SAXParseException sAXParseException)
              throws SAXException {
            log.error("WARNING : ");
            print(sAXParseException);
        }


        private void print(SAXParseException error) {
            log.error("[L=" + error.getLineNumber() + " C=" + error.getColumnNumber()
                      + "] " + error.toString());
        }
    }
}
