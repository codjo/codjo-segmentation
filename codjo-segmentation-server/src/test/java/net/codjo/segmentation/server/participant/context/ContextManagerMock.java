package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.family.XmlPreferenceLoader;
import java.util.HashMap;
import java.util.Map;
/**
 *
 */
public class ContextManagerMock extends ContextManager {
    Map<String, XmlFamilyPreference> mockedValue = new HashMap<String, XmlFamilyPreference>();


    public ContextManagerMock() {
        super(new XmlPreferenceLoader());
    }


    @Override
    public XmlFamilyPreference getFamilyPreference(String familyId) {
        return mockedValue.get(familyId);
    }


    public void mockGetFamilyPreference(String familyId, XmlFamilyPreference xmlFamilyPreference) {
        mockedValue.put(familyId, xmlFamilyPreference);
    }
}
