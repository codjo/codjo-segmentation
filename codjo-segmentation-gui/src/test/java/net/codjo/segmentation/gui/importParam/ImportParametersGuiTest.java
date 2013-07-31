package net.codjo.segmentation.gui.importParam;
import java.io.StringReader;
import net.codjo.mad.gui.request.Preference;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.security.common.api.UserMock;
import net.codjo.segmentation.gui.SegmentationGuiContext;
import org.apache.log4j.Logger;
import org.uispec4j.Button;
import org.uispec4j.RadioButton;
import org.uispec4j.Table;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;
import org.xml.sax.InputSource;

import static net.codjo.mad.gui.request.PreferenceFactory.addMapping;
import static net.codjo.mad.gui.request.PreferenceFactory.getPreference;
import static net.codjo.mad.gui.request.PreferenceFactory.loadMapping;

public class ImportParametersGuiTest extends UISpecTestCase {
    private static Logger LOG = Logger.getLogger(ImportParametersGuiTest.class);
    private TextBox axeFileTextBox;
    private Table classificationTable;
    private Table sleeveTable;
    private Button importButton;
    private RadioButton classificationRadio;
    private RadioButton sleeveRadio;
    private Button closeButton;
    private Window window;

    private ImportParametersGui gui;

    private static final String CLASSIFICATION_PREFERENCE =
          "<?xml version=\"1.0\"?>                                                            "
          + "<preferenceList>                                                                 "
          + "    <preference id=\"ImportClassificationParameters\">                           "
          + "        <column fieldName=\"classificationId\" label=\"Id Axe\"/>                "
          + "        <column fieldName=\"classificationName\" label=\"Libellé Axe\"/>         "
          + "        <column fieldName=\"classificationType\" label=\"Famille\"/>             "
          + "        <column fieldName=\"anomalyLog\" label=\"Log\"/>                         "
          + "    </preference>                                                                "
          + "</preferenceList>                                                                ";
    private static final String CLASSIFICATION_STRUCTURE_PREFERENCE =
          "<?xml version=\"1.0\"?>                                                            "
          + "<preferenceList>                                                                 "
          + "    <preference id=\"ImportSleeveParameters\">                                   "
          + "        <column fieldName=\"classificationId\" label=\"Id Axe\"/>                "
          + "        <column fieldName=\"sleeveCode\" label=\"Code Poche\"/>                  "
          + "        <column fieldName=\"sleeveName\" label=\"Libellé Poche\"/>               "
          + "        <column fieldName=\"sleeveDustbin\" label=\"Poche fourre-tout\"/>        "
          + "        <column fieldName=\"terminalElement\" label=\"Poche de terminaison\"/>   "
          + "        <column fieldName=\"formula\" label=\"Formule\"/>                        "
          + "        <column fieldName=\"anomalyLog\" label=\"Log\"/>                         "
          + "        <hidden>                                                                 "
          + "        <column fieldName=\"sleeveId\"/>                                         "
          + "        <column fieldName=\"sleeveRowId\"/>                                      "
          + "        </hidden>                                                                "
          + "    </preference>                                                                "
          + "</preferenceList>                                                                ";


    public void test_init() throws Exception {
        window.assertTitleEquals("Import de paramétrage");

        assertTrue(axeFileTextBox.isEditable());
        assertEquals("", axeFileTextBox.getText());

        assertFalse(importButton.isEnabled());
        assertTrue(closeButton.isEnabled());

        assertTrue(classificationRadio.isSelected());
        assertFalse(sleeveRadio.isSelected());

        assertEquals(0, classificationTable.getRowCount());
        String[] columnNames = classificationTable.getHeader().getColumnNames();
        assertEquals("Id Axe", columnNames[0]);
        assertEquals("Libellé Axe", columnNames[1]);
        assertEquals("Famille", columnNames[2]);
        assertEquals("Log", columnNames[3]);
    }


    public void test_sleeveRadioClick() throws Exception {
        sleeveRadio.click();
        sleeveTable = window.getTable("ImportSleeveParameters");

        assertEquals(0, sleeveTable.getRowCount());

        String[] columnNames = sleeveTable.getHeader().getColumnNames();
        assertEquals("Id Axe", columnNames[0]);
        assertEquals("Code Poche", columnNames[1]);
        assertEquals("Libellé Poche", columnNames[2]);
        assertEquals("Poche fourre-tout", columnNames[3]);
        assertEquals("Poche de terminaison", columnNames[4]);
        assertEquals("Formule", columnNames[5]);
        assertEquals("Log", columnNames[6]);
    }


