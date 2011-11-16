/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import net.codjo.expression.FunctionManager;
import net.codjo.expression.UserFunctionHolder;
import net.codjo.expression.help.FunctionHelp;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.gui.preference.PreferenceGui}.
 */
public class PreferenceGuiTest extends TestCase {
    private PreferenceGui pref;


    protected void setUp() throws Exception {
        pref = new PreferenceGui("myFamily");
    }


    public void test_getPreferenceTables() throws Exception {
        pref.addTable("AP_TOTO");

        List tablesList = pref.getTables();
        assertEquals(1, tablesList.size());
        assertEquals("AP_TOTO", tablesList.get(0));
    }


    public void test_getColumnsFor() throws Exception {
        pref.setStructure(new FakeDBStructure() {
            public List getColumnsFor(String tableName) {
                List columnsList = new ArrayList();
                columnsList.add(tableName + ".COL1");
                return columnsList;
            }
        });
        assertEquals("AP_TOTO.COL1", pref.getColumnsFor("AP_TOTO").get(0));
    }


    public void test_getColumnLabelFor() throws Exception {
        pref.setStructure(new FakeDBStructure() {
            public String getColumnLabelFor(String tableName, String fieldSql) {
                return "nom logique pour " + tableName + "." + fieldSql;
            }
        });
        assertEquals("nom logique pour AP_TOTO.COL_1",
                     pref.getColumnLabelFor("AP_TOTO", "COL_1"));
    }


    public void test_getDefaultFunctionsHelp() throws Exception {
        FunctionManager etalon = new FunctionManager();
        List functionList = pref.getAllFunctionsHelp();
        assertEquals(etalon.getAllFunctionsHelp().size(), functionList.size());
    }


    public void test_getFunctionsHelp_withAnotherFunction()
          throws Exception {
        FunctionManager etalon = new FunctionManager();
        UserFunctionHolder user = new UserFunctionHolder("user");
        user.addFunction(java.sql.Types.VARCHAR, "foo", "Usage : foo(chaîne)",
                         "public String foo(String var)...");
        FunctionManager functionManager = new FunctionManager();
        functionManager.addFunctionHolder(user);
        pref.setAllFunctionsHelp(functionManager.getAllFunctionsHelp());
        List functionList = pref.getAllFunctionsHelp();

        assertEquals(etalon.getAllFunctionsHelp().size() + 1, functionList.size());

        boolean found = false;
        for (int idx = 0; idx < functionList.size(); idx++) {
            FunctionHelp help = (FunctionHelp)functionList.get(idx);
            if ("user.foo".equals(help.getFunctionName())) {
                found = true;
            }
        }
        assertTrue("La méthode foo est trouvé", found);
    }


    public static class FakeDBStructure implements DBStructure {
        public List getColumnsFor(String tableName) {
            return null;
        }


        public String getColumnLabelFor(String tableName, String sqlField) {
            return null;
        }
    }
}
