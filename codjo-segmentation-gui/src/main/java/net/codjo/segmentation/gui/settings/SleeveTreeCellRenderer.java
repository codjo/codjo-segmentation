package net.codjo.segmentation.gui.settings;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;
/**
 *
 */
/**
 * Renderer sur l'arbre : la poche fourre-tout est affichée en rouge.
 */
class SleeveTreeCellRenderer extends DefaultTreeCellRenderer {
    SleeveTreeCellRenderer() {
    }


    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean focused) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focused);
        if ((value instanceof Sleeve)) {
            Sleeve sleeve = (Sleeve)value;
            if (sleeve.isDustbin()) {
                setForeground(Color.red);
                setIcon(getFourreToutIcon());
            }
            else if (sleeve.isTerminal()) {
                setIcon(getPocheIcon());
            }
            else {
                setIcon(getNoeudIcon());
            }
        }
        else if ((value instanceof ClassificationsEditorTreeModel.Sleeve)) {
            ClassificationsEditorTreeModel.Sleeve sleeve =
                  (ClassificationsEditorTreeModel.Sleeve)value;
            if (sleeve.isTerminalElement()) {
                setIcon(getPocheIcon());
            }
            else {
                setIcon(getNoeudIcon());
            }
        }
        else {
            setIcon(getAxeIcon());
        }
        return this;
    }


    private Icon getAxeIcon() {
        return new ImageIcon(getClass().getResource("axe.png"));
    }


    private Icon getNoeudIcon() {
        return new ImageIcon(getClass().getResource("noeud.png"));
    }


    private Icon getPocheIcon() {
        return new ImageIcon(getClass().getResource("poche.png"));
    }


    private Icon getFourreToutIcon() {
        return new ImageIcon(getClass().getResource("fourre_tout.png"));
    }
}