    public void test_handleInform_classification() throws Exception {
        String[][] results = getResultsForClassification();

        gui.handleInform(results);

        String[][] expected = new String[][]{
              {"1", "Axe 1", "Type 1", "Doublon"},
              {"1", "Axe 2", "Type 1", "Doublon"},
              {"2", "Axe 3", "Type 2", "Axe pas sympa"}
        };

        assertTrue(classificationTable.contentEquals(expected));
    }


    public void test_handleInform_classificationStructure() throws Exception {
        sleeveRadio.click();
        sleeveTable = window.getTable("ImportSleeveParameters");

        String[][] results = getResultsForClassificationStructure();

        gui.handleInform(results);

        Object[][] expected = new Object[][]{
              {"2", "01-1", "Poche 2", false, true, "manager == 3",
               "Cet axe ne comporte pas de poche fourre-tout"},
              {"2", "01-2", "Poche 3", false, true, "manager == 4",
               "Cet axe ne comporte pas de poche fourre-tout"}
        };

        assertTrue(sleeveTable.contentEquals(expected));
    }


    public void test_handleError() throws Exception {
        WindowInterceptor.init(new Trigger() {
            public void run() throws Exception {
                gui.handleError("An error message for you!");
            }
        }).process("Erreur", new WindowHandler() {
            @Override
            public Trigger process(Window window) throws Exception {
                return window.getButton("OK").triggerClick();
            }
        }).run();
    }


    public void test_clearLog_classification() throws Exception {
        Button clearLogButton = window.getButton("ImportClassificationParameters.clearLogAction");

        assertFalse(clearLogButton.isEnabled());

        String[][] results = getResultsForClassification();

        assertClearButtonBehaviour(clearLogButton, classificationTable, results);
    }


    public void test_clearLog_sleeve() throws Exception {
        sleeveRadio.click();

        sleeveTable = window.getTable("ImportSleeveParameters");

        Button clearLogButton = window.getButton("ImportSleeveParameters.clearLogAction");

        assertFalse(clearLogButton.isEnabled());

        String[][] results = getResultsForClassificationStructure();

        assertClearButtonBehaviour(clearLogButton, sleeveTable, results);
    }


    public void test_swithBetweenTables() throws Exception {
        String[][] results = getResultsForClassification();

        gui.handleInform(results);

        sleeveRadio.click();

        classificationRadio.click();

        assertFalse(classificationTable.isEmpty());

        Button clearLogButton = window.getButton("ImportClassificationParameters.clearLogAction");
        assertTrue(clearLogButton.isEnabled());
    }


    private void assertClearButtonBehaviour(Button clearLogButton, Table table, String[][] results) {
        gui.handleInform(results);
        assertTrue(clearLogButton.isEnabled());

        clearLogButton.click();

        assertTrue(table.isEmpty());
        assertFalse(clearLogButton.isEnabled());
    }


    private String[][] getResultsForClassification() {
        return new String[][]{
              {"CLASSIFICATION_ID", "CLASSIFICATION_NAME", "CLASSIFICATION_TYPE", "ANOMALY_LOG"},
              {"1", "Axe 1", "Type 1", "Doublon"},
              {"1", "Axe 2", "Type 1", "Doublon"},
              {"2", "Axe 3", "Type 2", "Axe pas sympa"}
        };
    }


    private String[][] getResultsForClassificationStructure() {
        return new String[][]{
              {"SLEEVE_ID", "SLEEVE_ROW_ID", "CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME",
               "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT", "FORMULA", "ANOMALY_LOG"},
              {"1001", "1001", "2", "01-1", "Poche 2", "0", "1", "manager == 3",
               "Cet axe ne comporte pas de poche fourre-tout"},
              {"1002", "1002", "2", "01-2", "Poche 3", "0", "1", "manager == 4",
               "Cet axe ne comporte pas de poche fourre-tout"}
        };
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        loadMapping(new InputSource(new StringReader(CLASSIFICATION_PREFERENCE)));
        addMapping(new InputSource(new StringReader(CLASSIFICATION_STRUCTURE_PREFERENCE)));

        Preference classificationPreference = getPreference("ImportClassificationParameters");
        Preference sleevePreference = getPreference("ImportSleeveParameters");

        SegmentationGuiContext context = new SegmentationGuiContext();
        context.setUser(new UserMock());
        gui = new ImportParametersGui(context, classificationPreference, sleevePreference);

        window = new Window(gui);

        axeFileTextBox = window.getInputTextBox("AxeFileName.fileName");
        classificationTable = window.getTable("ImportClassificationParameters");
        importButton = window.getButton("importButton");
        closeButton = window.getButton("cancelButton");
        classificationRadio = window.getRadioButton("classificationRadio");
        sleeveRadio = window.getRadioButton("sleeveRadio");
    }


    @Override
    protected void tearDown() throws Exception {
        PreferenceFactory.clearPreferences();
        super.tearDown();
    }
}
