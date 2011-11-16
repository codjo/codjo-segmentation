/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.gui.toolkit.syntax.SyntaxControlPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
/**
 * Le Panel qu'il faut Utiliser pour l'édition des Expression.
 *
 * @author Lajmi
 */
class EditorMainPanelGui extends JSplitPane {
    private JList functionList;
    private JList operationsList;
    private JList familyFieldList;
    private JTextPane expressionTextPane;
    private JTextPane logTextArea;
    private JButton undoButton;
    private JButton redoButton;
    private JTabbedPane tabbedPane;
    /**
     * Split pane qui contient les colonnes (en haut) et les valeurs (en bas).
     */
    private JSplitPane splitPane;


    EditorMainPanelGui() {
        super(JSplitPane.HORIZONTAL_SPLIT);
        jbInit();
    }


    public JList getFunctionJList() {
        return functionList;
    }


    public JList getOperationsJList() {
        return operationsList;
    }


    public JList getFamilyFieldJList() {
        return familyFieldList;
    }


    public JTextPane getExpressionTextPane() {
        return expressionTextPane;
    }


    public JButton getUndoButton() {
        return undoButton;
    }


    public JButton getRedoButton() {
        return redoButton;
    }


    public StyledDocument getLogStyledDocument() {
        return logTextArea.getStyledDocument();
    }


    public Style addLogStyle(String name, Style parent) {
        return logTextArea.addStyle(name, parent);
    }


    public void setLogText(String logText) {
        logTextArea.setText(logText);
        logTextArea.setCaretPosition(0);
    }


    public Document getLogDocument() {
        return logTextArea.getDocument();
    }


    public Style getLogStyle(String styleName) {
        return logTextArea.getStyle(styleName);
    }


    private void jbInit() {
        undoButton = new JButton(UIManager.getIcon("undo"));
        redoButton = new JButton(UIManager.getIcon("redo"));
        undoButton.setToolTipText("Undo");
        redoButton.setToolTipText("Redo");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(undoButton);
        buttonPanel.add(redoButton);

        // Initialize
        functionList = new JList();
        operationsList = new JList();
        familyFieldList = new JList();

        functionList.setName("editor.functionList");
        operationsList.setName("editor.operationList");
        familyFieldList.setName("editor.columnList");

        functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        operationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        familyFieldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        functionList.setDragEnabled(true);
        operationsList.setDragEnabled(true);
        familyFieldList.setDragEnabled(true);

        JScrollPane functionScrollPane = new JScrollPane(functionList);
        JScrollPane operationsScrollPane = new JScrollPane(operationsList);
        JScrollPane familyFieldScrollPane = new JScrollPane(familyFieldList);

        functionList.setBorder(BorderFactory.createEtchedBorder());
        operationsList.setBorder(BorderFactory.createEtchedBorder());
        familyFieldList.setBorder(BorderFactory.createEtchedBorder());

        expressionTextPane = new JTextPane();
        expressionTextPane.setName("editor.expression");
        logTextArea = new JTextPane();
        logTextArea.setName("editor.usage");
        logTextArea.setEditable(false);

        JScrollPane expressionScrollPane = new JScrollPane(expressionTextPane);
        expressionScrollPane.setPreferredSize(new Dimension(500, 220));

        // Create a panel for the Syntax Control
        SyntaxControlPanel syntaxControlPanel = new SyntaxControlPanel();
        syntaxControlPanel.setExpressionTextComponent(expressionTextPane);

        ///////////////////////////////////////////////////////////////////////
        // Construction des panels d'assemblage
        ///////////////////////////////////////////////////////////////////////
        JPanel textPanel = new JPanel(new BorderLayout());

        textPanel.add(expressionScrollPane, BorderLayout.CENTER);
        textPanel.add(buttonPanel, BorderLayout.NORTH);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, familyFieldScrollPane, null);
        // Construction d'un table pane qui sera affiché du côté gauche de la fenêtre.
        tabbedPane = new JTabbedPane();
        tabbedPane.setName("editor.helperTabs");

        tabbedPane.add("Fonctions", functionScrollPane);
        tabbedPane.add("Opérateurs", operationsScrollPane);
        tabbedPane.add("Colonnes et valeurs", splitPane);
        setLeftComponent(tabbedPane);

        // Ce splitPane apparaitra sous expression
        JSplitPane syntaxLogSplitPane =
              new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, syntaxControlPanel, logTextArea);

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textPanel, syntaxLogSplitPane);

        rightSplitPane.setResizeWeight(1.0);

        setRightComponent(rightSplitPane);
    }


    public void addExternalPanel(JPanel panel) {
        splitPane.setRightComponent(panel);
    }


    public void addLeftPanel(JPanel leftPanel) {
        tabbedPane.add("Axes", leftPanel);
    }
}
