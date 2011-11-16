/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 * Axe de classification. L'axe doit être passé à la construction du
 * <code>AxisTreeModel</code>.
 */
public class AxisTreeNode extends DefaultMutableTreeNode {
    private String name;

    public AxisTreeNode(String name) {
        this.name = name;
    }

    /**
     * Retourne le nom
     *
     * @return Le nom
     */
    public String getName() {
        return name;
    }


    /**
     * Positionne le nouveau nom
     *
     * @param newName Le nouveau nom
     */
    public void setName(String newName) {
        name = newName;
    }


    public String toString() {
        return name;
    }
}
