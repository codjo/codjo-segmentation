/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.expression.help.FunctionHelp;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.variable.basic.BasicVariableReplacer;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.apache.log4j.Logger;
/**
 * La partie logique du main panel. prends en entrée la liste des fields et la liste des fontions. La listes
 * des opération est codé en dur !
 *
 * @author Lajmi
 */
class EditorMainPanelLogic {
    private static final Logger APP = Logger.getLogger(EditorMainPanelLogic.class);
    private EditorMainPanelGui editorMainPanelGui = new EditorMainPanelGui();
    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    private UndoManager undoManager = new UndoManager();
    private AttributeSet defaultAttributeSet = new SimpleAttributeSet();
    private HighlightingStyledDocument document = new HighlightingStyledDocument();
    private JPopupMenu popupMenu = new JPopupMenu();
    private OperatorHelper operators = new OperatorHelper();
    private JListMouseListener jListMouseListener = new JListMouseListener();
    private JListKeyListener jListKeyListener = new JListKeyListener();

    private WaitingPanel waitingPanel;
    private HelpStyle helpStyle;

    private List<Map> listOfMaptoReplaced = new ArrayList<Map>();
    private Map fieldMap;
    private Object[] fieldsDataList;

    private List<FunctionHelp> functionsList;
    private Object[] functionDataList;


    /**
     * Constructeur de base
     *
     * @param fieldMap      la Map des Fields : Key : TABLE$COL (<b>String</b>), value : LogicalName
     * @param functionsList la liste des fontions <b>FunctionHelp</b>
     */
    EditorMainPanelLogic(Map fieldMap, List<FunctionHelp> functionsList, WaitingPanel waitingPanel) {
        this.functionsList = functionsList;
        this.functionDataList = functionsList.toArray();

        this.fieldMap = fieldMap;
        this.fieldsDataList = fieldMap.values().toArray();
        Arrays.sort(this.fieldsDataList);

        this.waitingPanel = waitingPanel;

        initialiseDataListsItems();
        initialiseEditorAndHelpStyles();
        initialiseUndoRedoButtons();
        initialiseListeners();
    }


    private void initialiseEditorAndHelpStyles() {
        listOfMaptoReplaced.add(fieldMap);
        document.setColumns(fieldMap.values());
        document.setFunctions(functionsList);
        document.setOperations(operators.getAllOperators());
        editorMainPanelGui.getExpressionTextPane().setStyledDocument(document);
        helpStyle = new HelpStyle(editorMainPanelGui, functionsList, operators.getAllOperators());
    }


    private void initialiseUndoRedoButtons() {
        JButton undoButton = editorMainPanelGui.getUndoButton();
        JButton redoButton = editorMainPanelGui.getRedoButton();
        undoButton.setAction(undoAction);
        redoButton.setAction(redoAction);
    }


    private void initialiseDataListsItems() {
        editorMainPanelGui.getFunctionJList().setListData(functionDataList);
        editorMainPanelGui.getFunctionJList().setCellRenderer(new FunctionListRenderer());
        editorMainPanelGui.getFamilyFieldJList().setListData(fieldsDataList);
        editorMainPanelGui.getOperationsJList().setListData(operators.getAllOperators().toArray());
    }


    private void initialiseListeners() {
        // Ajout du listner des listes
        jListMouseListener = new JListMouseListener();
        jListKeyListener = new JListKeyListener();

        editorMainPanelGui.getFunctionJList().addMouseListener(jListMouseListener);
        editorMainPanelGui.getFamilyFieldJList().addMouseListener(jListMouseListener);
        editorMainPanelGui.getFamilyFieldJList().addMouseListener(new FieldListRightClickMouseListener());
        editorMainPanelGui.getOperationsJList().addMouseListener(jListMouseListener);

        MyListSelectionListener selectionListener = new MyListSelectionListener();
        editorMainPanelGui.getFunctionJList().addListSelectionListener(selectionListener);
        editorMainPanelGui.getFamilyFieldJList().addListSelectionListener(selectionListener);
        editorMainPanelGui.getOperationsJList().addListSelectionListener(selectionListener);

        editorMainPanelGui.getFunctionJList().addKeyListener(jListKeyListener);
        editorMainPanelGui.getFamilyFieldJList().addKeyListener(jListKeyListener);
        editorMainPanelGui.getOperationsJList().addKeyListener(jListKeyListener);

        // ajout du listener de Undo et redo
        editorMainPanelGui.getExpressionTextPane().getStyledDocument()
              .addUndoableEditListener(new ExpressionUndoableEditListener());
        editorMainPanelGui.getExpressionTextPane().addKeyListener(new TextPaneKeyListener());
    }


    public void addStringsStyle(StringsStyle stringsStyle) {
        document.addStringsStyle(stringsStyle);
    }


