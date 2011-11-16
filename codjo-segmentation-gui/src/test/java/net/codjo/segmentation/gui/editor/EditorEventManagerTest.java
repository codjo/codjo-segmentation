/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.expression.help.FunctionHelp;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.4 $
 */
public class EditorEventManagerTest extends TestCase {
    private EditorEventManager editorEventManager;
    private EditorMainPanelLogic editorMainPanelLogic;
    private ListSelectionListener listSelectionListener;


    public void test_addColumnsListListener() throws Exception {
        editorEventManager.addColumnsListListener(listSelectionListener);
        //TODO liste non triée. test a revoir.
        ListSelectionListener[] listeners = editorMainPanelLogic.getEditorMainPanelGui().getFamilyFieldJList()
              .getListeners(ListSelectionListener.class);
        assertEquals("THIS IS THE LISTENER ADDED", listeners[listeners.length - 2].toString());
    }


    @Override
    protected void setUp() throws Exception {
        editorMainPanelLogic = new EditorMainPanelLogic(new HashMap(), new ArrayList<FunctionHelp>(), new WaitingPanel());
        editorEventManager = new EditorEventManager(editorMainPanelLogic);

        listSelectionListener =
              new ListSelectionListener() {

                  public void valueChanged(ListSelectionEvent evt) {
                  }


                  @Override
                  public String toString() {
                      return "THIS IS THE LISTENER ADDED";
                  }
              };
    }
}
