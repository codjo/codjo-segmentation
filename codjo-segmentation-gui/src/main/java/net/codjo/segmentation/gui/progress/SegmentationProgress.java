package net.codjo.segmentation.gui.progress;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.segmentation.common.MidAuditKey;
import static net.codjo.segmentation.common.SegmentationLevelNames.ANALYZE_LEVEL;
import static net.codjo.segmentation.common.SegmentationLevelNames.COMPUTE_LEVEL;
import static net.codjo.segmentation.common.SegmentationLevelNames.DELETE_LEVEL;
import static net.codjo.segmentation.common.SegmentationLevelNames.PAGINATE_LEVEL;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobAudit;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
/**
 *
 */
public class SegmentationProgress {
    private JPanel gui = new JPanel();
    private Map<String, FamilyProgressGui> familyGui = new HashMap<String, FamilyProgressGui>();
    private WaitingPanel waitingPanel;


    public SegmentationProgress() {
        BoxLayout layout = new BoxLayout(gui, BoxLayout.Y_AXIS);
        gui.setLayout(layout);
        gui.setBackground(Color.WHITE);
    }

    public void clearAll() {
        gui.removeAll();
    }


    public void receivePostAudit(JobAudit audit) {
        for (FamilyProgressGui familyProgressGui : familyGui.values()) {
            familyProgressGui.receivePostAudit(audit);
        }
    }


    public void receivePreAudit(JobAudit audit) {
        waitingPanel = new WaitingPanel("Mise en place de la segmentation ...");
        waitingPanel.setDelayBeforeAnimation(0);
        gui.getRootPane().setGlassPane(waitingPanel);
        waitingPanel.setSize(gui.getSize());
        waitingPanel.startAnimation();
    }


    public void receiveAudit(Arguments arguments) {
        String level = arguments.get(MidAuditKey.LEVEL_KEY);

        if (ANALYZE_LEVEL.equals(level)) {
            waitingPanel.stopAnimation();
            String[] families = arguments.get(MidAuditKey.FAMILY_KEY).replaceAll(" ", "").split(",");
            for (String family : families) {
                FamilyProgressGui progressGui = new FamilyProgressGui(family);
                familyGui.put(family, progressGui);
                gui.add(progressGui.getMain());
            }
        }
        else if (DELETE_LEVEL.equals(level)) {
            String family = arguments.get(MidAuditKey.FAMILY_KEY);
            FamilyProgressGui progressGui = familyGui.get(family);
            progressGui.setDeleteStatus(getIsLast(arguments));
        }
        else if (PAGINATE_LEVEL.equals(level)) {
            String family = arguments.get(MidAuditKey.FAMILY_KEY);
            FamilyProgressGui progressGui = familyGui.get(family);
            progressGui.setPaginateStatus(Integer.parseInt(arguments.get(MidAuditKey.PAGE_COUNT_KEY)),
                                          getIsLast(arguments));
        }
        else if (COMPUTE_LEVEL.equals(level)) {
            String family = arguments.get(MidAuditKey.FAMILY_KEY);
            FamilyProgressGui progressGui = familyGui.get(family);
            progressGui.declareOnePageHasBeenComputed();
        }
    }


    private boolean getIsLast(Arguments arguments) {
        return Boolean.valueOf(arguments.get(MidAuditKey.IS_LAST_KEY));
    }


    public JComponent getGui() {
        return gui;
    }
}
