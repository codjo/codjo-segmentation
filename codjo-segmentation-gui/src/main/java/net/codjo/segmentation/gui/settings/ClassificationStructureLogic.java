/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.DataLink;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.JoinKeys;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.ErrorHandler;
import net.codjo.mad.gui.request.event.DataSourceAdapter;
import net.codjo.mad.gui.request.event.DataSourceEvent;
import net.codjo.mad.gui.request.util.ButtonPanelLogic;
import net.codjo.mad.gui.request.util.ProxyErrorHandler;
import net.codjo.mad.gui.structure.StructureCache;
import net.codjo.segmentation.gui.plugin.SegmentationGuiPlugin;
import net.codjo.segmentation.gui.preference.PreferenceGui;
import net.codjo.segmentation.gui.preference.PreferenceGuiManager;
import net.codjo.segmentation.gui.preference.PreferenceGuiManagerFactory;
import net.codjo.xml.XmlException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
/**
 * Logique de la fenêtre de paramétrage d'un axe.
 */
public class ClassificationStructureLogic {
    private static final Logger LOG = Logger.getLogger(ClassificationStructureLogic.class.getName());
    private static final String DELETE_CLASS_STRUCTURE_HANDLER = "deleteClassificationStructure";
    private static final String NEW_CLASSI_STRUCTURE_HANDLER = "newClassificationStructure";
    private String updateClassStructureHandler = "updateClassificationStructure";
    private String selectClassStrucByClassIdHandler = "selectClassificationStructureByClassId";
    private ClassificationStructureGui gui;
    private DetailDataSource dataSource;
    private ListDataSource axisSleevesDataSource;
    private DetailDataSource selectedSleeveDataSource;
    private AxisTreeLogic axisTreeLogic;
    private ButtonPanelLogic buttonPanelLogic;
    private AxisTreeToolBarLogic axisTreeToolBarLogic;
    private PreferenceGuiManager prefManager;
    private List<String> additionalAxisSleevesColumns = new ArrayList<String>();
    private ExpressionButtonListener expressionButtonListener;
    private DataLink axisSleevesLink;


    public ClassificationStructureLogic(DetailDataSource dataSource) throws Exception {
        this(dataSource, new ClassificationStructureGui("Axe d'analyse"));
    }


    public void setAdditionalAxisSleevesColumns(List<String> additionalAxisSleevesColumns) {
        this.additionalAxisSleevesColumns = additionalAxisSleevesColumns;
    }


    public void addAdditionalAxisSleevesColumn(String columnName) {
        additionalAxisSleevesColumns.add(columnName);
    }


    public ClassificationStructureLogic(DetailDataSource dataSource,
                                        ClassificationStructureGui gui) throws Exception {
        this.gui = gui;
        this.dataSource = dataSource;

        buttonPanelLogic = new ButtonPanelLogic(gui.getButtonPanelGui());
        axisTreeToolBarLogic = new AxisTreeToolBarLogic(gui.getAxisTreeToolBar());
        buttonPanelLogic.showUndoRedo(false);

        if (dataSource.getLoadResult() != null) {
            gui.getClassificationType().setEnabled(false);
        }

        initPreferenceGui(dataSource.getGuiContext());
        gui.setMaximumNodeDepth((Integer)dataSource.getGuiContext()
              .getProperty(SegmentationGuiPlugin.SEGMENTATION_AXIS_TREE_MAX_DEPTH));

        PreferenceGui[] allPreference = prefManager.getAllPreference();
        for (PreferenceGui anAllPreference : allPreference) {
            gui.getClassificationType().addItem(anAllPreference.getFamilyId());
        }

        SegmentationSettingsCustomizer customizer = (SegmentationSettingsCustomizer)dataSource.getGuiContext()
              .getProperty(SegmentationSettingsCustomizer.SEGMENTATION_SETTINGS_CUSTOMIZER);
        if (customizer != null) {
            customizer.doPreDataSourceInit(this, dataSource);
        }
        initLogic();
        if (customizer != null) {
            customizer.doPostDataSourceInit(this, dataSource);
        }
        //noinspection InstanceVariableUsedBeforeInitialized
        axisSleevesDataSource.addPropertyChangeListener(ListDataSource.SELECTED_ROW_PROPERTY,
                                                        new SleeveSelectionListener());
    }


