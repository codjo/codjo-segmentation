/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.wizard;
import net.codjo.mad.gui.framework.SimpleListGui;
import java.awt.Dimension;
/**
 * GUI de la fenêtre d'affichage des résultats de la segmentation.
 */
public class LogWindowGui extends SimpleListGui {

    public LogWindowGui(String title) {
        super(title, new Dimension(700, 400));
    }
}