    MouseListener getjListMouseListener() {
        return jListMouseListener;
    }


    public Map getFieldMap() {
        return fieldMap;
    }


    /**
     * le getter de l'interface graphique.
     *
     * @return le panel à placer dans l'IHM
     */
    public EditorMainPanelGui getEditorMainPanelGui() {
        return editorMainPanelGui;
    }


    /**
     * use this method to initalize the TextPane with a text
     *
     * @param expression chaine de caractères
     */
    public void setExpression(String expression) {
        for (Object aListOfMaptoReplaced : listOfMaptoReplaced) {
            Map map = (Map)aListOfMaptoReplaced;
            expression = BasicVariableReplacer.replaceKeysPerValues(expression, map);
        }
        getEditorMainPanelGui().getExpressionTextPane().setText(expression);
    }


    /**
     * the method replace the columns logical names by the SRC_TAB$COL (for example)
     *
     * @return the edited Expression
     */
    public String getExpression() {
        Document doc = getEditorMainPanelGui().getExpressionTextPane().getDocument();
        String expressionLogic;
        try {
            expressionLogic = doc.getText(0, doc.getEndPosition().getOffset() - 1);
            for (Object aListOfMaptoReplaced : listOfMaptoReplaced) {
                Map map = (Map)aListOfMaptoReplaced;
                expressionLogic =
                      BasicVariableReplacer.replaceValuesPerKeys(expressionLogic, map);
            }
        }
        catch (BadLocationException ex) {
            expressionLogic = "ERROR";
        }

        return expressionLogic;
    }


    /**
     * determine si le caret position est dans des parentheses ou non.
     *
     * @param text          le text
     * @param caretPosition ou est le curseur
     *
     * @return true si le caret est apres (  et false si on est apres ) ou rien
     */
    boolean isInParentheses(String text, int caretPosition) {
        String part = text.substring(0, caretPosition);
        int indexOpen = part.lastIndexOf("(");
        int indexClose = part.lastIndexOf(")");
        return indexOpen > indexClose;
    }


    public void addExternalPanel(JPanel panel) {
        editorMainPanelGui.addExternalPanel(panel);
    }


    public void addMapToBeReplaced(Map toBeReplaced) {
        listOfMaptoReplaced.add(toBeReplaced);
    }


    public void addLeftPanel(JPanel leftPanel) {
        editorMainPanelGui.addLeftPanel(leftPanel);
    }


    void insertText(String text) {
        try {
            JTextPane expression = editorMainPanelGui.getExpressionTextPane();
            expression.getDocument().insertString(expression.getCaretPosition(), text,
                                                  defaultAttributeSet);
        }
        catch (Exception ex) {
            APP.error(ex.getMessage(), ex);
        }
    }


    public void initFieldListPopupMenu(ValueListPanel listPanel) {
        JMenuItem item = new JMenuItem("Afficher les valeurs");
        item.addActionListener(new ColumnsListListener(listPanel, fieldMap,
                                                       this.getEditorMainPanelGui().getFamilyFieldJList(),
                                                       waitingPanel));
        popupMenu.add(item);
    }


