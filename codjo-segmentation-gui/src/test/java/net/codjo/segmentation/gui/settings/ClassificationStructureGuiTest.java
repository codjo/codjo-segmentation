package net.codjo.segmentation.gui.settings;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
/**
 *
 */
public class ClassificationStructureGuiTest extends UISpecTestCase {

    public void test_setClassificationExtensionPanel() throws Exception {
        ClassificationStructureGui structureGui = new ClassificationStructureGui("toto");

        JPanel jPanel = new JPanel();
        JTextField field = new JTextField(10);
        field.setText("My text");
        field.setName("toto2");
        jPanel.add(field);

        structureGui.setClassificationExtensionPanel(jPanel);

        Window window = new Window(structureGui);
        TextBox textBox = window.getTextBox("toto2");
        assertEquals("My text", textBox.getText());
    }


    public void test_addClassificationExtensionField() throws Exception {
        ClassificationStructureGui structureGui = new ClassificationStructureGui("title");

        JComponent myCustomizedField = new JTextField(5);
        ((JTextField)myCustomizedField).setText("My text");
        myCustomizedField.setName("myCustomizedField");
        structureGui
              .addClassificationExtensionField("Custom textField", myCustomizedField);

        Window window = new Window(structureGui);
        TextBox textBox = window.getTextBox("myCustomizedField");
        assertEquals("My text", textBox.getText());
    }
}
