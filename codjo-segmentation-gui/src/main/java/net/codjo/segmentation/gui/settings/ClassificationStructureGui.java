/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.gui.toolkit.LabelledItemPanel;
import net.codjo.mad.gui.request.util.ButtonPanelGui;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
/**
 * Ecran détail d'un axe de segmentation.
 */
public class ClassificationStructureGui extends JInternalFrame {
    private JComboBox classificationType;
    private JTextField classificationName;
    private AxisTree axisTree;
    private JTextField sleeveCode;
    private JTextField sleeveName;
    private JCheckBox sleeveDustbin;
    private JTextArea formula;
    private ButtonPanelGui buttonPanelGui;
    private JPanel mainPanel;
    private AxisTreeToolBar axisTreeToolBar;
    private JButton editExpressionButton;
    private LabelledItemPanel classifExtensionPanel;
    private LabelledItemPanel sleeveExtensionPanel;
    private JPanel classificationPanel;
    private List<JComponent> sleeveExtensionFields = new ArrayList<JComponent>();
    private Map<JComponent, ActionListener> mapComponentListener = new HashMap<JComponent, ActionListener>();
    private int maximumNodeDepth;


    ClassificationStructureGui(String title) {
        super(title, true, true, true, true);
        axisTree.setClassificationStructureGui(this);
        getContentPane().add(mainPanel);
        classifExtensionPanel.setLabelLeftAlignment(true);
        sleeveExtensionPanel.setLabelLeftAlignment(true);
    }

    public LabelledItemPanel getSleeveExtensionPanel() {
        return sleeveExtensionPanel;
    }


    public LabelledItemPanel getClassifExtensionPanel() {
        return classifExtensionPanel;
    }


    public void switchToUpdateMode() {
    }


    public ButtonPanelGui getButtonPanelGui() {
        return buttonPanelGui;
    }


    public AxisTree getAxisTree() {
        return axisTree;
    }


    public JTextField getSleeveName() {
        return sleeveName;
    }


    public JTextField getClassificationName() {
        return classificationName;
    }


    public AxisTreeToolBar getAxisTreeToolBar() {
        return axisTreeToolBar;
    }


    public String getFormulaText() {
        return formula.getText();
    }


    public JComboBox getClassificationType() {
        return classificationType;
    }


    public void enableFormula(boolean isEnabled) {
        editExpressionButton.setEnabled(isEnabled);
    }


    public void setSleeveNameEnabled(boolean isEnabled) {
        sleeveName.setEnabled(isEnabled);
    }


    public void setSleeveDustbinEnabled(boolean isEnabled) {
        sleeveDustbin.setEnabled(isEnabled);
    }


    public void setSleeveEditExpressionButtonEnabled(boolean isEnabled) {
        editExpressionButton.setEnabled(isEnabled);
    }


    public void setSleeveExtensionFieldsEnabled(boolean isEnabled) {
        for (JComponent myComponent : sleeveExtensionFields) {
            myComponent.setEnabled(isEnabled);
        }
    }


    public void addActionListener(JComponent component, ActionListener actionListener) {
        mapComponentListener.put(component, actionListener);
        if (component instanceof JComboBox) {
            ((JComboBox)component).addActionListener(actionListener);
        }
        if (component instanceof JCheckBox) {
            ((JCheckBox)component).addActionListener(actionListener);
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        for (Map.Entry<JComponent, ActionListener> entry : mapComponentListener.entrySet()) {
            JComponent loopComponent = entry.getKey();
            if (loopComponent instanceof JComboBox) {
                ((JComboBox)loopComponent).removeActionListener(entry.getValue());
            }
            if (loopComponent instanceof JCheckBox) {
                ((JCheckBox)loopComponent).removeActionListener(entry.getValue());
            }
        }
    }


    public void addExpressionActionListener(ExpressionButtonListener listener) {
        editExpressionButton.addActionListener(listener);
    }


    public boolean confirmClearFormula() {
        return JOptionPane.showConfirmDialog(null,
                                             "Etes vous sûr de vouloir convertir la poche en fourre-tout ?\nL'expression va être perdue !",
                                             "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane
              .YES_OPTION;
    }


    public void addSleeveDustbinListener(ClassificationStructureLogic.DustbinListener listener) {
        sleeveDustbin.addActionListener(listener);
    }


    void addClassificationExtensionField(String label, JComponent component) {
        getClassifExtensionPanel().addItem(label, component, new Insets(0, 0, 2, 5), new Insets(0, 5, 2, 0));
    }

    public void setClassificationExtensionPanel(JPanel panel) {
        classificationPanel.remove(classifExtensionPanel);
        classificationPanel.add(panel, BorderLayout.CENTER);
    }


    void addSleeveExtensionField(String label, JComponent component) {
        sleeveExtensionFields.add(component);
        getSleeveExtensionPanel().addItem(label, component, new Insets(0, 0, 2, 5), new Insets(0, 5, 2, 0));
    }


    public JComponent getSleeveCode() {
        return sleeveCode;
    }


    public JComponent getFormula() {
        return formula;
    }


    public JComponent getSleeveDustbin() {
        return sleeveDustbin;
    }


    public void setMaximumNodeDepth(Integer maximumNodeDepth) {
        this.maximumNodeDepth = maximumNodeDepth;
    }


    public int getMaximumNodeDepth() {
        return maximumNodeDepth;
    }


}
