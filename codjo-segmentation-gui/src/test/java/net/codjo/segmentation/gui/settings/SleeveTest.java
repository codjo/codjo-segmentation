/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import junit.framework.TestCase;
import java.util.Map;
import java.util.HashMap;
/**
 * Classe de test de {@link Sleeve}.
 */
public class SleeveTest extends TestCase {
    private Sleeve sleeve;

    public void test_getPathCode() throws Exception {
        assertEquals("1.2", sleeve.getPathCode());
    }


    @Override
    protected void setUp() throws Exception {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("classificationId", "1");
        fields.put("sleeveId", "1");
        fields.put("sleeveRowId", "" + System.currentTimeMillis());
        fields.put("sleeveCode", "02-1.2");
        fields.put("sleeveName", "Noeud");
        fields.put("sleeveDustbin", String.valueOf(false));
        fields.put("terminalElement", String.valueOf(false));
        fields.put("formula", "");
        sleeve = new Sleeve(fields);
    }
}
