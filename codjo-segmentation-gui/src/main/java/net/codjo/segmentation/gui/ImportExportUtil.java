package net.codjo.segmentation.gui;
import java.util.List;
import javax.swing.SwingUtilities;
/**
 *
 */
public class ImportExportUtil {
    private ImportExportUtil() {
    }


    public static void inform(final List<ImportExportProgresListener> listeners,
                              final String content) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (ImportExportProgresListener listener : listeners) {
                    listener.handleInform(content);
                }
            }
        });
    }


    public static void inform(final List<ImportExportProgresListener> listeners,
                              final String[][] eventQuarantine) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (ImportExportProgresListener listener : listeners) {
                    listener.handleInform(eventQuarantine);
                }
            }
        });
    }


    public static void informError(final List<ImportExportProgresListener> listeners,
                                   final String errorMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (ImportExportProgresListener listener : listeners) {
                    listener.handleError(errorMessage);
                }
            }
        });
    }
}
