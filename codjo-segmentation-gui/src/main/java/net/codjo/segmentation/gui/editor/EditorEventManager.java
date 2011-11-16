/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Map;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.event.ListSelectionListener;
/**
 * Gestionnaire des événements de l'éditeur d'expressions.
 *
 * @version $Revision: 1.6 $
 */
public class EditorEventManager {
    private EditorMainPanelLogic editorMainPanelLogic;


    public EditorEventManager(EditorMainPanelLogic editorMainPanelLogic) {
        this.editorMainPanelLogic = editorMainPanelLogic;
    }


    public void addColumnsListListener(ListSelectionListener listener) {
        editorMainPanelLogic.getEditorMainPanelGui().getFamilyFieldJList()
              .addListSelectionListener(listener);
    }


    public Map getFieldMap() {
        return editorMainPanelLogic.getFieldMap();
    }


    public MouseListener getjListMouseListener() {
        return editorMainPanelLogic.getjListMouseListener();
    }


    public void insertText(String text) {
        editorMainPanelLogic.insertText(text);
    }


    public void addMapToBeReplaced(Map toBeReplaced) {
        editorMainPanelLogic.addMapToBeReplaced(toBeReplaced);
    }


    public void addStringsStyle(SimpleAttributeSet attributeStyle, Collection stringStyle) {
        StringsStyle stringsStyle = new StringsStyle(attributeStyle, stringStyle);
        editorMainPanelLogic.addStringsStyle(stringsStyle);
    }


    public void initFieldListPopupMenu(ValueListPanel valueListPanel) {
        editorMainPanelLogic.initFieldListPopupMenu(valueListPanel);
    }
}
