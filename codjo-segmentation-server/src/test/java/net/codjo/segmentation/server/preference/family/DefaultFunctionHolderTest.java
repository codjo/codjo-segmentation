/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import net.codjo.segmentation.server.preference.family.DefaultFunctionHolder;
/**
 * Classe de test de {@link net.codjo.segmentation.server.preference.family.DefaultFunctionHolder}.
 */
public class DefaultFunctionHolderTest extends TestCase {
    private DefaultFunctionHolder defaultFunctionHolder;


    public void test_getName() throws Exception {
        assertEquals("utils", defaultFunctionHolder.getName());
    }


    public void test_allFunctionDeclared() {
        Map map = new HashMap();
        List all = defaultFunctionHolder.getAllFunctions();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            String func = (String)iter.next();
            int idx = func.indexOf("(");
            map.put(func.substring(defaultFunctionHolder.getName().length() + 1, idx),
                    func);
        }

        // Cas particulier
        map.put("getAllFunctions", "");
        map.put("getName", "");

        Method[] method = defaultFunctionHolder.getClass().getDeclaredMethods();
        for (int i = 0; i < method.length; i++) {
            if (method[i].getModifiers() == Modifier.PUBLIC) {
                if (!map.containsKey(method[i].getName())) {
                    fail("La methode " + method[i].getName()
                         + " n'est pas declarée dans la methode getAllFunctions");
                }
                else {
                    map.remove(method[i].getName());
                }
            }
        }

        assertEquals("Toutes les methodes de getAllFunctions existent", 0, map.size());
    }


    public void test_caseOf_OneSleeve() throws Exception {
        assertEquals("TEST2",
                     defaultFunctionHolder.caseOf(new boolean[]{false, true, false},
                                                  new String[]{"TEST1", "TEST2", "TEST3"}, "AUTRE"));
    }


    public void test_caseOf_NoSleeve() throws Exception {
        assertEquals("AUTRE",
                     defaultFunctionHolder.caseOf(new boolean[]{false, false},
                                                  new String[]{"TEST1", "TEST2"}, "AUTRE"));
    }


    public void test_caseOf_intersection() throws Exception {
        try {
            defaultFunctionHolder.caseOf(new boolean[]{false, true, false, true},
                                         new String[]{"TEST1", "TEST2", "TEST3", "TEST4"}, "AUTRE");
            fail("caseOf doit échouer : intersection");
        }
        catch (DefaultFunctionHolder.IntersectionException ex) {
            assertEquals("Les valeurs possibles sont : [TEST2, TEST4]", ex.getMessage());
        }
    }


    public void test_caseOf_badParams() throws Exception {
        try {
            defaultFunctionHolder.caseOf(null, new String[]{"TEST1", "TEST2"}, "AUTRE");
            fail("caseOf doit échouer : sleeve null");
        }
        catch (IllegalArgumentException e) {
        }

        try {
            defaultFunctionHolder.caseOf(new boolean[]{false, true}, null, "AUTRE");
            fail("caseOf doit échouer : sleeveId null");
        }
        catch (IllegalArgumentException e) {
        }

        try {
            defaultFunctionHolder.caseOf(new boolean[]{false, true},
                                         new String[]{"TEST1", "TEST2"}, null);
            fail("caseOf doit échouer : sleeveOtherId null");
        }
        catch (IllegalArgumentException e) {
        }

        try {
            defaultFunctionHolder.caseOf(new boolean[]{false, true, false},
                                         new String[]{"TEST1", "TEST2"}, "AUTRE");
            fail("caseOf doit échouer : nombre d'éléments différent");
        }
        catch (IllegalArgumentException e) {
        }
    }


    protected void setUp() throws Exception {
        defaultFunctionHolder = new DefaultFunctionHolder();
    }
}
