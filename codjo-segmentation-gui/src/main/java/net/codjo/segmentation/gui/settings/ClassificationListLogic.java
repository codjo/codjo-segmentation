/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.gui.framework.AbstractListLogic;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.SimpleListGui;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.mad.gui.request.Position;
import static net.codjo.segmentation.gui.settings.ClassificationListCustomizer.CLASSIFICICATION_LIST_CUSTOMIZER;
import javax.swing.Action;

public class ClassificationListLogic extends AbstractListLogic {
    private static final String ACTION_NAME = "CopyAction";


    ClassificationListLogic(GuiContext context, String newGui, String preferenceId) throws RequestException {
        super(context, new SimpleListGui(newGui), preferenceId);
        addCopyAction(context);
        initCustomizer(context);
    }


    private void addCopyAction(GuiContext guiContext) {
        Action action = new CopyAction(guiContext, getGui().getTable());
        RequestToolBar toolbar = getGui().getToolBar();
        toolbar.add(action, ACTION_NAME, Position.left(RequestToolBar.ACTION_DELETE), true);
    }


    private void initCustomizer(GuiContext context) {
        ClassificationListCustomizer customizer = (ClassificationListCustomizer)
              context.getProperty(CLASSIFICICATION_LIST_CUSTOMIZER);
        if (customizer != null) {
            customizer.initCustomizer(this);
        }
    }


    public RequestToolBar getToolBar() {
        return getGui().getToolBar();
    }


    public ListDataSource getListDataSource() {
        return getGui().getTable().getDataSource();
    }
}
