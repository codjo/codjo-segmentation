package net.codjo.segmentation.gui.progress;
import static net.codjo.segmentation.common.MidAuditKey.FAMILY_KEY;
import static net.codjo.segmentation.common.MidAuditKey.IS_LAST_KEY;
import static net.codjo.segmentation.common.MidAuditKey.LEVEL_KEY;
import static net.codjo.segmentation.common.MidAuditKey.PAGE_COUNT_KEY;
import static net.codjo.segmentation.common.SegmentationLevelNames.ANALYZE_LEVEL;
import static net.codjo.segmentation.common.SegmentationLevelNames.COMPUTE_LEVEL;
import static net.codjo.segmentation.common.SegmentationLevelNames.DELETE_LEVEL;
import static net.codjo.segmentation.common.SegmentationLevelNames.PAGINATE_LEVEL;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobAudit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JRootPane;
import junit.framework.TestCase;
/**
 *
 */
public class SegmentationProgressTest extends TestCase {

    public void test_displayAnalyzeResult() throws Exception {
        SegmentationProgress progress = new SegmentationProgress();

        JComponent gui = progress.getGui();
        new JRootPane().add(gui);
        
        assertNotNull(gui);
        assertEquals(0, gui.getComponentCount());

        progress.receivePreAudit(new JobAudit());
        // Analyze
        Arguments arguments = new Arguments();
        arguments.put(LEVEL_KEY, ANALYZE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS, TITRE");
        progress.receiveAudit(arguments);

        assertEquals(2, gui.getComponentCount());

        // Delete
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "TITRE");
        progress.receiveAudit(arguments);

        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(IS_LAST_KEY, "true");
        progress.receiveAudit(arguments);

        // Pagine
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "TITRE");
        progress.receiveAudit(arguments);

        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(IS_LAST_KEY, "true");
        progress.receiveAudit(arguments);
    }


    @SuppressWarnings({"OverlyLongMethod"})
    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("Test SegmentationProgress");
        final SegmentationProgress segmentationProgress = new SegmentationProgress();

        frame.setContentPane(segmentationProgress.getGui());
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
        Thread.sleep(500);

        Arguments arguments = new Arguments();
        arguments.put(LEVEL_KEY, ANALYZE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS, TITRE");
        segmentationProgress.receiveAudit(arguments);

        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "TITRE");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, DELETE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(IS_LAST_KEY, "");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, PAGINATE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(PAGE_COUNT_KEY, "1");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, PAGINATE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(PAGE_COUNT_KEY, "2");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, PAGINATE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(PAGE_COUNT_KEY, "3");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, PAGINATE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(PAGE_COUNT_KEY, "4");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
        calculate(segmentationProgress);
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, PAGINATE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        arguments.put(PAGE_COUNT_KEY, "7");
        arguments.put(IS_LAST_KEY, "true");
        receive(segmentationProgress, arguments);
        calculate(segmentationProgress);
        calculate(segmentationProgress);
        calculate(segmentationProgress);
        calculate(segmentationProgress);
        calculate(segmentationProgress);
        calculate(segmentationProgress);
    }


    private static void receive(final SegmentationProgress segmentationProgress,
                                final Arguments arguments) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                segmentationProgress.receiveAudit(arguments);
            }
        });
    }


    private static void calculate(SegmentationProgress segmentationProgress)
          throws InterruptedException {
        Arguments arguments;
        arguments = new Arguments();
        arguments.put(LEVEL_KEY, COMPUTE_LEVEL);
        arguments.put(FAMILY_KEY, "ENCOURS");
        receive(segmentationProgress, arguments);
        Thread.sleep(500);
    }
}
