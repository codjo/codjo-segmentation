/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.expression.help.FunctionHelp;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
/**
 * Pour Tester L'IHM.
 */
public class EditorMainPanelLogicTest extends TestCase {
    EditorMainPanelLogic editorMainPanelLogic;


    @Override
    protected void setUp() throws Exception {
        Map<String, String> fieldsMap = new HashMap<String, String>();
        fieldsMap.put("SRC_TAB1$COL1", "U_Column1");
        fieldsMap.put("SRC_TAB2$COL1_U", "Column1");
        fieldsMap.put("SRC_TAB3$COL3", "Column3");
        fieldsMap.put("SRC_TAB4$COL4", "column4");

        editorMainPanelLogic = new EditorMainPanelLogic(fieldsMap, getFunctionListForTest(),
                                                        new WaitingPanel());
    }


    public static List<FunctionHelp> getFunctionListForTest() {
        List<FunctionHelp> aList = new ArrayList<FunctionHelp>();
        int idx = -1;
        FunctionHelp func = new FunctionHelp("in", idx, "Usage : in(field, valeur1, valeur2, ...)");
        aList.add(func);
        int i1 = -1;
        func = new FunctionHelp("notIn", i1, "Usage : notIn(field, value1, value2, ...)");
        aList.add(func);
        func = new FunctionHelp("Math.abs", 1, "Usage : Math.abs(valeur)");
        aList.add(func);
        func = new FunctionHelp("iif", 3, "Usage : iif(condition, trueValue, falseValue)");
        aList.add(func);
        func = new FunctionHelp("Math.max", 2, "Usage : Math.max(a, b)");
        aList.add(func);
        func = new FunctionHelp("Math.min", 2, "Usage : Math.min(a, b)");
        aList.add(func);
        return aList;
    }


    public void testGetEditorMainPanelGui() throws Exception {
        EditorMainPanelGui editorMainPanelGui = editorMainPanelLogic.getEditorMainPanelGui();
        assertNotNull(editorMainPanelGui);
    }


    public void testGetExpression() throws Exception {
        editorMainPanelLogic.setExpression(
              "test SRC_TAB1$COL1, SRC_TAB1$COL1_U, SRC_TAB1$COL1 (rrr( SRC_TAB4$COL4)");
        assertEquals("test SRC_TAB1$COL1, SRC_TAB1$COL1_U, SRC_TAB1$COL1 (rrr( SRC_TAB4$COL4)",
                     editorMainPanelLogic.getExpression());
    }


    public void testIsInParentheses() throws Exception {
        assertFalse(editorMainPanelLogic.isInParentheses("    ", 2));
        assertFalse(editorMainPanelLogic.isInParentheses(" )   ", 4));
        assertTrue(editorMainPanelLogic.isInParentheses("if ( g, ) ", 5));
        assertTrue(editorMainPanelLogic.isInParentheses("if ( g,  ", 5));
        assertFalse(editorMainPanelLogic.isInParentheses("if  (", 3));
        assertFalse(editorMainPanelLogic.isInParentheses("if (i) ", 6));
    }
}
