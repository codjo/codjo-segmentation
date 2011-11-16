/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.results;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.gui.toolkit.text.TextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
/**
 * Classe permettant d'afficher Le résultat de la classification.
 *
 * @author Lajmi
 */
public class SegmentationResultWindowGui extends JInternalFrame {
    private RequestTable classificationResultTable = new RequestTable();
    private RequestToolBar classificationResultToolBar = new RequestToolBar();
    private JPanel filterPanel = new JPanel();
    private RequestComboBox familyFilter = new RequestComboBox();
    private RequestComboBox axeFilter = new RequestComboBox();
    private TextField anomalyFilter = new TextField();
    private int nextFieldIndex = 0;
    private JButton goButton = new JButton("Go");
    private GuiContext context;


    public SegmentationResultWindowGui(String label, GuiContext context) throws Exception {
        super(label);
        this.context = context;
        jbInit();
    }


    private void jbInit() throws Exception {
        setClosable(true);
        setResizable(true);
        setIconifiable(true);

        getContentPane().setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(classificationResultTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));

        // Le filtre
        filterPanel.setBorder(new TitledBorder(
              BorderFactory.createEtchedBorder(Color.white, new Color(134, 134, 134)), "Filtres"));
        filterPanel.setLayout(new GridBagLayout());

        addCustomField("Famille", familyFilter, 10.0);
        addCustomField("Axe", axeFilter, 10.0);
        anomalyFilter.setColumns(4);
        anomalyFilter.setMaxTextLength(4);
        addCustomField("Anomalie", anomalyFilter, 0.0);


        classificationResultToolBar.init(context, classificationResultTable);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        classificationResultToolBar.setHasExcelButton(true);
        this.getContentPane().add(classificationResultToolBar, BorderLayout.SOUTH);
        this.getContentPane().add(filterPanel, BorderLayout.NORTH);
    }


    public void addCustomField(String label, JComponent control, double weightx) {
        filterPanel.add(new JLabel(label),
                        new GridBagConstraints(nextFieldIndex, 0, 1, 1, weightx, 0.0, GridBagConstraints.WEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
        filterPanel.add(control,
                        new GridBagConstraints(nextFieldIndex++, 1, 1, 1, weightx, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL, new Insets(2, 10, 10, 10), 0,
                                               0));
    }

    public void addGoButton() {
        goButton.setEnabled(false);
        filterPanel.add(goButton,
                        new GridBagConstraints(nextFieldIndex++, 0, 1, 2, 0.0, 0.0, GridBagConstraints.WEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
    }

    public RequestComboBox getAxeFilter() {
        return axeFilter;
    }


    public JTextField getAnomalyFilter() {
        return anomalyFilter;
    }


    public RequestTable getClassificationResultTable() {
        return classificationResultTable;
    }


    public RequestToolBar getClassificationResultToolBar() {
        return classificationResultToolBar;
    }


    public RequestComboBox getFamilyFilter() {
        return familyFilter;
    }


    public JButton getGoButton() {
        return goButton;
    }
}
