package net.codjo.segmentation.server.preference.family;
import net.codjo.xml.fast.ClientContentHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 *
 */
public class VarFieldBuilder implements ClientContentHandler {
    List<VarField> variables = new ArrayList<VarField>();
    boolean intag = false;


    public void startElement(String name, Map attributes) {
        if ("variables".equals(name)) {
            intag = true;
        }
        if (intag && "variable".equals(name)) {
            variables.add(new VarField((String)attributes.get("name"),
                                       (String)attributes.get("sqlType"),
                                       (String)attributes.get("label")));
        }
    }


    public void endElement(String name, String value) {
        if ("variables".equals(name)) {
            intag = false;
        }
    }


    public List<VarField> getVariables() {
        return variables;
    }
}
