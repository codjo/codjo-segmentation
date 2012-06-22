package net.codjo.segmentation.releasetest;
import java.awt.Dimension;
import java.io.InputStream;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import net.codjo.i18n.gui.plugin.InternationalizationGuiPlugin;
import net.codjo.mad.client.plugin.MadConnectionPlugin;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.base.AbstractGuiPlugin;
import net.codjo.mad.gui.base.GuiConfiguration;
import net.codjo.mad.gui.base.MadGuiCore;
import net.codjo.mad.gui.plugin.MadGuiPlugin;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.util.ApplicationData;
import net.codjo.security.client.plugin.SecurityClientPlugin;
import net.codjo.security.gui.plugin.SecurityGuiPlugin;
import net.codjo.segmentation.gui.plugin.SegmentationGuiPlugin;
import net.codjo.segmentation.gui.results.SegmentationResultCustomizer;
import net.codjo.segmentation.gui.results.SegmentationResultWindowLogic;
import net.codjo.segmentation.gui.settings.ClassificationStructureGui;
import net.codjo.segmentation.gui.settings.ClassificationStructureLogic;
import net.codjo.segmentation.gui.settings.SegmentationSettingsCustomizer;
import net.codjo.segmentation.gui.wizard.AnomalyLogWindowCustomizer;
import net.codjo.segmentation.gui.wizard.ClassificationWizardWindow;
import net.codjo.segmentation.gui.wizard.LogWindowLogic;
import net.codjo.segmentation.gui.wizard.SegmentationWizardCustomizer;
import net.codjo.workflow.gui.plugin.WorkflowGuiPlugin;
/**
 */
public class SegmentationGuiTestMock {
//    protected final UserMock userMock = new UserMock();
//    protected ServerCore serverCore;


    public static void main(String[] arguments) throws Exception {
        final MadGuiCore guiCore = new MadGuiCore(SegmentationGuiTestMock.class.getResource("menu.xml"),
                                                  null);

        guiCore.getConfiguration().setMainWindowSize(new Dimension(1100, 900));

        guiCore.addPlugin(InternationalizationGuiPlugin.class);
        guiCore.addPlugin(SecurityClientPlugin.class);
        guiCore.addPlugin(MadConnectionPlugin.class);
        guiCore.addPlugin(MadGuiPlugin.class);
        guiCore.addPlugin(SecurityGuiPlugin.class);
        guiCore.addPlugin(WorkflowGuiPlugin.class);
        guiCore.addPlugin(SegmentationGuiPlugin.class);
        guiCore.addPlugin(SegmentationGuiTestPlugin.class);

        InputStream properties = SegmentationGuiTestMock.class.getResourceAsStream("/conf/application.properties");
        ApplicationData applicationData = new ApplicationData(properties);

        arguments = new String[]{applicationData.getDefaultLogin(),
                                 applicationData.getDefaultPassword(),
                                 "localhost", "35714"};
        guiCore.show(arguments, applicationData);
    }


    public static class SegmentationGuiTestPlugin extends AbstractGuiPlugin {
        public SegmentationGuiTestPlugin(SegmentationGuiPlugin plugin) {
            plugin.getConfiguration().setWizardCustomizer(new MyWizardCustomizer());
            plugin.getConfiguration()
                  .setClassificationAnomalyPreferenceId("TestClassificationAnomalyWindow");
            plugin.getConfiguration().setMaximumNodeDepth(4);
            plugin.getConfiguration().setResultCustomizer(new MyResultCustomizer());
            plugin.getConfiguration().setAnomalyLogWindowCustomizer(new MyAnomalyLogWindowCustomizer());

/***********************************************************************************************************/
/*      Exemple d'utilisation du ClassificationSettingsCustomizer                                          */
/* Ex1: plugin.getConfiguration().setSettingsCustomizer(new MyClassificationSettingsCustomizer());         */
/* Ex2: plugin.getConfiguration().setSettingsCustomizer(new ClassificationSettingsCustomizerWithMyPanel());*/
/***********************************************************************************************************/
        }


        public void initGui(GuiConfiguration configuration) throws Exception {

        }
    }

