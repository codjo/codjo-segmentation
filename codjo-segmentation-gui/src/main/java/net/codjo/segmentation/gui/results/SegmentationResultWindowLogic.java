/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.results;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.Column;
import net.codjo.mad.gui.request.DataLink;
import net.codjo.mad.gui.request.JoinKeys;
import net.codjo.mad.gui.request.Preference;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.event.DataSourceAdapter;
import net.codjo.mad.gui.request.event.DataSourceEvent;
import net.codjo.mad.gui.request.factory.RequetorFactory;
import net.codjo.mad.gui.request.factory.SelectFactory;
import net.codjo.mad.gui.structure.StructureCache;
import net.codjo.segmentation.gui.preference.PreferenceGui;
import net.codjo.segmentation.gui.preference.PreferenceGuiManagerFactory;
import net.codjo.xml.XmlException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTextField;
/**
 * Classe permettant de Manipuler les listener sur SegmentationResultWindowGui.
 */
public class SegmentationResultWindowLogic {
    private final SegmentationResultWindowGui gui;
    private GuiContext guiContext;
    private Map<String, JTextField> customFields = new HashMap<String, JTextField>();


    public SegmentationResultWindowLogic(GuiContext context, String label)
          throws Exception {
        this.guiContext = context;
        gui = new SegmentationResultWindowGui(label, context);

        RequestComboBox axisCombo = gui.getAxeFilter();
        axisCombo.setColumns(new String[]{"classificationId", "classificationName"});
        axisCombo.setRendererFieldName("classificationName");
        axisCombo.setModelFieldName("classificationId");
        axisCombo.setSelectFactoryId("selectAllClassificationByFamily");

        RequestComboBox familyCombo = gui.getFamilyFilter();
        familyCombo.setColumns(new String[]{"familyId"});
        familyCombo.setRendererFieldName("familyId");
        familyCombo.setModelFieldName("familyId");
        familyCombo.setSelectFactoryId("selectAllFamily");

        JoinKeys joinKeys = new JoinKeys();
        joinKeys.addAssociation("familyId", "classificationType");
        DataLink dataLink = new DataLink(familyCombo.getDataSource(), axisCombo.getDataSource(), joinKeys);
        dataLink.setLoadPolicy(DataLink.Policy.AFTER_FATHER);
        dataLink.start();
        familyCombo.load();

        setNames();

        gui.getClassificationResultToolBar().init(guiContext, gui.getClassificationResultTable());

        gui.getClassificationResultTable().getDataSource()
              .addDataSourceListener(new ClassificationResultDataSourceAdaptor());

        gui.getGoButton().addActionListener(new GoButtonActionListener());

        gui.getAxeFilter().addActionListener(new AxisActionListener());
    }


    private void setNames() {
        gui.getAxeFilter().setName("axeFilter");
        gui.getFamilyFilter().setName("familyFilter");
        gui.getAnomalyFilter().setName("anomalyFilter");
        gui.getGoButton().setName("goButton");
    }


    public GuiContext getGuiContext() {
        return guiContext;
    }


    public void addCustomFilterField(String label, String fieldName, JTextField component) {
        customFields.put(fieldName, component);
        gui.addCustomField(label, component, 0.0);
    }


    public void addGoButton() {
        getGui().addGoButton();
    }


    public SegmentationResultWindowGui getGui() {
        return gui;
    }


    private boolean isValidAxeSelected() {
        return (gui.getAxeFilter().getDataSource().getSelectedRow() != null);
    }


    private void initRequestTable() throws RequestException, IOException, XmlException {
        String familyId = gui.getFamilyFilter().getDataSource().getSelectedRow().getFieldValue("familyId");

        PreferenceGui preferenceGui = getPreferenceGuiManager(guiContext, familyId);
        Map<String, String> resultColumns = preferenceGui.getResultColumns();
        List<Column> columns = new ArrayList<Column>();
        for (Map.Entry<String, String> entry : resultColumns.entrySet()) {
            columns.add(new Column(entry.getValue(), entry.getKey()));
        }

        Preference preference = new Preference();
        preference.setColumns(columns);
        preference.setSelectAllId("getSegmentationResult");
        if (preferenceGui.getResultRequetor() != null && !"".equals(preferenceGui.getResultRequetor())) {
            preference.setRequetor(new RequetorFactory(preferenceGui.getResultRequetor()));
        }
        else {
            preference.setRequetor(null);
        }
        preference.setId("classificationResultTable");
        gui.getClassificationResultTable().setPreference(preference);

        gui.getClassificationResultToolBar().init(guiContext, gui.getClassificationResultTable());
    }


    private void loadRequestTableWithFilter() throws RequestException, IOException, XmlException {
        String familyId = gui.getFamilyFilter().getDataSource().getSelectedRow().getFieldValue("familyId");
        String axeId = gui.getAxeFilter().getDataSource().getSelectedRow().getFieldValue("classificationId");

        gui.getClassificationResultTable().getDataSource()
              .setLoadFactory(new SelectFactory("getSegmentationResult"));
        FieldsList fieldsList = new FieldsList("familyId", familyId);
        fieldsList.addField("segmentationId", axeId);
        String anomaly = getGui().getAnomalyFilter().getText();
        fieldsList.addField("anomaly", "".equals(anomaly) ? "-1" : anomaly);
        for (Map.Entry<String, JTextField> entry : customFields.entrySet()) {
            fieldsList.addField(entry.getKey(), entry.getValue().getText());
        }
        gui.getClassificationResultTable().setSelector(fieldsList);
        gui.getClassificationResultTable().setCurrentPage(1);
        gui.getClassificationResultTable().load();
    }


    private PreferenceGui getPreferenceGuiManager(GuiContext ctxt, String familyId)
          throws IOException, XmlException, RequestException {
        StructureCache structureCache = (StructureCache)ctxt.getProperty(StructureCache.STRUCTURE_CACHE);
        StructureReader madReader = structureCache.getStructureReader();
        return new PreferenceGuiManagerFactory(madReader).getPreferenceGuiManager().getPreference(familyId);
    }


    private class AxisActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            gui.getGoButton().setEnabled(gui.getAxeFilter().getDataSource().getSelectedRow() != null);
            gui.getClassificationResultTable().getPreference().setRequetor(null);
            if (isValidAxeSelected()) {
                try {
                    initRequestTable();
                }
                catch (Exception exception) {
                    ErrorDialog.show(guiContext.getDesktopPane(), "Erreur au chargement des résultats",
                                     exception);
                }
            }
            else {
                RequestTable classificationTable = gui.getClassificationResultTable();
                classificationTable.getDataSource().clear();
                gui.getClassificationResultToolBar().init(guiContext, gui.getClassificationResultTable());
            }
        }
    }

    private class GoButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                loadRequestTableWithFilter();
            }
            catch (Exception e1) {
                ErrorDialog.show(guiContext.getDesktopPane(), "Erreur au chargement des résultats", e1);
            }
        }
    }

    private class ClassificationResultDataSourceAdaptor extends DataSourceAdapter {

        @Override
        public void beforeLoadEvent(DataSourceEvent event) {
            super.beforeLoadEvent(event);
            if (!"getSegmentationResult".equals(event.getDataSource().getLoadFactory().getId())) {
                gui.getAnomalyFilter().setText("");
                for (Map.Entry<String, JTextField> entry : customFields.entrySet()) {
                    entry.getValue().setText("");
                }
            }
        }
    }
}
