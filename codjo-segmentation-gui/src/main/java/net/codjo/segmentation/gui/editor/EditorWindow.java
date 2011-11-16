/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.expression.help.FunctionHelp;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.RequestSender;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.ResultManager;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.client.request.SelectRequest;
import net.codjo.segmentation.gui.preference.PreferenceGui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
/**
 * Frame contenant le Panel d'edition d'expression.
 *
 * @author Palmont
 */
class EditorWindow extends JInternalFrame {
    private static final Logger LOG = Logger.getLogger(EditorWindow.class.getName());

    private BorderLayout borderLayout = new BorderLayout();
    private EditorMainPanelLogic editor = null;
    private JButton okButton;
    private JButton cancelButton;
    private final String classificationId;
    private final String sleeveRowId;
    private String oldExpression;


    EditorWindow(PreferenceGui preference, final SegmentationEditorAction.EditorManager editorManager,
                 String classificationId, String sleeveRowId) {
        this.classificationId = classificationId;
        this.sleeveRowId = sleeveRowId;

        // Liste des colonnes
        Map<String, String> fieldsMap = getColumns(preference);

        // Liste des fonctions
        List<FunctionHelp> functionsList = preference.getAllFunctionsHelp();

        setGlassPane(new WaitingPanel());
        editor = new EditorMainPanelLogic(fieldsMap, functionsList, (WaitingPanel)getGlassPane());
        editor.getEditorMainPanelGui().getExpressionTextPane().getDocument()
              .addDocumentListener(new ExpressionListener());

        jbInit();

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                boolean mustModifyFormula = isFormulaIsUsedByIncludes();
                if (mustModifyFormula && editorManager.editorOk(getExpression())) {
                    closeFrame();
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                closeFrame();
            }
        });
    }


    private boolean isFormulaIsUsedByIncludes() {
        try {
            ResultManager resultManager = findIncludes();
            Collection results = resultManager.getResults();
            if (results == null) {
                return true;
            }
            for (Object result : results) {
                List<Row> list = ((Result)result).getRows();
                if (list == null) {
                    return true;
                }
                StringBuffer includesInfo = new StringBuffer();
                for (Row row : list) {
                    String classificationName = row.getFieldValue("classificationName");
                    String sleeveName = row.getFieldValue("sleeveName");
                    if (isCycleExists(row, sleeveName, classificationName)) {
                        return false;
                    }
                    includesInfo.append("'").append(sleeveName).append("' de l'axe '")
                          .append(classificationName).append("'\n");
                }
                int answer =
                      JOptionPane.showConfirmDialog(this,
                                                    "Cette poche est utilisée dans les poches :\n"
                                                    + includesInfo
                                                    + "Etes-vous certain de vouloir la modifier ?",
                                                    "Confirmation", JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
        }
        catch (RequestException e) {
            LOG.error("Erreur lors du contrôle des alias", e);
        }

        return true;
    }


    private boolean isCycleExists(Row row, String sleeveName, String classificationName) {
        String includeStringForCurrentSleeve = "INC_$$" + row.getFieldValue("classificationId") + "$" + row
              .getFieldValue("sleeveRowId");
        String includeStringForAliasSleeve = "INC_$$" + classificationId + "$" + sleeveRowId;

        if (getExpression().contains(includeStringForCurrentSleeve)
            && row.getFieldValue("formula").contains(includeStringForAliasSleeve)) {
            JOptionPane.showMessageDialog(this,
                                          "Vous venez de créer une référence cyclique avec la poche '"
                                          + sleeveName + "' de l'axe '" + classificationName
                                          + "'.",
                                          "Erreur",
                                          JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }


    private ResultManager findIncludes() throws RequestException {
        SelectRequest selectAlias = new SelectRequest();
        selectAlias.setPage(1, Integer.MAX_VALUE);
        selectAlias.setId("selectAliasSleeves");

        FieldsList selector = new FieldsList();
        selector.addField("classificationId", classificationId);
        selector.addField("sleeveRowId", sleeveRowId);
        selectAlias.setSelector(selector);
        RequestSender requestHelper = new RequestSender();
        return requestHelper.send(selectAlias);
    }


    private void closeFrame() {
        try {
            setClosed(true);
        }
        catch (PropertyVetoException e) {
            ;
        }
        setVisible(false);
        dispose();
    }


    public void setExpression(String expression) {
        oldExpression = expression;
        editor.setExpression(expression);
    }


    public EditorMainPanelLogic getEditorMainPanelLogic() {
        return editor;
    }


    private Map<String, String> getColumns(PreferenceGui preference) {
        Map<String, String> fieldsMap = new HashMap<String, String>();
        List<Object> tablesList = new ArrayList<Object>();
        tablesList.addAll(preference.getTables());

        for (Object aTablesList : tablesList) {
            String tableName = (String)aTablesList;
            fieldsMap = fillFieldsMap(preference, fieldsMap, tableName);
        }
        return fieldsMap;
    }


    private Map<String, String> fillFieldsMap(PreferenceGui preference, Map<String, String> fieldsMap2,
                                              String tableName) {
        for (Object columns : preference.getColumnsFor(tableName)) {
            String fieldSql = (String)columns;
            String label = preference.getColumnLabelFor(tableName, fieldSql);
            if (!PreferenceGui.VAR_TABLE.equals(tableName)) {
                fieldSql = new StringBuilder("SRC_").append(tableName).append("$").append(fieldSql)
                      .toString();
            }

            fieldsMap2.put(fieldSql, label);
        }
        return fieldsMap2;
    }


    private void jbInit() {
        // Fix pour l'héritage de la couleur de fond
        setContentPane(new JPanel());
        setSize(1000, 600);
        setLocation(100, 100);
        setClosable(false);
        setResizable(true);
        setTitle("Paramétrage des expressions");
        setVisible(true);
        getContentPane().setLayout(borderLayout);
        getContentPane().add(editor.getEditorMainPanelGui(), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("Valider");
        okButton.setName("editor.okButton");
        okButton.setEnabled(false);
        cancelButton = new JButton("Annuler");
        cancelButton.setName("editor.cancelButton");
        buttonPanel.add(okButton, 0);
        buttonPanel.add(cancelButton, 1);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }


    public String getExpression() {
        return editor.getExpression();
    }


    /**
     * Listener chargé d'activer le bouton valider si l'expression change et de le désactiver sinon.
     */
    private class ExpressionListener implements DocumentListener {

        public void insertUpdate(DocumentEvent event) {
            enableOkButton();
        }


        public void removeUpdate(DocumentEvent event) {
            enableOkButton();
        }


        public void changedUpdate(DocumentEvent event) {
            enableOkButton();
        }


        private void enableOkButton() {
            String expression = getExpression();
            if (expression.equals(oldExpression)) {
                okButton.setEnabled(false);
            }
            else {
                okButton.setEnabled(true);
            }
        }
    }
}
