/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.xml.XmlException;
import net.codjo.xml.fast.ClientContentHandler;
import net.codjo.xml.fast.XmlParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
/**
 * Classe permettant de charger les préférences à partir d'un fichier XML.
 *
 * @version $Revision: 1.7 $
 */
public class XmlPreferenceLoader {
    private static final Logger LOG = Logger.getLogger(XmlPreferenceLoader.class);
    private Map<String, XmlFamilyPreference> familyPreferences = new HashMap<String, XmlFamilyPreference>();


    public void load(InputSource source) throws IOException, XmlException, BadConfigurationException {
        XmlParser xmlParser = new XmlParser();
        PreferenceBuilder preferenceBuilder = new PreferenceBuilder();
        familyPreferences = preferenceBuilder.result;
        xmlParser.parse(source, preferenceBuilder);
        for (XmlFamilyPreference preference : familyPreferences.values()) {
            preference.compileConfiguration();
        }
    }


    int familyCount() {
        return familyPreferences.size();
    }


    public XmlFamilyPreference getFamilyPreference(String familyId) {
        return familyPreferences.get(familyId);
    }


    /**
     * Construction des objets à partir du fichier XML.
     */
    private static class PreferenceBuilder implements ClientContentHandler {
        private Map<String, XmlFamilyPreference> result = new HashMap<String, XmlFamilyPreference>();
        private XmlFamilyPreference xmlFamilyPreference;
        private List<String> arguments;
        private List<String> functions;
        private DefaultConfigBuilder dcBuilder;
        private DefaultConfigBuilder scBuilder;
        private ResultConfigBuilder rcBuilder;
        private VarFieldBuilder varBuilder = new VarFieldBuilder();
        private String classFilterName;


        public void startElement(String name, Map attributes) {
            if ("family-list".equals(name)) {
                return;
            } if ("family".equals(name)) {
                String id = (String)attributes.get("id");
                String root = (String)attributes.get("root");
                String destination = (String)attributes.get("destination");
                dcBuilder = new DefaultConfigBuilder("delete-config", destination);
                scBuilder = new DefaultConfigBuilder("select-config", root);
                rcBuilder = new ResultConfigBuilder(destination);
                varBuilder = new VarFieldBuilder();
                xmlFamilyPreference = new XmlFamilyPreference(id, root, destination);
                result.put(id, xmlFamilyPreference);
            } else if ("arguments".equals(name)) {
                arguments = new ArrayList<String>();
            } else if ("functions".equals(name)) {
                functions = new ArrayList<String>();
            } else if ("select-filter".equals(name)) {
                classFilterName = (String)attributes.get("class");
            }
            dcBuilder.startElement(name, attributes);
            scBuilder.startElement(name, attributes);
            rcBuilder.startElement(name, attributes);
            varBuilder.startElement(name, attributes);
        }


        public void endElement(String name, String value) {
            if ("arguments".equals(name)) {
                xmlFamilyPreference.setArgumentNameList(arguments);
            } else if ("name".equals(name)) {
                arguments.add(value);
            } else if ("holder".equals(name)) {
                functions.add(value);
            } else if ("functions".equals(name)) {
                xmlFamilyPreference.setFunctionHolderClassList(functions);
            } else if ("select-filter".equals(name)) {
                try {
                    RowFilter filter = (RowFilter)Class.forName(classFilterName).newInstance();
                    xmlFamilyPreference.setFilter(filter);
                }
                catch (Exception error) {
                    LOG.debug("Impossible d'instancier le filtre " + classFilterName, error);
                }
            }
            //do nothing if: where-clause, delete-config, select-config, result-config
            dcBuilder.endElement(name, value);
            scBuilder.endElement(name, value);
            rcBuilder.endElement(name, value);
            varBuilder.endElement(name, value);
            if ("family".equals(name)) {
                xmlFamilyPreference.setDeleteConfig(dcBuilder.getConfig());
                xmlFamilyPreference.setSelectConfig(scBuilder.getConfig());
                xmlFamilyPreference.setResultConfig(rcBuilder.getConfig());
                xmlFamilyPreference.setResultColumns(rcBuilder.getTableColumns());
                xmlFamilyPreference.setVariables(varBuilder.getVariables());
            }
        }
    }
}
