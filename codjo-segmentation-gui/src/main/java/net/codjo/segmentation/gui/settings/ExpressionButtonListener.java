/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.CommandRequest;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.segmentation.gui.editor.EditorEventManager;
import net.codjo.segmentation.gui.editor.SegmentationEditorAction;
import net.codjo.segmentation.gui.editor.ValueListPanel;
import net.codjo.segmentation.gui.editor.ValuesListInitialiserListener;
import net.codjo.segmentation.gui.preference.PreferenceGui;
import net.codjo.segmentation.gui.preference.PreferenceGuiManager;
import net.codjo.xml.XmlException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JPanel;
/**
 * Listener chargé de lancer l'éditeur d'expression et de mettre à jour les données dans le DetailDataSource
 */
public class ExpressionButtonListener implements ActionListener,
                                                 SegmentationEditorAction.EditorManager {
    private final DetailDataSource dataSource;
    private final ListDataSource axisSleevesDataSource;
    private final DetailDataSource selectedSleeveDataSource;
    private final PreferenceGuiManager prefManager;
    private final ClassificationStructureGui gui;
    private SegmentationEditorAction editorAction;


    public ExpressionButtonListener(DetailDataSource dataSource,
                                    ListDataSource axisSleevesDataSource,
                                    DetailDataSource selectedSleeveDataSource,
                                    PreferenceGuiManager prefManager, ClassificationStructureGui gui)
          throws IOException, XmlException, RequestException {
        this.dataSource = dataSource;
        this.axisSleevesDataSource = axisSleevesDataSource;
        this.selectedSleeveDataSource = selectedSleeveDataSource;
        this.prefManager = prefManager;
        this.gui = gui;
        editorAction = new SegmentationEditorAction(dataSource.getGuiContext());
    }


    public void actionPerformed(ActionEvent event) {
        editorAction.setEditorManager(this);
        editorAction.actionPerformed(event);
    }


    public SegmentationEditorAction.EditorConfig editorStart() {
        String family = dataSource.getFieldValue("classificationType");
        PreferenceGui preferenceGui = prefManager.getPreference(family);
        return new SegmentationEditorAction.EditorConfig(preferenceGui, gui.getFormulaText(), gui);
    }


    public JPanel getPanel(EditorEventManager editorEventManager) {
        ValueListPanel valueListPanel = new ValueListPanel();
        valueListPanel.getList().addMouseListener(editorEventManager.getjListMouseListener());
        ValuesListInitialiserListener listener = new ValuesListInitialiserListener(valueListPanel);
        editorEventManager.addColumnsListListener(listener);
        editorEventManager.initFieldListPopupMenu(valueListPanel);
        return valueListPanel;
    }


    public JPanel getLeftPanel(EditorEventManager editorEventManager) {
        String editingSleeveCode = selectedSleeveDataSource.getFieldValue(("sleeveCode"));

        return new SleeveTreePanelGui(dataSource.getGuiContext(), editorEventManager,
                                      editingSleeveCode, axisSleevesDataSource, dataSource);
    }


    public boolean editorOk(String newExpression) {
        try {
            FieldsList fieldsList = new FieldsList();
            fieldsList.addField("expression", newExpression);
            fieldsList.addField("familyId", dataSource.getFieldValue("classificationType"));
            dataSource.getGuiContext().getSender().send(new CommandRequest("expressionCompiler", fieldsList));
        }
        catch (RequestException e) {
            ErrorDialog.show(gui, "Erreur lors de la validation", e.getLocalizedMessage(), e);
            return false;
        }
        gui.enableFormula(true);
        selectedSleeveDataSource.setFieldValue("formula", newExpression);
        return true;
    }


    public void setSelectedSleeveRowId(String selectedSleeveRowId) {
        editorAction.setSelectedSleeveRowId(selectedSleeveRowId);
    }


    public void setSelectedClassificationId(String selectedClassificationId) {
        editorAction.setSelectedClassificationId(selectedClassificationId);
    }
}
