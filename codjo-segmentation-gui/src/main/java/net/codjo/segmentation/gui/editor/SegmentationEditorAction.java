/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.gui.toolkit.util.Modal;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.segmentation.gui.preference.PreferenceGui;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
/**
 * Action permettant d'afficher l'editeur d'expression pour les traitements. Cette classe fera partie de
 * codjo-database-gui.
 */
public class SegmentationEditorAction extends AbstractGuiAction {
    private EditorManager editorManager;
    private String selectedSleeveRowId;
    private String selectedClassificationId;


    public SegmentationEditorAction(GuiContext ctxt) {
        super(ctxt, "Edition de l'expression",
              "Assistant permettant de faciliter l'édition d'une expression ");
        putValue(ID_PROPERTY, "segmentation.edit.expression");
    }


    public EditorManager getEditorManager() {
        return editorManager;
    }


    public void setEditorManager(EditorManager editorManager) {
        this.editorManager = editorManager;
    }


    public void actionPerformed(ActionEvent event) {
        displayEditor();
    }


    private void displayEditor() {
        EditorConfig config = editorManager.editorStart();

        final EditorWindow editor =
              new EditorWindow(config.getPreference(), editorManager, selectedClassificationId,
                               selectedSleeveRowId);

        EditorEventManager editorEventManager =
              new EditorEventManager(editor.getEditorMainPanelLogic());

        editor.getEditorMainPanelLogic().addExternalPanel(editorManager.getPanel(
              editorEventManager));
        editor.getEditorMainPanelLogic().addLeftPanel(editorManager.getLeftPanel(
              editorEventManager));
        editor.setExpression(config.getExpression());
//        editor.setSize(1000, 700);
//        editor.setLocation(100, 100);
        getGuiContext().getDesktopPane().add(editor);
        Modal.applyModality(config.getParentEditor(), editor);

        editor.setVisible(true);
        editor.setClosable(true);
        select(editor);
        editor.moveToFront();
    }


    private void select(EditorWindow editor) {
        try {
            editor.setSelected(true);
        }
        catch (PropertyVetoException error) {
            ; // Pas grave
        }
    }


    public void setSelectedSleeveRowId(String selectedSleeveRowId) {
        this.selectedSleeveRowId = selectedSleeveRowId;
    }


    public void setSelectedClassificationId(String selectedClassificationId) {
        this.selectedClassificationId = selectedClassificationId;
    }


    /**
     * Représente le manager d'une session d'édition d'une expression. Le manager donne l'ancienne expression
     * et recois la nouvell.
     */
    public interface EditorManager {
        // TODO Cette interface devrait être revue en ce qui concerne le
        // getPanel et le panel.
        public EditorConfig editorStart();


        /**
         * Retourne la section valeur du tab colonne et valeur. TODO Revoir la signature de cette interface
         */
        public JPanel getPanel(EditorEventManager listenerManager);


        /**
         * Retourne le tab des axes, TODO Revoir la signature de cette interface
         */
        public JPanel getLeftPanel(EditorEventManager manager);


        public boolean editorOk(String newExpression);
    }

    /**
     * Représente une configuration permettant d'afficher l'éditeur d'expression.
     */
    public static class EditorConfig {
        private PreferenceGui preference;
        private String expression;
        private JInternalFrame parentEditor;


        public EditorConfig(PreferenceGui preference, String expression,
                            JInternalFrame parent) {
            this.preference = preference;
            this.expression = expression;
            this.parentEditor = parent;
        }


        public JInternalFrame getParentEditor() {
            return parentEditor;
        }


        public PreferenceGui getPreference() {
            return preference;
        }


        public String getExpression() {
            return expression;
        }
    }
}
