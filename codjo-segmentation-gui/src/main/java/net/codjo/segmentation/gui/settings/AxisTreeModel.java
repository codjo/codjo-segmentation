/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
/**
 * Modèle (i.e. données) de l'arbre contenant les poches.
 */
public class AxisTreeModel extends DefaultTreeModel {
    /**
     * DetailDataSource contenant les données de l'axe.
     */
    private DetailDataSource fatherDataSource;
    /**
     * ListDataSource contenant les données des poches.
     */
    private ListDataSource sonDataSource;
    /**
     * Séquence utilisée pour générer les identifiants temporaires des nouvelles poches
     */
    private int fakeSleeveIdSeq = -1;
    /**
     * Map donnant une poche en fonction de sonDataSource id.
     */
    private Map<String, Sleeve> sleeveMap = new HashMap<String, Sleeve>();
    private DecimalFormat df = new DecimalFormat("00");
    private AxisTree gui;


    /**
     * Création du Model à partir des données contenues dans les DataSources.
     *
     * @param fatherDataSourceParam
     * @param sonDataSourceParam
     * @param guiParam
     *
     * @throws Exception
     */
    public AxisTreeModel(DetailDataSource fatherDataSourceParam,
                         ListDataSource sonDataSourceParam, AxisTree guiParam)
          throws Exception {
        super(newClassification(fatherDataSourceParam), true);
        sonDataSource = sonDataSourceParam;
        fatherDataSource = fatherDataSourceParam;
        gui = guiParam;

        gui.addTreeSelectionListener(new SelectedRowUpdater());

        load();
    }


    /**
     * Creates a tree in which any node can have children.
     *
     * @param root a TreeNode object that is the root of the tree
     *
     * @see #DefaultTreeModel(javax.swing.tree.TreeNode,boolean)
     */
    public AxisTreeModel(TreeNode root) {
        super(root);
    }


    /**
     * Creates a tree specifying whether any node can have children, or whether only certain nodes can have
     * children.
     *
     * @param root               a TreeNode object that is the root of the tree
     * @param asksAllowsChildren a boolean, false if any node can have children, true if each node is asked to
     *                           see if it can have children
     *
     * @see #asksAllowsChildren
     */
    public AxisTreeModel(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }


    private void load() {
        // On parcourt le ListDataSource contenant les données des poches
        // et on construit une Map d'objets Sleeve, en ordonnant
        // les poches suivant leur code poche.
        Result rs = sonDataSource.getLoadResult();
        Map<String, Sleeve> treeMap = new TreeMap<String, Sleeve>(new SleeveCodeComparator());
        for (int i = 0; i < rs.getRowCount(); i++) {
            Sleeve sleeve = new Sleeve(rs.getRow(i));
            treeMap.put(sleeve.getSleeveCode(), sleeve);
        }

        // On construit le Model à partir de la Map, en insérant les
        // poches dans le bon ordre (par code poche croissant).
        for (String sleeveCode : treeMap.keySet()) {
            Sleeve sleeve = treeMap.get(sleeveCode);
            Sleeve parent = parentSleeveForChildCode(sleeveCode);
            addSleeve(sleeve, parent);
        }
    }


    public int nextFakeSleeveIdSeq() {
        return fakeSleeveIdSeq--;
    }


    private static AxisTreeNode newClassification(DetailDataSource fatherDataSource) {
        if (fatherDataSource.getLoadResult() == null) {
            fatherDataSource.setFieldValue("classificationName",
                                           Constants.DEFAULT_CLASSIFICATION);
        }

        String classificationName = fatherDataSource.getFieldValue("classificationName");
        return new AxisTreeNode(classificationName);
    }


