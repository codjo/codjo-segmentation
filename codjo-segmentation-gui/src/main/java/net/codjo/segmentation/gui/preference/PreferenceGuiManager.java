/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import java.util.HashMap;
import java.util.Map;
/**
 * Manager des préférences de traitement Gui.
 */
public class PreferenceGuiManager {
    private Map<String, PreferenceGui> familyMap = new HashMap<String, PreferenceGui>();


    public void addPreference(PreferenceGui preferenceGui) {
        familyMap.put(preferenceGui.getFamilyId(), preferenceGui);
    }


    public PreferenceGui getPreference(String familyId) {
        return familyMap.get(familyId);
    }


    public PreferenceGui[] getAllPreference() {
        return familyMap.values().toArray(new PreferenceGui[familyMap.size()]);
    }
}
