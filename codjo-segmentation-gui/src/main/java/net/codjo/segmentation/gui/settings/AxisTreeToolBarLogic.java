/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
/**
 * Logique de la barre d'outils de l'arbre.
 */
public class AxisTreeToolBarLogic {
    private AxisTreeToolBar gui;
    private AxisTreeLogic axisTreeLogic;


    public AxisTreeToolBarLogic(AxisTreeToolBar toolbar) {
        gui = toolbar;
        gui.addSleeveActionListener(new AddSleeveAction());
        gui.addFolderActionListener(new AddFolderAction());
        gui.addDeleteActionListener(new DeleteAction());
    }


    public void init(AxisTreeLogic newAxisTreeLogic) {
        axisTreeLogic = newAxisTreeLogic;
        newAxisTreeLogic.addTreeSelectionListener(new SleeveSelectionListener());
    }


    public AxisTreeToolBar getGui() {
        return gui;
    }


    /**
     * Listener chargé de désactiver les boutons d'ajout lorsque la poche sélectionnée est une feuille, et de
     * les activer dans le cas contraire.
     */
    protected class SleeveSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent event) {
            TreeNode node = (TreeNode)event.getPath().getLastPathComponent();
            AxisTree axisTree = axisTreeLogic.getGui();
            TreePath selectionPath = axisTree.getSelectionPath();

            boolean isSleeve = node instanceof Sleeve;
            boolean isSleeveTerminal = isSleeve && ((Sleeve)node).isTerminal();
            int maxNodeDepth = axisTree.getClassificationStructureGui().getMaximumNodeDepth();
            
            if (selectionPath != null && selectionPath.getPathCount() > maxNodeDepth) {
                gui.getAddFolderButton().setEnabled(false);
            }
            else {
                gui.getAddFolderButton().setEnabled(!isSleeveTerminal);
            }
            gui.getDeleteButton().setEnabled(isSleeve);
            gui.getAddSleeveButton().setEnabled(!isSleeveTerminal);
        }
    }

    /**
     * Listener chargé d'ajouter une poche dans l'arbre des poches.
     */
    protected class AddSleeveAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            axisTreeLogic.addSleeve();
        }
    }

    /**
     * Listener chargé d'ajouter un dossier dans l'arbre des poches.
     */
    protected class AddFolderAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            axisTreeLogic.addFolder();
        }
    }

    /**
     * Listener chargé de supprimer une poche ou un dossier de l'arbre des poches.
     */
    protected class DeleteAction implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            axisTreeLogic.delete();
        }
    }
}
