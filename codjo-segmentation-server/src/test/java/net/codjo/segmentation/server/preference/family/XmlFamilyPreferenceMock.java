package net.codjo.segmentation.server.preference.family;
import net.codjo.expression.FunctionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 *
 */
public class XmlFamilyPreferenceMock extends XmlFamilyPreference {
    private List<String> mockedArguments = new ArrayList<String>();


    public XmlFamilyPreferenceMock() {
        super("familyMocked", "ap_mock", "sysobjects");
    }


    public XmlFamilyPreferenceMock(String familyId, String rootTable, String destinationTable) {
        super(familyId, rootTable, destinationTable);
    }


    @Override
    public FunctionManager createFunctionManager() {
        if (getFunctionHolderList() == null) {
            try {
                compileConfiguration();
            }
            catch (BadConfigurationException e) {
                throw new IllegalStateException("Erreur interne", e);
            }
        }
        return super.createFunctionManager();
    }


    @Override
    public List<String> getArgumentNameList() {
        return mockedArguments;
    }


    public void mockGetArgumentNameList(String... arguments) {
        mockedArguments = Arrays.asList(arguments);
    }
}
