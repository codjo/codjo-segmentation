/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import javax.swing.tree.TreePath;
/**
 * Logique de l'arbre contenant les poches.
 */
public class AxisTreeLogic {
    private AxisTree gui;
    private AxisTreeModel model;

    /**
     * Construit l'arbre des poches à partir des données de l'axe et des poches contenues
     * dans les DataSources.
     *
     * @param fatherDataSource
     * @param sonDataSource
     * @param guiParam
     *
     * @throws Exception
     */
    public AxisTreeLogic(DetailDataSource fatherDataSource, ListDataSource sonDataSource,
        AxisTree guiParam) throws Exception {
        gui = guiParam;
        model = new AxisTreeModel(fatherDataSource, sonDataSource, guiParam);
        gui.setModel(model);
    }

    public AxisTree getGui() {
        return gui;
    }


    public AxisTreeModel getModel() {
        return model;
    }


    public void addTreeSelectionListener(
        AxisTreeToolBarLogic.SleeveSelectionListener sleeveSelectionListener) {
        gui.addTreeSelectionListener(sleeveSelectionListener);
    }


    public void addSleeve() {
        model.addNewSleeve(true);
    }


    public void addFolder() {
        model.addNewSleeve(false);
    }


    public void delete() {
        model.removeSleeve();
    }


    public void updateTree() {
        TreePath path = getGui().getSelectionPath();
        if (path != null && path.getLastPathComponent() instanceof Sleeve) {
            Sleeve sleeve = (Sleeve)path.getLastPathComponent();
            getModel().nodeChanged(sleeve);
        }
    }


    public Object getLastPathComponent() {
        TreePath selectionPath = getGui().getSelectionPath();
        if (selectionPath == null) {
            return null;
        }
        return selectionPath.getLastPathComponent();
    }
}