    private void initPreferenceGui(GuiContext ctxt)
          throws IOException, XmlException, RequestException {
        StructureCache structureCache = (StructureCache)ctxt.getProperty(StructureCache.STRUCTURE_CACHE);
        StructureReader madReader = structureCache.getStructureReader();
        prefManager = new PreferenceGuiManagerFactory(madReader).getPreferenceGuiManager();
    }


    public void setUpdateClassificationStructureHandler(String updateClassStructureHandler) {
        this.updateClassStructureHandler = updateClassStructureHandler;
    }


    public void setSelectClassificationStructureByClassIdHandler(String selectClassStrucByClassIdHandler) {
        this.selectClassStrucByClassIdHandler = selectClassStrucByClassIdHandler;
    }


    protected void initLogic() throws Exception {
        selectedSleeveDataSource = new DetailDataSource(dataSource.getGuiContext());
        axisSleevesDataSource = buildAxisSleevesDataSource();
        dataSource.addDataSourceListener(new FatherDataSourceListener());

        // Initialisation du ListDataSource de l'arbre et liaison avec le datasource père.
        linkDataSources();

        declareFields();

        dataSource.load();

        axisTreeLogic =
              new AxisTreeLogic(this.dataSource, axisSleevesDataSource,
                                this.gui.getAxisTree());

        axisTreeToolBarLogic.init(axisTreeLogic);

        // On attache l'arbre à la fenêtre générale.
        // On crée les listeners
        declareSleeveField("sleeveName");
        gui.addSleeveDustbinListener(new DustbinListener());
        declareSleeveField("formula");

        dataSource.addPropertyChangeListener("classificationName",
                                             new ClassificationNameListener());

        expressionButtonListener = new ExpressionButtonListener(dataSource,
                                                                axisSleevesDataSource,
                                                                selectedSleeveDataSource,
                                                                prefManager,
                                                                gui);
        gui.addExpressionActionListener(expressionButtonListener);
    }


    private void declareFields() {
        dataSource.declare("classificationName", getGui().getClassificationName());
        dataSource.declare("classificationType", getGui().getClassificationType());
        dataSource.declare("classificationId");
        dataSource.setFieldValue("classificationId", "-1");

        selectedSleeveDataSource.declare("sleeveCode", getGui().getSleeveCode());
        selectedSleeveDataSource.declare("sleeveName", getGui().getSleeveName());
        selectedSleeveDataSource.declare("sleeveDustbin", getGui().getSleeveDustbin());
        selectedSleeveDataSource.declare("formula", getGui().getFormula());
    }


    protected void declareSleeveField(String fieldName) {
        selectedSleeveDataSource.addPropertyChangeListener(fieldName, new SleevePropertyChange());
    }


    public DetailDataSource getDataSource() {
        return dataSource;
    }


    public DetailDataSource getSelectedSleeveDataSource() {
        return selectedSleeveDataSource;
    }


    public ListDataSource getAxisSleevesDataSource() {
        return axisSleevesDataSource;
    }


    public AxisTreeLogic getAxisTreeLogic() {
        return axisTreeLogic;
    }


    public ClassificationStructureGui getGui() {
        return gui;
    }

    public void setAxisSleevesLinkErrorHandler(ErrorHandler handler) {
        axisSleevesLink.setErrorHandler(handler);
    }

    private void linkDataSources() {
        axisSleevesLink =
              new DataLink(dataSource, axisSleevesDataSource,
                           new JoinKeys("classificationId"));
        axisSleevesLink.setLoadPolicy(DataLink.Policy.AFTER_FATHER);
        axisSleevesLink.setSavePolicy(DataLink.Policy.AFTER_FATHER);
        axisSleevesLink.start();
        ProxyErrorHandler checker =
              new ProxyErrorHandler(axisSleevesLink.getErrorHandler());
        axisSleevesLink.setErrorHandler(checker);

        buttonPanelLogic.setMainDataSource(dataSource);
        buttonPanelLogic.addSubDataSource(axisSleevesDataSource);
        buttonPanelLogic.addErrorChecker(checker);
    }


    private ListDataSource buildAxisSleevesDataSource() {
        ListDataSource listDataSource = new ListDataSource();
        listDataSource.setLoadFactoryId(getAxisSleevesLoadFactoryId());
        listDataSource.setInsertFactoryId(NEW_CLASSI_STRUCTURE_HANDLER);
        listDataSource.setUpdateFactoryId(updateClassStructureHandler);
        listDataSource.setDeleteFactoryId(DELETE_CLASS_STRUCTURE_HANDLER);

        List<String> columns = new ArrayList<String>();
        columns.add("classificationId");
        columns.add("sleeveId");
        columns.add("sleeveRowId");
        columns.add("sleeveCode");
        columns.add("sleeveName");
        columns.add("sleeveDustbin");
        columns.add("terminalElement");
        columns.add("formula");
        columns.addAll(additionalAxisSleevesColumns);
        listDataSource.setColumns(columns.toArray(new String[]{}));
        return listDataSource;
    }


