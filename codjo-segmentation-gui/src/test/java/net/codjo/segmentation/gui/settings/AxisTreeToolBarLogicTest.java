/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.Field;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.DefaultGuiContext;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.security.common.api.UserMock;
import net.codjo.test.common.LogString;
import java.util.ArrayList;
import java.util.List;
import org.uispec4j.Button;
import org.uispec4j.Tree;
import org.uispec4j.UISpecTestCase;
/**
 * Classe de test de {@link AxisTreeToolBarLogic}.
 */
public class AxisTreeToolBarLogicTest extends UISpecTestCase {
    private LogString log = new LogString();
    AxisTreeToolBarLogic axisTreeToolBarLogic;
    private Button addFolderButton;
    private Button deleteNodeButton;
    private Button addSleeveButton;
    private Tree tree;


    public void test_sleeveButtonAction() throws Exception {
        tree.selectRoot();
        addSleeveButton.click();
        log.assertContent("AxisTreeLogicMock.addSleeve()");

        tree.select("Node 1");
        addSleeveButton.click();
        log.assertContent("AxisTreeLogicMock.addSleeve(), AxisTreeLogicMock.addSleeve()");
    }


    public void test_folderButtonAction() throws Exception {
        tree.selectRoot();
        addFolderButton.click();
        log.assertContent("AxisTreeLogicMock.addFolder()");

        tree.select("Node 1|Node 2");
        addFolderButton.click();
        log.assertContent("AxisTreeLogicMock.addFolder(), AxisTreeLogicMock.addFolder()");
    }


    public void test_deleteButtonAction() throws Exception {
        tree.select("Node 1|Node 2");
        deleteNodeButton.click();
        log.assertContent("AxisTreeLogicMock.delete()");

        tree.select("Node 1|Node 2|Sleeve 2");
        deleteNodeButton.click();
        log.assertContent("AxisTreeLogicMock.delete(), AxisTreeLogicMock.delete()");
    }


    public void test_multipleLevelsSelection() throws Exception {
        tree.select("Node 1");
        assertButtons(true, true, true);

        tree.select("Node 1|Sleeve 1");
        assertButtons(false, false, true);

        tree.select("Node 1|Node 2");
        assertButtons(true, true, true);

        tree.select("Node 1|Node 2|Sleeve 2");
        assertButtons(false, false, true);

        tree.select("Node 1|Node 2|Node 3");
        assertButtons(true, true, true);

        tree.select("Node 1|Node 2|Node 3|Sleeve 3a");
        assertButtons(false, false, true);

        tree.select("Node 1|Node 2|Node 3|Node 4");
        assertButtons(true, false, true);

        tree.select("Node 1|Node 2|Node 3|Sleeve 3b");
        assertButtons(false, false, true);


        tree.select("Node 1|Node 2|Node 3|Node 4|Sleeve 4");
        assertButtons(false, false, true);
    }


    private void assertButtons(boolean isAddSleeveEnabled,
                               boolean isAddFolderEnabled,
                               boolean isDeleteNodeEnabled) {
        assertEquals(isDeleteNodeEnabled, deleteNodeButton.isEnabled());
        assertEquals(isAddFolderEnabled, addFolderButton.isEnabled());
        assertEquals(isAddSleeveEnabled, addSleeveButton.isEnabled());
    }


    @Override
    protected void setUp() throws Exception {
        axisTreeToolBarLogic = new AxisTreeToolBarLogic(new AxisTreeToolBar());
        ListDataSource childDataSource = createChildDataSource();

        DefaultGuiContext defaultGuiContext = new DefaultGuiContext();
        defaultGuiContext.setUser(new UserMock());

        DetailDataSource fatherDataSource = new DetailDataSource(defaultGuiContext);
        fatherDataSource.declare("classificationName");
        fatherDataSource.setFieldValue("classificationName", "root");

        ClassificationStructureGui structureGui = new ClassificationStructureGui("test");
        structureGui.setMaximumNodeDepth(4);
        AxisTree axisTree = new AxisTree();
        axisTree.setClassificationStructureGui(structureGui);
        AxisTreeLogic axisTreeLogic = new AxisTreeLogicMock(fatherDataSource, childDataSource, axisTree, log);

        axisTreeToolBarLogic.init(axisTreeLogic);

        addFolderButton = new Button(axisTreeToolBarLogic.getGui().getAddFolderButton());
        addSleeveButton = new Button(axisTreeToolBarLogic.getGui().getAddSleeveButton());
        deleteNodeButton = new Button(axisTreeToolBarLogic.getGui().getDeleteButton());
        tree = new Tree(axisTree);
        tree.setSeparator("|");
    }


    private ListDataSource createChildDataSource() {
        ListDataSource sonDataSource = new ListDataSource();
        sonDataSource.setColumns(new String[]{
              "sleeveId", "sleeveRowId", "sleeveCode", "sleeveName", "formula", "terminalElement",
              "sleeveDustbin"
        });

        Result result = new Result();
        result.addRow(buildRow("01-1", "Node 1", "false"));
        result.addRow(buildRow("02-1.1", "Sleeve 1", "true"));
        result.addRow(buildRow("02-1.2", "Node 2", "false"));
        result.addRow(buildRow("03-1.2.1", "Sleeve 2", "true"));
        result.addRow(buildRow("03-1.2.2", "Node 3", "false"));
        result.addRow(buildRow("04-1.2.2.1", "Sleeve 3a", "true"));
        result.addRow(buildRow("04-1.2.2.2", "Sleeve 3b", "true"));
        result.addRow(buildRow("04-1.2.2.3", "Node 4", "false"));
        result.addRow(buildRow("05-1.2.2.3.1", "Sleeve 4", "true"));
        result.addRow(buildRow("05-1.2.2.3.2", "Node 5", "false"));

        sonDataSource.setLoadResult(result);
        return sonDataSource;
    }


    private Row buildRow(String code, String name, String terminalElement) {
        Row row = new Row();
        List<Field> fields = new ArrayList<Field>();
        fields.add(new Field("sleeveId", name));
        fields.add(new Field("sleeveRowId", name));
        fields.add(new Field("sleeveCode", code));
        fields.add(new Field("sleeveName", name));
        fields.add(new Field("formula", name));
        fields.add(new Field("terminalElement", terminalElement));
        fields.add(new Field("sleeveDustbin", name));
        row.setFields(fields);
        return row;
    }


    class AxisTreeLogicMock extends AxisTreeLogic {
        private LogString mockLog;


        /**
         * Construit l'arbre des poches à partir des données de l'axe et des poches contenues dans les
         * DataSources.
         *
         * @param fatherDataSource
         * @param sonDataSource
         * @param gui
         * @param log
         *
         * @throws Exception
         */
        AxisTreeLogicMock(DetailDataSource fatherDataSource, ListDataSource sonDataSource, AxisTree gui,
                          LogString log) throws Exception {
            super(fatherDataSource, sonDataSource, gui);
            mockLog = new LogString("AxisTreeLogicMock", log);
        }


        @Override
        public void addSleeve() {
            mockLog.call("addSleeve");
        }


        @Override
        public void addFolder() {
            mockLog.call("addFolder");
        }


        @Override
        public void delete() {
            mockLog.call("delete");
        }
    }
}
