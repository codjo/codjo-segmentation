/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.wizard;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.gui.toolkit.wizard.StepPanel;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.Request;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.RequestSender;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.ResultManager;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.Preference;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.mad.gui.request.factory.UpdateFactory;
import net.codjo.segmentation.common.message.SegmentationJobRequest;
import net.codjo.segmentation.gui.SegmentationGuiOperations;
import net.codjo.segmentation.gui.progress.SegmentationProgress;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.common.subscribe.JobEventHandler;
import net.codjo.workflow.gui.util.SwingWrapper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
/**
 * La Step qui affiche les progress.
 */
public class ProgressStep extends StepPanel {
    private JButton buttonLog;
    private GuiContext guiContext;
    private final String anomalyPreferenceId;
    private String postSegmentationTreatment = null;
    private String segmentationIds;
    private final SegmentationProgress segmentationProgress = new SegmentationProgress();
    private final JTextField statusField = new JTextField();
    private Collection<ClassificationWizardWindow.SegmentationJobRequestFiller> requestFillers =
          new ArrayList<ClassificationWizardWindow.SegmentationJobRequestFiller>();
    FieldsList selector;
    private final AnomalyLogWindowCustomizer customizer;


    public ProgressStep(GuiContext guiContext, String anomalyPreferenceId) {
        this(guiContext, anomalyPreferenceId, null, null);
    }


    public ProgressStep(GuiContext context, String anomalyPreferenceId, String postSegmentationTreatment) {
        this(context, anomalyPreferenceId, postSegmentationTreatment, null);
    }


    public ProgressStep(GuiContext context,
                        String anomalyPreferenceId,
                        String postSegmentationTreatment,
                        AnomalyLogWindowCustomizer customizer) {
        this.guiContext = context;
        this.anomalyPreferenceId = anomalyPreferenceId;
        this.postSegmentationTreatment = postSegmentationTreatment;
        this.customizer = customizer;
        setName("Segmentation");
        jbInit();
    }


    @Override
    public void start(Map previousStepState) {
        segmentationIds = (String)previousStepState.get(ClassificationStep.ASSET_CL_LIST_KEY);

        new Thread(new Runnable() {
            public void run() {
                startTreatment();
            }
        }).start();
    }


    /**
     * Démarrer le traitement en envoyant un message JMS.
     */
    private void startTreatment() {
        segmentationProgress.clearAll();

        JobEventHandler eventHandler =
              new JobEventHandler() {
                  @Override
                  protected void handleAudit(JobAudit audit) {
                      ProgressStep.this.handleAudit(audit);
                  }
              };

        SegmentationGuiOperations operations =
              (SegmentationGuiOperations)guiContext.getProperty("SegmentationGuiOperations");

        SegmentationJobRequest request = new SegmentationJobRequest();
        request.setSegmentationIds(segmentationIds);
        selector = new FieldsList();
        Map<String, String> mapParameters = new HashMap<String, String>();
        for (ClassificationWizardWindow.SegmentationJobRequestFiller requestFiller : requestFillers) {
            requestFiller.fillRequest(mapParameters);
        }
        request.putParameters(mapParameters);
        selector.addAllField(mapParameters);

        try {
            operations.startSegmentation(request, SwingWrapper.wrapp(eventHandler));
        }
        catch (Exception e) {
            ErrorDialog.show(guiContext.getDesktopPane(), "Le lancement de la segmentation a échoué", e);
        }
    }


    private void handleAudit(JobAudit audit) {
        if (audit.getType() == JobAudit.Type.PRE) {
            segmentationProgress.receivePreAudit(audit);
        }
        else if (audit.getType() == JobAudit.Type.MID) {
            segmentationProgress.receiveAudit(audit.getArguments());
            validate();
        }
        else if (audit.getType() == JobAudit.Type.POST) {
            handlePostAudit(audit);
            segmentationProgress.receivePostAudit(audit);
        }
    }


    private void handlePostAudit(JobAudit audit) {
        if (audit.getStatus() != JobAudit.Status.OK) {
            statusField.setForeground(Color.RED);
        }

        // S'il y a des anomalies, on retourne true
        Result anomalieResult = getAnomalieResult();

        int rowCount = anomalieResult.getTotalRowCount();
        if (rowCount > 0) {
            buttonLog.setAction(
                  new LogWindowAction(guiContext, anomalieResult, anomalyPreferenceId, customizer));
            statusField.setForeground(Color.RED);
            statusField.setText(rowCount + " anomalie" + (rowCount > 1 ? "s" : "")
                                + " ! Merci de consulter le log audit");
            enableLogButton();
        }
        else {
            statusField.setText("Le traitement de segmentation est terminé : " + toString(audit.getStatus()));
        }

        //postSegmentationTreatment
        if (postSegmentationTreatment != null) {
            UpdateFactory factory = new UpdateFactory(postSegmentationTreatment);
            FieldsList fields = new FieldsList(selector);
            fields.addField("status", audit.getStatus().toString());
            factory.init(fields);

            RequestSender requestSender = new RequestSender();
            Request request = factory.buildRequest(new HashMap());
            try {
                ResultManager rm = requestSender.send(new Request[]{request});
                if (rm.hasError()) {
                    throw new RequestException(rm.getErrorResult());
                }
            }
            catch (RequestException ex) {
                ErrorDialog.show(this.getGui(), "Impossible d'exécuter le traitement post segmentation.", ex);
            }
        }
    }


    private String toString(JobAudit.Status status) {
        if (status == JobAudit.Status.OK) {
            return "OK";
        }
        else {
            return "ERREUR";
        }
    }


    private void jbInit() {
        statusField.setName("statusField");
        statusField.setEditable(false);
        statusField.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
//        statusField.setBackground(Color.WHITE);
        statusField.setOpaque(false);

        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(segmentationProgress.getGui(), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
//        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setLayout(new BorderLayout());

        buttonLog = new JButton();

        buttonLog.setText("Afficher les anomalies");
        buttonLog.setName("displayAnomaly");
        buttonLog.setEnabled(false);
        bottomPanel.add(statusField, BorderLayout.CENTER);
        bottomPanel.add(buttonLog, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void enableLogButton() {
        buttonLog.setEnabled(true);
    }


    private Result getAnomalieResult() {
        Result anomalyResult = null;
        ListDataSource anomalyList = new ListDataSource();
        Preference preference = PreferenceFactory.getPreference(anomalyPreferenceId);
        anomalyList.setLoadFactory(preference.getSelectAll());
        selector.addField("assetClassificationList", segmentationIds);
        anomalyList.setSelector(selector);
        anomalyList.setColumns(preference.getColumnsName());

        try {
            anomalyList.load();
            anomalyResult = anomalyList.getLoadResult();
        }
        catch (Exception e) {
            ErrorDialog.show(this.getGui(), "Impossible de charger la liste des anomalies.", e);
        }
        return anomalyResult;
    }


    public void addRequestFiller(ClassificationWizardWindow.SegmentationJobRequestFiller requestFiller) {
        requestFillers.add(requestFiller);
    }
}
