package net.codjo.segmentation.gui.importParam;
import net.codjo.gui.toolkit.path.FilePathField;
import net.codjo.gui.toolkit.swing.CheckBoxRenderer;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.mad.client.request.Field;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.Position;
import net.codjo.mad.gui.request.Preference;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.mad.gui.request.util.RequestTableRendererSorter;
import net.codjo.segmentation.gui.ImportExportProgresListener;
import net.codjo.util.string.StringUtil;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;

public class ImportParametersGui extends JInternalFrame implements ImportExportProgresListener {

    private JButton importButton;
    private JButton cancelButton;
    private FilePathField importFileName;
    private RequestToolBar requestToolbar;
    private JPanel mainPanel;
    private JRadioButton classificationRadio;
    private JRadioButton sleeveRadio;
    private JPanel cardPanel;
    private RequestTable axeImportAnomaly = new RequestTable(new NoLoadListDataSource());
    private RequestTable sleeveImportAnomaly = new RequestTable(new NoLoadListDataSource());
    private CardLayout cardLayout;
    private GuiContext guiContext;
    private WaitingPanel waitingPanel = new WaitingPanel();
    private JButton clearActionButton;


    public ImportParametersGui(GuiContext guiContext,
                               Preference classificationLogPreference,
                               Preference sleevePreference) {
        super("Import de paramétrage", true, true, true, true);
        this.guiContext = guiContext;
        initNames();
        initTables(classificationLogPreference, sleevePreference);
        initToolbar(axeImportAnomaly);
        initListeners();
        initCardLayout();
        importFileName.getFileNameField().setEditable(true);
        setContentPane(mainPanel);
        setGlassPane(waitingPanel);
        setPreferredSize(new Dimension(900, 600));
    }


    public void handleInform(String infoMessage) {
        ;
    }


    public void handleInform(String[][] quarantine) {
        getWaitingPanel().stopAnimation();

        if (quarantine == null || quarantine.length == 0) {
            return;
        }

        int rowCount = quarantine.length;
        int colCount = quarantine[0].length;

        ListDataSource dataSource;

        if (classificationRadio.isSelected()) {
            dataSource = axeImportAnomaly.getDataSource();
        }
        else {
            dataSource = sleeveImportAnomaly.getDataSource();
        }

        for (int row = 1; row < rowCount; row++) {
            List<Field> columns = initHeader(quarantine);
            Row currentRow = new Row();
            currentRow.setFields(columns);
            for (int col = 0; col < colCount; col++) {
                String name = columns.get(col).getName();
                String value = quarantine[row][col];
                if ("sleeveDustbin".equals(name) || "terminalElement".equals(name)) {
                    value = "1".equals(value) ? "true" : "false";
                }
                currentRow.setFieldValue(name, value);
            }

            dataSource.addRow(currentRow);
        }
        dataSource.cancelEditMode();

        if (sleeveRadio.isSelected()) {
            RequestTableRendererSorter model = (RequestTableRendererSorter)sleeveImportAnomaly.getModel();
            model.sortByColumn(0, false);
        }
    }


    public void handleError(String errorMessage) {
        getWaitingPanel().stopAnimation();
        ErrorDialog.show(this, errorMessage, errorMessage);
    }


    private void initNames() {
        importFileName.setName("AxeFileName");
        cancelButton.setName("cancelButton");
        importButton.setName("importButton");
        classificationRadio.setName("classificationRadio");
        sleeveRadio.setName("sleeveRadio");
    }


