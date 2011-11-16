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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
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
public class ClassificationResultWindowGui extends JInternalFrame {
    private RequestTable classificationResultTable = new RequestTable();
    private RequestToolBar classificationResultToolBar = new RequestToolBar();
    private JPanel filterPanel = new JPanel();
    private RequestComboBox axeFilter = new RequestComboBox();
    private JTextField anomalyFilter = new JTextField();
    private int nextFieldIndex = 2;
    private GuiContext guiContext;


    public ClassificationResultWindowGui(GuiContext guiContext) throws Exception {
        this.guiContext = guiContext;
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
        filterPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
              Color.white,
              new Color(134, 134, 134)), "Filtres"));
        filterPanel.setLayout(new GridBagLayout());

        filterPanel.add(new JLabel("Axe"),
                        new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
        filterPanel.add(axeFilter,
                        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL, new Insets(2, 10, 10, 0), 0,
                                               0));

        filterPanel.add(new JLabel("Anomalie"),
                        new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
        filterPanel.add(anomalyFilter,
                        new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL, new Insets(2, 10, 10, 10), 0,
                                               0));

        classificationResultToolBar.init(guiContext, classificationResultTable);

        // Construction de la frame
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        classificationResultToolBar.setHasExcelButton(true);
        this.getContentPane().add(classificationResultToolBar, BorderLayout.SOUTH);
        this.getContentPane().add(filterPanel, BorderLayout.NORTH);
        axeFilter.setPreferredSize(new Dimension(200, 21));
//        setSize(new Dimension(900, 300));
    }


    public void addCustomField(String label, JComponent control) {
        filterPanel.add(new JLabel(label),
                        new GridBagConstraints(nextFieldIndex, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                                               GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
        filterPanel.add(control,
                        new GridBagConstraints(nextFieldIndex++, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                                               GridBagConstraints.HORIZONTAL, new Insets(2, 10, 10, 10), 0,
                                               0));
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
}
