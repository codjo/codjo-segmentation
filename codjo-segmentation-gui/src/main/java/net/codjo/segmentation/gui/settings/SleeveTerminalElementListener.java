package net.codjo.segmentation.gui.settings;
import net.codjo.segmentation.gui.editor.EditorEventManager;
import net.codjo.variable.basic.BasicVariableReplacer;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

public class SleeveTerminalElementListener extends MouseAdapter {

    private EditorEventManager manager;
    private ClassificationsEditorTreeModel.Sleeve sleeve = null;
    private ClassificationsEditorTreeModel model = null;
    private TerminalElementContextualMenu popUpMenu;


    SleeveTerminalElementListener(EditorEventManager manager) {
        this.manager = manager;
    }


    @Override
    public void mouseClicked(MouseEvent event) {
        JTree tree = (JTree)event.getSource();
        model = (ClassificationsEditorTreeModel)tree.getModel();
        Object object = tree.getLastSelectedPathComponent();

        if (object instanceof ClassificationsEditorTreeModel.Sleeve) {
            sleeve = (ClassificationsEditorTreeModel.Sleeve)object;
            if (sleeve.isTerminalElement()) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    initPopUpMenu(tree, event);
                }
                else if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
                    insertAlias();
                }
            }
        }
    }


    public void insertAlias() {
        manager.insertText(" " + sleeve.getClassificationName() + "$" + sleeve.getSleeveName() + " ");
    }


    public void insertFormula() {
        String formula = sleeve.getFormula();
        String logicalName = model.getToBeReplaced().get(formula.trim());
        if (logicalName != null) {
            formula = " " + logicalName + " ";
        }
        else {
            formula = BasicVariableReplacer.replaceKeysPerValues(formula, manager.getFieldMap());
        }
        manager.insertText(formula);
    }


    private void initPopUpMenu(JTree tree, MouseEvent event) {
        if (popUpMenu == null) {
            popUpMenu = new TerminalElementContextualMenu();
            popUpMenu.setInvoker(tree);
        }
        Point invokerOrigin = tree.getLocationOnScreen();
        popUpMenu.setLocation(invokerOrigin.x + event.getX(), invokerOrigin.y + event.getY());
        popUpMenu.setVisible(true);
    }


    private class TerminalElementContextualMenu extends JPopupMenu {

        private JMenuItem aliasCopyItem = new JMenuItem("Copier alias");
        private JMenuItem formulaCopyItem = new JMenuItem("Copier formule");


        private TerminalElementContextualMenu() {
            add(aliasCopyItem);
            add(formulaCopyItem);

            aliasCopyItem.addActionListener(new AliasCopyActionListener());
            formulaCopyItem.addActionListener(new FormulaCopyActionListener());
        }
    }

    private class AliasCopyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            insertAlias();
        }
    }

    private class FormulaCopyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            insertFormula();
        }
    }
}
