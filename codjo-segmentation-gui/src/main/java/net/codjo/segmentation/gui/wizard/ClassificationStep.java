/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.wizard;
import net.codjo.gui.toolkit.LabelledItemPanel;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.gui.toolkit.wizard.StepPanel;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.request.RequestTable;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/**
 * Step d'initialisation du wizard de lancement des calculs.
 */
public class ClassificationStep extends StepPanel {
    public static final String ASSET_CL_LIST_KEY = "assetClassificationList";
    private RequestTable classificationTable = new RequestTable();
    private CheckFulfilledListener fulfilledListener = new CheckFulfilledListener();
    private final String preferenceId;
    final LabelledItemPanel mainPanel = new LabelledItemPanel();
    private Collection<ClassificationWizardWindow.ComponentValidator> validators
          = new ArrayList<ClassificationWizardWindow.ComponentValidator>();
    private JLabel infoField;


    public ClassificationStep(String preferenceId) {
        this.preferenceId = preferenceId;
        setName("Selection des axes à classer :");

        jbInit();

        setValue(ASSET_CL_LIST_KEY, "");

        addListSelectionListener(fulfilledListener);
    }


    public void addCustomField(String label,
                               JTextField newComponent,
                               ClassificationWizardWindow.ComponentValidator updater) {
        mainPanel.addItem(label, newComponent);
        validators.add(updater);
        newComponent.getDocument().addDocumentListener(fulfilledListener);
    }


    public void addListSelectionListener(ListSelectionListener listener) {
        classificationTable.getSelectionModel().addListSelectionListener(listener);
    }


    Row[] getClassificationTableSelectedRows() {
        return classificationTable.getAllSelectedDataRows();
    }


    @Override
    public void start(Map previousStepState) {
        initClassificationList();
    }


    private void initClassificationList() {
        classificationTable.setPreference(preferenceId);

        try {
            classificationTable.load();
        }
        catch (RequestException ex) {
            ErrorDialog.show(this, "Impossible de charger la liste des axes.", ex);
        }
    }


    public void checkFulfilled() {
        for (ClassificationWizardWindow.ComponentValidator validator : validators) {
            if (!validator.isComponentValid()) {
                setFulfilled(false);
                return;
            }
        }
        setFulfilled(classificationTable.getSelectedRowCount() > 0);
    }


    private void updateAssetClassificationList() {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (int index : classificationTable.getSelectedRows()) {
            if (first) {
                first = false;
            }
            else {
                buffer.append(", ");
            }
            buffer.append(classificationTable.getColumnValue(index, "classificationId"));
        }
        setValue(ASSET_CL_LIST_KEY, buffer.toString());
    }


    private void jbInit() {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(5);
        setLayout(borderLayout);
        setBorder(new EmptyBorder(0, 0, 5, 0));
        classificationTable.setName("AssistantSegmentation.ListeDesAxes");
        add(new JScrollPane(classificationTable), BorderLayout.CENTER);

        JScrollPane pane = new JScrollPane(mainPanel);
        pane.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(pane, BorderLayout.EAST);

        infoField = new JLabel();
        infoField.setName("Summary");
        add(infoField, BorderLayout.SOUTH);
    }


    public void setInfoField(String label) {
        infoField.setText(label);
    }


    public RequestTable getClassificationTable() {
        return classificationTable;
    }


    /**
     * Listener verifiant que les données sont remplies avant de lancer le traitement.
     */
    private class CheckFulfilledListener implements DocumentListener,
                                                    ListSelectionListener {
        public void insertUpdate(DocumentEvent event) {
            checkFulfilled();
        }


        public void removeUpdate(DocumentEvent event) {
            checkFulfilled();
        }


        public void changedUpdate(DocumentEvent event) {
            checkFulfilled();
        }


        public void valueChanged(ListSelectionEvent event) {
            updateAssetClassificationList();
            checkFulfilled();
        }
    }
}