    private class MyListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            Object obj = ((JList)event.getSource()).getSelectedValue();
            String help = "";
            if (obj instanceof FunctionHelp) {
                help = ((FunctionHelp)obj).getHelp();
            }
            else if (obj instanceof OperatorHelper.OperatorHelp) {
                help = ((OperatorHelper.OperatorHelp)obj).getHelp();
            }
            try {
                editorMainPanelGui.setLogText(help);
                helpStyle.colorLog(help);
            }
            catch (BadLocationException ex) {
                APP.error(ex.getMessage(), ex);
            }
        }
    }

    // listener sur les listes pour inserer l'élément selectionné dans la position
    // du curseur dans le text
    private class JListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent event) {
            Object obj = ((JList)event.getSource()).getSelectedValue();
            String value;
            if (obj instanceof FunctionHelp) {
                value = ((FunctionHelp)obj).getFunctionName();
            }
            else {
                value = obj.toString();
            }

            if (event.getClickCount() == 2) {
                JTextPane expression = editorMainPanelGui.getExpressionTextPane();
                try {
                    expression.getDocument().insertString(expression.getCaretPosition(),
                                                          value, defaultAttributeSet);
                }
                catch (Exception ex) {
                    APP.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private class FieldListRightClickMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }


        @Override
        public void mouseReleased(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    private class JListKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent event) {
            if (event.getKeyChar() == KeyEvent.VK_ENTER) {
                Object obj = ((JList)event.getSource()).getSelectedValue();
                String value;
                if (obj instanceof FunctionHelp) {
                    value = ((FunctionHelp)obj).getFunctionName();
                }
                else {
                    value = obj.toString();
                }
                JTextPane expression = editorMainPanelGui.getExpressionTextPane();
                try {
                    expression.getDocument().insertString(expression.getCaretPosition(),
                                                          value, defaultAttributeSet);
                }
                catch (Exception ex) {
                    APP.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private class ExpressionJPopupMenu extends JDialog {
        ExpressionJPopupMenu(int posX, int posY, boolean isFunction) {
            JScrollPane scrollPane;
            JList workingJList = new JList();
            if (isFunction) {
                workingJList.setListData(fieldsDataList);
                workingJList.setBackground(new Color(204, 255, 255));
            }
            else {
                workingJList.setListData(functionDataList);
                workingJList.setBackground(new Color(204, 255, 204));
            }

            workingJList.setSelectedIndex(0);
            workingJList.setVisibleRowCount(5);
            scrollPane = new JScrollPane(workingJList);
            scrollPane.setBorder(null);

            // comme les autres listes il inserent dans le text
            workingJList.addMouseListener(jListMouseListener);
            workingJList.addKeyListener(jListKeyListener);

            this.setModal(true);
            this.setLocationRelativeTo(editorMainPanelGui);
            this.setResizable(false);
            this.addKeyListener(new MyKeyAdapter(this));

            this.getContentPane().add(scrollPane);
            this.pack();
            this.setLocation(posX, posY);
            this.setVisible(true);
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        private JDialog popupMenu;

        MyKeyAdapter(JDialog popupMenu) {

            this.popupMenu = popupMenu;
        }

        @Override
        public void keyPressed(KeyEvent event) {
            popupMenu.dispose();
        }
    }

    private class TextPaneKeyListener extends KeyAdapter {
        private ExpressionJPopupMenu expop = null;


        @Override
        public void keyTyped(KeyEvent event) {
            if (event.isControlDown() && event.getKeyChar() == KeyEvent.VK_SPACE) {
                int textX =
                      (int)editorMainPanelGui.getExpressionTextPane().getLocationOnScreen()
                            .getX();
                int textY =
                      (int)editorMainPanelGui.getExpressionTextPane().getLocationOnScreen()
                            .getY();
                int posX =
                      textX
                      + (int)editorMainPanelGui.getExpressionTextPane().getCaret()
                            .getMagicCaretPosition().getX();
                int posY =
                      15 + textY
                      + (int)editorMainPanelGui.getExpressionTextPane().getCaret()
                            .getMagicCaretPosition().getY();
                boolean inParentheses =
                      isInParentheses(editorMainPanelGui.getExpressionTextPane().getText(),
                                      editorMainPanelGui.getExpressionTextPane().getCaretPosition());
                expop = new ExpressionJPopupMenu(posX, posY, inParentheses);
            }
        }


        @Override
        public void keyPressed(KeyEvent event) {
            if (event.getKeyChar() == KeyEvent.VK_ESCAPE) {
                if (expop != null) {
                    expop.setVisible(false);
                }
            }
        }
    }

    //Listener des action sur le document text
    private class ExpressionUndoableEditListener implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent event) {
            if (!"modification de style".equalsIgnoreCase(
                  event.getEdit().getPresentationName())) {
                undoManager.addEdit(event.getEdit());
                undoAction.updateUndoState();
                redoAction.updateRedoState();
            }
        }
    }

    // Action Undo
    class UndoAction extends AbstractAction {
        UndoAction() {
            super("", UIManager.getIcon("undo"));
            setEnabled(false);
        }


        public void actionPerformed(ActionEvent event) {
            try {
                undoManager.undo();
            }
            catch (CannotUndoException ex) {
                ; // it is not possible to undo this operation
            }
            updateUndoState();
            redoAction.updateRedoState();
        }


        protected void updateUndoState() {
            if (undoManager.canUndo()) {
                setEnabled(true);
                putValue(Action.SHORT_DESCRIPTION, undoManager.getUndoPresentationName());
            }
            else {
                setEnabled(false);
                putValue(Action.SHORT_DESCRIPTION, "Undo");
            }
        }
    }

    // Redo Action
    class RedoAction extends AbstractAction {
        RedoAction() {
            super("", UIManager.getIcon("redo"));
            setEnabled(false);
        }


        public void actionPerformed(ActionEvent event) {
            try {
                undoManager.redo();
            }
            catch (CannotRedoException ex) {
                ; //it is not possible to redo this operation
            }
            updateRedoState();
            undoAction.updateUndoState();
        }


        protected void updateRedoState() {
            if (undoManager.canRedo()) {
                setEnabled(true);
                putValue(Action.SHORT_DESCRIPTION, undoManager.getRedoPresentationName());
            }
            else {
                setEnabled(false);
                putValue(Action.SHORT_DESCRIPTION, "Redo");
            }
        }
    }

    class FunctionListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            this.setText(((FunctionHelp)value).getFunctionName());
            return this;
        }
    }
}
