/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
/**
 * GUI de la barre d'outils de l'arbre, contenant les boutons permettant d'ajouter ou de
 * supprimer des noeuds et des feuilles.
 */
public class AxisTreeToolBar extends JToolBar {
    private Map allActions = new HashMap();
    private JPopupMenu popupMenu = new JPopupMenu();
    private JButton addSleeveButton = new JButton("Ajouter poche");
    private JButton addFolderButton = new JButton("Ajouter noeud");
    private JButton deleteButton = new JButton("Supprimer");

    public AxisTreeToolBar() {
        buildAndAddItems();
    }

    private void buildAndAddItems() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setFloatable(false);

        addSleeveButton.setEnabled(false);
        addSleeveButton.setName("addSleeveNode");
        addFolderButton.setEnabled(false);
        addFolderButton.setName("addFolderNode");
        deleteButton.setName("deleteNode");
        deleteButton.setEnabled(false);

        add(addSleeveButton);
        add(addFolderButton);
        add(deleteButton);
        doEffect(this);
    }


    public static void doEffect(JToolBar tb) {
        Component[] components = tb.getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                ((JButton)component).setMargin(new Insets(1, 3, 1, 3));
            }
        }
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Object object : allActions.values()) {
            Action action = (Action)object;
            action.setEnabled(enabled);
        }
    }


    public void init() {
        removeAll();
        allActions.clear();
        popupMenu.removeAll();
    }


    public JButton getAddSleeveButton() {
        return addSleeveButton;
    }


    public JButton getAddFolderButton() {
        return addFolderButton;
    }


    public JButton getDeleteButton() {
        return deleteButton;
    }


    public void addSleeveActionListener(AxisTreeToolBarLogic.AddSleeveAction listener) {
        addSleeveButton.addActionListener(listener);
    }


    public void addFolderActionListener(AxisTreeToolBarLogic.AddFolderAction listener) {
        addFolderButton.addActionListener(listener);
    }


    public void addDeleteActionListener(AxisTreeToolBarLogic.DeleteAction listener) {
        deleteButton.addActionListener(listener);
    }
}
