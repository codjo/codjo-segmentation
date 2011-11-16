/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.gui.preference.PreferenceGuiManager}.
 */
public class PreferenceGuiManagerTest extends TestCase {
    private PreferenceGuiManager manager;


    public void test_getPreference() throws Exception {
        PreferenceGui preferenceGui = new PreferenceGui("maFamily");
        manager.addPreference(preferenceGui);

        assertSame(preferenceGui, manager.getPreference("maFamily"));
    }


    public void test_getAllPreference() throws Exception {
        PreferenceGui[] allPref = manager.getAllPreference();
        assertEquals(0, allPref.length);
    }


    public void test_getAllPreference_one() throws Exception {
        manager.addPreference(new PreferenceGui("maFamily"));
        PreferenceGui[] allPref = manager.getAllPreference();
        assertEquals(1, allPref.length);
        assertEquals("maFamily", allPref[0].getFamilyId());
    }


    protected void setUp() throws Exception {
        manager = new PreferenceGuiManager();
    }
}