    private void initListeners() {
        importFileName.addPropertyChangeListener("fileName", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                importButton.setEnabled(!("".equals(evt.getNewValue())));
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        classificationRadio.addActionListener(new TableListenerEvent("axeImportAnomaly", axeImportAnomaly));
        sleeveRadio.addActionListener(new TableListenerEvent("sleeveImportAnomaly", sleeveImportAnomaly));

        ClearLogButtonUpdater actionUpdater = new ClearLogButtonUpdater();

        axeImportAnomaly.getDataSource().addPropertyChangeListener(actionUpdater);
        sleeveImportAnomaly.getDataSource().addPropertyChangeListener(actionUpdater);
    }


    private void initTables(Preference classificationPreference, Preference sleevePreference) {
        axeImportAnomaly.setPreference(classificationPreference);
        sleeveImportAnomaly.setPreference(sleevePreference);
        sleeveImportAnomaly.setCellRenderer("sleeveDustbin", new CheckBoxRenderer());
        sleeveImportAnomaly.setCellRenderer("terminalElement", new CheckBoxRenderer());
    }


    private void initCardLayout() {
        cardPanel.add("axeImportAnomaly", new JScrollPane(axeImportAnomaly));
        cardPanel.add("sleeveImportAnomaly", new JScrollPane(sleeveImportAnomaly));
        cardLayout = (CardLayout)cardPanel.getLayout();
        cardLayout.show(cardPanel, "axeImportAnomaly");
    }


    private void initToolbar(RequestTable table) {
        requestToolbar.setHasExcelButton(true);
        requestToolbar.setHasNavigatorButton(false);
        requestToolbar.setHasUndoRedoButtons(false);
        requestToolbar.setHasValidationButton(false);
        requestToolbar.init(guiContext, table);
        requestToolbar.removeAction(RequestToolBar.ACTION_ADD);
        requestToolbar.removeAction(RequestToolBar.ACTION_CLEAR);
        requestToolbar.removeAction(RequestToolBar.ACTION_DELETE);
        requestToolbar.removeAction(RequestToolBar.ACTION_EDIT);
        requestToolbar.removeAction(RequestToolBar.ACTION_LOAD);
        requestToolbar.removeAction(RequestToolBar.ACTION_NEXT_PAGE);
        requestToolbar.removeAction(RequestToolBar.ACTION_PREVIOUS_PAGE);
        requestToolbar.removeAction(RequestToolBar.ACTION_RELOAD);

        ClearLogAction clearLogAction = new ClearLogAction(guiContext, table);
        requestToolbar.add(clearLogAction, "clearLogAction", Position.last(), false);
        clearActionButton = requestToolbar.getButtonInToolBar(clearLogAction);
        clearActionButton.setEnabled(false);
    }


    private List<Field> initHeader(String[][] quarantine) {
        List<Field> columns = new ArrayList<Field>();

        for (int i = 0; i < quarantine[0].length; i++) {
            String columnName = StringUtil.sqlToJavaName(quarantine[0][i]);
            Field currentField = new Field(columnName, "null");
            columns.add(currentField);
        }
        return columns;
    }


    public void setImportButtonListener(ActionListener listener) {
        importButton.addActionListener(listener);
    }


    public FilePathField getImportFilePathField() {
        return importFileName;
    }


    public boolean isClassificationSelected() {
        return classificationRadio.isSelected();
    }


    public WaitingPanel getWaitingPanel() {
        return waitingPanel;
    }


    public RequestTable getAxeImportAnomaly() {
        return axeImportAnomaly;
    }


    public RequestTable getSleeveImportAnomaly() {
        return sleeveImportAnomaly;
    }


    private RequestTable getCurrentTable() {
        if (classificationRadio.isSelected()) {
            return axeImportAnomaly;
        }
        else {
            return sleeveImportAnomaly;
        }
    }


    private void updateButton() {
        RequestTable table = getCurrentTable();
        int rowCount = table.getDataSource().getRowCount();
        boolean enabled = rowCount > 0;
        clearActionButton.setEnabled(enabled);
    }


    public void setValidFileListener(DocumentListener documentListener) {
        importFileName.getFileNameField().getDocument().addDocumentListener(documentListener);
    }


    void setEnabledImportButton(boolean enabled) {
        importButton.setEnabled(enabled);
    }


    private class NoLoadListDataSource extends ListDataSource {

        @Override
        public void load() throws RequestException {
            ;
        }
    }

    private class ClearLogButtonUpdater implements PropertyChangeListener {
        @SuppressWarnings({"InnerClassTooDeeplyNested"})
        public void propertyChange(final PropertyChangeEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateButton();
                }
            });
        }
    }

    private class TableListenerEvent implements ActionListener {
        private String layoutId;
        private RequestTable table;


        private TableListenerEvent(String layoutId, RequestTable table) {
            this.layoutId = layoutId;
            this.table = table;
        }


        public void actionPerformed(ActionEvent event) {
            cardLayout.show(cardPanel, layoutId);
            initToolbar(table);
            updateButton();
            repaint();
        }
    }
}
