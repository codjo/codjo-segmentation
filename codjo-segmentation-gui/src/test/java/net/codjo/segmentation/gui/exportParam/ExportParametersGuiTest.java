package net.codjo.segmentation.gui.exportParam;
import java.awt.Color;
import org.uispec4j.Button;
import org.uispec4j.RadioButton;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;
/**
 *
 */
public class ExportParametersGuiTest extends UISpecTestCase {
    private TextBox axeFileTextBox;
    private Button exportButton;
    private RadioButton classificationRadio;
    private RadioButton sleeveRadio;
    private Button closeButton;
    private Window window;

    private ExportParametersGui gui;


    public void test_init() throws Exception {
        window.assertTitleEquals("Export de paramétrage");

        assertTrue(axeFileTextBox.isEditable());
        assertEquals("", axeFileTextBox.getText());

        assertFalse(exportButton.isEnabled());
        assertTrue(closeButton.isEnabled());

        assertTrue(classificationRadio.isSelected());
        assertFalse(sleeveRadio.isSelected());
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


    public void test_assertExportButtonBehaviour() {
        axeFileTextBox.setText("C:");
        assertFalse(exportButton.isEnabled());
        assertTrue(axeFileTextBox.getAwtComponent().getForeground().equals(Color.red));

        axeFileTextBox.setText("C:\\dev");
        assertFalse(exportButton.isEnabled());
        assertTrue(axeFileTextBox.getAwtComponent().getForeground().equals(Color.red));

        axeFileTextBox.setText("C:\\dev\\toto.html");
        assertFalse(exportButton.isEnabled());
        assertTrue(axeFileTextBox.getAwtComponent().getForeground().equals(Color.red));

        axeFileTextBox.setText("C:\\bidon\\toto.txt");
        assertFalse(exportButton.isEnabled());
        assertTrue(axeFileTextBox.getAwtComponent().getForeground().equals(Color.red));

        axeFileTextBox.setText("C:\\dev\\toto.txt");
        assertTrue(exportButton.isEnabled());
        assertTrue(axeFileTextBox.getAwtComponent().getForeground().equals(Color.black));
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gui = new ExportParametersGui();
        window = new Window(gui);

        axeFileTextBox = window.getInputTextBox("DirectoryPathField.directoryNameField");
        exportButton = window.getButton("exportButton");
        closeButton = window.getButton("cancelButton");
        classificationRadio = window.getRadioButton("classificationRadio");
        sleeveRadio = window.getRadioButton("sleeveRadio");
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