    /**
     * Ajoute une feuille au dossier sélectionné (une poche qui est un noeud, ou l'axe).
     *
     * @param terminal
     */
    public void addNewSleeve(boolean terminal) {
        TreePath path = gui.getSelectionPath();
        DefaultMutableTreeNode lastItem =
              (DefaultMutableTreeNode)path.getLastPathComponent();
        String sleeveName;
        if (terminal) {
            sleeveName = Constants.DEFAULT_SLEEVE;
        }
        else {
            sleeveName = Constants.DEFAULT_NODE;
        }
        String sleeveCode = buildSleeveCode(lastItem);
        String classificationId = fatherDataSource.getFieldValue("classificationId");
        int sleeveId = this.nextFakeSleeveIdSeq();
        Map<String, String> fields = buildSleeveFieldsMap(classificationId, sleeveId, sleeveCode, sleeveName, terminal);
        Sleeve child = new Sleeve(fields);
        sonDataSource.addRow(child.getRow());
        if (lastItem instanceof AxisTreeNode) {
            addSleeve(child, null);
        }
        else {
            Sleeve parent = (Sleeve)lastItem;
            addSleeve(child, parent);
            nodeStructureChanged(parent);

            if (!parent.isTerminal()) {
                gui.expandPath(path);
            }
        }
    }


    private Map<String, String> buildSleeveFieldsMap(String classificationId,
                                      int sleeveId,
                                      String sleeveCode, String sleeveName, boolean terminal) {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("classificationId", classificationId);
        fields.put("sleeveId", String.valueOf(sleeveId));
        fields.put("sleeveRowId", "" + System.currentTimeMillis());
        fields.put("sleeveCode", sleeveCode);
        fields.put("sleeveName", sleeveName);
        fields.put("sleeveDustbin", String.valueOf(false));
        fields.put("terminalElement", String.valueOf(terminal));
        fields.put("formula", "");
        for (String column : sonDataSource.getColumns()) {
            if (!fields.containsKey(column)) {
                fields.put(column, "null");
            }
        }
        return fields;
    }


    /**
     * Ajoute une poche à une autre poche de ce modèle.
     *
     * @param child  la poche à ajouter.
     * @param parent la poche sur laquelle ajouter <code>child</code>.
     */
    private void addSleeve(Sleeve child, Sleeve parent) {
        if (parent != null) {
            insertNodeInto(child, parent, parent.getChildCount());
        }
        else {
            AxisTreeNode axe = (AxisTreeNode)this.getRoot();
            insertNodeInto(child, axe, axe.getChildCount());
        }
        sleeveMap.put(child.getSleeveId(), child);
    }


    /**
     * Retourne le code poche
     *
     * @param parentItem
     *
     * @return Le code poche (ex: 05-02-2.1)
     */
    private String buildSleeveCode(DefaultMutableTreeNode parentItem) {
        return df.format(parentItem.getLevel() + 1) + "-" + sleevePath(parentItem);
    }


    private String sleevePath(DefaultMutableTreeNode parentItem) {
        if (parentItem.isRoot()) {
            // If the sleeveParent of this sleeve is the root (i.e. the AxisTreeNode),
            // then this sleeve's path is just the position of this sleeve in
            // the list of its brothers.
            return "" + (parentItem.getChildCount() + 1);
        }
        else {
            // Else, this sleeve's path is just the path of its sleeveParent
            // + "." + the position of this sleeve in the list of its brothers.
            return ((Sleeve)parentItem).getPathCode() + "."
                   + (parentItem.getChildCount() + 1);
        }
    }


    /**
     * Supprime la poche sélectionnée.
     */
    public void removeSleeve() {
        TreePath path = gui.getSelectionPath();
        DefaultMutableTreeNode lastItem =
              (DefaultMutableTreeNode)path.getLastPathComponent();

        if (lastItem instanceof AxisTreeNode) {
            int nbOfChild = lastItem.getChildCount();
            for (int i = nbOfChild - 1; i >= 0; i--) {
                Sleeve sleeve = (Sleeve)lastItem.getChildAt(i);
                removeSleeve(sleeve);
            }
        }
        else {
            Sleeve sleeveToRemove = (Sleeve)lastItem;
            final DefaultMutableTreeNode parentNode =
                  (DefaultMutableTreeNode)lastItem.getParent();
            DefaultMutableTreeNode firstChildToRename =
                  (DefaultMutableTreeNode)parentNode.getChildAfter(lastItem);

            removeSleeve(sleeveToRemove);

            updateSleeveCodes(firstChildToRename, parentNode);
        }
    }


