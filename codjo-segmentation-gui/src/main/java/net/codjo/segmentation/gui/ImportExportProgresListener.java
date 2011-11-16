package net.codjo.segmentation.gui;
/**
 *
 */
public interface ImportExportProgresListener {
    void handleInform(String infoMessage);


    void handleInform(String[][] quarantine);


    void handleError(String errorMessage);
}
