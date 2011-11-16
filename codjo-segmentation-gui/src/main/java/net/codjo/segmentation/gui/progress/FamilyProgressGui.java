package net.codjo.segmentation.gui.progress;
import net.codjo.gui.toolkit.text.AntialiasedJLabel;
import net.codjo.workflow.common.message.JobAudit;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
/**
 *
 */
public class FamilyProgressGui {
    private JProgressBar progressBar;
    private JPanel main;
    private AntialiasedJLabel familyLabel;
    private JLabel deleteLabel;
    private JLabel paginateLabel;
    private JLabel computeLabel;
    private Status deleteStatus = Status.WAITING;
    private Status paginateStatus = Status.WAITING;
    private Status computeStatus = Status.WAITING;


    public FamilyProgressGui(String familyName) {
        familyLabel.setText("<html>Famille <i>" + familyName + "</i>");
    }


    public JPanel getMain() {
        return main;
    }


    public void receivePostAudit(JobAudit audit) {
        boolean auditOk = audit.getStatus() == JobAudit.Status.OK;

        closeStep(deleteLabel, deleteStatus, auditOk);
        closeStep(paginateLabel, paginateStatus, auditOk);
        closeStep(computeLabel, computeStatus, auditOk);

        progressBar.setValue(progressBar.getMaximum());
    }


    public void setDeleteStatus(boolean isLast) {
        if (!isLast) {
            deleteStatus = Status.RUNNING;
            deleteLabel.setIcon(getRunningIcon());
        }
        else {
            deleteStatus = Status.FINISHED;
            deleteLabel.setIcon(getFinishedIcon());
        }
    }


    public void setPaginateStatus(int count, boolean isLast) {
        if (isLast) {
            paginateLabel.setIcon(getFinishedIcon());
            paginateStatus = Status.FINISHED;
        }
        else if (paginateStatus == Status.WAITING) {
            paginateLabel.setIcon(getRunningIcon());
            paginateStatus = Status.RUNNING;
        }

        progressBar.setMaximum(count);
    }


    public void declareOnePageHasBeenComputed() {
        if (computeStatus == Status.WAITING) {
            computeLabel.setIcon(getRunningIcon());
            computeStatus = Status.RUNNING;
        }

        progressBar.setValue(progressBar.getValue() + 1);

        if (paginateStatus == Status.FINISHED && progressBar.getValue() == progressBar.getMaximum()) {
            computeStatus = Status.FINISHED;
            computeLabel.setIcon(getFinishedIcon());
        }
    }


    private ImageIcon getFinishedIcon() {
        return new ImageIcon(getClass().getResource("finished.gif"));
    }


    private void closeStep(JLabel label, Status status, boolean auditOk) {
        if (Status.FINISHED == status) {
            return;
        }
        label.setIcon(auditOk ? getFinishedIcon() : getWarningIcon());
    }


    private ImageIcon getWarningIcon() {
        return new ImageIcon(getClass().getResource("warning.gif"));
    }


    private ImageIcon getRunningIcon() {
        return new ImageIcon(getClass().getResource("running.gif"));
    }


    private enum Status {
        WAITING,
        RUNNING,
        FINISHED;
    }
}