    protected String getAxisSleevesLoadFactoryId() {
        return selectClassStrucByClassIdHandler;
    }


    public void addClassificationExtensionField(String label, String fieldName, JComponent component) {
        getGui().getClassifExtensionPanel()
              .addItem(label, component, new Insets(0, 0, 2, 5), new Insets(0, 5, 2, 0));
        dataSource.declare(fieldName, component);
    }


    public void addSleeveExtensionField(String label, String fieldName, JComponent component) {
        component.setName(fieldName);
        getGui().getSleeveExtensionPanel()
              .addItem(label, component, new Insets(0, 0, 2, 5), new Insets(0, 5, 2, 0));
        selectedSleeveDataSource.declare(fieldName, component);
        additionalAxisSleevesColumns.add(fieldName);
        declareSleeveField(fieldName);
    }


    public void setClassificationTypeEnabled(boolean enabled) {
        getGui().getClassificationType().setEnabled(enabled);
    }


    public void addClassificationTypeActionListener(ActionListener actionListener) {
        getGui().getClassificationType().addActionListener(actionListener);
    }


    public ButtonPanelLogic getButtonPanelLogic() {
        return buttonPanelLogic;
    }


    /**
     * <p> DataSourceListener chargé de vérifier et modifier la ListDataSource contenant les données des
     * poches au moment de la sauvegarde. On vérifie qu'il y a une poche fourre-tout, on supprime toutes les
     * poches et on les recrée, en recalculant le code poche à partir de la position de la poche dans l'arbre.
     * </p>
     *
     * <p> Le mécanisme de suppression-recréation a été mis en place à cause de la contrainte d'unicité
     * existant sur le couple (CLASSIFICATION_ID, SLEEVE_CODE). </p>
     */
    private class FatherDataSourceListener extends DataSourceAdapter {
        @Override
        public void beforeSaveEvent(DataSourceEvent event) {
            AxisTreeModel axisTreeModel = axisTreeLogic.getModel();

            // S'il n'existe pas de poche fourre-tout, on lance une
            // Exception : une popup sera présentée à l'utilisateur.
            if (!axisTreeModel.hasDustBin()) {
                throw new ClassificationException("Il faut une poche fourre-tout.");
            }

            // S'il existe une poche non fourre-tout sans formule, on lance une
            // Exception : une popup sera présentée à l'utilisateur.
            String sleeveNameWithoutFormula = axisTreeModel.getSleeveNameWithoutFormula();
            if (sleeveNameWithoutFormula != null) {
                throw new ClassificationException("La poche '" + sleeveNameWithoutFormula
                                                  + "' n'a pas de formule.");
            }

            // S'il existe 2 poches avec le même nom.
            String doubleSleeveName = axisTreeModel.findDoubleSleeveName();
            if (doubleSleeveName != null) {
                throw new ClassificationException("Plusieurs poches portent le libellé '"
                                                  + doubleSleeveName + "'.");
            }
        }
    }

