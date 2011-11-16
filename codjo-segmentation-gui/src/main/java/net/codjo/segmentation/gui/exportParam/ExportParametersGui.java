package net.codjo.segmentation.gui.exportParam;
import net.codjo.gui.toolkit.path.DirectoryPathField;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.segmentation.gui.ImportExportProgresListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ExportParametersGui extends JInternalFrame implements ImportExportProgresListener {

    private JButton exportButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JRadioButton classificationRadio;
    private JRadioButton sleeveRadio;
    private DirectoryPathField directoryPathField;
    private WaitingPanel waitingPanel = new WaitingPanel();


    public ExportParametersGui() {
        super("Export de paramétrage", true, true, true, true);
        initNames();
        initListeners();
        directoryPathField.getDirectoryNameField().setEditable(true);
        setContentPane(mainPanel);
        setGlassPane(waitingPanel);
        setPreferredSize(new Dimension(800, 150));
    }


    public void handleInform(String infoMessage) {
        getWaitingPanel().stopAnimation();
    }


    public void handleInform(String[][] quarantine) {
        ;
    }


    public void handleError(String errorMessage) {
        getWaitingPanel().stopAnimation();
        ErrorDialog.show(this, errorMessage, errorMessage);
    }


    private void initNames() {
        directoryPathField.setName("AxeFileName");
        cancelButton.setName("cancelButton");
        exportButton.setName("exportButton");
        classificationRadio.setName("classificationRadio");
        sleeveRadio.setName("sleeveRadio");
    }


    private void initListeners() {
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        directoryPathField.getDirectoryNameField().getDocument()
              .addDocumentListener(new ValidDirectoryListener());
    }


    public void setExportButtonListener(ActionListener listener) {
        exportButton.addActionListener(listener);
    }


    public File getExportFile() {
        return directoryPathField.getDirectory();
    }


    public boolean isClassificationSelected() {
        return classificationRadio.isSelected();
    }


    public WaitingPanel getWaitingPanel() {
        return waitingPanel;
    }


    private class ValidDirectoryListener implements DocumentListener {
        public void insertUpdate(DocumentEvent event) {
            setEnabledButton();
        }


        public void removeUpdate(DocumentEvent event) {
            setEnabledButton();
        }


        public void changedUpdate(DocumentEvent event) {
            setEnabledButton();
        }


        private void setEnabledButton() {
            JTextField directoryField = directoryPathField.getDirectoryNameField();
            File directory = directoryPathField.getDirectory();
            if (directory == null) {
                exportButton.setEnabled(false);
                return;
            }

            File parentFile = directory.getParentFile();
            if (parentFile == null) {
                directoryField.setForeground(Color.red);
                exportButton.setEnabled(false);
                return;
            }

            boolean mustEnable = parentFile.isDirectory()
                                 && parentFile.exists()
                                 && directory.getName().endsWith(".txt");
            directoryField.setForeground(mustEnable ? Color.black : Color.red);
            exportButton.setEnabled(mustEnable);
        }
    }
}
