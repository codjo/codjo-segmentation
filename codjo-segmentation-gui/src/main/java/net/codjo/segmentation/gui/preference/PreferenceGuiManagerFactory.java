/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import net.codjo.expression.help.FunctionHelp;
import net.codjo.mad.client.request.CommandRequest;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.gui.request.util.RequestHelper;
import net.codjo.xml.XmlException;
import net.codjo.xml.fast.ClientContentHandler;
import net.codjo.xml.fast.XmlParser;
import net.codjo.util.string.StringUtil;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.InputSource;
/**
 * Classe responsable de la creation PreferenceGuiManager
 *
 * @version $Revision: 1.6 $
 */
public class PreferenceGuiManagerFactory {
    private PreferenceGuiManager preferenceGuiManager;


    public PreferenceGuiManagerFactory(StructureReader madReader)
          throws RequestException, IOException, XmlException {
        Row rs = RequestHelper.sendSimpleRequest(new CommandRequest("getSegmentationConfig"));
        String xmlFile = rs.getField(0).getValue();

        XmlParser xmlParser = new XmlParser();
        DBStructureMad mad = new DBStructureMad();
        mad.setMad(madReader);
        ManagerBuilder builder = new ManagerBuilder(mad);
        xmlParser.parse(new InputSource(new StringReader(xmlFile)), builder);

        preferenceGuiManager = builder.preferenceGuiManager;
    }


    public PreferenceGuiManager getPreferenceGuiManager() {
        return preferenceGuiManager;
    }


    private static class ManagerBuilder implements ClientContentHandler {
        private PreferenceGui pref = null;
        private PreferenceGuiManager preferenceGuiManager = new PreferenceGuiManager();
        private List<FunctionHelp> help;
        private DBStructureVariable variables;
        private DBStructureFilter filters;
        private DBStructureMad mad;
        private Map<String, String> columns;
        private boolean inTablesNode;


        ManagerBuilder(DBStructureMad mad) {
            this.mad = mad;
        }


        public void startElement(String name, Map attributes) {
            if ("family".equals(name)) {
                String id = (String)attributes.get("id");
                pref = new PreferenceGui(id);
                variables = new DBStructureVariable();
                filters = new DBStructureFilter();
                columns = new LinkedHashMap<String, String>();
            }
            else if ("tables".equals(name)) {
                inTablesNode = true;
            }
            else if ("table".equals(name) && inTablesNode) {
                pref.addTable((String)attributes.get("name"));
            }
            else if ("help".equals(name)) {
                help = new ArrayList<FunctionHelp>();
            }
            else if ("function".equals(name)) {
                String funcName = (String)attributes.get("name");
                int nbArg = Integer.valueOf((String)attributes.get("arg"));
                String funcHelp = (String)attributes.get("help");
                help.add(new FunctionHelp(funcName, nbArg, funcHelp));
            }
            else if ("variable".equals(name)) {
                String var = (String)attributes.get("name");
                String label = (String)attributes.get("label");
                variables.addVariable(var, label);
            }
            else if ("filter".equals(name)) {
                String table = (String)attributes.get("table");
                String column = (String)attributes.get("column");
                filters.addFilter(table, column);
            }
            else if ("column".equals(name)) {
                columns.put((String)attributes.get("label"), StringUtil.sqlToJavaName((String)attributes.get("value")));
            }
        }


        public void endElement(String name, String value) {
            if ("family".equals(name)) {
                filters.setSubDBStructure(variables);
                variables.setSubDBStructure(mad);
                pref.setStructure(filters);
                pref.setResultColumns(columns);
                preferenceGuiManager.addPreference(pref);
                pref = null;
            }
            else if ("help".equals(name)) {
                pref.setAllFunctionsHelp(help);
            }
            else if ("variables".equals(name)) {
                pref.addTable(DBStructureVariable.VAR_TABLE);
            }
            else if ("tables".equals(name)) {
                inTablesNode = false;
            }
            else if ("requetor".equals(name)) {
                pref.setResultRequetor(value);
            }
        }
    }
}