    /**
     * Mise à jour des sleeveCode pour tous les noeuds (et ses fils) après suppression d'un noeud.
     *
     * @param childAfter : point de départ de la mise à jour.
     * @param parentNode
     */
    private void updateSleeveCodes(DefaultMutableTreeNode childAfter,
                                   final DefaultMutableTreeNode parentNode) {
        while (childAfter != null) {
            final String newSleevCode = updateSleeveCode(childAfter);
            final Row row = ((Sleeve)childAfter).getRow();
            final int rowIndex = sonDataSource.getLoadResult().getRowIndex(row);
            sonDataSource.setValue(rowIndex, "sleeveCode", newSleevCode);

            Enumeration children = childAfter.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode child =
                      (DefaultMutableTreeNode)children.nextElement();

                updateSleeveCodes(child, childAfter);
            }

            childAfter = (DefaultMutableTreeNode)parentNode.getChildAfter(childAfter);
        }
    }


    private String updateSleeveCode(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parentItem = (DefaultMutableTreeNode)node.getParent();

        if (parentItem instanceof AxisTreeNode) {
            return df.format(1) + "-" + (parentItem.getIndex(node) + 1);
        }
        else {
            Sleeve parentSleeve = (Sleeve)parentItem;
            int indexOfLastPoint = parentSleeve.getSleeveCode().lastIndexOf("-");
            final String parentPath =
                  parentSleeve.getSleeveCode().substring(indexOfLastPoint + 1);

            return df.format(parentItem.getLevel() + 1) + "-" + parentPath + "."
                   + (parentItem.getIndex(node) + 1);
        }
    }


    /**
     * Supprime une poche de ce Model.
     *
     * @param sleeve
     */
    private void removeSleeve(Sleeve sleeve) {
        removeSleeveFromSonDataSource(sleeve);
        removeNodeFromParent(sleeve);
    }


    /**
     * Retourne la poche correspondant au code poche donné, ou <code>null</code> si le code poche fourni est
     * <code>null</code> (cas de l'axe).
     *
     * @param sleeveCode
     *
     * @return
     */
    public Sleeve getSleeveByCode(String sleeveCode) {
        // Cas de la racine
        if (sleeveCode == null) {
            return null;
        }

        TreeNode node = (TreeNode)this.getRoot();
        int caretIndex = sleeveCode.indexOf("-");
        String path = sleeveCode.substring(caretIndex + 1);
        StringTokenizer stknzr = new StringTokenizer(path, ".");
        while (stknzr.hasMoreTokens()) {
            String indexStr = stknzr.nextToken();
            int index = Integer.parseInt(indexStr);
            node = node.getChildAt(index - 1);
        }
        return (Sleeve)node;
    }


    /**
     * Retourne la poche correspondant à l'id donné.
     *
     * @param sleeveId
     *
     * @return
     */
    public Sleeve sleeveForId(int sleeveId) {
        return sleeveMap.get(String.valueOf(sleeveId));
    }


    /**
     * Gets the parent <code>Sleeve</code> of the <code>Sleeve</code> corresponding to the given code. Note:
     * the sleeve with code <code>childCode</code> does not need to be already attached to this
     * <code>AxisTreeModel</code>.
     *
     * @param childCode
     *
     * @return
     */
    public Sleeve parentSleeveForChildCode(String childCode) {
        String parentSleeveCode = parentCodeForChildCode(childCode);
        return getSleeveByCode(parentSleeveCode);
    }


    private String parentCodeForChildCode(String childCode) {
        int firstIndex = childCode.indexOf("-");
        int level = Integer.parseInt(childCode.substring(0, firstIndex));

        String parentCode;
        if (level > 1) {
            int indexOfLastPoint = childCode.lastIndexOf(".");
            parentCode =
                  df.format(level - 1) + childCode.substring(firstIndex, indexOfLastPoint);
        }
        else {
            parentCode = null;
        }

        return parentCode;
    }


    /**
     * Supprime une poche et ses enfants de la ListDataSource.
     *
     * @param sleeve
     */
    private void removeSleeveFromSonDataSource(Sleeve sleeve) {
        // First, remove the children of this sleeve
        int nbOfChildren = sleeve.getChildCount();
        for (int i = nbOfChildren - 1; i >= 0; i--) {
            Sleeve childSleeve = (Sleeve)sleeve.getChildAt(i);
            removeSleeveFromSonDataSource(childSleeve);
        }

        // Finally, remove the sleeve itself
        for (int i = 0; i < sonDataSource.getRowCount(); i++) {
            if (sonDataSource.containsField(i, "sleeveId")) {
                if (sonDataSource.getValueAt(i, "sleeveId").equals(String.valueOf(
                      sleeve.getSleeveId()))) {
                    sonDataSource.removeRow(i);
                }
            }
        }
        sonDataSource.setSelectedRow(null);
    }


    public boolean hasDustBin() {
        return hasDustBinUnder((TreeNode)getRoot());
    }


    private boolean hasDustBinUnder(TreeNode node) {
        if (node instanceof Sleeve && ((Sleeve)node).isDustbin()) {
            return true;
        }
        else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (hasDustBinUnder(node.getChildAt(i))) {
                    return true;
                }
            }
            return false;
        }
    }


    public String getSleeveNameWithoutFormula() {
        return getSleeveWithoutFormula((TreeNode)getRoot());
    }


    private String getSleeveWithoutFormula(TreeNode node) {
        if (node instanceof Sleeve) {
            Sleeve sleeve = (Sleeve)node;
            if (!sleeve.isDustbin()
                && sleeve.isTerminal()
                && "null".equals(sleeve.getFormula())) {
                return node.toString();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            String guilty = getSleeveWithoutFormula(node.getChildAt(i));
            if (guilty != null) {
                return guilty;
            }
        }
        return null;
    }


    public String findDoubleSleeveName() {
        Set<String> names = new HashSet<String>();
        return findDoubleSleeveName(names, (TreeNode)getRoot());
    }


    private String findDoubleSleeveName(Set<String> names, TreeNode node) {
        if (node instanceof Sleeve) {
            final String sleeveName = ((Sleeve)node).getSleeveName();
            if (names.contains(sleeveName)) {
                return node.toString();
            }
            names.add(sleeveName);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            String guilty = findDoubleSleeveName(names, node.getChildAt(i));
            if (guilty != null) {
                return guilty;
            }
        }

        return null;
    }


    private class SelectedRowUpdater implements TreeSelectionListener {
        private boolean isUpdating = false;


        public void valueChanged(TreeSelectionEvent event) {
            if (isUpdating) {
                return;
            }
            isUpdating = true;
            try {
                TreePath selectionPath = gui.getSelectionPath();
                if (selectionPath != null) {
                    Object obj = selectionPath.getLastPathComponent();
                    if (obj instanceof Sleeve) {
                        // Cas où une poche est sélectionnée
                        Sleeve selectedSleeve = (Sleeve)obj;

                        selectedSleeve.getSleeveId();
                        Row selectedRow = selectedSleeve.getRow();
                        sonDataSource.setSelectedRow(selectedRow);
                    }
                    else {
                        sonDataSource.setSelectedRow(null);
                    }
                }
            }
            finally {
                isUpdating = false;
            }
        }
    }
}
