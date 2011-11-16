package net.codjo.segmentation.server.participant.context;
import java.util.Map;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
/**
 *
 */
public class FamilyContextMock extends FamilyContext {
    private XmlFamilyPreference familyPreferenceMock;
    private Map<String, String> params;


    public FamilyContextMock() {
        super(null, null);
    }


    @Override
    public XmlFamilyPreference getFamilyPreference() {
        return familyPreferenceMock;
    }


    public void mockGetXmlFamilyPreference(XmlFamilyPreference preference) {
        familyPreferenceMock = preference;
    }


    @Override
    public Map<String, String> getParameters() {
        return params;
    }


    public void mockGetParams(Map<String, String> map) {
        params = map;
    }
}