    /**
     * Listener chargé de mettre à jour le nom de l'axe lorsqu'il est changé dans le TextField.
     */
    class ClassificationNameListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            String classificationName = gui.getClassificationName().getText();
            AxisTreeNode axe = (AxisTreeNode)axisTreeLogic.getModel().getRoot();
            axe.setName(classificationName);
            axisTreeLogic.getModel().nodeChanged(axe);
        }
    }

    /**
     * Listener chargé d'afficher les données de la poche sélectionnée.
     */
    private class SleeveSelectionListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            final Row selectedRow = ((ListDataSource)evt.getSource()).getSelectedRow();
            if (selectedRow != null) {
                Object currentSelection = axisTreeLogic.getLastPathComponent();
                if (currentSelection != null) {
//                TreePath selectionPath = axisTreeLogic.getGui().getSelectionPath();
//                if (selectionPath != null) {
//                    Object currentSelection = selectionPath.getLastPathComponent();
                    if (currentSelection instanceof Sleeve) {
                        final Sleeve sleeve = ((Sleeve)currentSelection);
                        if (sleeve.isTerminal()) {
                            boolean dustbinAllreadyExists =
                                  axisTreeLogic.getModel().hasDustBin();
                            setSleeveDisplay(sleeve.isDustbin(), dustbinAllreadyExists);
                        }
                        else {
                            setFolderDisplay();
                        }

                        final Result loadResult = new Result();
                        loadResult.addRow(new Row(selectedRow));
                        selectedSleeveDataSource.setLoadResult(loadResult);
                        expressionButtonListener
                              .setSelectedSleeveRowId(selectedRow.getFieldValue("sleeveRowId"));
                        expressionButtonListener.setSelectedClassificationId(
                              dataSource.getSelectedRow().getFieldValue("classificationId"));
                    }
                    else {
                        // Cas où l'axe est sélectionné
                        disableSelectionDisplay();
                        selectedSleeveDataSource.clear();
                    }
                }
            }
            else {
                // Cas où aucune poche n'est sélectionnée
                disableSelectionDisplay();
                selectedSleeveDataSource.clear();
            }
            try {
                selectedSleeveDataSource.load();
            }
            catch (RequestException exception) {
                LOG.error("SleeveSelectionListener.propertyChange", exception);
            }
        }
    }


    protected void disableSelectionDisplay() {
        gui.setSleeveNameEnabled(false);
        gui.setSleeveDustbinEnabled(false);
        gui.setSleeveEditExpressionButtonEnabled(false);
        gui.setSleeveExtensionFieldsEnabled(false);
    }


    protected void setSleeveDisplay(boolean isDustbin, boolean dustbinAllreadyExists) {
        gui.setSleeveNameEnabled(true);
        gui.setSleeveDustbinEnabled(isDustbin == dustbinAllreadyExists);
        gui.setSleeveEditExpressionButtonEnabled(!isDustbin);
        gui.setSleeveExtensionFieldsEnabled(true);
    }


    protected void setFolderDisplay() {
        gui.setSleeveNameEnabled(true);
        gui.setSleeveDustbinEnabled(false);
        gui.setSleeveEditExpressionButtonEnabled(false);
        gui.setSleeveExtensionFieldsEnabled(false);
    }


    protected class SleevePropertyChange implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (axisSleevesDataSource.getSelectedRow() != null) {
                final int selectedRowIndex = axisSleevesDataSource.getSelectedRowIndex();
                axisSleevesDataSource.setValue(selectedRowIndex, evt.getPropertyName(),
                                               (String)evt.getNewValue());

                axisTreeLogic.updateTree();
            }
        }
    }

    /**
     * Listener chargé de mettre à jour le statut fourre-tout lorsqu'il est changé par la CheckBox.
     */

//    class DustbinListener implements PropertyChangeListener {
//        public void propertyChange(PropertyChangeEvent event) {
    class DustbinListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (axisSleevesDataSource.getSelectedRow() != null) {
                Object currentSelection = axisTreeLogic.getLastPathComponent();
                if (currentSelection != null && currentSelection instanceof Sleeve) {
                    Sleeve selectedSleeve = (Sleeve)currentSelection;

//                TreePath path = axisTreeLogic.getGui().getSelectionPath();
//                if (path != null && path.getLastPathComponent() instanceof Sleeve) {
//                    Sleeve selectedSleeve = (Sleeve)path.getLastPathComponent();
                    final int selectedRowIndex =
                          axisSleevesDataSource.getSelectedRowIndex();

                    if (Boolean.valueOf(selectedSleeveDataSource.getFieldValue(
                          "sleeveDustbin"))) {
                        boolean expressionIsEmpty =
                              selectedSleeve.getFormula() == null
                              || selectedSleeve.getFormula().trim().length() == 0;
                        if (!expressionIsEmpty) {
                            // si on convertit une poche en fourre-tout et qu'on a déja
                            // édité son équation, il faut lancer un message de confirmation
                            // pour effacer l'équation
                            if (gui.confirmClearFormula()) {
                                selectedSleeveDataSource.setFieldValue("formula", "");
                            }
                            else {
                                selectedSleeveDataSource.setFieldValue("sleeveDustbin",
                                                                       "false");
                                return;
                            }
                        }
                    }
                    axisSleevesDataSource.setValue(selectedRowIndex, "sleeveDustbin",
                                                   selectedSleeveDataSource.getFieldValue("sleeveDustbin"));

                    axisTreeLogic.updateTree();

                    gui.enableFormula(!selectedSleeve.isDustbin());
                }
            }
        }
    }
}
