/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.segmentation.gui.editor.EditorEventManager;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class SleeveTreePanelGui extends JPanel {

    public SleeveTreePanelGui(GuiContext guiContext,
                              EditorEventManager editorEventManager, String editingSleeveCode,
                              ListDataSource sonDataSource, DetailDataSource fatherDataSource) {

        ClassificationsEditorTreeModel model = new ClassificationsEditorTreeModel();
        try {
            model.init(editingSleeveCode, sonDataSource, fatherDataSource);
        }
        catch (Exception exception) {
            ErrorDialog.show(guiContext.getMainFrame(), "impossible de charger les axes", exception);
        }

        editorEventManager.addMapToBeReplaced(model.getToBeReplaced());
        editorEventManager.addStringsStyle(createSleeveStyle(), model.getSleeveListForStyle());

        JTree tree = new JTree();
        tree.setName("editor.sleeveTree");
        tree.setCellRenderer(new SleeveTreeCellRenderer());
        tree.setModel(model);
        tree.addMouseListener(new SleeveTerminalElementListener(editorEventManager));

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(tree), BorderLayout.CENTER);
    }


    private SimpleAttributeSet createSleeveStyle() {
        SimpleAttributeSet sleeveStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(sleeveStyle, "Monospaced");
        StyleConstants.setFontSize(sleeveStyle, 12);
        StyleConstants.setItalic(sleeveStyle, false);
        StyleConstants.setBold(sleeveStyle, true);
        StyleConstants.setForeground(sleeveStyle, new Color(0, 102, 102));
        StyleConstants.setBackground(sleeveStyle, new Color(204, 255, 204));
        return sleeveStyle;
    }
}
