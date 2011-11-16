/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
/**
 * GUI de l'arbre contenant les poches.
 */
public class AxisTree extends JTree {
    private ClassificationStructureGui classificationStructureGui;


    public AxisTree() {
        setEditable(false);
        setName("axisTree");
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        putClientProperty("JTree.lineStyle", "Angled");
        setShowsRootHandles(true);
        setCellRenderer(new SleeveTreeCellRenderer());
    }


    public void setClassificationStructureGui(ClassificationStructureGui gui) {
        classificationStructureGui = gui;
    }

    public ClassificationStructureGui getClassificationStructureGui() {
        return classificationStructureGui;
    }
}