    private static class MyWizardCustomizer implements SegmentationWizardCustomizer {
        private JTextField keyField;


        public void customizeWindow(ClassificationWizardWindow wizardWindow) {
            keyField = new JTextField(25);
            keyField.setName("keyField");
            wizardWindow.addCustomField("Key",
                                        keyField,
                                        new MyComponentValidator(),
                                        new MySegmentationJobRequestFiller());
            wizardWindow.setInfoField("Ceci est un label pour dire rien du tout.");
        }


        private class MyComponentValidator implements ClassificationWizardWindow.ComponentValidator {
            public boolean isComponentValid() {
                return true;
            }
        }

        private class MySegmentationJobRequestFiller
              implements ClassificationWizardWindow.SegmentationJobRequestFiller {

            public void fillRequest(Map<String, String> mapParameters) {
                mapParameters.put("myKey", keyField.getText());
            }
        }
    }

    private static class MyResultCustomizer implements SegmentationResultCustomizer {

        public void customizeWindow(SegmentationResultWindowLogic resultWindowLogic) {
            JTextField keyField = new JTextField(10);
            keyField.setName("keyField");
            resultWindowLogic.addCustomFilterField("Key", "myKey", keyField);
            resultWindowLogic.addGoButton();
        }
    }

    private static class MyAnomalyLogWindowCustomizer implements AnomalyLogWindowCustomizer {
        public void customizeWindow(LogWindowLogic anomalyLogWindowLogic) {

            anomalyLogWindowLogic.getGui().getTable()
                  .setCellRenderer("sleeveCode", new MySimpleTableCellRenderer());
        }
    }

    private static class MySimpleTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            if ("".equals(value.toString().trim())) {
                setText("-");
            }
            else {
                setText(value.toString());
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static class MyClassificationSettingsCustomizer implements SegmentationSettingsCustomizer {

        public void doPreDataSourceInit(ClassificationStructureLogic structureLogic,
                                        DetailDataSource dataSource)
              throws RequestException {

            JComboBox myCustomizedCombo = new JComboBox();

            JTextField myCustomizedField = new JTextField(5);
            myCustomizedField.setPreferredSize(new Dimension(20, 20));
            myCustomizedField.setMinimumSize(new Dimension(20, 20));
            myCustomizedField.setMaximumSize(new Dimension(20, 20));

            myCustomizedCombo.addItem("1");
            myCustomizedCombo.addItem("2");
            myCustomizedCombo.addItem("3");

            RequestComboBox myCustomizedRequest = RequestComboBox.createRequestComboBox("toto", "toto", true);
            Row newRow = new Row();
            newRow.addField("toto", "1");
            myCustomizedRequest.getDataSource().addRow(newRow);
            newRow = new Row();
            newRow.addField("toto", "2");
            myCustomizedRequest.getDataSource().addRow(newRow);

            myCustomizedField.setColumns(5);

            structureLogic.addClassificationExtensionField("Custom combo", "myCombo", myCustomizedCombo);
            structureLogic
                  .addClassificationExtensionField("Custom combo REquest", "toto", myCustomizedRequest);
            structureLogic.addClassificationExtensionField("Custom field", "myField", myCustomizedField);

            myCustomizedCombo.setSelectedIndex(1);
        }


        public void doPostDataSourceInit(ClassificationStructureLogic structureLogic,
                                         DetailDataSource dataSource)
              throws RequestException {

        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static class ClassificationSettingsCustomizerWithMyPanel
          implements SegmentationSettingsCustomizer {

        public void doPreDataSourceInit(ClassificationStructureLogic structureLogic,
                                        DetailDataSource dataSource)
              throws RequestException {

            addCustomPanel(structureLogic);
        }


        private void addCustomPanel(ClassificationStructureLogic structureLogic) {
            ClassificationStructureGui structureGui = structureLogic.getGui();
            structureGui.setClassificationExtensionPanel(new ClassificationGuiCustomizer().getMainPanel());
        }


        public void doPostDataSourceInit(ClassificationStructureLogic structureLogic,
                                         DetailDataSource dataSource)
              throws RequestException {

        }
    }
}
